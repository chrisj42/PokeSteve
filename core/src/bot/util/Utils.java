package bot.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Utils {
	private Utils() {}
	
	
	// MATH UTILS
	
	
	public static int randInt(int min, int max) {
		return ((int) (Math.random() * (max-min+1))) + min;
	}
	
	public static <T> T pickRandom(List<T> list) {
		if(list.size() == 0)
			return null;
		return list.get(randInt(0, list.size() - 1));
	}
	public static <T> T pickRandom(T[] array) {
		if(array.length == 0)
			return null;
		return array[randInt(0, array.length-1)];
	}
	
	// classic out-of-100 chance integers
	public static boolean chance(int chance) {
		if(chance == 0) return true;
		return randInt(0, 99) < chance;
	}
	
	public static <T extends Comparable<T>> T clamp(T value, T min, T max) {
		if(value.compareTo(min) < 0)
			return min;
		if(value.compareTo(max) > 0)
			return max;
		return value;
	}
	
	public static int hourToMs(int hours) { return hourToMs(hours, 0); }
	public static int hourToMs(int hours, int minutes) { return hourToMs(hours, minutes, 0); }
	public static int hourToMs(int hours, int minutes, int seconds) { return hourToMs(hours, minutes, seconds, 0); }
	public static int hourToMs(int hours, int minutes, int seconds, int ms) { return minToMs(hours * 60 + minutes, seconds, ms); }
	public static int minToMs(int minutes) { return minToMs(minutes, 0); }
	public static int minToMs(int minutes, int seconds) { return minToMs(minutes, seconds, 0); }
	public static int minToMs(int minutes, int seconds, int ms) { return secToMs(minutes * 60 + seconds, ms); }
	public static int secToMs(int seconds) { return secToMs(seconds, 0); }
	public static int secToMs(int seconds, int ms) { return seconds * 1000 + ms; }
	
	
	// STRING UTILS
	
	
	public static String capitalizeFirst(String str) {
		if(str.length() == 0) return str;
		if(str.length() == 1) return str.toUpperCase();
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	public static String plural(int amount, String suffix) { return plural(amount, suffix, "", "s"); }
	public static String plural(int amount, String main, String plural) {
		return plural(amount, main, "", plural);
	}
	public static String plural(int amount, String main, String single, String plural) {
		return amount + " " + main + (amount == 1 ? single : plural);
	}
	
	
	// ARRAY UTILS
	
	
	public static <T> boolean matchAny(T[] collection, Predicate<T> acceptFunc) {
		for(T val: collection) {
			if(acceptFunc.test(val))
				return true;
		}
		return false;
	}
	
	public static <A, B> Iterable<B> map(Iterable<A> iterable, Function<A, B> mapper) {
		LinkedList<B> list = new LinkedList<>();
		iterable.forEach(a -> list.add(mapper.apply(a)));
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T, U> T map(Class<T> resultArrayType, U[] source, Function<U, Object> valueFetcher) {
		T array = (T) Array.newInstance(resultArrayType.getComponentType(), source.length);
		fill(array, i -> valueFetcher.apply(source[i]));
		return array;
	}
	@SuppressWarnings("unchecked")
	public static <T, U> T map(Class<T> resultArrayType, Collection<U> source, Function<U, Object> valueFetcher) {
		T array = (T) Array.newInstance(resultArrayType.getComponentType(), source.size());
		int i = 0;
		for(U obj: source)
			Array.set(array, i++, valueFetcher.apply(obj));
		return array;
	}
	
	public static void fill(Object array, Function<Integer, Object> valueFetcher) {
		for(int i = 0; i < Array.getLength(array); i++)
			Array.set(array, i, valueFetcher.apply(i));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] append(T[] ar, T value) {
		T[] newAr = (T[]) Array.newInstance(ar.getClass().getComponentType(), ar.length + 1);
		System.arraycopy(ar, 0, newAr, 0, ar.length);
		newAr[ar.length] = value;
		return newAr;
	}
	
	
	// ERROR UTILS
	
	
	public static <T extends Throwable, V> V throwIfNull(V val, Supplier<T> exceptionGenerator) throws T {
		if(val == null)
			throw exceptionGenerator.get();
		return val;
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
	
	private static final HashMap<Class<? extends Enum<?>>, Object[]> enumArrays = new HashMap<>();
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T[] values(Class<T> enumClass) {
		return (T[]) enumArrays.computeIfAbsent(enumClass, Class::getEnumConstants);
	}
}
