package bot.world.pokemon;

import java.text.DecimalFormat;
import java.util.EnumMap;

import bot.data.DataCore;
import bot.data.json.MissingPropertyException;
import bot.data.json.NodeParser;
import bot.data.json.node.JsonArrayNode;
import bot.data.json.node.JsonObjectNode;
import bot.util.Ref;
import bot.util.Utils;
import bot.world.pokemon.EvolutionChain.LevelUpEvolution;

import discord4j.core.spec.EmbedCreateSpec;

import com.fasterxml.jackson.databind.JsonNode;

public class PokemonSpecies {
	
	public final String name;
	public final int dex;
	private final String flavorText; // pokedex entry
	private final String genus; // essentially more flavor text
	public final String frontSpriteUrl, backSpriteUrl;
	
	public final Type primaryType, secondaryType;
	private final EnumMap<Stat, Integer> baseStats;
	public final LearnSet learnableMoves;
	
	// private final Ref<PokemonSpecies> evolvesFrom;
	private final Ref<EvolutionChain> evoChain;
	// private final EggGroup[] eggGroups;
	final GrowthRate growthRate;
	
	// public final Habitat habitat;
	public final int catchRate;
	public final int femaleRate; // in eighths
	public final int baseDefeatExp;
	private final EnumMap<Stat, Integer> baseDefeatEVs;
	
	// pass it species and pokemon
	public PokemonSpecies(JsonObjectNode snode, JsonObjectNode pnode) throws MissingPropertyException {
		name = Utils.capitalizeFirst(snode.parseValueNode("name", JsonNode::textValue));
		// System.out.println("loading pokemon "+name);
		dex = snode.parseValueNode("id", JsonNode::intValue);
		flavorText = NodeParser.getEnglishNode(snode.getArrayNode("flavor_text_entries"), false).parseValueNode("flavor_text", JsonNode::textValue).replaceAll("[\n\r]", " ");
		genus = NodeParser.getEnglishNode(snode.getArrayNode("genera"), true).parseValueNode("genus", JsonNode::textValue);
		evoChain = DataCore.EVO_CHAINS.getRef(NodeParser.getResourceId(snode.getObjectNode("evolution_chain")));
		
		JsonObjectNode spriteNode = pnode.getObjectNode("sprites");
		frontSpriteUrl = spriteNode.parseValueNode("front_default", JsonNode::textValue);
		backSpriteUrl = spriteNode.parseValueNode("back_default", JsonNode::textValue);
		
		JsonArrayNode typeArray = pnode.getArrayNode("types");
		primaryType = NodeParser.getEnumResource(Type.class, typeArray.getObjectNode(0).getObjectNode("type"));
		if(typeArray.getLength() < 2)
			secondaryType = null;
		else
			secondaryType = NodeParser.getEnumResource(Type.class, typeArray.getObjectNode(1).getObjectNode("type"));
		
		learnableMoves = new LearnSet(this, pnode.getArrayNode("moves"));
		
		baseStats = new EnumMap<>(Stat.class);
		baseDefeatEVs = new EnumMap<>(Stat.class);
		JsonArrayNode statArray = pnode.getArrayNode("stats");
		for(int i = 0; i < statArray.getLength(); i++) {
			JsonObjectNode statNode = statArray.getObjectNode(i);
			Stat stat = NodeParser.getEnumResource(Stat.class, statNode.getObjectNode("stat"));
			baseStats.put(stat, statNode.parseValueNode("base_stat", JsonNode::intValue));
			baseDefeatEVs.put(stat, statNode.parseValueNode("effort", JsonNode::intValue));
		}
		
		catchRate = snode.parseValueNode("capture_rate", JsonNode::intValue);
		femaleRate = snode.parseValueNode("gender_rate", JsonNode::intValue);
		// evolvesFrom = DataCore.POKEMON.getRef(snode.getObjectNode("evolves_from_species"));
		// NOTE: turtwig has no habitat entry in database
		// habitat = NodeParser.getEnumResource(Habitat.class, true, snode.getObjectNode("habitat"));
		growthRate = NodeParser.getEnumResource(GrowthRate.class, snode.getObjectNode("growth_rate"));
		
		baseDefeatExp = pnode.parseValueNode("base_experience", JsonNode::intValue);
	}
	
	public int getBaseStat(Stat stat) {
		return baseStats.get(stat);
	}
	
	public void addDefeatEV(StatData data) {
		int ev = baseDefeatEVs.getOrDefault(data.getStatType(), 0);
		if(ev != 0)
			data.addEV(ev);
	}
	
	public LevelUpEvolution getEvolution() {
		return evoChain.resolve().getEvoFor(dex);
	}
	
	public Pokemon spawnPokemon(int level) {
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
		
		return new Pokemon(this, level, Utils.pickRandom(Nature.values), gender);
	}
	
	public EmbedCreateSpec.Builder buildDexEntry() {
		var e = EmbedCreateSpec.builder();
		final String title = "#"+dex+" - "+name;
		e.title(title).footer(title, null);
		e.addField("Typing", primaryType+(secondaryType == null ? "" : " | "+secondaryType), false);
		e.addField(genus, flavorText, false);
		e.addField("Base Stats", ""
				+"Health      - "+getBaseStat(Stat.Health)
				+"\nAttack      - "+getBaseStat(Stat.Attack)
				+"\nDefense     - "+getBaseStat(Stat.Defense)
				+"\nSp. Attack  - "+getBaseStat(Stat.SpAttack)
				+"\nSp. Defense - "+getBaseStat(Stat.SpDefense)
				+"\nSpeed       - "+getBaseStat(Stat.Speed)
			, true);
		e.addField("EV Bonuses", ""
				+"Health       +"+baseDefeatEVs.get(Stat.Health)
				+"\nAttack       +"+baseDefeatEVs.get(Stat.Attack)
				+"\nDefense      +"+baseDefeatEVs.get(Stat.Defense)
				+"\nSp. Attack   +"+baseDefeatEVs.get(Stat.SpAttack)
				+"\nSp. Defense  +"+baseDefeatEVs.get(Stat.SpDefense)
				+"\nSpeed        +"+baseDefeatEVs.get(Stat.Speed)
			, true);
		e.image(getSpritePath());
		return e;
	}
	
	private static final DecimalFormat spriteDexFormat = new DecimalFormat("000");
	public String getSpritePath() {
		return "https://raw.githubusercontent.com/chrisj42/PokeSteve/master/resources/sprites/"+ spriteDexFormat.format(dex)+".png";
	}
	
	@Override
	public String toString() { return name; }
}
