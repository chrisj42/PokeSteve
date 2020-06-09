package bot.pokemon;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.move.ApplyStatusEffect;
import bot.pokemon.move.DamageEffect;
import bot.pokemon.move.MoveDescription;
import bot.pokemon.move.MoveTarget;
import bot.pokemon.move.StatEffect;

import com.fasterxml.jackson.databind.JsonNode;

public class Move {
	
	public final String name;
	public final int id;
	public final MoveDescription description;
	public final Type type;
	public final int pp;
	public final int accuracy;
	public final int priority;
	public final MoveTarget target;
	
	// private final MoveEffect[] effects;
	public final DamageEffect damage;
	public final StatEffect stat;
	public final ApplyStatusEffect status;
	public final int effectChance; // after hit, chance of non-damage effects
	public final int minTurns;
	public final int maxTurns;
	public final int drain; // positive is drain, negative is recoil
	public final int healing;
	public final int flinchChance;
	
	public Move(JsonObjectNode node) throws MissingPropertyException {
		final JsonObjectNode meta = node.getObjectNode("meta");
		name = node.parseValueNode("name", JsonNode::textValue);
		id = node.parseValueNode("id", JsonNode::intValue);
		description = new MoveDescription(node);
		type = Type.values[NodeParser.getResourceId(node.getObjectNode("type"))-1];
		pp = node.parseValueNode("pp", JsonNode::intValue);
		accuracy = node.parseValueNode("accuracy", JsonNode::intValue);
		// effects = MoveEffect.parseEffects(node);
		priority = node.parseValueNode("priority", JsonNode::intValue);
		target = MoveTarget.getTarget(node.getObjectNode("target").parseValueNode("name", JsonNode::textValue));
		effectChance = node.parseValueNode("effect_chance", JsonNode::intValue);
		
		damage = new DamageEffect(this, node, meta);
		stat = new StatEffect(this, node, meta);
		status = new ApplyStatusEffect(this, node, meta);
		
		minTurns = meta.parseValueNode("min_turns", JsonNode::intValue);
		maxTurns = meta.parseValueNode("max_turns", JsonNode::intValue);
		drain = meta.parseValueNode("drain", JsonNode::intValue);
		healing = meta.parseValueNode("healing", JsonNode::intValue);
		flinchChance = meta.parseValueNode("flinch_chance", JsonNode::intValue);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public Type getType() {
		return type;
	}
}
