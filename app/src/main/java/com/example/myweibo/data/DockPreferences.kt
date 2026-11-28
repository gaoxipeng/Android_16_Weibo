package com.example.myweibo.data

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.example.myweibo.R
import java.net.URLEncoder
import java.text.Normalizer
import java.util.Locale

/** A dock destination. Search and Profile are permanent endcaps, not grouped entries. */
data class DockEntry(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int,
    val timelineKind: TimelineKind? = null,
    val group: TimelineGroup? = null,
    @StringRes val titleRes: Int? = null,
    val groupBadge: String? = null,
) {
    val isTimeline: Boolean get() = timelineKind != null || group != null

    companion object {
        val Home = DockEntry(
            "HOME", "首页", R.drawable.ic_tab_home, timelineKind = TimelineKind.Following,
            titleRes = R.string.dock_home,
        )
        val Messages = DockEntry("MESSAGES", "消息", R.drawable.ic_tab_messages, titleRes = R.string.dock_messages)
        val Compose = DockEntry("COMPOSE", "写微博", R.drawable.ic_tab_compose, titleRes = R.string.dock_compose)
        val Search = DockEntry("SEARCH", "搜索", R.drawable.ic_tab_search, titleRes = R.string.dock_search)
        val Profile = DockEntry("PROFILE", "我的", R.drawable.ic_tab_mine, titleRes = R.string.dock_profile)

        fun forTimeline(kind: TimelineKind): DockEntry {
            // Persist explicit tokens instead of enum names, ordinals, or display labels.
            val (token, icon) = when (kind) {
                TimelineKind.Following -> return Home
                TimelineKind.FriendsCircle -> "friends-circle" to R.drawable.ic_dock_friends
                TimelineKind.SpecialFollowing -> "special-following" to R.drawable.ic_dock_special
                TimelineKind.Favorites -> "favorites" to R.drawable.ic_dock_favorites
                TimelineKind.Liked -> "liked" to R.drawable.ic_status_like
                TimelineKind.Hot -> "hot" to R.drawable.ic_dock_hot
                TimelineKind.Nearby -> "nearby" to R.drawable.ic_feed_location
            }
            return DockEntry("$TIMELINE_PREFIX$token", kind.label, icon, timelineKind = kind)
        }

        fun forGroup(
            accountId: String,
            group: TimelineGroup,
            badge: String = dockGraphemes(group.name).first(),
        ): DockEntry = DockEntry(
            id = "$GROUP_PREFIX${encodeDockKeyPart(accountId)}:${encodeDockKeyPart(group.id)}",
            title = group.name,
            iconRes = R.drawable.ic_dock_group,
            group = group,
            groupBadge = badge,
        )
    }
}

internal const val DOCK_MAX_CENTER_SLOTS = 4

private const val TIMELINE_PREFIX = "timeline:"
private const val LEGACY_FOLLOWING_ID = "timeline:following"
private const val GROUP_PREFIX = "group:"
private val defaultEntries = listOf(DockEntry.Home, DockEntry.Messages, DockEntry.Compose)
private val defaultIds = defaultEntries.map(DockEntry::id)

// Encode each component independently so even embedded separators are unambiguous.
private fun encodeDockKeyPart(value: String): String = URLEncoder.encode(value, "UTF-8")

/** Build from the same account and group snapshot as the Home long-press menu. */
fun dockCatalogue(accountId: String?, groups: List<TimelineGroup>): List<DockEntry> = buildList {
    val kinds = TimelineKind.entries
    val primaryCount = kinds.indexOf(TimelineKind.SpecialFollowing) + 1
    addAll(kinds.take(primaryCount).map { DockEntry.forTimeline(it) })
    if (!accountId.isNullOrBlank()) {
        val accountGroups = groups.distinctBy(TimelineGroup::id).filter { it.id.isNotBlank() }
        val badges = dockGroupBadges(accountGroups)
        accountGroups.forEach { group -> add(DockEntry.forGroup(accountId, group, badges.getValue(group.id))) }
    }
    addAll(kinds.drop(primaryCount).map { DockEntry.forTimeline(it) })
}

private val extendedGrapheme = Regex("\\X")
private fun dockGraphemes(name: String): List<String> = extendedGrapheme.findAll(name.trim())
    .take(2).map { it.value }.toList().ifEmpty { listOf("组") }

private fun badgeKey(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFC).uppercase(Locale.ROOT)

