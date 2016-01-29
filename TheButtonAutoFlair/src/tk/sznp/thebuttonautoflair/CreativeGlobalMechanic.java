package tk.sznp.thebuttonautoflair;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.event.HandlerList;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;

public class CreativeGlobalMechanic extends GameMechanicBase {

	@Override
	public boolean checkCanStart(Minigame arg0, MinigamePlayer arg1) {
		return true;
	}

	@Override
	public MinigameModule displaySettings(Minigame arg0) {
		return null;
	}

	@Override
	public void endMinigame(Minigame arg0, List<MinigamePlayer> arg1,
			List<MinigamePlayer> arg2) {

	}

	@Override
	public String getMechanic() {
		return "creativeglobal";
	}

	@Override
	public void joinMinigame(Minigame mg, MinigamePlayer mp) {
		
	}

	@Override
	public void quitMinigame(Minigame mg, MinigamePlayer mp, boolean forced) {
		mg.getBlockRecorder().clearRestoreData();
	}

	@Override
	public void startMinigame(Minigame mg, MinigamePlayer mp) {

	}

	@Override
	public void stopMinigame(Minigame arg0, MinigamePlayer arg1) {
		
	}

	@Override
	public EnumSet<MinigameType> validTypes() {
		return EnumSet.of(MinigameType.MULTIPLAYER);
	}

}
