package bot.world.pokemon;

import java.util.HashMap;

import bot.data.json.MissingPropertyException;
import bot.data.json.NodeParser;
import bot.data.json.node.JsonObjectNode;

import com.fasterxml.jackson.databind.JsonNode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvolutionChain {
	
	public final int id;
	
	// maps species dex number to the evolution object that specifies what level is needed and what the resulting species is
	private HashMap<Integer, LevelUpEvolution> levelUpEvolutions = new HashMap<>(4);
	
	public static class LevelUpEvolution {
		public final int minLevel;
		public final int nextSpeciesDex;
		
		private LevelUpEvolution(int minLevel, int nextSpeciesDex) {
			this.minLevel = minLevel;
			this.nextSpeciesDex = nextSpeciesDex;
		}
	}
	
	public EvolutionChain(JsonObjectNode node) throws MissingPropertyException {
		id = node.parseValueNode("id", JsonNode::intValue);
		EvoChainLink base = new EvoChainLink(node.getObjectNode("chain"));
		tryAdd(null, base);
	}
	
	private void tryAdd(@Nullable EvoChainLink prevFormLink, @NotNull EvoChainLink nextFormLink) {
		if(prevFormLink != null) {
			for(EvoRequirements req: nextFormLink.prevFormReqs) {
				if(req.trigger == EvolutionTrigger.LevelUp && req.minLevel > 0) {
					levelUpEvolutions.put(prevFormLink.speciesDex, new LevelUpEvolution(req.minLevel, nextFormLink.speciesDex));
				}
			}
		}
		for(EvoChainLink furtherLink: nextFormLink.furtherEvolutions)
			tryAdd(nextFormLink, furtherLink);
	}
	
	public LevelUpEvolution getEvoFor(PokemonSpecies species) { return getEvoFor(species.dex); }
	public LevelUpEvolution getEvoFor(int dex) {
		return levelUpEvolutions.get(dex);
	}
	
	private static class EvoChainLink {
		
		private final int speciesDex;
		private final EvoRequirements[] prevFormReqs;
		private final EvoChainLink[] furtherEvolutions;
		
		public EvoChainLink(JsonObjectNode node) throws MissingPropertyException {
			speciesDex = NodeParser.getResourceId(node.getObjectNode("species"));
			
			// JsonArrayNode dnode = node.getArrayNode("evolution_details");
			// prevFormReqs = new EvoRequirements[dnode.getLength()];
			// for(int i = 0; i < prevFormReqs.length; i++)
			// 	prevFormReqs[i] = new EvoRequirements(dnode.getObjectNode(i));
			// System.out.println("evo reqs for dex "+speciesDex);
			prevFormReqs = NodeParser.parseObjectArray(node.getArrayNode("evolution_details"), EvoRequirements.class);
			// if(prevFormReqs.length > 1)
			// 	System.out.println("chain link for species "+speciesDex+" has "+prevFormReqs.length+" form requirement arrays.");
			
			// JsonArrayNode lnode = node.getArrayNode("evolves_to");
			// furtherEvolutions = new EvoChainLink[lnode.getLength()];
			// for(int i = 0; i < furtherEvolutions.length; i++)
			// 	furtherEvolutions[i] = new EvoChainLink(lnode.getObjectNode(i));
			// System.out.println("further evos for dex "+speciesDex);
			furtherEvolutions = NodeParser.parseObjectArray(node.getArrayNode("evolves_to"), EvoChainLink.class);
		}
		
		// public 
	}
	
	private static class EvoRequirements {
		
		public final int minLevel;
		public final EvolutionTrigger trigger;
		
		public EvoRequirements(JsonObjectNode node) throws MissingPropertyException {
			minLevel = node.parseValueNode("min_level", JsonNode::intValue);
			trigger = NodeParser.parseEnumResource(node.getObjectNode("trigger"), EvolutionTrigger.class);
		}
		
	}
}
