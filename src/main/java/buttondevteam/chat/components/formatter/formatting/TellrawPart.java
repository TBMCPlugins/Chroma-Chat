package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.lib.chat.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class TellrawPart implements Serializable {
	private static final long serialVersionUID = 4125357644462144024L;
	private Color color;
	private boolean italic;
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

	public Color getColor() {
		return color;
	}

	public TellrawPart setColor(Color color) {
		this.color = color;
		return this;
	}

	public boolean isItalic() {
		return italic;
	}

	public TellrawPart setItalic(boolean italic) {
		this.italic = italic;
		return this;
	}

	public boolean isBold() {
		return bold;
	}

	public TellrawPart setBold(boolean bold) {
		this.bold = bold;
		return this;
	}

	public boolean isUnderlined() {
		return underlined;
	}

	public TellrawPart setUnderlined(boolean underlined) {
		this.underlined = underlined;
		return this;
	}

	public boolean isStrikethrough() {
		return strikethrough;
	}

	public TellrawPart setStrikethrough(boolean strikethrough) {
		this.strikethrough = strikethrough;
		return this;
	}

	public boolean isObfuscated() {
		return obfuscated;
	}

	public TellrawPart setObfuscated(boolean obfuscated) {
		this.obfuscated = obfuscated;
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
