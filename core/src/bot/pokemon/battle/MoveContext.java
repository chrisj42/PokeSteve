package bot.pokemon.battle;

import bot.pokemon.Move;
import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;
import bot.pokemon.battle.BattleInstance.Player;

public class MoveContext {
	
	public final String userName;
	public final BattlePokemon user;
	public final Pokemon userPokemon;
	public final PokemonSpecies userSpecies;
	public final String opponentName;
	public final BattlePokemon opponent;
	public final Pokemon opponentPokemon;
	public final PokemonSpecies opponentSpecies;
	public final Move move;
	
	public MoveContext(Player user, Player opponent, Move move) {
		this.move = move;
		userName = user.name;
		this.user = user.pokemon;
		userPokemon = this.user.pokemon;
		userSpecies = userPokemon.species;
		opponentName = opponent.name;
		this.opponent = opponent.pokemon;
		opponentPokemon = this.opponent.pokemon;
		opponentSpecies = opponentPokemon.species;
	}
}
