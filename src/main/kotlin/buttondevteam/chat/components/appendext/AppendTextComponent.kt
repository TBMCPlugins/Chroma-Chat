package buttondevteam.chat.components.appendext

import buttondevteam.chat.PluginMain
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.IHaveConfig
import buttondevteam.lib.architecture.config.IConfigData
import buttondevteam.lib.chat.*
import java.lang.reflect.Method
import java.util.function.Consumer

/**
 * Allows players to append tableflips and other things to their messages. Everything is configurable here.
 */
class AppendTextComponent : Component<PluginMain>() {
    private var appendTexts: Map<String, IHaveConfig>? = null

    private fun helpText(config: IHaveConfig): IConfigData<Array<String>> {
        return config.getData(
            "helpText", arrayOf(
                "Tableflip",  //
                "This command appends a tableflip after your message",  //
                "Or just makes you tableflip",  //
            ),
            { if (it is Array<*>) it.map { it.toString() }.toTypedArray() else arrayOf() },
            { it.map { it as Any }.toTypedArray() }
        )
    }

    private fun appendedText(config: IHaveConfig): IConfigData<String> {
        return config.getData("appendedText", "tableflip")
    }

    override fun enable() {
        val map = mutableMapOf<String, Consumer<IHaveConfig>>()
        map["tableflip"] = Consumer { conf: IHaveConfig ->
            helpText(conf).set(
                arrayOf(
                    "Tableflip",  //
                    "This command appends a tableflip after your message",  //
                    "Or just makes you tableflip",  //
                )
            )
            appendedText(conf).set("(╯°□°）╯︵ ┻━┻")
        }
        map["unflip"] = Consumer { conf ->
            helpText(conf).set(
                arrayOf(
                    "Unflip",  //
                    "This command appends an unflip after your message",  //
                    "Or just unflips as you",  //
                )
            )
            appendedText(conf).set("┬─┬ ノ( ゜-゜ノ)")
        }
        map["shrug"] = Consumer { conf ->
            helpText(conf).set(
                arrayOf(
                    "Shrug",  //
                    "This command appends a shrug after your message",  //
                    "Or just makes you shrug",  //
                )
            )
            appendedText(conf).set("¯\\\\\\_(ツ)\\_/¯")
        }
        map["lenny"] = Consumer { conf ->
            helpText(conf).set(
                arrayOf(
                    "Lenny",  //
                    "This command appends a Lenny face after your message",  //
                    "Or just sends one",  //
                )
            )
            appendedText(conf).set("( ͡° ͜ʖ ͡°)")
        }
        map["wait what"] = Consumer { conf ->
            helpText(conf).set(
                arrayOf(
                    "Wait what",  //
                    "Wait what" //
                )
            )
            appendedText(conf).set("wait what")
        }
        appendTexts = getConfigMap("texts", map)
        for (cmd in appendTexts!!.keys) registerCommand(CommandHandler(cmd))
    }

    override fun disable() {
    }

    @CommandClass
    inner class CommandHandler internal constructor(command: String) : ICommand2MC() {
        override val commandPath: String
        private val helpText: Array<String>
        private val appendedText: String?

        init {
            val conf = appendTexts!![command] ?: throw NoSuchElementException("AppendText command not found: $command")
            this.commandPath = command
            helpText = helpText(conf).get()
            appendedText = appendedText(conf).get()
        }

        @Command2.Subcommand
        fun def(sender: Command2MCSender, @Command2.OptionalArg @Command2.TextArg message: String?) {
            TBMCChatAPI.sendChatMessage(
                ChatMessage.Companion.builder(
                    sender.sender,
                    net.kyori.adventure.text.Component.text((if (message == null) "" else "$message ") + appendedText)
                ).fromCommand(true).permCheck(sender.permCheck).build()
            )
        }

        override fun getHelpText(method: Method, ann: Command2.Subcommand): Array<String> {
            return helpText
        }
    }
}
