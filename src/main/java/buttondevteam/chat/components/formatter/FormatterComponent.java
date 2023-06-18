package buttondevteam.chat.components.formatter;

import buttondevteam.chat.PluginMain;
import buttondevteam.core.ComponentManager;
import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.config.IConfigData;

/**
 * This component handles the custom processing of chat messages. If this component is disabled channels won't be supported in Minecraft.
 * If you only want to disable the formatting features, set allowFormatting to false.
 * If you're using another chat plugin, you should disable the whole component but that will make it impossible to use channels.
 */
public class FormatterComponent extends Component<PluginMain> {
	/**
	 * Determines whether Markdown formatting, name mentioning and similar features are enabled.
	 */
	IConfigData<Boolean> allowFormatting = getConfig().getData("allowFormatting", true);

	/**
	 * The sound to play when a player is mentioned. Leave empty to use default.
	 */
	public IConfigData<String> notificationSound = getConfig().getData("notificationSound", "");

	/**
	 * The pitch of the notification sound.
	 */
	public IConfigData<Float> notificationPitch = getConfig().getData("notificationPitch", 1.0f);

	/**
	 * The minimum time between messages in milliseconds.
	 */
	public IConfigData<Integer> minTimeBetweenMessages = getConfig().getData("minTimeBetweenMessages", 100);

	@Override
	protected void enable() {
		MainPlugin.instance.setChatHandlerEnabled(false); //Disable Core chat handler - if this component is disabled then let it do its job
	}

	@Override
	protected void disable() {
		MainPlugin.instance.setChatHandlerEnabled(true);
	}

	/**
	 * Handles the chat if the component is enabled.
	 *
	 * @param event The chat event
	 * @return Whether the chat message shouldn't be sent for some reason
	 */
	public static boolean handleChat(TBMCChatEvent event) {
		FormatterComponent component = ComponentManager.getIfEnabled(FormatterComponent.class);
		if (component == null) return false;
		return ChatProcessing.ProcessChat(event, component);
	}
}
