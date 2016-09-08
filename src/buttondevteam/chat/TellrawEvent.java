package buttondevteam.thebuttonmcchat;

import java.io.Serializable;

public final class TellrawEvent<T extends TellrawEvent.Action> implements Serializable {
	private static final long serialVersionUID = -1681364161210561505L;
	private boolean hoverEvent;
	private T action;
	private String value;
	private TellrawPart valueobj; // TODO: FIx serializer, actually add own serializer, etc.

	private TellrawEvent(Class<T> cl, T action, String value) {
		this.hoverEvent = HoverAction.class.equals(cl);
		this.action = action;
		this.value = value;
	}

	private TellrawEvent(Class<T> cl, T action, TellrawPart value) {
		this.hoverEvent = HoverAction.class.equals(cl);
		this.action = action;
		this.valueobj = value;
	}

	public static final Class<HoverAction> HoverAC = HoverAction.class;
	public static final Class<ClickAction> ClickAC = ClickAction.class;

	public static <V extends TellrawEvent.Action> TellrawEvent<V> create(Class<V> cl, V action, String value) {
		return new TellrawEvent<>(cl, action, value);
	}

	public static <V extends TellrawEvent.Action> TellrawEvent<V> create(Class<V> cl, V action, TellrawPart value) {
		return new TellrawEvent<>(cl, action, value);
	}

	public boolean isHoverEvent() {
		return hoverEvent;
	}

	public T getAction() {
		return action;
	}

	public String getValue() {
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
