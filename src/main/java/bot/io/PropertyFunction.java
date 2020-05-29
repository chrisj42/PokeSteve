package bot.io;

import bot.util.ThrowFunction;

public interface PropertyFunction<T, R> extends ThrowFunction<T, R, MissingPropertyException> {
	
	static <T1, T2, T3> PropertyFunction<T1, T3> attach(PropertyFunction<T1, T2> part1, PropertyFunction<T2, T3> part2) {
		return val -> part2.apply(part1.apply(val));
	}
}
