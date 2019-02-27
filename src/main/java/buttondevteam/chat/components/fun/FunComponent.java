package buttondevteam.chat.components.fun;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.TBMCChatEventBase;
import buttondevteam.lib.TBMCCommandPreprocessEvent;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.ThorpeUtils;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayer;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Random;

public class FunComponent extends Component implements Listener {
	private final static String[] LaughStrings = new String[]{"xd", "lel", "lawl", "kek", "lmao", "hue", "hah", "rofl"};
	private boolean ActiveF = false;
	private ChatPlayer FPlayer = null;
	private BukkitTask Ftask = null;
	private ArrayList<CommandSender> Fs = new ArrayList<>();
	private UnlolCommand command;
	private TBMCSystemChatEvent.BroadcastTarget unlolTarget;
	@Override
	protected void enable() {
		unlolTarget = TBMCSystemChatEvent.BroadcastTarget.add("unlol");
		val pc = new PressCommand();
		registerCommand(pc);
		registerListener(pc);
		registerCommand(command=new UnlolCommand());
		registerListener(this);
		registerCommand(new FTopCommand());
	}

	@Override
	protected void disable() {

	}

	public void onChat(CommandSender sender, TBMCChatEventBase event, String message) {
		if (ActiveF && !Fs.contains(sender) && message.equalsIgnoreCase("F"))
			Fs.add(sender);

		String msg = message.toLowerCase();
		val lld = new UnlolCommand.LastlolData(sender, event, System.nanoTime());
		boolean add;
		if (add = msg.contains("lol"))
			lld.setLolornot(true);
		else {
			for (int i = 0; i < LaughStrings.length; i++) {
				if (add = msg.contains(LaughStrings[i])) {
					lld.setLolornot(false);
					break;
				}
			}
		}
		if (add)
			command.Lastlol.put(event.getChannel(), lld);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		// MinigamePlayer mgp = Minigames.plugin.pdata.getMinigamePlayer(e.getEntity());
		if (/* (mgp != null && !mgp.isInMinigame()) && */ new Random().nextBoolean()) { // Don't store Fs for NPCs
			Runnable tt = () -> {
				if (ActiveF) {
					ActiveF = false;
					if (FPlayer != null && FPlayer.FCount().get() < Integer.MAX_VALUE - 1)
						FPlayer.FCount().set(FPlayer.FCount().get() + Fs.size());
					Bukkit.broadcastMessage("§b" + Fs.size() + " " + (Fs.size() == 1 ? "person" : "people")
						+ " paid their respects.§r");
					Fs.clear();
				}
			};
			if (Ftask != null) {
				Ftask.cancel();
				tt.run(); //Finish previous one
			}
			ActiveF = true;
			Fs.clear();
			FPlayer = TBMCPlayer.getPlayer(e.getEntity().getUniqueId(), ChatPlayer.class);
			FPlayer.FDeaths().set(FPlayer.FDeaths().get() + 1);
			Bukkit.broadcastMessage("§bPress F to pay respects.§r");
			Bukkit.getScheduler().runTaskLaterAsynchronously(PluginMain.Instance, tt, 15 * 20);
		}
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		command.Lastlol.values().removeIf(lld -> lld.getLolowner().equals(event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommandPreprocess(TBMCCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		final String cmd = event.getMessage();
		// We don't care if we have arguments
		if (cmd.toLowerCase().startsWith("/un")) {
			for (HelpTopic ht : PluginMain.Instance.getServer().getHelpMap().getHelpTopics()) {
				if (ht.getName().equalsIgnoreCase(cmd))
					return;
			}
			if (PluginMain.permission.has(event.getSender(), "thorpe.unanything")) {
				event.setCancelled(true);
				String s = cmd.substring(3);
				int index = event.getMessage().indexOf(' ');
				if (index == -1) {
					event.getSender().sendMessage("§cUsage: /un" + s + " <player>");
					return;
				}
				Player target = Bukkit.getPlayer(event.getMessage().substring(index + 1));
				if (target == null) {
					event.getSender().sendMessage("§cError: Player not found. (/un" + s + " <player>)");
					return;
				}
				val user = ChromaGamerBase.getFromSender(event.getSender());
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 5, false, false));
				val chan = user.channel().get();
				TBMCChatAPI.SendSystemMessage(chan, chan.getRTR(event.getSender()), ThorpeUtils.getDisplayName(event.getSender()) + " un" + s
					+ "'d " + target.getDisplayName(), unlolTarget);
			}
		}
	}
}
