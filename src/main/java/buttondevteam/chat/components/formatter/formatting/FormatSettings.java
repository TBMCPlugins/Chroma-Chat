package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.lib.chat.Color;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FormatSettings {
	boolean italic;
	boolean bold;
	boolean underlined;
	boolean strikethrough;
	boolean obfuscated;
	Color color;
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
					//System.out.println("Setting " + field.getType() + " " + field.getName() + " from " + field.get(this) + " to " + field.get(settings));
					field.set(this, field.get(settings));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
