package bot.io;

import java.io.File;
import java.io.IOException;

import bot.Core;
import bot.io.json.MissingPropertyException;
import bot.io.json.node.JsonObjectNode;

public enum DataFile {
	
	AUTH("auth.json"),
	
	CONFIG("config.json"),
	
	DATA("data.json");
	
	private final String fileName;
	private final String path;
	private final File file;
	
	DataFile(String fileName) {
		this.fileName = fileName;
		this.path = "src/main/resources/"+fileName;
		this.file = new File(path);
	}
	
	public JsonObjectNode readJson() throws MissingPropertyException, IOException {
		return new JsonObjectNode(Core.jsonMapper.readTree(file));
	}
}
