package buttondevteam.chat.commands.ucmds

import buttondevteam.chat.PluginMain
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.ChromaUtils.asString
import buttondevteam.lib.chat.ChatMessage
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.CustomTabCompleteMethod
import buttondevteam.lib.player.ChromaGamerBase
import java.util.concurrent.atomic.AtomicBoolean


@CommandClass(
    helpText = ["Chat History" //
        , "Returns the last 10 messages the player can see." //
    ]
)
class HistoryCommand : UCommandBase() {
    @Command2.Subcommand
    fun def(sender: ChromaGamerBase, @Command2.OptionalArg channel: String?): Boolean {
        return showHistory(sender, channel)
    }

    @CustomTabCompleteMethod(param = "channel")
    fun def(): Iterable<String?> {
        return Iterable {
            Channel.channelList.map { it.identifier }.iterator()
        }
    }

    /**
     * @param timestamp System.nanoTime()
     */
    private class HistoryEntry(
        val timestamp: Long,
        val chatMessage: ChatMessage,
        val channel: Channel?
    )

    companion object {
        /**
         * Key: ChannelID_groupID
         */
        private val messages = mutableMapOf<String, MutableList<HistoryEntry>>()

        fun showHistory(sender: ChromaGamerBase, channel: String?): Boolean {
            if (!PluginMain.Instance!!.storeChatHistory) {
                sender.sendMessage("§6Chat history is disabled")
                return true
            }
            val getThem = { ch: Channel ->
                messages[ch.identifier + "_" + ch.getGroupID(sender)]
            } //If can't see, groupID is null, and that shouldn't be in the map
            sender.sendMessage("§6---- Chat History ----")
            val stream: List<Channel>
            if (channel == null) {
                stream = Channel.channelList
            } else {
                val och = Channel.channelList.firstOrNull { chan: Channel ->
                    chan.identifier.equals(
                        channel,
                        ignoreCase = true
                    )
                }
                if (och == null) {
                    sender.sendMessage("§cChannel not found. Use the ID, for example: /u history g")
                    return true
                }
                stream = listOf(och)
            }
            val sent = AtomicBoolean()
            synchronized(messages) {
                val arr = stream.mapNotNull(getThem)
                    .flatten()
                    .sortedBy { it.timestamp }
                    .toTypedArray()
                for (i in (arr.size - 10).coerceAtLeast(0)..<arr.size) {
                    val e: HistoryEntry = arr[i]
                    val cm = e.chatMessage
                    sender.sendMessage(
                        "[" + e.channel!!.displayName + "] " + cm.user.name + ": " + cm.message.asString() // TODO: Components
                    )
                    sent.set(true)
                }
            }
            if (!sent.get()) sender.sendMessage("No messages can be found.")
            return true
        }

        fun addChatMessage(chatMessage: ChatMessage, channel: Channel) {
            if (!PluginMain.Instance!!.storeChatHistory) return
            val groupID = channel.getGroupID(chatMessage.permCheck) ?: return //Just to be sure

            synchronized(messages) {
                val ll = messages.computeIfAbsent(channel.identifier + "_" + groupID) { mutableListOf() }
                ll.add(HistoryEntry(System.nanoTime(), chatMessage, channel)) //Adds as last element
                while (ll.size > 10) ll.removeFirst() //Removes the first element
            }
        }
    }
}