/** Use one grapheme when possible, two when helpful, then append a stable collision index. */
private fun dockGroupBadges(groups: List<TimelineGroup>): Map<String, String> {
    val parts = groups.associate { it.id to dockGraphemes(it.name) }
    val byInitial = groups.groupBy { badgeKey(parts.getValue(it.id).first()) }
    val candidates = groups.associate { group ->
        val graphemes = parts.getValue(group.id)
        val initial = graphemes.first()
        group.id to if (byInitial.getValue(badgeKey(initial)).size == 1) initial else graphemes.joinToString("")
    }
    val used = candidates.values.map(::badgeKey).toMutableSet()
    return groups.groupBy { badgeKey(candidates.getValue(it.id)) }.toSortedMap().values.flatMap { matching ->
        matching.sortedBy(TimelineGroup::id).mapIndexed { index, group ->
            val candidate = candidates.getValue(group.id)
            if (index == 0) group.id to candidate else {
                val initial = parts.getValue(group.id).first()
                var suffix = index + 1
                while (!used.add(badgeKey("$initial$suffix"))) suffix++
                group.id to "$initial$suffix"
            }
        }
    }.toMap()
}

private fun groupedEntries(catalogue: List<DockEntry>): Map<String, DockEntry> =
    (catalogue.filter { isGroupedId(it.id) } + defaultEntries).associateBy(DockEntry::id)

/** Compatibility for callers that check visibility of the original grouped tabs. */
enum class DockItem { HOME, MESSAGES, COMPOSE }

data class DockPreferences(
    val visibleEntryIds: List<String> = defaultIds,
    val showLabels: Boolean = true,
    internal val entryOrder: List<String> = defaultIds,
) {
    val visibleItems: List<DockItem>
        get() = normalized().visibleEntryIds.filter { it in defaultIds }.map { DockItem.valueOf(it) }

    /** Cap the current catalogue's projection without discarding stored or unavailable choices. */
    fun orderedVisibleEntries(catalogue: List<DockEntry> = emptyList()): List<DockEntry> {
        val current = normalized()
        val available = groupedEntries(catalogue)
        val selected = current.visibleEntryIds.toSet()
        val renderedIds = if (hasSelectedTimeline(available, selected)) selected else selected + DockEntry.Home.id
        val ordered = current.entryOrder.filter(renderedIds::contains).mapNotNull(available::get)
        val capped = ordered.take(DOCK_MAX_CENTER_SLOTS)
        // Reserve a slot for the first timeline if it would otherwise be cut off.
        return if (capped.any(DockEntry::isTimeline)) capped
        else capped.take(DOCK_MAX_CENTER_SLOTS - 1) + ordered.first(DockEntry::isTimeline)
    }

    internal fun canShowEntry(id: String, catalogue: List<DockEntry>): Boolean {
        val available = groupedEntries(catalogue)
        return canShowDockEntry(id, available, normalized().visibleEntryIds.toSet())
    }

    fun canHideEntry(id: String, catalogue: List<DockEntry>): Boolean {
        val available = groupedEntries(catalogue)
        val selected = normalized().visibleEntryIds.toSet()
        val entry = available[id] ?: return false
        return id in selected && (!entry.isTimeline || hasSelectedTimeline(available, selected - id))
    }

    internal fun orderedEntries(catalogue: List<DockEntry>): List<DockEntry> {
        val available = groupedEntries(catalogue)
        return (normalized().entryOrder + available.keys).distinct().mapNotNull(available::get)
    }

    /** Normalize stored IDs, never dropping those missing from the current catalogue. */
    internal fun normalized(): DockPreferences {
        val visible = visibleEntryIds.map(::migrateEntryId).filter(::isGroupedId).distinct()
        val completeOrder = (entryOrder + defaultIds + visible).map(::migrateEntryId).filter(::isGroupedId).distinct()
        val visibleSet = visible.toSet()
        val nextVisible = visible.iterator()
        return copy(visibleEntryIds = visible, entryOrder = completeOrder.map {
            if (it in visibleSet) nextVisible.next() else it
        })
    }

    fun withEntryVisibility(
        id: String,
        visible: Boolean,
        catalogue: List<DockEntry>,
    ): DockPreferences {
        val current = normalized()
        val available = groupedEntries(catalogue)
        if (if (visible) !canShowDockEntry(id, available, current.visibleEntryIds.toSet())
            else !current.canHideEntry(id, catalogue)
        ) return current
        val order = (current.entryOrder + current.orderedEntries(catalogue).map(DockEntry::id)).distinct()
        val nextVisible = if (visible) current.visibleEntryIds + id else current.visibleEntryIds - id
        return current.copy(visibleEntryIds = order.filter(nextVisible.toSet()::contains), entryOrder = order)
    }

    /** Reorder the displayed selections, leaving fallback Home and non-displayed IDs in place. */
    fun moveEntry(id: String, targetIndex: Int, catalogue: List<DockEntry>): DockPreferences {
        val current = normalized()
        val selected = current.visibleEntryIds.toSet()
        val availableVisible = current.orderedVisibleEntries(catalogue).map(DockEntry::id).filter(selected::contains)
        val from = availableVisible.indexOf(id)
        if (from == -1 || from == targetIndex || targetIndex !in availableVisible.indices) return current
        val moved = availableVisible.toMutableList().apply { add(targetIndex, removeAt(from)) }.iterator()
        val movableIds = availableVisible.toSet()
        return current.copy(visibleEntryIds = current.visibleEntryIds.map {
            if (it in movableIds) moved.next() else it
        }).normalized()
    }

    /** Reset options for the current catalogue without erasing offline or other-account shortcuts. */
    internal fun restoringDefaults(catalogue: List<DockEntry>): DockPreferences {
        val current = normalized()
        val available = groupedEntries(catalogue)
        val defaults = defaultIds.iterator()
        val order = current.entryOrder.mapNotNull { id ->
            when {
                id !in available -> id
                id in defaultIds -> defaults.next()
                else -> null
            }
        }
        val visible = current.visibleEntryIds.toSet()
        return DockPreferences(
            visibleEntryIds = order.filter { it in defaultIds || it !in available && it in visible },
            entryOrder = order,
        )
    }
}

class DockPreferencesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "dock_prefs",
        Context.MODE_PRIVATE,
    )

    fun read(): DockPreferences {
        val saved = prefs.all
        return decodeDockPreferences(
            saved[KEY_VISIBLE_ITEMS], saved[KEY_SHOW_LABELS], saved[KEY_ITEM_ORDER],
        )
    }

    fun write(preferences: DockPreferences) {
        val config = preferences.normalized()
        prefs.edit()
            .putString(KEY_VISIBLE_ITEMS, config.visibleEntryIds.joinToString(","))
            .putString(KEY_ITEM_ORDER, config.entryOrder.joinToString(","))
            .putBoolean(KEY_SHOW_LABELS, config.showLabels)
            .apply()
    }

    private companion object {
        const val KEY_VISIBLE_ITEMS = "visible_items"
        const val KEY_SHOW_LABELS = "show_labels"
        const val KEY_ITEM_ORDER = "item_order"
    }
}

internal fun decodeDockPreferences(
    items: Any?,
    showLabels: Any?,
    itemOrder: Any? = null,
): DockPreferences {
    // PROFILE alone was a valid legacy value: it meant both optional main tabs were hidden.
    val savedVisible = decodeEntryIds(items)
    val visible = savedVisible.filter(::isGroupedId)
    val hadLegacyProfile = DockEntry.Profile.id in savedVisible &&
        visible.none { it !in defaultIds }
    return DockPreferences(
        visibleEntryIds = when {
            hadLegacyProfile && DockEntry.Home.id !in visible -> visible + DockEntry.Home.id
            visible.isNotEmpty() || items == "" -> visible
            else -> defaultIds
        },
        showLabels = showLabels as? Boolean ?: true,
        entryOrder = decodeEntryIds(itemOrder).filter(::isGroupedId),
    ).normalized()
}

private fun decodeEntryIds(value: Any?): List<String> = (value as? String)
    ?.split(',')?.map(String::trim).orEmpty()

private fun migrateEntryId(id: String): String = if (id == LEGACY_FOLLOWING_ID) DockEntry.Home.id else id

private fun canShowDockEntry(
    id: String,
    available: Map<String, DockEntry>,
    selected: Set<String>,
): Boolean {
    if (id !in available || id in selected) return false
    val next = selected + id
    val fallbackSlots = if (hasSelectedTimeline(available, next)) 0 else 1
    return next.count(available::containsKey) + fallbackSlots <= DOCK_MAX_CENTER_SLOTS
}

private fun hasSelectedTimeline(available: Map<String, DockEntry>, selected: Set<String>): Boolean =
    selected.any { available[it]?.isTimeline == true }

private fun isGroupedId(id: String): Boolean = id in defaultIds ||
    id.startsWith(TIMELINE_PREFIX) && id.length > TIMELINE_PREFIX.length ||
    id.startsWith(GROUP_PREFIX) && id.length > GROUP_PREFIX.length

/** Hoist once above both the dock and settings so changes are immediately observable in both. */
@Stable
class DockPreferencesState internal constructor(private val store: DockPreferencesStore) {
    var preferences: DockPreferences by mutableStateOf(store.read())
        private set

    val config: DockPreferences get() = preferences

    fun update(preferences: DockPreferences) {
        val next = preferences.normalized()
        if (this.preferences == next) return
        store.write(next)
        this.preferences = next
    }
}

val LocalDockState = staticCompositionLocalOf<DockPreferencesState> {
    error("DockPreferencesState was not provided")
}

@Composable
fun rememberDockPreferencesState(): DockPreferencesState {
    val context = LocalContext.current.applicationContext
    return remember(context) { DockPreferencesState(DockPreferencesStore(context)) }
}
