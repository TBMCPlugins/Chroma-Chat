package tk.sznp.thebuttonautoflair;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.inventivegames.TellRawAutoMessage.Reflection;

public class PlayerListener implements Listener
{ //2015.07.16.
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player p=event.getPlayer();
		MaybeOfflinePlayer.AddPlayerIfNeeded(p.getName()); //2015.08.08.
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(p.getName()); //2015.08.08.
		if(mp.CommentedOnReddit)
			PluginMain.AppendPlayerDisplayFlair(mp, p); //2015.08.09.
		else
		{ //2015.07.20.
			String json="[\"\",{\"text\":\"§6Hi! If you'd like your flair displayed ingame, write your §6Minecraft name to \"},{\"text\":\"[this thread.]\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.reddit.com/r/TheButtonMinecraft/comments/3d25do/\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread§r\"}]}}}]";
			sendRawMessage(p, json);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
	}

	public static String NotificationSound; //2015.08.14.
	public static float NotificationPitch; //2015.08.14.
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		MaybeOfflinePlayer player = MaybeOfflinePlayer.AllPlayers.get(event.getPlayer().getName());
		String flair=player.Flair; //2015.08.08.
		if(player.IgnoredFlair)
			flair="";
		String message=event.getMessage(); //2015.08.08.
		for(Player p : PluginMain.GetPlayers())
		{ //2015.08.12.
			String color=""; //2015.08.17.
			if(message.contains(p.getName()))
			{
				if(NotificationSound==null)
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f); //2015.08.12.
				else
					p.playSound(p.getLocation(), NotificationSound, 1.0f, NotificationPitch); //2015.08.14.
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p.getName()); //2015.08.17.
				color=mp.Flair.substring(0, 2);
			}
			message = message.replaceAll(p.getName(), color+p.getName()+"§r");
		}
		event.setFormat(event.getFormat().substring(0, event.getFormat().indexOf(">"))+flair+"> "+message); //2015.08.08.
	}

	private static Class<?>	nmsChatSerializer		= Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");
	private static Class<?>	nmsPacketPlayOutChat	= Reflection.getNMSClass("PacketPlayOutChat");
	public static void sendRawMessage(Player player, String message)
	{
		try {
			Object handle = Reflection.getHandle(player);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object serialized = Reflection.getMethod(nmsChatSerializer, "a", String.class).invoke(null, message);
			Object packet = nmsPacketPlayOutChat.getConstructor(Reflection.getNMSClass("IChatBaseComponent")).newInstance(serialized);
			Reflection.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
		} catch (Exception e) {
			e.printStackTrace();
			PluginMain.LastException=e; //2015.08.09.
		}
	}

}
