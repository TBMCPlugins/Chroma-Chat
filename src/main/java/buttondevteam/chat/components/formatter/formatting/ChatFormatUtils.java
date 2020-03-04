package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.chat.commands.ucmds.admin.DebugCommand;

import java.util.ArrayList;
import java.util.Arrays;

public final class ChatFormatUtils {
	private ChatFormatUtils() {}

	static void sendMessageWithPointer(String str, int... pointer) {
		DebugCommand.SendDebugMessage(str);
		StringBuilder sb = new StringBuilder(str.length());
		Arrays.sort(pointer);
		for (int i = 0; i < pointer.length; i++) {
			for (int j = 0; j < pointer[i] - (i > 0 ? pointer[i - 1] + 1 : 0); j++)
				sb.append(' ');
			if (pointer[i] == (i > 0 ? pointer[i - 1] : -1))
				continue;
			sb.append('^');
		}
		DebugCommand.SendDebugMessage(sb.toString());
	}

	/**
	 * Check if the given start and end position is inside any of the ranges
	 */
	static boolean isInRange(int start, int end, ArrayList<int[]> ranges) {
		return ranges.stream().anyMatch(range -> range[1] >= start && range[0] <= end);
	}
}
