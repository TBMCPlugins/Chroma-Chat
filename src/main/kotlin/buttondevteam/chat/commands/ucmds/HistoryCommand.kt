package buttondevteam.chat.commands.ucmds


@CommandClass(
    helpText = ["Chat History" //
        , "Returns the last 10 messages the player can see." //
    ]
)
class HistoryCommand : UCommandBase() {
    @Subcommand
    fun def(sender: ChromaGamerBase, @OptionalArg channel: kotlin.String?): kotlin.Boolean {
        return HistoryCommand.Companion.showHistory(sender, channel)
    }

    @CustomTabCompleteMethod(param = "channel")
    fun def(): kotlin.collections.Iterable<kotlin.String?> {
        return kotlin.collections.Iterable {
            buttondevteam.core.component.channel.Channel.Companion.getChannels()
                .map<kotlin.String?>(::buttondevteam.core.component.channel.Channel.identifier).iterator()
        }
    }

    /**
     * @param timestamp System.nanoTime()
     */
    private class HistoryEntry(
        val timestamp: kotlin.Long,
        chatMessage: ChatMessage,
        channel: buttondevteam.core.component.channel.Channel?
    ) {
        val chatMessage: ChatMessage
        val channel: buttondevteam.core.component.channel.Channel?

        init {
            this.chatMessage = chatMessage
            this.channel = channel
        }
    }

    companion object {
        /**
         * Key: ChannelID_groupID
         */
        private val messages = java.util.HashMap<kotlin.String?, java.util.LinkedList<HistoryEntry?>>()

        fun showHistory(sender: ChromaGamerBase, channel: kotlin.String?): kotlin.Boolean {
            if (!PluginMain.Companion.Instance.storeChatHistory.get()) {
                sender.sendMessage("§6Chat history is disabled")
                return true
            }
            val getThem: java.util.function.Function<buttondevteam.core.component.channel.Channel?, java.util.LinkedList<HistoryEntry?>?> =
                java.util.function.Function { ch: buttondevteam.core.component.channel.Channel? ->
                    HistoryCommand.Companion.messages.get(ch!!.identifier + "_" + ch.getGroupID(sender))
                } //If can't see, groupID is null, and that shouldn't be in the map
            sender.sendMessage("§6---- Chat History ----")
            val stream: java.util.stream.Stream<buttondevteam.core.component.channel.Channel?>
            if (channel == null) {
                stream = buttondevteam.core.component.channel.Channel.Companion.getChannels()
            } else {
                val och = buttondevteam.core.component.channel.Channel.Companion.getChannels()
                    .filter { chan: buttondevteam.core.component.channel.Channel? ->
                        chan!!.identifier.equals(
                            channel,
                            ignoreCase = true
                        )
                    }.findAny()
                if (!och.isPresent()) {
                    sender.sendMessage("§cChannel not found. Use the ID, for example: /u history g")
                    return true
                }
                stream = java.util.stream.Stream.of<buttondevteam.core.component.channel.Channel?>(och.get())
            }
            val sent = java.util.concurrent.atomic.AtomicBoolean()
            kotlin.synchronized(HistoryCommand.Companion.messages) {
                val arr = stream.map<java.util.LinkedList<HistoryEntry?>?>(getThem)
                    .filter { obj: java.util.LinkedList<HistoryEntry?>? -> java.util.Objects.nonNull(obj) }
                    .flatMap<HistoryEntry?> { obj: java.util.LinkedList<HistoryEntry?>? -> obj!!.stream() }
                    .sorted(java.util.Comparator.comparingLong<HistoryEntry?>(java.util.function.ToLongFunction { he: HistoryEntry? -> he!!.timestamp }))
                    .toArray<HistoryEntry?> { _Dummy_.__Array__() }
                for (i in java.lang.Math.max(0, arr.length - 10)..<arr.length) {
                    val e: HistoryEntry = arr[i]
                    val cm = e.chatMessage
                    sender.sendMessage(
                        "[" + e.channel!!.displayName.get() + "] " + cm.getUser().getName() + ": " + cm.getMessage()
                    )
                    sent.set(true)
                }
            }
            if (!sent.get()) sender.sendMessage("No messages can be found.")
            return true
        }

        fun addChatMessage(chatMessage: ChatMessage, channel: buttondevteam.core.component.channel.Channel) {
            if (!PluginMain.Companion.Instance.storeChatHistory.get()) return
            val groupID = channel.getGroupID(chatMessage.permCheck)
            if (groupID == null) return  //Just to be sure

            kotlin.synchronized(HistoryCommand.Companion.messages) {
                val ll: java.util.LinkedList<HistoryEntry?> =
                    HistoryCommand.Companion.messages.computeIfAbsent(channel.identifier + "_" + groupID) { k: kotlin.String? -> java.util.LinkedList<HistoryEntry?>() }  //<-- TIL
                ll.add(HistoryEntry(java.lang.System.nanoTime(), chatMessage, channel)) //Adds as last element
                while (ll.size > 10) ll.remove() //Removes the first element
            }
        }
    }
}
