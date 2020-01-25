package buttondevteam.chat.components.fun;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.ChromaUtils;
import buttondevteam.lib.TBMCChatEventBase;
import buttondevteam.lib.TBMCCommandPreprocessEvent;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
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

import java.util.HashSet;
import java.util.Random;

public class FunComponent extends Component<PluginMain> implements Listener {
	private boolean ActiveF = false;
	private ChatPlayer FPlayer = null;
	private BukkitTask Ftask = null;
	private HashSet<CommandSender> Fs = new HashSet<>();
	private UnlolCommand command;
	private TBMCSystemChatEvent.BroadcastTarget unlolTarget;
	private TBMCSystemChatEvent.BroadcastTarget fTarget;

	private ConfigData<String[]> laughStrings() {
		return getConfig().getData("laughStrings", () -> new String[]{"xd", "lel", "lawl", "kek", "lmao", "hue", "hah", "rofl"});
	}

	private ConfigData<Boolean> respect() {
		return getConfig().getData("respect", true);
	}

	private ConfigData<Boolean> unlol() {
		return getConfig().getData("unlol", true);
	}

	@Override
	protected void enable() {
		unlolTarget = TBMCSystemChatEvent.BroadcastTarget.add("unlol");
		fTarget = TBMCSystemChatEvent.BroadcastTarget.add("respect");
		val pc = new PressCommand();
		registerCommand(pc);
		registerListener(pc);
		registerCommand(command=new UnlolCommand(unlolTarget));
		registerListener(this);
		registerCommand(new FTopCommand());
		registerCommand(new OpmeCommand());
		registerCommand(new YeehawCommand());
		registerCommand(new CCommand());
	}

	@Override
	protected void disable() {

	}

	public void onChat(CommandSender sender, TBMCChatEventBase event, String message) {
		if (ActiveF && !Fs.contains(sender) && message.equalsIgnoreCase("F"))
			Fs.add(sender);

		if (unlol().get()) {
			String msg = message.toLowerCase();
			val lld = new UnlolCommand.LastlolData(sender, event, System.nanoTime());
			boolean add;
			if (add = msg.contains("lol"))
				lld.setLolornot(true);
			else {
				String[] laughs = laughStrings().get();
				for (String laugh : laughs) {
					if (add = msg.contains(laugh)) {
						lld.setLolornot(false);
						break;
					}
				}
			}
			if (add)
				command.Lastlol.put(event.getChannel(), lld);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		// MinigamePlayer mgp = Minigames.plugin.pdata.getMinigamePlayer(e.getEntity());
		if (e.getDeathMessage().length() > 0 && respect().get() && new Random().nextBoolean()) { // Don't store Fs for NPCs
			Runnable tt = () -> {
				if (ActiveF) {
					ActiveF = false;
					if (FPlayer != null && FPlayer.FCount().get() < Integer.MAX_VALUE - 1)
						FPlayer.FCount().set(FPlayer.FCount().get() + Fs.size());
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL,
						"§b" + Fs.size() + " " + (Fs.size() == 1 ? "person" : "people")
						+ " paid their respects.§r", fTarget);
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
			TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL,
				"§bPress F to pay respects.§r", fTarget);
			Ftask = Bukkit.getScheduler().runTaskLaterAsynchronously(PluginMain.Instance, tt, 15 * 20);
		}
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (unlol().get())
			command.Lastlol.values().removeIf(lld -> lld.getLolowner().equals(event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommandPreprocess(TBMCCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		if (!unlol().get()) return;
		final String cmd = event.getMessage();
		// We don't care if we have arguments
		if (cmd.toLowerCase().startsWith("/un")) {
			for (HelpTopic ht : PluginMain.Instance.getServer().getHelpMap().getHelpTopics()) {
				if (ht.getName().equalsIgnoreCase(cmd))
					return;
			}
			if (PluginMain.permission.has(event.getSender(), "chroma.unanything")) {
				event.setCancelled(true);
				int index = cmd.lastIndexOf(' ');
				if (index == -1) {
					event.getSender().sendMessage("§cUsage: " + cmd + " <player>");
					return;
				}
				String s = cmd.substring(3, index);
				Player target = Bukkit.getPlayer(cmd.substring(index + 1));
				if (target == null) {
					event.getSender().sendMessage("§cError: Player not found. (/un" + s + " <player>)");
					return;
				}
				val user = ChromaGamerBase.getFromSender(event.getSender());
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 5, false, false));
				val chan = user.channel().get();
				TBMCChatAPI.SendSystemMessage(chan, chan.getRTR(event.getSender()), ChromaUtils.getDisplayName(event.getSender()) + " un" + s
					+ "'d " + target.getDisplayName(), unlolTarget);
			}
		}
	}
}
