package buttondevteam.chat;

public class Channel {
	public final String DisplayName;
	public final ChatFormatter.Color Color;
	public final String Command;

	public Channel(String displayname, ChatFormatter.Color color, String command) {
		DisplayName = displayname;
		Color = color;
		Command = command;
	}

	public static Channel GlobalChat = new Channel("§fg§f",
			ChatFormatter.Color.White, "g");
	public static Channel TownChat = new Channel("§3TC§f",
			ChatFormatter.Color.DarkAqua, "tc");
	public static Channel NationChat = new Channel("§6NC§f",
			ChatFormatter.Color.Gold, "nc");
	public static Channel AdminChat = new Channel("§cADMIN§f",
			ChatFormatter.Color.Red, "a");
	public static Channel ModChat = new Channel("§9MOD§f",
			ChatFormatter.Color.Blue, "mod");
}
