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
	ChatFormatter.TriFunc<String, ChatFormatter, FormattedSection, String> onmatch;
	String openlink;
}
