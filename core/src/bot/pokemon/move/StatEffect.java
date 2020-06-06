package bot.pokemon.move;

import java.util.EnumMap;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonArrayNode;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.Move;
import bot.pokemon.Stat;
import bot.pokemon.battle.BattlePokemon;
import bot.pokemon.battle.MoveContext;
import bot.util.Utils;

import com.fasterxml.jackson.databind.JsonNode;

public class StatEffect extends MoveEffect {
	
	private final Move move;
	public final EnumMap<Stat, Integer> statStageChanges = new EnumMap<>(Stat.class);
	public final int chance;
	
	public StatEffect(Move move, JsonObjectNode node, JsonObjectNode meta) throws MissingPropertyException {
		this.move = move;
		chance = meta.parseValueNode("stat_chance", JsonNode::intValue);
		if(chance != 0 && move.effectChance != chance)
			System.err.println("move "+move+" has inconsistent effect chance and stat change chance.");
		
		JsonArrayNode statChanges = node.getArrayNode("stat_changes");
		for(int i = 0; i < statChanges.getLength(); i++) {
			JsonObjectNode change = statChanges.getObjectNode(i);
			Stat stat = Stat.values[NodeParser.getResourceId(change.getObjectNode("stat")) - 1];
			int amount = change.parseValueNode("change", JsonNode::intValue);
			statStageChanges.put(stat, amount);
		}
	}
	
	// returns whether a stat change occurred
	public boolean doStatEffect(MoveContext context, StringBuilder msg) {
		if(statStageChanges.size() == 0)
			return false;
		if(chance > 0 && Utils.randInt(0, 99) >= chance)
			return false;
		
		boolean self = context.move.target != MoveTarget.Enemy;
		boolean enemy = context.move.target != MoveTarget.Self;
		statStageChanges.forEach((stat, amt) -> {
			if(self) {
				boolean change = context.user.alterStatStage(stat, amt);
				String message = getStatChangeMessage(amt, change);
				if(message != null)
					msg.append("\n").append(context.userName).append("'s ").append(stat).append(message);
			}
			if(enemy) {
				boolean change = context.opponent.alterStatStage(stat, amt);
				String message = getStatChangeMessage(amt, change);
				if(message != null)
					msg.append("\n").append(context.opponentName).append("'s ").append(stat).append(message);
			}
		});
		return true;
	}
	
	private static String getStatChangeMessage(int amt, boolean change) {
		if(!change && amt < 0) return " can't go any lower!";
		if(!change && amt > 0) return " can't go any higher!";
		if(amt > 2) return " rose drastically!";
		if(amt == 2) return " sharply rose!";
		if(amt == 1) return " rose!";
		if(amt == -1) return " fell!";
		if(amt == -2) return " harshly fell!";
		if(amt < -2) return " severely fell!";
		return null;
	}
}
