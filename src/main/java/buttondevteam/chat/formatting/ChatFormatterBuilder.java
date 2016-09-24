package buttondevteam.chat.formatting;

import java.util.function.Function;
import java.util.regex.Pattern;

import buttondevteam.chat.formatting.ChatFormatter.Color;
import buttondevteam.chat.formatting.ChatFormatter.Format;
import buttondevteam.chat.formatting.ChatFormatter.Priority;

public class ChatFormatterBuilder {
	private Pattern regex;
	private Format format;
	private Color color;
	private Function<String, String> onmatch;
	private String openlink;
	private Priority priority;
	private short removecharcount = 0;
	private short removecharpos = -1;

	public ChatFormatter build() {
		return new ChatFormatter(regex, format, color, onmatch, openlink, priority, removecharcount, removecharpos);
	}

	public Pattern getRegex() {
		return regex;
	}

	public ChatFormatterBuilder setRegex(Pattern regex) {
		this.regex = regex;
		return this;
	}

	public Format getFormat() {
		return format;
	}

	public ChatFormatterBuilder setFormat(Format format) {
		this.format = format;
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
}
