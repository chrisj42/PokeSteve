package bot.cmd;

public class Argument<T> {
	public final String argName;
	// public final Class<T> type;
	public final ArgType<T> type;
	private int pos;
	
	public Argument(String argName, ArgType<T> type) {
		this.argName = argName;
		this.type = type;
	}
	
	void setPos(int pos) { this.pos = pos; }
	int getPos() { return pos; }
}
