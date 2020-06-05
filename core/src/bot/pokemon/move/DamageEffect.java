package bot.pokemon.move;

import bot.io.json.node.JsonObjectNode;
import bot.pokemon.DamageType;

public class DamageEffect extends MoveEffect {
	
	private DamageType damageType;
	private int accuracy;
	private int power;
	private int minTimes;
	private int maxTimes;
	
	public DamageEffect(JsonObjectNode node) {
		
	}
	
}
