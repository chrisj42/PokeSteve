package bot.pokemon.battle;

import bot.pokemon.Pokemon;
import bot.pokemon.battle.BattleInstance.Player;

public class AiPlayer extends Player {
	
	public AiPlayer(Pokemon pokemon) {
		super("Wild "+pokemon.species, pokemon);
	}
}
