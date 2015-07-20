package tk.sznp.thebuttonautoflair;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener
{ //2015.07.16.
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player p=event.getPlayer();
		PluginMain.Players.add(p);
		//event.getPlayer().setDisplayName(p.getDisplayName()+PluginMain.GetFlair(p));
		PluginMain.AppendPlayerDisplayFlair(p, PluginMain.PlayerUserNames.get(p.getName()), PluginMain.GetFlair(p));
		System.out.println("Added player "+p.getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		//for(Player player : PluginMain.Players)
		for(int i=0; i<PluginMain.Players.size();)
		{
			Player player=PluginMain.Players.get(i);
			if(player.getName().equals(event.getPlayer().getName()))
			{
				PluginMain.Players.remove(player);
				System.out.println("Removed player "+event.getPlayer().getName());
			}
			else
				i++; //If the player is removed, the next item will be on the same index
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		
	}
}
