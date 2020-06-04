package bot.pokemon;

import java.util.function.Function;

public enum GrowthRate {
	
	Slow(lv -> 5*lv*lv*lv/4),
	Medium(lv -> lv*lv*lv),
	Fast(lv -> 4*lv*lv*lv/5),
	MediumSlow(lv -> 6*lv*lv*lv/5 - 15*lv*lv + 100*lv - 140),
	SlowThenFast(lv -> {
		final int cube = lv*lv*lv;
		if(lv <= 50)
			return cube*(100-lv)/50;
		if(lv <= 68)
			return cube*(150-lv)/100;
		if(lv <= 98)
			return cube*((1911-10*lv)/3)/500;
		return cube*(160-lv)/100;
	}),
	FastThenSlow(lv -> {
		final int cube = lv*lv*lv;
		if(lv <= 15)
			return cube*((lv+1)/3+24)/50;
		if(lv <= 36)
			return cube*(lv+14)/50;
		return cube*((lv/2)+32)/50;
	});
	
	private final Function<Integer, Integer> xpFetcher;
	
	GrowthRate(Function<Integer, Integer> xpFetcher) {
		this.xpFetcher = xpFetcher;
	}
	
	public int getXpRequirement(int level) {
		return xpFetcher.apply(level);
	}
}
