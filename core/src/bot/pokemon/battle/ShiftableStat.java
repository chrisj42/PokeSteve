package bot.pokemon.battle;

import java.util.function.Function;

public enum ShiftableStat {
	
	// these are the stats that can be altered in stages during battle
	
	Attack(StageEquation.Main),
	Defense(StageEquation.Main),
	SpAttack(StageEquation.Main),
	SpDefense(StageEquation.Main),
	Speed(StageEquation.Main),
	Accuracy(StageEquation.Altered),
	Evasion(StageEquation.Altered);
	
	public static final ShiftableStat[] values = ShiftableStat.values();
	
	public final StageEquation statModifier;
	
	ShiftableStat(StageEquation statModifier) {
		this.statModifier = statModifier;
	}
	
	public StatStageManager getStatManager() {
		return new StatStageManager();
	}
	
	public enum StageEquation {
		
		Main(s -> Math.max(2f, 2 + s) / Math.max(2, 2 - s)),
		
		Altered(s -> Math.max(3f, 3 + s) / Math.max(3, 3 - s));
		
		private final Function<Integer, Float> statModifer;
		
		StageEquation(Function<Integer, Float> statModifer) {
			this.statModifer = statModifer;
		}
		
		public float getMultiplier(int stage) {
			return statModifer.apply(stage);
		}
	}
	
	public class StatStageManager {
		
		public static final int MAX_VALUE = 6;
		public static final int MIN_VALUE = -6;
		
		private int stage = 0; // defaults to zero
		
		public StatStageManager() {}
		
		public boolean alterStage(int amount) {
			final int start = stage;
			stage += amount;
			stage = Math.min(MAX_VALUE, Math.max(MIN_VALUE, stage));
			return stage != start;
		}
		
		public int getStat(int baseStat) {
			return Math.round(baseStat * statModifier.getMultiplier(stage));
		}
	}
}
