package buttondevteam.chat.formatting;

import java.util.function.Function;
import java.util.regex.Pattern;

import buttondevteam.lib.chat.*;

public class ChatFormatterBuilder {
	private Pattern regex;
	private boolean italic;
	private boolean bold;
	private boolean underlined;
	private boolean strikethrough;
	private boolean obfuscated;
	private Color color;
	private Function<String, String> onmatch;
	private String openlink;
	private Priority priority;
	private short removecharcount = 0;
	private short removecharpos = -1;
	private boolean range = false;

	public ChatFormatter build() {
		return new ChatFormatter(regex, italic, bold, underlined, strikethrough, obfuscated, color, onmatch, openlink,
				priority, removecharcount, removecharpos, range);
	}

	public Pattern getRegex() {
		return regex;
	}

	public ChatFormatterBuilder setRegex(Pattern regex) {
		this.regex = regex;
		return this;
	}

	public boolean isItalic() {
		return italic;
	}

	public ChatFormatterBuilder setItalic(boolean italic) {
		this.italic = italic;
		return this;
	}

	public boolean isBold() {
		return bold;
	}

	public ChatFormatterBuilder setBold(boolean bold) {
		this.bold = bold;
		return this;
	}

	public boolean isUnderlined() {
		return underlined;
	}

	public ChatFormatterBuilder setUnderlined(boolean underlined) {
		this.underlined = underlined;
		return this;
	}

	public boolean isStrikethrough() {
		return strikethrough;
	}

	public ChatFormatterBuilder setStrikethrough(boolean strikethrough) {
		this.strikethrough = strikethrough;
		return this;
	}

	public boolean isObfuscated() {
		return obfuscated;
	}

	public ChatFormatterBuilder setObfuscated(boolean obfuscated) {
		this.obfuscated = obfuscated;
		return this;
	}

	public Color getColor() {
		return color;
	}

	public ChatFormatterBuilder setColor(Color color) {
		this.color = color;
		return this;
	}

	public Function<String, String> getOnmatch() {
		return onmatch;
	}

	public ChatFormatterBuilder setOnmatch(Function<String, String> onmatch) {
		this.onmatch = onmatch;
		return this;
	}

	public String getOpenlink() {
		return openlink;
	}

	public ChatFormatterBuilder setOpenlink(String openlink) {
		this.openlink = openlink;
		return this;
	}

	public Priority getPriority() {
		return priority;
	}

	public ChatFormatterBuilder setPriority(Priority priority) {
		this.priority = priority;
		return this;
	}

	public short getRemoveCharCount() {
		return removecharcount;
	}

	/**
	 * Sets the amount of characters to be removed from the start and the end of the match.
	 * 
	 * @return This instance
	 */
	public ChatFormatterBuilder setRemoveCharCount(short removecharcount) {
		this.removecharcount = removecharcount;
		return this;
	}

	public short getRemoveCharPos() {
		return removecharpos;
	}

	/**
	 * Sets the position where a single character should be removed. Setting -1 will disable it.
	 * 
	 * @return This instance
	 */
	public ChatFormatterBuilder setRemoveCharPos(short removecharpos) {
		this.removecharpos = removecharpos;
		return this;
	}

	public boolean isRange() {
		return range;
	}

	public ChatFormatterBuilder setRange(boolean range) {
		this.range = range;
		return this;
	}
}
