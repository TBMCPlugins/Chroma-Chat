package buttondevteam.chat.components.formatter.formatting;

import lombok.Builder;
import lombok.Data;
import net.kyori.adventure.text.format.TextColor;

/**
 * Describes how a matched section of the message should look. May be combined with other format settings.
 */
@Data
@Builder
public class FormatSettings {
	boolean italic;
	boolean bold;
	boolean underlined;
	boolean strikethrough;
	boolean obfuscated;
	TextColor color;
	ChatFormatter.TriFunc<String, FormatSettings, FormattedSection, String> onmatch;
	String openlink;
	String hoverText;

	public void copyFrom(FormatSettings settings) {
		try {
			for (var field : FormatSettings.class.getDeclaredFields()) {
				if (field.getType() == boolean.class) {
					if (field.getBoolean(settings))
						field.setBoolean(this, true); //Set to true if either of them are true
				} else if (field.get(settings) != null) {
					field.set(this, field.get(settings));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
