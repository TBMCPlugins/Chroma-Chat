package buttondevteam.chat.formatting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class TellrawPart implements Serializable {
	private static final long serialVersionUID = 4125357644462144024L;
	private ChatFormatter.Color color;
	private transient ChatFormatter.Format format;
	private boolean italics;
	private boolean bold;
	private boolean underlined;
	private boolean strikethrough;
	private boolean obfuscated;
	private List<TellrawPart> extra = new ArrayList<>();
	private String text;
	private TellrawEvent<TellrawEvent.HoverAction> hoverEvent;
	private TellrawEvent<TellrawEvent.ClickAction> clickEvent;

	public TellrawPart(String text) {
		this.text = text;
	}

	public ChatFormatter.Color getColor() {
		return color;
	}

	public TellrawPart setColor(ChatFormatter.Color color) {
		this.color = color;
		return this;
	}

	public ChatFormatter.Format getFormat() {
		return format;
	}

	public TellrawPart setFormat(ChatFormatter.Format format) {
		this.format = format;
		this.italics = false;
		this.bold = false;
		this.underlined = false;
		this.strikethrough = false;
		this.obfuscated = false;
		if (format.equals(ChatFormatter.Format.Italic))
			this.italics = true;
		else if (format.equals(ChatFormatter.Format.Bold))
			this.bold = true;
		else if (format.equals(ChatFormatter.Format.Underlined))
			this.underlined = true;
		else if (format.equals(ChatFormatter.Format.Strikethrough))
			this.strikethrough = true;
		else if (format.equals(ChatFormatter.Format.Obfuscated))
			this.obfuscated = true;
		else // TODO: Don't serialize false values, find out why is it bugging
			throw new UnsupportedOperationException("Trying to set to an unknown format!");
		return this;
	}

	public Iterable<TellrawPart> getExtra() {
		return extra;
	}

	public TellrawPart addExtra(TellrawPart extra) {
		this.extra.add(extra);
		return this;
	}

	public String getText() {
		return text;
	}

	public TellrawPart setText(String text) {
		this.text = text;
		return this;
	}

	public TellrawEvent<TellrawEvent.HoverAction> getHoverEvent() {
		return hoverEvent;
	}

	public TellrawPart setHoverEvent(TellrawEvent<TellrawEvent.HoverAction> hoverEvent) {
		this.hoverEvent = hoverEvent;
		return this;
	}

	public TellrawEvent<TellrawEvent.ClickAction> getClickEvent() {
		return clickEvent;
	}

	public TellrawPart setClickEvent(TellrawEvent<TellrawEvent.ClickAction> clickEvent) {
		this.clickEvent = clickEvent;
		return this;
	}
}
