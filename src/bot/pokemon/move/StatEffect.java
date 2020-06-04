package bot.pokemon.move;

import java.util.EnumMap;

import bot.io.json.node.JsonObjectNode;
import bot.pokemon.Stat;

public class StatEffect extends MoveEffect {
	
	private final EnumMap<Stat, Integer> statStageChanges;
	
	public StatEffect(JsonObjectNode node) {
		
	}
	
}
