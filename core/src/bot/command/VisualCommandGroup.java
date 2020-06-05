package bot.command;

public enum VisualCommandGroup {
	
	MESSAGING(""),
	
	MUSIC(""),
	
	MISC("");
	
	public final String description;
	
	VisualCommandGroup(String description) {
		this.description = description;
	}
	
}
