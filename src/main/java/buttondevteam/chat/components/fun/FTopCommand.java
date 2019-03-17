package buttondevteam.chat.components.fun;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
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

@CommandClass(helpText = {
	"§6---- F Top ----", //
	"Shows the respect leaderboard" //
})
public class FTopCommand extends ICommand2MC {

    private final File playerdir = new File(TBMCPlayerBase.TBMC_PLAYERS_DIR);
    private ChatPlayer[] cached;
    private long lastcache = 0;

    public boolean def(CommandSender sender, @Command2.OptionalArg int page) {
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
				i = page<1?1:page;
			} catch (Exception e) {
				i = 1;
			}
            val ai = new AtomicInteger();
            sender.sendMessage("§6---- Top Fs ----");
            sender.sendMessage(Arrays.stream(cached).skip((i - 1) * 10).limit(i * 10)
                    .map(cp -> String.format("%d. %s - %f.2", ai.incrementAndGet(), cp.PlayerName().get(), cp.getF()))
                    .collect(Collectors.joining("\n")));
        });
        return true;
    }

}
