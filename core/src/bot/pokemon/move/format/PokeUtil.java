package bot.pokemon.move.format;

import bot.util.Utils;

public final class PokeUtil {
	
	private PokeUtil() {}
	
	// i'll list various uses of integers here
	
	// CHANCE
	// a chance of something occurring; 0-100
	// 100 means normally guaranteed
	// 0 means always guaranteed
	public static boolean chance(int chance) {
		if(chance == 0) return true;
		return Utils.randInt(0, 99) < chance;
	}
	
	// RANGE
	public static class RangeValue {
		
		private int min;
		private int max;
		
		public RangeValue() {}
		
		public int getValue() {
			return Utils.randInt(min, max);
		}
		
		public int getMin() {
			return min;
		}
		
		public int getMax() {
			return max;
		}
	}
}
