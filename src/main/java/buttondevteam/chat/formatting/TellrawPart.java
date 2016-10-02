package buttondevteam.chat.formatting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class TellrawPart implements Serializable {
	private static final long serialVersionUID = 4125357644462144024L;
	private ChatFormatter.Color color;
	private transient int format;
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

	public ChatFormatter.Color getColor() {
		return color;
	}

	public TellrawPart setColor(ChatFormatter.Color color) {
		this.color = color;
		return this;
	}

	public int getFormat() {
		return format;
	}

	public TellrawPart setFormat(int format) {
		this.format = format;
		this.italic = false;
		this.bold = false;
		this.underlined = false;
		this.strikethrough = false;
		this.obfuscated = false;
		if ((format & ChatFormatter.Format.Italic.getFlag()) != 0)
			this.italic = true;
		else if ((format & ChatFormatter.Format.Bold.getFlag()) != 0)
			this.bold = true;
		else if ((format & ChatFormatter.Format.Underlined.getFlag()) != 0)
			this.underlined = true;
		else if ((format & ChatFormatter.Format.Strikethrough.getFlag()) != 0)
			this.strikethrough = true;
		else if ((format & ChatFormatter.Format.Obfuscated.getFlag()) != 0)
			this.obfuscated = true;
		else
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
