package buttondevteam.chat.components.announce;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.chat.components.formatter.ChatProcessing;
import buttondevteam.chat.components.formatter.FormatterComponent;
import buttondevteam.chat.components.formatter.formatting.TellrawEvent;
import buttondevteam.chat.components.formatter.formatting.TellrawPart;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandClass(modOnly = true)
@RequiredArgsConstructor
public class AnnounceCommand extends UCommandBase {
	private final AnnouncerComponent component;

	@Command2.Subcommand(helpText = {
		"Add announcement",
		"This command adds a new announcement",
	})
	public boolean add(CommandSender sender, @Command2.TextArg String text) {
		String finalmessage = text.replace('&', '§');
		component.announceMessages.get().add(finalmessage);
		sender.sendMessage("§bAnnouncement added.§r");
		return true;
	}

	@Command2.Subcommand(helpText = {
		"Edit announcement",
		"This command lets you edit an announcement by its index.",
		"Shouldn't be used directly, use either command blocks or click on an announcement in /u announce list instead."
	})
	public boolean edit(CommandSender sender, byte index, @Command2.TextArg String text) {
		String finalmessage1 = text.replace('&', '§');
		if (index > 100)
			return false;
		while (component.announceMessages.get().size() <= index)
			component.announceMessages.get().add("");
		component.announceMessages.get().set(index, finalmessage1);
		sender.sendMessage("Announcement edited.");
		return true;
	}

	@Command2.Subcommand(helpText = {
		"List announcements",
		"This command lists the announcements and the time between them"
	})
	public boolean list(CommandSender sender) {
		sender.sendMessage("§bList of announce messages:§r");
		sender.sendMessage("§bFormat: [index] message§r");
		int i = 0;
		for (String message : component.announceMessages.get()) {
			String msg = "[" + i++ + "] " + message;
			//noinspection SuspiciousMethodCalls
			if (!ComponentManager.isEnabled(FormatterComponent.class) || !Bukkit.getOnlinePlayers().contains(sender)) {
				sender.sendMessage(msg);
				continue;
			}
			String json = ChatProcessing.toJson(new TellrawPart(msg)
				.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT, "Click to edit"))
				.setClickEvent(TellrawEvent.create(TellrawEvent.ClickAction.SUGGEST_COMMAND,
					"/" + getCommandPath() + " edit " + (i - 1) + " " + message.replace('§', '&'))));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + json);
		}
		sender.sendMessage("§bCurrent wait time between announcements: "
			+ component.announceTime.get() / 60 / 1000 + " minute(s)§r");
		return true;
	}

	@Command2.Subcommand(helpText = {
		"Remove announcement",
		"This command removes an announcement"
	})
	public boolean remove(CommandSender sender, int index) {
		val msgs = component.announceMessages.get();
		if (index < 0 || index > msgs.size()) return false;
		msgs.remove(index);
		sender.sendMessage("Announcement removed.");
		return true;
	}

	@Command2.Subcommand(helpText = {
		"Set time",
		"This command sets the time between the announcements"
	})
	public boolean settime(CommandSender sender, int minutes) {
		component.announceTime.set(minutes * 60 * 1000);
		sender.sendMessage("Time set between announce messages to " + minutes + " minutes");
		return true;
	}
}
