package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.lib.chat.TellrawSerializableEnum;

import java.io.Serializable;

public final class TellrawEvent<T extends TellrawEvent.Action> implements Serializable {
	private static final long serialVersionUID = -1681364161210561505L;
	private transient boolean hoverEvent;
	private T action;
	private Object value;

	private TellrawEvent(T action, String value) {
		this.hoverEvent = action instanceof HoverAction;
		this.action = action;
		this.value = value;
	}

	private TellrawEvent(T action, TellrawPart value) {
		this.hoverEvent = action instanceof HoverAction;
		this.action = action;
		this.value = value;
	}

	public static <V extends TellrawEvent.Action> TellrawEvent<V> create(V action, String value) {
		return new TellrawEvent<>(action, value);
	}

	public static <V extends TellrawEvent.Action> TellrawEvent<V> create(V action, TellrawPart value) {
		return new TellrawEvent<>(action, value);
	}

	public boolean isHoverEvent() {
		return hoverEvent;
	}

	public T getAction() {
		return action;
	}

	public Object getValue() {
		return value;
	}

	public enum ClickAction implements Action {
		OPEN_URL("open_url"), RUN_COMMAND("run_command"), SUGGEST_COMMAND("suggest_command");
		private String action;

		ClickAction(String action) {
			this.action = action;
		}

		@Override
		public String getName() {
			return action;
		}
	}

	public enum HoverAction implements Action {
		SHOW_TEXT("show_text"), SHOW_ITEM("show_item"), SHOW_ACHIEVEMENT("show_achievement"), SHOW_ENTITY(
				"show_entity");
		private String action;

		HoverAction(String action) {
			this.action = action;
		}

		@Override
		public String getName() {
			return action;
		}
	}

	public static interface Action extends TellrawSerializableEnum {
	}
}
