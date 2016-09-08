package buttondevteam.chat.commands.ucmds;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;
import buttondevteam.chat.PluginMain;

import com.earth2me.essentials.Mob;
import com.earth2me.essentials.Mob.MobException;

public class KittycannonCommand extends UCommandBase {

	private static Random random = new Random();

	public static String KittyCannonMinigame = "KittyCannon";

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Kittycannon ----",
				"This command is designed for the Kittycannon minigame" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		Player player = (Player) sender;
		MinigamePlayer mp = Minigames.plugin.pdata.getMinigamePlayer(player);
		if (!(mp.isInMinigame() && mp.getMinigame().getName(false)
				.equalsIgnoreCase(KittyCannonMinigame))) {
			sender.sendMessage("§cYou can only use KittyCannon in it's minigame!");
			return true;
		}
		try {
			final Mob cat = Mob.OCELOT;
			final Ocelot ocelot = (Ocelot) cat.spawn(player.getWorld(),
					player.getServer(), player.getEyeLocation());
			if (ocelot == null) {
				return true;
			}
			final ArrayList<String> lore = new ArrayList<>();
			lore.add(player.getName());
			final int i = random.nextInt(Ocelot.Type.values().length);
			ocelot.setCatType(Ocelot.Type.values()[i]);
			ocelot.setTamed(true);
			ocelot.setBaby();
			ocelot.addPotionEffect(new PotionEffect(
					PotionEffectType.DAMAGE_RESISTANCE, 5, 5));
			ocelot.setVelocity(player.getEyeLocation().getDirection()
					.multiply(2));
			Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.Instance,
					new Runnable() {
						@SuppressWarnings("deprecation")
						@Override
						public void run() {
							final Location loc = ocelot.getLocation();
							ocelot.remove();
							loc.getWorld().createExplosion(loc, 0F);
							final ItemStack head = new ItemStack(
									Material.SKULL_ITEM, 1, (short) 3, (byte) 3);
							SkullMeta im = (SkullMeta) head.getItemMeta();
							im.setDisplayName("§rOcelot Head");
							im.setOwner("MHF_Ocelot");
							im.setLore(lore);
							head.setItemMeta(im);
							loc.getWorld().dropItem(loc, head);
						}
					}, 20);
		} catch (MobException e) {
		}
		return true;
	}

	@Override
	public String GetUCommandPath() {
		return "kittycannon";
	}

}
