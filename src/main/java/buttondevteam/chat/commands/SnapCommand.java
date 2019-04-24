package buttondevteam.chat.commands;

import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Random;

@CommandClass(modOnly = true)
public class SnapCommand extends ICommand2MC {
	@Command2.Subcommand
	public void def(CommandSender sender) {
		val pls = new ArrayList<>(Bukkit.getOnlinePlayers());
		int target = pls.size() / 2;
		Random rand = new Random();
		//noinspection SuspiciousMethodCalls
		if (pls.remove(sender) && target > 0)
			target--; //The sender isn't kicked, so kick someone else
		while (pls.size() > target)
			pls.remove(rand.nextInt(pls.size())).kickPlayer("You were saved by Thanos.");
		sender.sendMessage(target + " grateful players remain.");
	}
}
