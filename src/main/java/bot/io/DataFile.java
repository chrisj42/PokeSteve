package bot.io;

public enum DataFile {
	
	AUTH("auth.json"),
	
	CONFIG("config.json"),
	
	DATA("data.json");
	
	public final String path;
	
	DataFile(String path) {
		this.path = "src/main/resources/"+path;
	}
	
}
