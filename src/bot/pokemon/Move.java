package bot.pokemon;

import bot.io.json.node.JsonObjectNode;
import bot.pokemon.move.MoveEffect;

public class Move {
	
	private final String name;
	private final String description;
	private final Type type;
	private final int pp;
	// meta-info / flags
	private final MoveEffect[] effects;
	private final int priority;
	
	
	public Move(JsonObjectNode node) {
		
	}
	
}
