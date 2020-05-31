package bot.util;

import bot.io.json.MissingPropertyException;

public interface MapFunction<T, R> {
	
	R apply(T val) throws MissingPropertyException;
	
	static <T1, T2, T3> MapFunction<T1, T3> chain(MapFunction<T1, T2> part1, MapFunction<T2, T3> part2) {
		return val -> part2.apply(part1.apply(val));
	}
}
