package bot.util;

public interface ThrowFunction<T, R, E extends Throwable> {
	R apply(T val) throws E;
}
