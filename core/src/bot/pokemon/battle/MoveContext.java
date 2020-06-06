package bot.pokemon.battle;

import bot.pokemon.Move;
import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;

public class MoveContext {
	
	// public final String userName;
	public final BattlePokemon user;
	public final Pokemon userPokemon;
	public final PokemonSpecies userSpecies;
	// public final String opponentName;
	public final BattlePokemon opponent;
	public final Pokemon opponentPokemon;
	public final PokemonSpecies opponentSpecies;
	public final Move move;
	
	public MoveContext(BattlePokemon user, BattlePokemon opponent, Move move) {
		this.move = move;
		this.user = user;
		userPokemon = user.pokemon;
		userSpecies = userPokemon.species;
		this.opponent = opponent;
		opponentPokemon = opponent.pokemon;
		opponentSpecies = opponentPokemon.species;
	}
}
