package bot.io.json;

import com.fasterxml.jackson.databind.JsonNode;

public interface NodeParser<T> {
	
	T parseNode(JsonNode node);
	
}
