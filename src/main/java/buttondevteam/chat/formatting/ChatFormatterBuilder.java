package buttondevteam.chat.formatting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import buttondevteam.lib.chat.*;
import lombok.SneakyThrows;

public class ChatFormatterBuilder implements Serializable {
	private static final long serialVersionUID = -6115913400749778686L;
	Pattern regex;
	boolean italic;
	boolean bold;
	boolean underlined;
	boolean strikethrough;
	boolean obfuscated;
	Color color;
	BiFunction<String, ChatFormatterBuilder, String> onmatch;
	String openlink;
	Priority priority = Priority.Normal;
	short removecharcount = 0;
	boolean range = false;

	/**
	 * The returned object is backed by this builder. All changes made to this object affets the returned one.
	 */
	@SneakyThrows
	public ChatFormatter build() {
		return new ChatFormatter(this);
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

	public BiFunction<String, ChatFormatterBuilder, String> getOnmatch() {
		return onmatch;
	}

	/**
	 * Making any changes here using the builder will not affect the previous matches with the current design
	 */
	public ChatFormatterBuilder setOnmatch(BiFunction<String, ChatFormatterBuilder, String> onmatch) {
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
		this.priority = priority == null ? Priority.Normal : priority;
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

	public boolean isRange() {
		return range;
	}

	public ChatFormatterBuilder setRange(boolean range) {
		this.range = range;
		return this;
	}
}
