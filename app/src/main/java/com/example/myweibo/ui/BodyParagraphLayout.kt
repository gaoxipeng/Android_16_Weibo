package com.example.myweibo.ui

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp

internal fun justifyBodyParagraph(
    text: AnnotatedString,
    paragraphStyle: TextStyle,
    widthPx: Int,
    inlineContent: Map<String, InlineTextContent>,
    maxLines: Int,
    overflow: TextOverflow,
    density: Density,
    measurer: TextMeasurer,
): AnnotatedString {
    val placeholders = text.getStringAnnotations(0, text.length).mapNotNull { range ->
        if (range.tag != "androidx.compose.foundation.text.inlineContent") null
        else inlineContent[range.item]?.let {
            AnnotatedString.Range(it.placeholder, range.start, range.end)
        }
    }
    fun measure(value: AnnotatedString) = measurer.measure(
        text = value, style = paragraphStyle, placeholders = placeholders,
        maxLines = maxLines, overflow = overflow,
        constraints = Constraints(maxWidth = widthPx),
    )
    val original = measure(text)
    // A short status must not become a distributed single-line heading.
    if (original.lineCount <= 1) return text
    data class LineGap(val start: Int, val end: Int, val ranges: List<IntRange>, var extraPx: Float) {
        val slots: Int get() = ranges.size
    }
    val dividers = Regex("[_\\p{Pd}\\u2500-\\u257F\\u23AF\\uFF3F\\uFE49-\\uFE4F]{4,}")
        .findAll(text.text).map { it.range }.toList()
    val gaps = (0 until original.lineCount).mapNotNull { line ->
        val start = original.getLineStart(line)
        val end = original.getLineEnd(line, visibleEnd = true)
        val nextLineIsDivider = if (line + 1 < original.lineCount) {
            val nextText = text.text.substring(original.getLineStart(line + 1),
                original.getLineEnd(line + 1, visibleEnd = true)).trim()
            nextText.isNotEmpty() && nextText.all {
                it in "_-—–─━﹏＿" || it.isWhitespace()
            }
        } else false
        val directlyBeforeDivider = dividers.any { divider ->
            divider.first >= end && text.text.substring(end, divider.first).all {
                it.isWhitespace() || it == '\u200b' || it == '\ufeff'
            }
        }
        val paragraphEnd = line == original.lineCount - 1 || nextLineIsDivider || directlyBeforeDivider ||
            text.text.substring(end, original.getLineEnd(line)).any {
                it == '\n' || it == '\r' || it == '\u2028' || it == '\u2029'
            }
        val naturalWidth = original.getLineRight(line) - original.getLineLeft(line)
        val iterator = java.text.BreakIterator.getCharacterInstance(java.util.Locale.ROOT)
        iterator.setText(text.text.substring(start, end))
        val ranges = buildList {
            var previous = iterator.first()
            while (true) {
                val next = iterator.next()
                if (next == java.text.BreakIterator.DONE || start + next >= end) break
                val codePoint = text.text.codePointAt(start + previous)
                val following = text.text.codePointAt(start + next)
                val slotStart = start + previous
                val slotEnd = start + next
                // Inline emoticons contain alternate text such as [月亮],
                // not necessarily U+FFFC. Never letter-space any part of
                // that range, or either of its immediate boundaries.
                val touchesPlaceholder = placeholders.any {
                    slotStart <= it.end && slotEnd >= it.start
                }
                fun isEmoji(cp: Int): Boolean = cp in 0x1f000..0x1faff ||
                    cp in 0x2600..0x27ff || cp == 0xfe0f || cp == 0x200d
                val preceding = if (slotStart > start) text.text.codePointBefore(slotStart) else -1
                val touchesEmoji = isEmoji(codePoint) || isEmoji(following) ||
                    (Character.isWhitespace(codePoint) && isEmoji(preceding))
                val script = Character.UnicodeScript.of(codePoint)
                val isCjk = script == Character.UnicodeScript.HAN ||
                    script == Character.UnicodeScript.HIRAGANA ||
                    script == Character.UnicodeScript.KATAKANA || script == Character.UnicodeScript.HANGUL
                // Do not track out letters/digits inside English models and
                // decimal numbers, or split emoji/inline image placeholders.
                val closesPunctuation = Character.getType(following) in listOf(
                    Character.END_PUNCTUATION.toInt(), Character.FINAL_QUOTE_PUNCTUATION.toInt(),
                    Character.OTHER_PUNCTUATION.toInt())
                if ((isCjk || Character.isWhitespace(codePoint)) && !closesPunctuation &&
                    !touchesPlaceholder && !touchesEmoji && following != 0xfffc) {
                    add((start + previous) until (start + next))
                }
                previous = next
            }
        }
        if (paragraphEnd || ranges.isEmpty() || original.isLineEllipsized(line) || naturalWidth < widthPx * 0.7f) null
        else LineGap(start, end, ranges, ((widthPx - 1f - naturalWidth) / ranges.size).coerceAtLeast(0f))
    }
    val originalSpacing = if (paragraphStyle.letterSpacing.type == androidx.compose.ui.unit.TextUnitType.Sp)
        paragraphStyle.letterSpacing.value else 0f
    fun build() = AnnotatedString.Builder(text).apply {
        gaps.forEach { gap ->
            gap.ranges.forEach { range ->
                addStyle(SpanStyle(letterSpacing = (originalSpacing + with(density) { gap.extraPx.toSp().value }).sp),
                    range.first, range.last + 1)
            }
        }
    }.toAnnotatedString()
    // Probe the complete paragraph once at a quarter of each estimated gap,
    // then scale each line's spacing from the observed width change. Rebuilding
    // and measuring the whole paragraph in a binary search for every line used
    // to perform hundreds of StaticLayout passes while a feed was scrolling,
    // blocking the UI thread long enough to trigger an ANR on long posts.
    val probeSpacing = gaps.map { it.extraPx * 0.25f }
    gaps.forEachIndexed { index, gap -> gap.extraPx = probeSpacing[index] }
    val probe = measure(build())
    gaps.forEachIndexed { index, gap ->
        val probeExtra = probeSpacing[index]
        gap.extraPx = 0f
        val line = probe.getLineForOffset(gap.start)
        val sameLine = probe.getLineStart(line) == gap.start &&
            probe.getLineEnd(line, visibleEnd = true) == gap.end
        val probeWidth = probe.getLineRight(line) - probe.getLineLeft(line)
        val probeDelta = probeWidth - (original.getLineRight(line) - original.getLineLeft(line))
        val availableWidth = (widthPx - 0.75f - (original.getLineRight(line) - original.getLineLeft(line)))
            .coerceAtLeast(0f)
        if (sameLine && probeExtra > 0f && probeDelta > 0.25f) {
            // Keep a sub-pixel safety margin so rounding cannot push the final
            // glyph beyond the same line width on a device with different density.
            gap.extraPx = (probeExtra * availableWidth / probeDelta * 0.99f)
                .coerceAtMost(probeExtra * 4f)
        }
    }
    return build()
}
