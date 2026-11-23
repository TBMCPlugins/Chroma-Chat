package buttondevteam.chat.components.formatter.formatting

import buttondevteam.chat.commands.ucmds.admin.DebugCommand
import buttondevteam.lib.architecture.IHaveConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.math.max
import kotlin.math.min

/**
 * A [MatchProvider] finds where the given [FormatSettings] need to be applied. [Combine]} is used to turn it into a [TellrawPart], combining
 * intersecting parts found, for example when `_abc*def*ghi_` is said in chat, it'll turn it into an underlined part, then an underlined *and italics* part, finally an underlined part
 * again.
 *
 * @author NorbiPeti
 */
object ChatFormatter {
    //synchronized: Some of the formatters are reused, see createSections(...)
    @Synchronized
    fun Combine(
        formatters: MutableList<MatchProviderBase>,
        str: String,
        tp: TextComponent.Builder,
        config: IHaveConfig?,
        defaults: FormatSettings
    ) {
        /*
		 * A global formatter is no longer needed
		 */
        header("ChatFormatter.Combine begin")
        val sections = mutableListOf<FormattedSection>()

        if (config != null)  //null if testing
            formatters.removeIf { cf -> !cf.enabled(config).get() }  //Remove disabled formatters

        val excluded = mutableListOf<IntArray>()
        /*
		 * 0: Start - 1: End index
		 */
        val remchars = mutableListOf<IntArray>()

        escapeThings(str, excluded, remchars)

        sections.add(FormattedSection(defaults, 0, str.length - 1, mutableListOf())) //Add entire message
        val regexProviders = formatters.filterIsInstance<RegexMatchProvider>().toMutableList()
        createSections(regexProviders, str, sections, excluded, remchars)
        val stringProviders = formatters.filterIsInstance<StringMatchProvider>().toMutableList()
        createSections(stringProviders, str, sections, excluded, remchars)
        val rangeProviders = formatters.filterIsInstance<RangeMatchProvider>().toMutableList()
        createSections(rangeProviders, str, sections, excluded, remchars)
        sortSections(sections)

        header("Section combining")
        combineSections(str, sections)

        header("Section applying")
        applySections(str, tp, sections, remchars)
        header("ChatFormatter.Combine done")
    }

    private fun escapeThings(str: String, ignoredAreas: MutableList<IntArray>, remchars: MutableList<IntArray>) {
        var i = 0
        while (i < str.length) {
            if (str[i] == '\\') {
                remchars.add(intArrayOf(i, i))
                ignoredAreas.add(intArrayOf(i + 1, i + 1))
                i++ //Ignore a potential second slash
            }
            i++
        }
    }

    private fun createSections(
        formatters: MutableList<out MatchProviderBase>, str: String, sections: MutableList<FormattedSection>,
        excludedAreas: MutableList<IntArray>, removedCharacters: MutableList<IntArray>
    ) {
        formatters.forEach(Consumer { obj: MatchProviderBase? -> obj!!.reset() }) //Reset state information, as we aren't doing deep cloning
        while (formatters.isNotEmpty()) {
            val iterator = formatters.iterator()
            while (iterator.hasNext()) {
                val formatter = iterator.next()
                DebugCommand.SendDebugMessage("Checking provider: $formatter")
                val sect = formatter.getNextSection(str, excludedAreas, removedCharacters)
                if (sect != null)  //Not excluding the area here because the range matcher shouldn't take it all
                    sections.add(sect)
                if (formatter.isFinished) {
                    DebugCommand.SendDebugMessage("Provider finished")
                    iterator.remove()
                }
            }
        }
    }

