package buttondevteam.chat.components.appendext

import buttondevteam.lib.architecture.Component
import java.lang.reflect.Method
import java.util.function.Consumer

/**
 * Allows players to append tableflips and other things to their messages. Everything is configurable here.
 */
class AppendTextComponent : Component<PluginMain?>() {
    private var appendTexts: MutableMap<String, IHaveConfig>? = null

    private fun helpText(config: IHaveConfig): IConfigData<Array<String?>?> {
        return config.getData<Array<String?>?>(
            "helpText", arrayOf<String>(
                "Tableflip",  //
                "This command appends a tableflip after your message",  //
                "Or just makes you tableflip",  //
            )
        )
    }

    private fun appendedText(config: IHaveConfig): IConfigData<String?> {
        return config.getData<String?>("appendedText", "tableflip")
    }

    override fun enable() {
        val map = HashMap<String?, Consumer<IHaveConfig?>?>()
        map.put("tableflip", { conf ->
            helpText(conf).set(
                arrayOf<String>(
                    "Tableflip",  //
                    "This command appends a tableflip after your message",  //
                    "Or just makes you tableflip",  //
                )
            )
            appendedText(conf).set("(╯°□°）╯︵ ┻━┻")
        })
        map.put("unflip", { conf ->
            helpText(conf).set(
                arrayOf<String>(
                    "Unflip",  //
                    "This command appends an unflip after your message",  //
                    "Or just unflips as you",  //
                )
            )
            appendedText(conf).set("┬─┬ ノ( ゜-゜ノ)")
        })
        map.put("shrug", { conf ->
            helpText(conf).set(
                arrayOf<String>(
                    "Shrug",  //
                    "This command appends a shrug after your message",  //
                    "Or just makes you shrug",  //
                )
            )
            appendedText(conf).set("¯\\\\\\_(ツ)\\_/¯")
        })
        map.put("lenny", { conf ->
            helpText(conf).set(
                arrayOf<String>(
                    "Lenny",  //
                    "This command appends a Lenny face after your message",  //
                    "Or just sends one",  //
                )
            )
            appendedText(conf).set("( ͡° ͜ʖ ͡°)")
        })
        map.put("waitwhat", { conf ->
            helpText(conf).set(
                arrayOf<String>(
                    "Wait what",  //
                    "Wait what" //
                )
            )
            appendedText(conf).set("wait what")
        })
        appendTexts = getConfigMap("texts", map)
        for (cmd in appendTexts!!.keys) registerCommand(CommandHandler(cmd))
    }

    override fun disable() {
    }

    @CommandClass
    inner class CommandHandler internal constructor(command: String) : ICommand2MC() {
        val commandPath: String
        private val helpText: Array<String?>?
        private val appendedText: String?

        init {
            val conf = appendTexts!!.get(command)
            if (conf == null) throw NoSuchElementException("AppendText command not found: " + command)
            this.commandPath = command
            println("Help text for " + command + ":")
            println(helpText(conf).get().contentToString())
            helpText = helpText(conf).get()
            appendedText = appendedText(conf).get()
        }

        @Subcommand
        fun def(sender: Command2MCSender, @OptionalArg @TextArg message: String?) {
            sendChatMessage(
                ChatMessage.Companion.builder(
                    sender.sender,
                    (if (message == null) "" else message + " ") + appendedText
                ).fromCommand(true).permCheck(sender.permCheck).build()
            )
        }

        override fun getHelpText(method: Method?, ann: Subcommand?): Array<String?>? {
            return helpText
        }
    }
}
