package buttondevteam.thebuttonmcchat;

import java.util.function.Function;
import java.util.regex.Pattern;

import buttondevteam.thebuttonmcchat.ChatFormatter.Color;
import buttondevteam.thebuttonmcchat.ChatFormatter.Format;
import buttondevteam.thebuttonmcchat.ChatFormatter.Priority;

public class ChatFormatterBuilder {
	private Pattern regex;
	private Format format;
	private Color color;
	private Function<String, String> onmatch;
	private String openlink;
	private Priority priority;
	private String replacewith;

	public ChatFormatter build() {
		return new ChatFormatter(regex, format, color, onmatch, openlink, priority, replacewith);
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

	public String getReplacewith() {
		return replacewith;
	}

	public ChatFormatterBuilder setReplacewith(String replacewith) {
		this.replacewith = replacewith;
		return this;
	}
}
