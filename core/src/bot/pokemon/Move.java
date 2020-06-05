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
	private final MoveDescription description;
	private final Type type;
	private final int pp;
	private final int accuracy;
	private final int priority;
	private final MoveTarget target;
	
	// private final MoveEffect[] effects;
	private final DamageEffect damage;
	private final StatEffect stat;
	private final ApplyStatusEffect status;
	public final int effectChance; // after hit, chance of non-damage effects
	private final int minTurns;
	private final int maxTurns;
	private final int drain; // positive is drain, negative is recoil
	private final int healing;
	private final int flinchChance;
	
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
		
		damage = new DamageEffect(this, node, meta);
		stat = new StatEffect(this, node, meta);
		status = new ApplyStatusEffect(this, node, meta);
		
		effectChance = node.parseValueNode("effect_chance", JsonNode::intValue);
		minTurns = meta.parseValueNode("min_turns", JsonNode::intValue);
		maxTurns = meta.parseValueNode("max_turns", JsonNode::intValue);
		drain = meta.parseValueNode("drain", JsonNode::intValue);
		healing = meta.parseValueNode("healing", JsonNode::intValue);
		flinchChance = meta.parseValueNode("flinch_chance", JsonNode::intValue);
	}
}
