package buttondevteam.chat.components.appendext;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.architecture.IHaveConfig;
import buttondevteam.lib.chat.*;
import buttondevteam.lib.player.ChromaGamerBase;
import lombok.val;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class AppendTextComponent extends Component<PluginMain> {
	private Map<String, IHaveConfig> appendTexts;
	private ConfigData<String[]> helpText(IHaveConfig config) {
		return config.getData("helpText", () -> new String[]{
			"Tableflip", //
			"This command appends a tableflip after your message", //
			"Or just makes you tableflip", //
		});
	}

	private ConfigData<String> appendedText(IHaveConfig config) {
		return config.getData("appendedText", () -> "tableflip");
	}

	@Override
	protected void enable() {
		val map = new HashMap<String, Consumer<IHaveConfig>>();
		map.put("tableflip", conf -> {
			helpText(conf).set(new String[]{
				"Tableflip", //
				"This command appends a tableflip after your message", //
				"Or just makes you tableflip", //
			});
			appendedText(conf).set("(╯°□°）╯︵ ┻━┻");
		});
		map.put("unflip", conf -> {
			helpText(conf).set(new String[]{
				"Unflip", //
				"This command appends an unflip after your message", //
				"Or just unflips as you", //
			});
			appendedText(conf).set("┬─┬ ノ( ゜-゜ノ)");
		});
		map.put("shrug", conf -> {
			helpText(conf).set(new String[]{
				"Shrug", //
				"This command appends a shrug after your message", //
				"Or just makes you shrug", //
			});
			appendedText(conf).set("¯\\\\\\_(ツ)\\_/¯");
		});
		map.put("lenny", conf -> {
			helpText(conf).set(new String[]{
				"Lenny", //
				"This command appends a Lenny face after your message", //
				"Or just sends one", //
			});
			appendedText(conf).set("( ͡° ͜ʖ ͡°)");
		});
		map.put("ww", conf -> {
			helpText(conf).set(new String[]{
				"Wait what", //
				"Wait what" //
			});
			appendedText(conf).set("wait what");
		});
		appendTexts = getConfigMap("texts", map);
		for (String cmd : appendTexts.keySet())
			registerCommand(new CommandHandler(cmd));
	}

	@Override
	protected void disable() {

	}

	@CommandClass
	public class CommandHandler extends ICommand2MC {
		private final String path;
		private final String[] helpText;
		private final String appendedText;

		CommandHandler(String command) {
			val conf = appendTexts.get(command);
			if (conf == null) throw new NoSuchElementException("AppendText command not found: " + command);
			path = command;
			helpText = helpText(conf).get();
			appendedText = appendedText(conf).get();

		}

		@Command2.Subcommand
		public void def(CommandSender sender, @Command2.OptionalArg @Command2.TextArg String message) {
			TBMCChatAPI.SendChatMessage(ChatMessage.builder(sender, ChromaGamerBase.getFromSender(sender),
				(message == null ? "" : message + " ") + appendedText).fromCommand(true).build());
		}

		@Override
		public String getCommandPath() {
			return path;
		}

		@Override
		public String[] getHelpText(Method method, Command2.Subcommand ann) {
			return helpText;
		}
	}
}
