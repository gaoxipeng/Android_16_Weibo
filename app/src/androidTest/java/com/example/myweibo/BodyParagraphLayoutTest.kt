package com.example.myweibo

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myweibo.ui.justifyBodyParagraph
import org.junit.Assert.*
import org.junit.Test

class BodyParagraphLayoutTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val density = Density(context.resources.displayMetrics.density, context.resources.configuration.fontScale)
    private val measurer = TextMeasurer(createFontFamilyResolver(context), density, LayoutDirection.Ltr)
    private val style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp,
        letterSpacing = (-0.2).sp, textAlign = TextAlign.Start, lineBreak = LineBreak.Simple)

    @Test fun mixedBodyReallyAlignsRenderedLines() {
        val body = "Trek发布了Aeolus RSL 65X和43X两款顶级轮组，65X前轮65框高，后轮58，43X的框高则是前轮43，后轮38。高框轮宽度也做了针对侧风的优化，前轮外宽高达37mm，后轮为31mm，Trek称比上一代快了2.1瓦。国际大牌们终于也都用上中国的碳纤维辐条了，重量降低不少，RSL 45X为1220克，比老款更低框高的RSL 37减重105克，RSL 65X 1475克，比老款轻了75克。"
        val width = (320 * density.density).toInt()
        val adjusted = justifyBodyParagraph(AnnotatedString(body), style, width, emptyMap(), Int.MAX_VALUE,
            TextOverflow.Clip, density, measurer)
        val result = measurer.measure(adjusted, style = style, constraints = Constraints(maxWidth = width))
        for (line in 0 until result.lineCount - 1) {
            val right = result.getLineRight(line)
            assertTrue("line $line leaves ${width - right}px", width - right < 2f)
            assertTrue("line $line overflows", right <= width + 0.1f)
        }
    }

    @Test fun sentenceBeforeDividerIsNotExpanded() {
        for (separator in listOf("\n", "\r\n", "\u2028", "\u2029")) {
            val body = "让你「走进婚姻」的决定性因素？${separator}________________________"
            val width = (400 * density.density).toInt()
            val adjusted = justifyBodyParagraph(AnnotatedString(body), style, width, emptyMap(), Int.MAX_VALUE,
                TextOverflow.Clip, density, measurer)
            val result = measurer.measure(adjusted, style = style, constraints = Constraints(maxWidth = width))
            assertTrue(result.getLineRight(0) < width * 0.9f)
            val end = result.getLineEnd(0, visibleEnd = true)
            assertFalse("expanded separator ${separator.toCharArray().map { it.code }}",
                adjusted.spanStyles.any { it.start < end })
        }
    }
}
