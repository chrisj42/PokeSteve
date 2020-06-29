package bot.data;

import java.io.File;
import java.io.IOException;

import bot.Core;
import bot.data.json.MissingPropertyException;
import bot.data.json.node.JsonObjectNode;

public enum DataFile {
	
	AUTH("auth.json"),
	
	CONFIG("config.json"),
	
	DATA("data.json");
	
	// private final String fileName;
	// private final String path;
	private final File file;
	
	DataFile(String path) {
		// this.fileName = fileName;
		// this.path = fileName;
		this.file = new File(path);
	}
	
	public JsonObjectNode readJson() throws MissingPropertyException, IOException {
		return new JsonObjectNode(Core.jsonMapper.readTree(file));
	}
}
