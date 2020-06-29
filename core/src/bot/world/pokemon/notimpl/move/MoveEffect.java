package bot.world.pokemon.notimpl.move;

public class MoveEffect {
	
	// an effect of a move. can be damage, status, or a number of things.
	
	/*public static MoveEffect[] parseEffects(JsonObjectNode moveNode) throws MissingPropertyException {
		JsonObjectNode metaNode = moveNode.getObjectNode("meta");
		
		LinkedList<MoveEffect> effects = new LinkedList<>();
		int power = moveNode.parseValueNode("power", JsonNode::intValue);
		if(power > 0)
			effects.add(new DamageEffect(moveNode, metaNode));
		
		return effects.toArray(new MoveEffect[0]);
	}*/
}
