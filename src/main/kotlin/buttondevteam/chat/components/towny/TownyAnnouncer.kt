package buttondevteam.chat.components.towny


object TownyAnnouncer {
    private val LOG_TYPE_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("\\[(\\w+) (?:Msg|Message)](?: (\\w+):)?")
    private const val APPENDER_NAME = "Chroma"
    private val HANDLER: AbstractAppender = object : AbstractAppender(
        TownyAnnouncer.APPENDER_NAME,
        LevelRangeFilter.createFilter(
            org.apache.logging.log4j.Level.INFO,
            org.apache.logging.log4j.Level.INFO,
            Filter.Result.ACCEPT,
            Filter.Result.ACCEPT
        ),
        PatternLayout.createDefaultLayout()
    ) {
        public override fun append(logRecord: LogEvent) {
            if (logRecord.getMessage() == null) return
            val message: kotlin.String = logRecord.getMessage().getFormattedMessage()
            val m = TownyAnnouncer.LOG_TYPE_PATTERN.matcher(message)
            if (!m.find()) return
            val groupID: kotlin.String? = m.group(2) //The group ID is correctly cased
            when (java.lang.String.valueOf(m.group(1))) {
                "Town" -> {
                    if (TownyAnnouncer.townChannel == null) return
                    SendSystemMessage(
                        TownyAnnouncer.townChannel,
                        RecipientTestResult(
                            buttondevteam.chat.components.towny.TownyComponent.Companion.getTownNationIndex(
                                groupID,
                                false
                            ), groupID
                        ),
                        message, TownyAnnouncer.target, ChatUtils.MCORIGIN
                    )
                }

                "Nation" -> {
                    if (TownyAnnouncer.nationChannel == null) return
                    SendSystemMessage(
                        TownyAnnouncer.nationChannel,
                        RecipientTestResult(
                            buttondevteam.chat.components.towny.TownyComponent.Companion.getTownNationIndex(
                                groupID,
                                true
                            ), groupID
                        ),
                        message, TownyAnnouncer.target, ChatUtils.MCORIGIN
                    )
                }

                "Global" -> SendSystemMessage(
                    buttondevteam.core.component.channel.Channel.Companion.globalChat,
                    RecipientTestResult.Companion.ALL,
                    message, TownyAnnouncer.target, ChatUtils.MCORIGIN
                )
            }
        }
    }

    private var target: BroadcastTarget? = null
    private var townChannel: buttondevteam.core.component.channel.Channel? = null
    private var nationChannel: buttondevteam.core.component.channel.Channel? = null

    fun setup(
        townChannel: buttondevteam.core.component.channel.Channel?,
        nationChannel: buttondevteam.core.component.channel.Channel?
    ) {
        TownyAnnouncer.target = BroadcastTarget.Companion.add("towny")
        TownyAnnouncer.townChannel = townChannel
        TownyAnnouncer.nationChannel = nationChannel
        /*System.out.println(LogManager.getLogger("com.palmergames.bukkit.towny"));
		((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getContext().getConfiguration().addAppender(HANDLER);
		System.out.println(((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getAppenders());
		((LoggerContext)LogManager.getContext(false)).updateLoggers();
		System.out.println(((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getAppenders());
		((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getContext().updateLoggers();
		System.out.println(((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getAppenders());*/
        val lc = org.apache.logging.log4j.LogManager.getContext(false) as LoggerContext
        TownyAnnouncer.HANDLER.start()
        lc.getConfiguration().addAppender(TownyAnnouncer.HANDLER)
        val logger: Logger = lc.getLogger("com.palmergames.bukkit.towny")
        //System.out.println(logger);
        //System.out.println(lc.getConfiguration().<Appender>getAppender(HANDLER.getName())); //T defaults to String because of the context which results in a cast exception
        logger.addAppender(lc.getConfiguration().getAppender(TownyAnnouncer.HANDLER.getName()))
        logger.get().addAppender(
            TownyAnnouncer.HANDLER,
            org.apache.logging.log4j.Level.INFO,
            TownyAnnouncer.HANDLER.getFilter()
        )
        lc.updateLoggers()
        //System.out.println(logger.getAppenders());
    }

    fun setdown() {
        BroadcastTarget.Companion.remove(TownyAnnouncer.target)
        TownyAnnouncer.target = null
        TownyAnnouncer.townChannel = null
        TownyAnnouncer.nationChannel = null
        (org.apache.logging.log4j.LogManager.getLogger("com.palmergames.bukkit.towny") as Logger).getAppenders()
            .remove(TownyAnnouncer.APPENDER_NAME)
    }
}
