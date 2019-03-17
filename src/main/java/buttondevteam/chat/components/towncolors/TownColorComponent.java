package buttondevteam.chat.components.towncolors;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.components.towncolors.admin.TCCount;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ComponentMetadata;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.player.TBMCPlayerJoinEvent;
import com.earth2me.essentials.User;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import lombok.Getter;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.towny.DTBridge;
import org.dynmap.towny.DynmapTownyPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@ComponentMetadata(depends = TownyComponent.class)
public class TownColorComponent extends Component<PluginMain> implements Listener {
	/**
	 * Names lowercased
	 */
	public static Map<String, Color[]> TownColors = new HashMap<>();
	/**
	 * Names lowercased - nation color gets added to town colors when needed
	 */
	public static Map<String, Color> NationColor = new HashMap<>();

	public ConfigData<Byte> colorCount() {
		return getConfig().getData("colorCount", (byte) 1, cc -> ((Integer) cc).byteValue(), Byte::intValue);
	}

	public ConfigData<Boolean> useNationColors() {
		return getConfig().getData("useNationColors", true);
	}

	@Getter
	private static TownColorComponent component;

	@SuppressWarnings("unchecked")
	@Override
	protected void enable() {
		component = this;
		//TODO: Don't register all commands automatically (welp)
		Consumer<ConfigurationSection> loadTC = cs -> TownColorComponent.TownColors.putAll(cs.getValues(true).entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, v -> ((List<String>) v.getValue()).stream()
				.map(Color::valueOf).toArray(Color[]::new))));
		boolean usenc = useNationColors().get();
		Consumer<ConfigurationSection> loadNC = ncs ->
			TownColorComponent.NationColor.putAll(ncs.getValues(true).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, v -> Color.valueOf((String) v.getValue()))));
		var cs = getConfig().getConfig().getConfigurationSection("towncolors");
		if (cs != null)
			loadTC.accept(cs);
		else
			load_old(loadTC, null); //Load old data
		if (usenc) {
			var ncs = getConfig().getConfig().getConfigurationSection("nationcolors");
			if (ncs != null)
				loadNC.accept(ncs);
			else
				load_old(null, loadNC); //Why not choose by making different args null
		}

		TownColors.keySet().removeIf(t -> !TownyComponent.TU.getTownsMap().containsKey(t)); // Removes town colors for deleted/renamed towns
		if (usenc)
			NationColor.keySet().removeIf(n -> !TownyComponent.TU.getNationsMap().containsKey(n)); // Removes nation colors for deleted/renamed nations

		Bukkit.getScheduler().runTask(getPlugin(), () -> {
			val dtp = (DynmapTownyPlugin) Bukkit.getPluginManager().getPlugin("Dynmap-Towny");
			if (dtp == null)
				return;
			for (val entry : TownColors.entrySet()) {
				try {
					val town = TownyComponent.TU.getTownsMap().get(entry.getKey());
					Nation nation;
					Color nc;
					if (!useNationColors().get())
						nc = null;
					else if (!town.hasNation() || (nation = town.getNation()) == null || (nc = NationColor.get(nation.getName().toLowerCase())) == null)
						nc = Color.White;
					setTownColor(dtp, buttondevteam.chat.components.towncolors.admin.TownColorCommand.getTownNameCased(entry.getKey()), entry.getValue(), nc);
				} catch (Exception e) {
					TBMCCoreAPI.SendException("Error while setting town color for town " + entry.getKey() + "!", e);
				}
			}
		});

		registerCommand(new TownColorCommand(this));
		if (useNationColors().get())
			registerCommand(new NationColorCommand());
		registerCommand(new buttondevteam.chat.components.towncolors.admin.TownColorCommand());
		if (useNationColors().get())
			registerCommand(new buttondevteam.chat.components.towncolors.admin.NationColorCommand());
		registerCommand(new TCCount());
		registerListener(new TownyListener());
		registerListener(this);
	}

	@Override
	protected void disable() {
		getConfig().getConfig().createSection("towncolors", TownColors.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
			v -> Arrays.stream(v.getValue()).map(Enum::toString).toArray(String[]::new))));
		if (useNationColors().get())
			getConfig().getConfig().createSection("nationcolors", NationColor.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
				v -> v.getValue().toString())));
	}

	/**
	 * Sets a town's color on Dynmap.
	 *
	 * @param dtp    A reference for the Dynmap-Towny plugin
	 * @param town   The town's name using the correct casing
	 * @param colors The town's colors
	 */

	public static void setTownColor(DynmapTownyPlugin dtp, String town, Color[] colors, Color nationcolor) {
		Function<Color, Integer> c2i = c -> c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
		try {
			DTBridge.setTownColor(dtp, town, c2i.apply(nationcolor == null ? colors[0] : nationcolor),
				c2i.apply(colors.length > 1 && nationcolor != null ? colors[1] : colors[0]));
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Failed to set town color for town " + town + "!", e);
		}
	}

	private static String getPlayerNickname(Player player, User user, ChatPlayer cp) {
		String nickname = user.getNick(true);
		if (nickname.contains("~")) //StartsWith doesn't work because of color codes
			nickname = nickname.replace("~", ""); //It gets stacked otherwise
		String name = ChatColor.stripColor(nickname); //Enforce "town colors" on non-members
		val res = TownyComponent.TU.getResidentMap().get(player.getName().toLowerCase());
		if (res == null || !res.hasTown())
			return name;
		try {
			Color[] clrs = Optional.ofNullable(
				TownColors.get(res.getTown().getName().toLowerCase())
			).orElse(new Color[]{Color.White}); //Use white as default town color
			StringBuilder ret = new StringBuilder();
			AtomicInteger prevlen = new AtomicInteger();
			BiFunction<Color, Integer, String> anyColoredNamePart = (c, len) -> "§" //Len==0 if last part
				+ Integer.toHexString(c.ordinal()) // 'Odds' are the last character is chopped off so we make sure to include all chars at the end
				+ (len == 0 ? name.substring(prevlen.get())
				: name.substring(prevlen.get(), prevlen.addAndGet(len)));
			BiFunction<Integer, Integer, String> coloredNamePart = (len, i)
				-> anyColoredNamePart.apply(clrs[i], i + 1 == clrs.length ? 0 : len);
			final int len = name.length() / (clrs.length + 1); //The above param is needed because this isn't always passed
			Color nc;
	        /*if(res.getTown().hasNation()
			        &&(nc=PluginMain.NationColor.get(res.getTown().getNation().getName().toLowerCase()))!=null)
	        	len = name.length() / (clrs.length+1);
	        else
	        	len = name.length() / clrs.length;*/
			boolean usenc = component.useNationColors().get();
			val nclar = cp.NameColorLocations().get();
			int[] ncl = nclar == null ? null : nclar.stream().mapToInt(Integer::intValue).toArray();
			if (ncl != null && (Arrays.stream(ncl).sum() != name.length() || ncl.length != clrs.length + (usenc ? 1 : 0))) //+1: Nation color
				ncl = null; // Reset if name length changed
			//System.out.println("ncl: "+Arrays.toString(ncl)+" - sum: "+Arrays.stream(ncl).sum()+" - name len: "+name.length());
			if (usenc) {
				if (!res.getTown().hasNation()
					|| (nc = NationColor.get(res.getTown().getNation().getName().toLowerCase())) == null)
					nc = Color.White;
				ret.append(anyColoredNamePart.apply(nc, ncl == null ? len : ncl[0])); //Make first color the nation color
			}
			for (int i = 0; i < clrs.length; i++)
				ret.append(coloredNamePart.apply(ncl == null ? len : (usenc ? ncl[i + 1] : ncl[i]), i));
			return ret.toString();
		} catch (NotRegisteredException e) {
			return nickname;
		}
	}

	/**
	 * Checks if the component is enabled
	 */
	public static void updatePlayerColors(Player player) { //Probably while ingame (/u ncolor)
		updatePlayerColors(player, ChatPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class));
	}

	/**
	 * Checks if the component is enabled
	 */
	private static void updatePlayerColors(Player player, ChatPlayer cp) { //Probably at join - nop, nicknames
		if (!ComponentManager.isEnabled(TownColorComponent.class))
			return;
		User user = PluginMain.essentials.getUser(player);
		user.setNickname(getPlayerNickname(player, user, cp));
		user.setDisplayNick(); //These won't fire the nick change event
		cp.FlairUpdate(); //Update in list
	}

	@EventHandler
	public void onPlayerJoin(TBMCPlayerJoinEvent event) {
		updatePlayerColors(event.getPlayer(), event.GetPlayer().asPluginPlayer(ChatPlayer.class));
	}

	private static void load_old(Consumer<ConfigurationSection> loadTC,
	                             Consumer<ConfigurationSection> loadNC) {
		PluginMain.Instance.getLogger().info("Loading files...");
		try {
			File file = new File("TBMC/chatsettings.yml");
			if (file.exists()) {
				YamlConfiguration yc = new YamlConfiguration();
				yc.load(file);
				ConfigurationSection cs;
				if (loadTC != null && (cs = yc.getConfigurationSection("towncolors")) != null)
					loadTC.accept(cs);
				if (loadNC != null && (cs = yc.getConfigurationSection("nationcolors")) != null)
					loadNC.accept(cs);
				PluginMain.Instance.getLogger().info("Loaded files!");
			} else
				PluginMain.Instance.getLogger().info("No files to load, first run probably.");
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while loading chat files!", e);
		}
	}
}