    private fun combineSections(str: String, sections: MutableList<FormattedSection>) {
        var i = 1
        while (i < sections.size) {
            DebugCommand.SendDebugMessage("i: $i")
            val firstSection: FormattedSection
            val lastSection: FormattedSection
            run {
                var firstSect = sections[i - 1]
                var lastSect = sections[i]
                if (
                    firstSect.Start > lastSect.Start //The first can't start later
                    || (firstSect.Start == lastSect.Start && firstSect.End < lastSect.End)
                ) {
                    val section = firstSect
                    firstSect = lastSect
                    lastSect = section
                }
                firstSection = firstSect
                lastSection = lastSect
            }
            DebugCommand.SendDebugMessage("Combining sections $firstSection")
            ChatFormatUtils.sendMessageWithPointer(str, firstSection.Start, firstSection.End)
            DebugCommand.SendDebugMessage(" and $lastSection")
            ChatFormatUtils.sendMessageWithPointer(str, lastSection.Start, lastSection.End)
            if (firstSection.Start == lastSection.Start && firstSection.End == lastSection.End) {
                firstSection.Settings.copyFrom(lastSection.Settings)
                firstSection.Matches.addAll(lastSection.Matches)
                DebugCommand.SendDebugMessage("To section $firstSection")
                ChatFormatUtils.sendMessageWithPointer(str, firstSection.Start, firstSection.End)
                sections.removeAt(i)
                i = 0
                sortSections(sections)
                i++
                continue
            } else if (firstSection.End >= lastSection.Start && firstSection.Start <= lastSection.End) {
                val middleStart = lastSection.Start
                // |----|--    |------|
                // --|----|    -|----|-
                val middleEnd = min(lastSection.End, firstSection.End)
                val lastSectEnd = max(lastSection.End, firstSection.End)
                val section = FormattedSection(
                    firstSection.Settings, middleStart, middleEnd,
                    firstSection.Matches
                )
                section.Settings.copyFrom(lastSection.Settings)
                section.Matches.addAll(lastSection.Matches)
                sections.add(i, section)

                if (firstSection.End > lastSection.End) { //Copy first section info to last as the lastSection initially cuts the firstSection in half
                    lastSection.Settings = FormatSettings()
                    lastSection.Settings.copyFrom(firstSection.Settings)
                }

                firstSection.End = middleStart - 1
                lastSection.Start = middleEnd + 1
                lastSection.End = lastSectEnd

                val removeIfNeeded = Predicate { s: FormattedSection? ->
                    if (s!!.Start < 0 || s.End < 0 || s.Start > s.End) {
                        DebugCommand.SendDebugMessage("  Removed: $s")
                        ChatFormatUtils.sendMessageWithPointer(str, s.Start, s.End)
                        sections.remove(s)
                        return@Predicate true
                    }
                    false
                }

                DebugCommand.SendDebugMessage("To sections")
                if (!removeIfNeeded.test(firstSection)) {
                    DebugCommand.SendDebugMessage("  1:$firstSection")
                    ChatFormatUtils.sendMessageWithPointer(str, firstSection.Start, firstSection.End)
                }
                if (!removeIfNeeded.test(section)) {
                    DebugCommand.SendDebugMessage("  2:$section")
                    ChatFormatUtils.sendMessageWithPointer(str, section.Start, section.End)
                }
                if (!removeIfNeeded.test(lastSection)) {
                    DebugCommand.SendDebugMessage("  3:$lastSection")
                    ChatFormatUtils.sendMessageWithPointer(str, lastSection.Start, lastSection.End)
                }
                i = 0
            }
            sortSections(sections)
            i++
        }
    }

