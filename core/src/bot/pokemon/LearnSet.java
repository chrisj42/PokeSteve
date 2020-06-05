package bot.pokemon;

import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonArrayNode;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.move.MoveLearnMethod;

import com.fasterxml.jackson.databind.JsonNode;

import org.jetbrains.annotations.NotNull;

public class LearnSet {
	
	private final Move[] moves;
	private final TreeSet<LevelUpMove> levelUpMoveSet;
	private final TreeMap<Integer, TreeSet<LevelUpMove>> levelUpMoveMap;
	
	// @SuppressWarnings("unchecked")
	public LearnSet(PokemonSpecies species, JsonArrayNode moves) throws MissingPropertyException {
		levelUpMoveSet = new TreeSet<>();
		levelUpMoveMap = new TreeMap<>();
		this.moves = new Move[moves.getLength()];
		for(int i = 0; i < this.moves.length; i++) {
			JsonObjectNode moveNode = moves.getObjectNode(i);
			this.moves[i] = DataCore.MOVES.get(NodeParser.getResourceId(moveNode.getObjectNode("move")));
			// this.moves[i] = DataCore.MOVES.getRef(moveNode.getObjectNode("move"));
			
			JsonArrayNode versionInfoList = moveNode.getArrayNode("version_group_details");
			JsonObjectNode latestVersionInfo = versionInfoList.getObjectNode(versionInfoList.getLength()-1);
			MoveLearnMethod learnMethod = MoveLearnMethod.getLearnMethod(latestVersionInfo.parseValueNode("move_learn_method", JsonNode::textValue));
			int level = latestVersionInfo.parseValueNode("level_learned_at", JsonNode::intValue);
			if(learnMethod == MoveLearnMethod.LevelUp) {
				LevelUpMove lMove = new LevelUpMove(level, this.moves[i]);
				levelUpMoveSet.add(lMove);
				levelUpMoveMap.computeIfAbsent(level, lvl -> new TreeSet<>()).add(lMove);
			} else if(level > 0)
				System.err.println("move "+this.moves[i].name+" for pokemon "+species.name+" (#"+(i+1)+" in move list) is not a level up move, but has a non-zero level learned at.");
		}
	}
	
	public Move[] getDefaultMoveset(int level) {
		LevelUpMove lastMove = levelUpMoveMap.headMap(level, true).lastEntry().getValue().last();
		NavigableSet<LevelUpMove> movePool = levelUpMoveSet.headSet(lastMove, true);
		Move[] moveset = new Move[4];
		for(int i = 0; i < 4 && lastMove != null; i++, lastMove = movePool.lower(lastMove)) {
			moveset[i] = lastMove.move;
		}
		return moveset;
	}
	
	private static class LevelUpMove implements Comparable<LevelUpMove> {
		private final int level;
		private final Move move;
		
		private LevelUpMove(int level, Move move) {
			this.level = level;
			this.move = move;
		}
		
		@Override
		public int compareTo(@NotNull LevelUpMove o) {
			int levelComp = Integer.compare(level, o.level);
			if(levelComp == 0)
				return Integer.compare(move.id, o.move.id);
			return levelComp;
		}
	}
}
