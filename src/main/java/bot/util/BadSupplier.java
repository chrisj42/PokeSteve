package bot.util;

public interface BadSupplier<T> {
	T get() throws Throwable;
}
