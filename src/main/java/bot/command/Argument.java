package bot.command;

public class Argument<T> {
	
	public static final Argument<?>[] NO_ARGS = new Argument[0];
	
	final String argName;
	
	// public final Class<T> type;
	public final ArgType<T> type;
	private int pos;
	
	public Argument(String argName, ArgType<T> type) {
		this.argName = argName;
		this.type = type;
	}
	
	public String getName() { return argName; }
	
	void setPos(int pos) { this.pos = pos; }
	public int getPos() { return pos; }
	
	public static String getUsageString(Argument<?>[] args) {
		
	}
	
	
}
