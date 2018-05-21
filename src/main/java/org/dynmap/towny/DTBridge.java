package org.dynmap.towny;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.markers.MarkerAPI;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class DTBridge {
	/**
	 * Sets the town color on Dynmap.
	 * 
	 * @param dtp
	 *            The Dynmap-Towny plugin
	 * @param townname
     *            The name of the town, using correct casing
	 * @param strokecolor
	 *            The stroke color in RGB format
	 * @param fillcolor
	 *            The fill color in RGB format
	 * @throws Exception
	 *             When couldn't set the town color
	 */
	public static void setTownColor(DynmapTownyPlugin dtp, String townname, int strokecolor, int fillcolor)
			throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, // Keeping these because why not
			IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
		Class<?> cl = Class.forName(DynmapTownyPlugin.class.getName() + "$AreaStyle");
		Field field = DynmapTownyPlugin.class.getDeclaredField("cusstyle");
        field.setAccessible(true); // Doesn't allow accessing it from the same package, if it's from a different plugin
		@SuppressWarnings("unchecked")
		val map = (Map<String, Object>) field.get(dtp);
		Object style = map.get(townname);
		if (style == null) {
			Constructor<?> c = cl.getDeclaredConstructor(FileConfiguration.class, String.class, MarkerAPI.class);
			c.setAccessible(true);
            style = c.newInstance(dtp.getConfig(), "custstyle." + townname,
					((DynmapPlugin) Bukkit.getPluginManager().getPlugin("dynmap")).getMarkerAPI());
			map.put(townname, style);
		}
		set(cl, style, "fillcolor", fillcolor);
		set(cl, style, "strokecolor", strokecolor);
	}

	private static <T> void set(Class<?> cl, Object style, String fieldname, T value)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = cl.getDeclaredField(fieldname);
		field.setAccessible(true);
		field.set(style, value);
	}
}
