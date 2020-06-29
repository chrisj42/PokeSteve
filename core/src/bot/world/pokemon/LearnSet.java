package bot.world.pokemon;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import bot.data.DataCore;
import bot.data.json.MissingPropertyException;
import bot.data.json.NodeParser;
import bot.data.json.node.JsonArrayNode;
import bot.data.json.node.JsonObjectNode;
import bot.world.pokemon.move.Move;
import bot.world.pokemon.move.MoveLearnMethod;

import com.fasterxml.jackson.databind.JsonNode;

import org.jetbrains.annotations.NotNull;

public class LearnSet {
	
	private static final TreeSet<LevelUpMove> EMPTY_SET = new TreeSet<>();
	
	private final HashSet<Move> allMoves;
	private final TreeSet<LevelUpMove> levelUpMoveSet;
	private final TreeMap<Integer, TreeSet<LevelUpMove>> splitMoveMap;
	
	// @SuppressWarnings("unchecked")
	public LearnSet(PokemonSpecies species, JsonArrayNode moves) throws MissingPropertyException {
		// final boolean print = species.dex == 381;
		
		// if(print) System.out.println("moves: "+moves.getLength());
		
		allMoves = new HashSet<>(moves.getLength());
		levelUpMoveSet = new TreeSet<>();
		splitMoveMap = new TreeMap<>();
		// this.allMoves = new Move[moves.getLength()];
		for(int i = 0; i < moves.getLength(); i++) {
			JsonObjectNode moveNode = moves.getObjectNode(i);
			final Move move = DataCore.MOVES.get(NodeParser.getResourceId(moveNode.getObjectNode("move")));
			if(move == null) {
				System.err.println("note: move "+i+" for pokemon "+species+" is null, skipping (resource name was "+moveNode.getObjectNode("move").parseValueNode("name", JsonNode::textValue)+")");
				continue;
			}
			allMoves.add(move);
			// this.moves[i] = DataCore.MOVES.getRef(moveNode.getObjectNode("move"));
			
			JsonArrayNode versionInfoList = moveNode.getArrayNode("version_group_details");
			for(int j = versionInfoList.getLength() - 1; j >= 0; j--) {
				JsonObjectNode latestVersionInfo = versionInfoList.getObjectNode(j);
				MoveLearnMethod learnMethod = MoveLearnMethod.getLearnMethod(latestVersionInfo.getObjectNode("move_learn_method").parseValueNode("name", JsonNode::textValue));
				if(learnMethod != MoveLearnMethod.LevelUp)
					continue;
				int level = latestVersionInfo.parseValueNode("level_learned_at", JsonNode::intValue);
				// if(print) System.out.println("move "+this.moves[i]+" learned at "+level+" with method "+learnMethod);
				LevelUpMove lMove = new LevelUpMove(level, move);
				levelUpMoveSet.add(lMove);
				splitMoveMap.computeIfAbsent(level, l -> new TreeSet<>()).add(lMove);
				break;
			}
		}
		
		// if(print)
		// 	System.out.println("pokemon "+species.name+" levelup map:"+levelUpMoveMap);
	}
	
	/*public TreeMap<Integer, TreeSet<LevelUpMove>> getMoveMap() {
		return splitMoveMap;
	}*/
	
	@NotNull
	public NavigableSet<LevelUpMove> getMovePool(int level) {
		Entry<Integer, TreeSet<LevelUpMove>> lastMoves = splitMoveMap.floorEntry(level);
		return lastMoves == null ? EMPTY_SET :
			levelUpMoveSet.headSet(lastMoves.getValue().last(), true);
	}
	
	// opposite of getMovePool, gets the as-yet-unlearned moves.
	@NotNull
	public NavigableSet<LevelUpMove> getFutureMoves(int level) {
		Entry<Integer, TreeSet<LevelUpMove>> lastMoves = splitMoveMap.floorEntry(level);
		return lastMoves == null ? EMPTY_SET :
			levelUpMoveSet.tailSet(lastMoves.getValue().last(), false);
	}
	
	public TreeSet<LevelUpMove> getNewMoves(int level) {
		return splitMoveMap.getOrDefault(level, EMPTY_SET);
	}
	
	public Move[] getDefaultMoveset(int level) {
		NavigableSet<LevelUpMove> movePool = getMovePool(level);
		LevelUpMove lastMove = movePool.last();
		// Move[] moveset = new Move[4];
		LinkedList<Move> moveset = new LinkedList<>();
		for(int i = 0; i < 4 && lastMove != null; i++, lastMove = movePool.lower(lastMove)) {
			moveset.add(lastMove.move);
		}
		return moveset.toArray(new Move[0]);
	}
	
	static class LevelUpMove implements Comparable<LevelUpMove> {
		final int level;
		final Move move;
		
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
		
		@Override
		public String toString() {
			return move+"-lv"+level;
		}
	}
}