    private fun applySections(
        str: String,
        tp: TextComponent.Builder,
        sections: MutableList<FormattedSection>,
        remchars: MutableList<IntArray>
    ) {
        var lasttp: TextComponent? = null
        var lastlink: String? = null
        for (section in sections) {
            DebugCommand.SendDebugMessage("Applying section: $section")
            var originaltext: String
            val start = section.Start
            val end = section.End
            DebugCommand.SendDebugMessage("Start: $start - End: $end")
            ChatFormatUtils.sendMessageWithPointer(str, start, end)
            val rci = remchars.filter { rc ->
                (rc[0] <= start && rc[1] >= start)
                    || (rc[0] >= start && rc[1] <= end)
                    || (rc[0] <= end && rc[1] >= end)
            }.sortedBy { rc -> rc[0] * 10000 + rc[1] }.toTypedArray()
            DebugCommand.SendDebugMessage(
                "Applying RC: ${rci.joinToString(", ", "[", "]") { it.contentToString() }}"
            )
            originaltext = str.substring(start, end + 1)
            val sb = StringBuilder(originaltext)
            for (x in rci.size - 1 downTo 0) sb.delete(
                (rci[x][0] - start).coerceAtLeast(0),
                (rci[x][1] - start).coerceAtMost(end) + 1
            ) //Delete going backwards

            originaltext = sb.toString()
            if (originaltext.isEmpty()) {
                DebugCommand.SendDebugMessage("Skipping section because of remchars")
                continue
            }
            DebugCommand.SendDebugMessage("Section text: $originaltext")
            var openlink: String? = null
            val settings = section.Settings
            DebugCommand.SendDebugMessage("Applying settings: $settings")
            val onmatch = settings.onmatch
            // The onmatch function can change the settings
            if (lasttp != null && hasSameDecorations(
                    lasttp,
                    settings
                ) && lastlink == settings.openlink && onmatch == null
            ) {
                DebugCommand.SendDebugMessage("This part has the same properties as the previous one, combining.")
                lasttp = lasttp.content(lasttp.content() + originaltext)
                continue  //Combine parts with the same properties
            }
            val newtp = Component.text()
            if (onmatch != null) originaltext = onmatch(originaltext, settings, section)
            if (settings.color != null) newtp.color(settings.color)
            if (settings.bold) newtp.decorate(TextDecoration.BOLD)
            if (settings.italic) newtp.decorate(TextDecoration.ITALIC)
            if (settings.underlined) newtp.decorate(TextDecoration.UNDERLINED)
            if (settings.strikethrough) newtp.decorate(TextDecoration.STRIKETHROUGH)
            if (settings.obfuscated) newtp.decorate(TextDecoration.OBFUSCATED)
            if (settings.openlink != null) openlink = settings.openlink
            val hoverText = settings.hoverText
            if (hoverText != null) newtp.hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.text(hoverText)
                )
            )
            if (lasttp != null) tp.append(lasttp)
            lastlink = openlink
            newtp.content(originaltext)
            if (!openlink.isNullOrEmpty()) {
                if (section.Matches.isNotEmpty()) openlink = openlink.replace("$1", section.Matches[0])
                newtp.clickEvent(ClickEvent.openUrl(openlink)).hoverEvent(
                    HoverEvent.hoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.text("Click to open").color(NamedTextColor.BLUE)
                    )
                )
            }
            lasttp = newtp.build()
        }
        if (lasttp != null) tp.append(lasttp)
    }

    private fun hasSameDecorations(c1: TextComponent, settings: FormatSettings): Boolean {
        return c1.color() == settings.color
            && c1.hasDecoration(TextDecoration.BOLD) == settings.bold && c1.hasDecoration(TextDecoration.ITALIC) == settings.italic && c1.hasDecoration(
            TextDecoration.UNDERLINED
        ) == settings.underlined && c1.hasDecoration(TextDecoration.STRIKETHROUGH) == settings.strikethrough && c1.hasDecoration(
            TextDecoration.OBFUSCATED
        ) == settings.obfuscated
    }

    private fun sortSections(sections: MutableList<FormattedSection>) {
        sections.sortWith(
            Comparator { s1, s2 ->
                if (s1.Start == s2.Start)
                    if (s1.End == s2.End) 0 else s1.End.compareTo(s2.End)
                else
                    s1.Start.compareTo(s2.Start)
            })
    }

    private fun header(message: String) {
        DebugCommand.SendDebugMessage("\n--------\n$message\n--------\n")
    }
}
