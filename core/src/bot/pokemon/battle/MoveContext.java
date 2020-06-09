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
	public final Move userMove;
	public final int userMoveIdx;
	public final String enemyName;
	public final BattlePokemon enemy;
	public final Pokemon enemyPokemon;
	public final PokemonSpecies enemySpecies;
	public final Move enemyMove;
	public final int enemyMoveIdx;
	
	public final boolean isFirst;
	public final StringBuilder msg;
	
	private boolean hadEffect = false;
	
	public MoveContext(Player user, Player enemy, boolean isFirst, StringBuilder msg) {
		userName = user.name;
		this.user = user.pokemon;
		userPokemon = this.user.pokemon;
		userSpecies = userPokemon.species;
		this.userMove = user.getMove();
		this.userMoveIdx = user.getMoveIdx();
		
		enemyName = enemy.name;
		this.enemy = enemy.pokemon;
		enemyPokemon = this.enemy.pokemon;
		enemySpecies = enemyPokemon.species;
		this.enemyMove = enemy.getMove();
		this.enemyMoveIdx = enemy.getMoveIdx();
		
		this.isFirst = isFirst;
		this.msg = msg;
	}
	
	public void setHadEffect() {
		hadEffect = true;
	}
	
	public boolean hadEffect() { return hadEffect; }
	
	public StringBuilder line(Object string) {
		return msg.append('\n').append(string);
	}
	
	public StringBuilder withEnemy(Object string) {
		return line(enemyName).append(' ').append(string);
	}
	
	public StringBuilder withUser(Object string) {
		return line(userName).append(' ').append(string);
	}
}
