package buttondevteam.chat.components.fun;

import buttondevteam.core.component.restart.ScheduledRestartCommand;
import buttondevteam.lib.ScheduledServerRestartEvent;
import buttondevteam.lib.ThorpeUtils;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.Bukkit;
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
		Bukkit.broadcastMessage(String.format("§b-- %s §bpressed at %.0fs", ThorpeUtils.getDisplayName(sender), command.getRestartCounter() / 20f));
		command.setRestartCounter(startTicks);
	}

	@EventHandler
	public void restartEvent(ScheduledServerRestartEvent event) {
		command = event.getCommand();
		pressers = new HashSet<>();
		startTicks = event.getRestartTicks();
	}
}
