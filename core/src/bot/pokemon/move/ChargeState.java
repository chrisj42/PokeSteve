package bot.pokemon.move;

import java.util.Arrays;
import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

public enum ChargeState {
	
	// each state has a different capacity to get affected by weather and other moves.
	// sometimes moves are negated, sometimes they double in power.
	
	Normal(true, "is charging up!", (Moves[]) null),
	Sky(true, "flew up high!", Moves.Gust, Moves.Hurricane, Moves.Sky_Uppercut, Moves.Smack_Down, Moves.Thousand_Arrows, Moves.Thunder, Moves.Twister),
	Underground(false, "dug a hole!", Moves.Earthquake, Moves.Magnitude),
	Underwater(false, "dived underwater!", Moves.Surf, Moves.Whirlpool),
	Invisible(true, "disappeared!");
	
	public final boolean affectedByWeather;
	public final String prepMessage;
	private final EnumSet<Moves> affectingMoves;
	
	ChargeState(boolean affectedByWeather, String prepMessage, @Nullable Moves... affectingMoves) {
		this.affectedByWeather = affectedByWeather;
		this.prepMessage = prepMessage;
		if(affectingMoves == null)
			this.affectingMoves = null;
		else if(affectingMoves.length == 0)
			this.affectingMoves = EnumSet.noneOf(Moves.class);
		else
			this.affectingMoves = EnumSet.copyOf(Arrays.asList(affectingMoves));
	}
	
	public boolean affectedBy(Move move) {
		if(move == null) return false;
		if(affectingMoves == null) return true;
		Moves enumVal = Moves.values[move.id-1];
		return affectingMoves.contains(enumVal);
	}
}
