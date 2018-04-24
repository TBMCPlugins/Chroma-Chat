package buttondevteam.chat.commands;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import buttondevteam.lib.player.TBMCPlayerBase;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

@CommandClass
public class FTopCommand extends TBMCCommandBase {

    @Override
    public String[] GetHelpText(String arg0) {
        return new String[]{ //
                "§6---- F Top ----", //
                "Shows the respect leaderboard" //
        };
    }

    private final File playerdir = new File(TBMCPlayerBase.TBMC_PLAYERS_DIR);
    private ChatPlayer[] cached;
    private long lastcache = 0;

    @Override
    public boolean OnCommand(CommandSender arg0, String arg1, String[] arg2) {
        if (cached == null || lastcache < System.nanoTime() - 60000000000L) { // 1m - (no guarantees of nanoTime's relation to 0, so we need the null check too)
            cached = Arrays.stream(playerdir.listFiles())
                    .map(f -> TBMCPlayerBase.getPlayer(
                            UUID.fromString(f.getName().substring(0, f.getName().length() - 4)), ChatPlayer.class))
                    .sorted((cp1, cp2) -> Float.compare((float) cp2.FCount().get() / (float) cp2.FDeaths().get(),
                            (float) cp1.FCount().get() / (float) cp1.FDeaths().get()))
                    .toArray(ChatPlayer[]::new); // TODO: Properly implement getting all players
            lastcache = System.nanoTime();
        }
        Arrays.stream(cached).limit(10);
        return true;
    }

}
