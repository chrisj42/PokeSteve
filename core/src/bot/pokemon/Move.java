package bot.pokemon;

import bot.io.json.node.JsonObjectNode;
import bot.pokemon.move.MoveEffect;

public class Move {
	
	private String name;
	private String description;
	private Type type;
	private int pp;
	// meta-info / flags
	private MoveEffect[] effects;
	private int priority;
	
	
	public Move(JsonObjectNode node) {
		
	}
	
}
