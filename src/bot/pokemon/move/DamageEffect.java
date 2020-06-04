package bot.pokemon.move;

import bot.io.json.node.JsonObjectNode;
import bot.pokemon.DamageType;

public class DamageEffect extends MoveEffect {
	
	private final DamageType damageType;
	private final int accuracy;
	private final int power;
	private final int minTimes;
	private final int maxTimes;
	
	public DamageEffect(JsonObjectNode node) {
		
	}
	
}
