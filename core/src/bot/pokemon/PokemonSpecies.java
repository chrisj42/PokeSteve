package bot.pokemon;

import java.util.EnumMap;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonArrayNode;
import bot.io.json.node.JsonObjectNode;
import bot.util.Ref;
import bot.util.Utils;

import com.fasterxml.jackson.databind.JsonNode;

public class PokemonSpecies {
	
	public final String name;
	public final int dex;
	private final String flavorText; // pokedex entry
	private final String genus; // essentially more flavor text
	private final String frontSpriteUrl, backSpriteUrl;
	
	private final Type primaryType, secondaryType;
	final EnumMap<Stat, Integer> baseStats;
	private final LearnSet learnableMoves;
	
	private final Ref<PokemonSpecies> evolvesFrom;
	// private final Ref<EvolutionChain> evoChain;
	// private final EggGroup[] eggGroups;
	final GrowthRate growthRate;
	
	private final Habitat habitat;
	private final int catchRate;
	private final int femaleRate; // in eighths
	private final int baseDefeatXp;
	private final EnumMap<Stat, Integer> baseDefeatEVs;
	
	// pass it species and pokemon
	public PokemonSpecies(JsonObjectNode snode, JsonObjectNode pnode) throws MissingPropertyException {
		name = snode.parseValueNode("name", JsonNode::textValue);
		dex = snode.parseValueNode("id", JsonNode::intValue);
		flavorText = NodeParser.getEnglishNode(snode.getArrayNode("flavor_text_entries"), false).parseValueNode("flavor_text", JsonNode::textValue);
		genus = NodeParser.getEnglishNode(snode.getArrayNode("genera"), true).parseValueNode("genus", JsonNode::textValue);
		
		JsonObjectNode spriteNode = pnode.getObjectNode("sprites");
		frontSpriteUrl = spriteNode.parseValueNode("front_default", JsonNode::textValue);
		backSpriteUrl = spriteNode.parseValueNode("back_default", JsonNode::textValue);
		
		JsonArrayNode typeArray = pnode.getArrayNode("types");
		primaryType = Type.values[NodeParser.getResourceId(typeArray.getObjectNode(0).getObjectNode("type"))-1];
		if(typeArray.getLength() < 2)
			secondaryType = null;
		else
			secondaryType = Type.values[NodeParser.getResourceId(typeArray.getObjectNode(1).getObjectNode("type"))-1];
		
		learnableMoves = new LearnSet(this, pnode.getArrayNode("moves"));
		
		baseStats = new EnumMap<>(Stat.class);
		baseDefeatEVs = new EnumMap<>(Stat.class);
		JsonArrayNode statArray = pnode.getArrayNode("stats");
		for(int i = 0; i < statArray.getLength(); i++) {
			JsonObjectNode statNode = statArray.getObjectNode(i);
			Stat stat = Stat.values[NodeParser.getResourceId(statNode.getObjectNode("stat"))-1];
			baseStats.put(stat, statNode.parseValueNode("base_stat", JsonNode::intValue));
			baseDefeatEVs.put(stat, statNode.parseValueNode("effort", JsonNode::intValue));
		}
		
		catchRate = snode.parseValueNode("capture_rate", JsonNode::intValue);
		femaleRate = snode.parseValueNode("gender_rate", JsonNode::intValue);
		evolvesFrom = DataCore.POKEMON.getRef(snode.getObjectNode("evolves_from_species"));
		habitat = Habitat.values[NodeParser.getResourceId(snode.getObjectNode("habitat"))-1];
		growthRate = GrowthRate.values[NodeParser.getResourceId(snode.getObjectNode("growth_rate"))-1];
		
		baseDefeatXp = pnode.parseValueNode("base_experience", JsonNode::intValue);
	}
	
	public Pokemon spawnPokemon() {
		Gender gender;
		if(femaleRate < 0)
			gender = Gender.Ungendered;
		else {
			float thresh = femaleRate / 8f;
			if(Math.random() < thresh)
				gender = Gender.Female;
			else
				gender = Gender.Male;
		}
		
		return new Pokemon(this, 20, Utils.pickRandom(Nature.values), gender, baseStats, learnableMoves.getDefaultMoveset(20));
	}
}
