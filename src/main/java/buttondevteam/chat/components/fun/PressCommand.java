package buttondevteam.chat.components.fun;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.core.component.restart.ScheduledRestartCommand;
import buttondevteam.lib.ChromaUtils;
import buttondevteam.lib.ScheduledServerRestartEvent;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import buttondevteam.lib.chat.TBMCChatAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;

@CommandClass
public class PressCommand extends ICommand2MC implements Listener {
	private HashSet<CommandSender> pressers; //Will be cleared with this class on shutdown/disable
	private ScheduledRestartCommand command;
	private int startTicks;

	@Command2.Subcommand
	public void def(CommandSender sender) {
		if (command == null) {
			sender.sendMessage("§cThe timer isn't ticking... yet.");
			return;
		}
		if (pressers.contains(sender)) {
			sender.sendMessage("§cYou cannot press more than once.");
			return;
		}
		pressers.add(sender);
		TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, String.format("§b-- %s §bpressed at %.0fs", ChromaUtils.getDisplayName(sender), command.getRestartCounter() / 20f), command.getComponent().getRestartBroadcast());
		command.setRestartCounter(startTicks);
	}

	@EventHandler
	public void restartEvent(ScheduledServerRestartEvent event) {
		command = event.getCommand();
		pressers = new HashSet<>();
		startTicks = event.getRestartTicks();
	}
}
