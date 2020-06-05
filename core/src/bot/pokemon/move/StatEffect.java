package bot.pokemon.move;

import java.util.EnumMap;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonArrayNode;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.Move;
import bot.pokemon.StageShiftStat;
import bot.pokemon.Stat;

import com.fasterxml.jackson.databind.JsonNode;

public class StatEffect extends MoveEffect {
	
	private final Move move;
	private final EnumMap<StageShiftStat, Integer> statStageChanges = new EnumMap<>(StageShiftStat.class);
	private final int chance;
	
	public StatEffect(Move move, JsonObjectNode node, JsonObjectNode meta) throws MissingPropertyException {
		this.move = move;
		chance = meta.parseValueNode("stat_chance", JsonNode::intValue);
		if(chance != 0 && move.effectChance != chance)
			System.err.println("move "+move.name+" has inconsistent effect chance and stat change chance.");
		
		JsonArrayNode statChanges = node.getArrayNode("stat_changes");
		for(int i = 0; i < statChanges.getLength(); i++) {
			JsonObjectNode change = statChanges.getObjectNode(i);
			StageShiftStat stat = StageShiftStat.values[NodeParser.getResourceId(change.getObjectNode("stat")) - 2]; // -2 is to account for HP, the first one
			int amount = change.parseValueNode("change", JsonNode::intValue);
			statStageChanges.put(stat, amount);
		}
	}
	
}
