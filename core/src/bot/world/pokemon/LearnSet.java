package bot.world.pokemon;

import java.util.*;
import java.util.Map.Entry;

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
	
	private final Move[] moves;
	private final TreeSet<LevelUpMove> levelUpMoveSet;
	private final TreeMap<Integer, TreeSet<LevelUpMove>> levelUpMoveMap;
	
	// @SuppressWarnings("unchecked")
	public LearnSet(PokemonSpecies species, JsonArrayNode moves) throws MissingPropertyException {
		// final boolean print = species.dex == 381;
		
		// if(print) System.out.println("moves: "+moves.getLength());
		
		levelUpMoveSet = new TreeSet<>();
		levelUpMoveMap = new TreeMap<>();
		this.moves = new Move[moves.getLength()];
		for(int i = 0; i < this.moves.length; i++) {
			JsonObjectNode moveNode = moves.getObjectNode(i);
			this.moves[i] = DataCore.MOVES.get(NodeParser.getResourceId(moveNode.getObjectNode("move")));
			if(this.moves[i] == null) {
				System.err.println("note: move "+i+" for pokemon "+species+" is null, skipping (resource name was "+moveNode.getObjectNode("move").parseValueNode("name", JsonNode::textValue)+")");
				continue;
			}
			// this.moves[i] = DataCore.MOVES.getRef(moveNode.getObjectNode("move"));
			
			JsonArrayNode versionInfoList = moveNode.getArrayNode("version_group_details");
			for(int j = versionInfoList.getLength() - 1; j >= 0; j--) {
				JsonObjectNode latestVersionInfo = versionInfoList.getObjectNode(j);
				MoveLearnMethod learnMethod = MoveLearnMethod.getLearnMethod(latestVersionInfo.getObjectNode("move_learn_method").parseValueNode("name", JsonNode::textValue));
				if(learnMethod != MoveLearnMethod.LevelUp)
					continue;
				int level = latestVersionInfo.parseValueNode("level_learned_at", JsonNode::intValue);
				// if(print) System.out.println("move "+this.moves[i]+" learned at "+level+" with method "+learnMethod);
				LevelUpMove lMove = new LevelUpMove(level, this.moves[i]);
				levelUpMoveSet.add(lMove);
				levelUpMoveMap.computeIfAbsent(level, lvl -> new TreeSet<>()).add(lMove);
				break;
			}
		}
		
		// if(print)
		// 	System.out.println("pokemon "+species.name+" levelup map:"+levelUpMoveMap);
	}
	
	@NotNull
	public NavigableSet<LevelUpMove> getMovePool(int level) {
		Entry<Integer, TreeSet<LevelUpMove>> lastMoves = levelUpMoveMap.floorEntry(level);
		return lastMoves == null
			? EMPTY_SET
			: levelUpMoveSet.headSet(lastMoves.getValue().last(), true);
	}
	
	public Move[] getNewMoves(int level) {
		LinkedList<Move> newMoves = new LinkedList<>();
		TreeSet<LevelUpMove> moveSet = levelUpMoveMap.get(level);
		if(moveSet != null)
			moveSet.forEach(m -> newMoves.add(m.move));
		return newMoves.toArray(new Move[0]);
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
