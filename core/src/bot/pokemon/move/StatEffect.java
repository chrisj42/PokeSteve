package bot.pokemon.move;

import java.util.EnumMap;

import bot.io.json.node.JsonObjectNode;
import bot.pokemon.Stat;

public class StatEffect extends MoveEffect {
	
	private final EnumMap<Stat, Integer> statStageChanges = new EnumMap<>(Stat.class);
	
	public StatEffect(JsonObjectNode node) {
		
	}
	
}
