package bot.util;

public interface ThrowConsumer<T, E extends Throwable> {
	void accept(T val) throws E;
}
