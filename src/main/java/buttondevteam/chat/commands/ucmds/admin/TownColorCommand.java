package buttondevteam.chat.commands.ucmds.admin;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.Color;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.dynmap.towny.DynmapTownyPlugin;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TownColorCommand extends AdminCommandBase {
    @Override
    public String GetHelpText(String alias)[] { // TODO: Command path aliases
        return new String[]{ //
                "§6---- Town Color ----", //
                "This command allows setting a color for a town.", //
                "The town will be shown with this color on Dynmap and all players in the town will appear in chat with these colors.", //
                "The colors will split the name evenly.", //
                "Usage: /" + GetCommandPath() + " <town> <colorname1> [colorname2...]", //
                "Example: /" + GetCommandPath() + " Alderon blue gray" //
        };
    }

    @Override
    public boolean OnCommand(CommandSender sender, String alias, String[] args) {
        return SetTownColor(sender, alias, args);
    }

    public static boolean SetTownColor(CommandSender sender, String alias, String[] args) {
        if (args.length < 2)
            return false;
        if (!PluginMain.TU.getTownsMap().containsKey(args[0].toLowerCase())) {
            sender.sendMessage("§cThe town '" + args[0] + "' cannot be found.");
            return true;
        }
        val clrs = new Color[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            val ii = i;
            val c = Arrays.stream(Color.values()).filter(cc -> cc.getName().equalsIgnoreCase(args[ii])).findAny();
            if (!c.isPresent()) {
                sender.sendMessage("§cThe color '" + args[i] + "' cannot be found.");
                sender.sendMessage("§cAvailable colors: " + Arrays.stream(Color.values()).map(Color::getName).collect(Collectors.joining(", ")));
                return true;
            }
            clrs[i - 1] = c.get();
        }
        PluginMain.TownColors.put(args[0].toLowerCase(), clrs);
        val dtp = (DynmapTownyPlugin) Bukkit.getPluginManager().getPlugin("Dynmap-Towny");
        if (dtp == null) {
            sender.sendMessage("§cDynmap-Towny couldn'5 be found to set town color.");
            return true;
        }
        PluginMain.setTownColor(dtp, args[0].toLowerCase(), clrs);
        sender.sendMessage("§bColor(s) set.");
        return true;
    }
}
