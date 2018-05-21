package buttondevteam.chat.commands;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import buttondevteam.lib.player.TBMCPlayerBase;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        Bukkit.getScheduler().runTaskAsynchronously(PluginMain.Instance, () -> {
            if (cached == null || lastcache < System.nanoTime() - 60000000000L) { // 1m - (no guarantees of nanoTime's relation to 0, so we need the null check too)
                cached = Arrays.stream(Objects.requireNonNull(playerdir.listFiles())).sequential()
                        .filter(f -> f.getName().length() > 4)
                        .map(f -> {
                            try {
                                return TBMCPlayerBase.getPlayer(
                                        UUID.fromString(f.getName().substring(0, f.getName().length() - 4)), ChatPlayer.class);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .sorted((cp1, cp2) -> Double.compare(cp2.getF(), cp1.getF()))
                        .toArray(ChatPlayer[]::new); // TODO: Properly implement getting all players
                lastcache = System.nanoTime();
            }
            int i;
            try {
                i = arg2.length > 0 ? Integer.parseInt(arg2[0]) : 1;
                if (i < 1)
                    i = 1; //i=1
            } catch (Exception e) {
                i = 1;
            }
            val ai = new AtomicInteger();
            arg0.sendMessage("§6---- Top Fs ----");
            arg0.sendMessage(Arrays.stream(cached).skip((i - 1) * 10).limit(i * 10)
                    .map(cp -> String.format("%d. %s - %f.2", ai.incrementAndGet(), cp.PlayerName().get(), cp.getF()))
                    .collect(Collectors.joining("\n")));
        });
        return true;
    }

}
