package bot.pokemon.battle;

import bot.pokemon.Pokemon;
import bot.pokemon.PokemonSpecies;
import bot.pokemon.battle.BattleInstance.Player;

public class PlayerContext {
	
	public final Player userPlayer;
	public final BattlePokemon user;
	public final Pokemon userPokemon;
	public final PokemonSpecies userSpecies;
	public final Player enemyPlayer;
	public final BattlePokemon enemy;
	public final Pokemon enemyPokemon;
	public final PokemonSpecies enemySpecies;
	
	public final StringBuilder msg;
	
	public PlayerContext(Player user, Player enemy, StringBuilder msg) {
		this.msg = msg;
		
		userPlayer = user;
		this.user = user.pokemon;
		userPokemon = this.user.pokemon;
		userSpecies = userPokemon.species;
		
		enemyPlayer = enemy;
		this.enemy = enemy.pokemon;
		enemyPokemon = this.enemy.pokemon;
		enemySpecies = enemyPokemon.species;
	}
	
	public StringBuilder line(Object string) {
		return msg.append('\n').append(string);
	}
	
	public StringBuilder with(Player player) {
		return line(player).append(' ');
	}
	
	public StringBuilder withUser(Object string) {
		return with(userPlayer).append(string);
	}
	
	public StringBuilder withEnemy(Object string) {
		return with(enemyPlayer).append(string);
	}
}
