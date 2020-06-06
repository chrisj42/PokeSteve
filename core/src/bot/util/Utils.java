package bot.util;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Utils {
	private Utils() {}
	
	public static int randInt(int min, int max) {
		return ((int) (Math.random() * (max-min+1))) + min;
	}
	
	public static <T> T pickRandom(T[] array) {
		if(array.length == 0)
			return null;
		return array[randInt(0, array.length-1)];
	}
	
	public static <T extends Comparable<T>> T clamp(T value, T min, T max) {
		if(value.compareTo(min) < 0)
			return min;
		if(value.compareTo(max) > 0)
			return max;
		return value;
	} 
	
	public static <T> boolean matchAny(T[] collection, Predicate<T> acceptFunc) {
		for(T val: collection) {
			if(acceptFunc.test(val))
				return true;
		}
		return false;
	}
	
	public static <T extends Throwable, V> V throwIfNull(V val, Supplier<T> exceptionGenerator) throws T {
		if(val == null)
			throw exceptionGenerator.get();
		return val;
	}
	
	public static <T1, T2, T3> Function<T1, T3> stepMap(Function<T1, T2> step1, Function<T2, T3> step2) {
		return step2.compose(step1);
	}
	
	/*public static boolean matchBot(User user) { return matchBot(user.getId()); }
	public static boolean matchBot(Snowflake id) {
		return Optional.ofNullable(Owners.get(id)).map(o -> o.id.equals(id)).orElse(Core.data.isBlobbo); 
	}
	public static boolean matchOwner(User user) { return matchOwner(user.getId()); }
	public static boolean matchOwner(Snowflake id) {
		return Core.data.owner.equals(id);
	}*/
	
	public static <A, B> Iterable<B> map(Iterable<A> iterable, Function<A, B> mapper) {
		LinkedList<B> list = new LinkedList<>();
		iterable.forEach(a -> list.add(mapper.apply(a)));
		return list;
	}
	
	/*@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <OT extends Throwable, NT extends Throwable, V> V wrapException(BadSupplier<V> valueFetcher, Function<OT, NT> exceptionWrapper, Class<? extends OT>... classesToCatch) throws NT {
		try {
			return valueFetcher.get();
		}
		catch(Throwable t) {
			if(matchAny(classesToCatch, clazz -> clazz.isAssignableFrom(t.getClass())))
				throw exceptionWrapper.apply((OT) t);
			
			if(!(t instanceof RuntimeException)) // these should have been handled better
				t = new RuntimeException(t);
			
			throw (RuntimeException) t;
		}
	}*/
	
}
