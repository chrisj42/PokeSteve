package bot.io;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

public class ReadOnlyJsonTraversal extends JsonTraversal<ReadOnlyJsonTraversal> {
	public ReadOnlyJsonTraversal(DataFile fileType) throws IOException, MissingPropertyException {
		super(fileType);
	}
	
	private ReadOnlyJsonTraversal(ReadOnlyJsonTraversal parent, String depthString, JsonNode node) {
		super(parent, depthString, node);
	}
	
	@Override
	ReadOnlyJsonTraversal intoObjectNode(String fieldName, JsonNode node) {
		return new ReadOnlyJsonTraversal(this, fieldName, node);
	}
	@Override
	ReadOnlyJsonTraversal intoArrayNode(int index, JsonNode node) {
		return new ReadOnlyJsonTraversal(this, "["+index+"]", node);
	}
}
