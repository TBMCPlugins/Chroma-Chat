package buttondevteam.chat;

import java.util.ArrayList;
import java.util.List;

import buttondevteam.chat.formatting.ChatFormatter;

public class Channel {
	public final String DisplayName;
	public final ChatFormatter.Color Color;
	public final String Command;

	private static List<Channel> channels = new ArrayList<>();

	public Channel(String displayname, ChatFormatter.Color color, String command) {
		DisplayName = displayname;
		Color = color;
		Command = command;
	}

	static {
		channels.add(GlobalChat = new Channel("§fg§f", ChatFormatter.Color.White, "g"));
		channels.add(TownChat = new Channel("§3TC§f", ChatFormatter.Color.DarkAqua, "tc"));
		channels.add(NationChat = new Channel("§6NC§f", ChatFormatter.Color.Gold, "nc"));
		channels.add(AdminChat = new Channel("§cADMIN§f", ChatFormatter.Color.Red, "a"));
		channels.add(ModChat = new Channel("§9MOD§f", ChatFormatter.Color.Blue, "mod"));
	}

	public static List<Channel> getChannels() {
		return channels;
	}

	public static Channel GlobalChat;
	public static Channel TownChat;
	public static Channel NationChat;
	public static Channel AdminChat;
	public static Channel ModChat;
}
