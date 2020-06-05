package bot.pokemon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.function.BiFunction;
import java.util.function.Function;

import bot.Core;
import bot.io.json.MissingPropertyException;
import bot.io.json.NodeParser;
import bot.io.json.node.JsonArrayNode;
import bot.io.json.node.JsonObjectNode;
import bot.util.Ref;

public class DataCore {
	
	private DataCore() {}
	
	public static final DataList<Move> MOVES = new DataList<>(Move.class, "moves-gen1.json", Move::new);
	
	public static final DataList<PokemonSpecies> POKEMON = new DataList<>(PokemonSpecies.class, "pokemon-species-gen1.json", "pokemon-gen1.json", PokemonSpecies::new);
	
	public static class DataList<T> {
		
		interface SingleNodeParser<T> {
			T parseNode(JsonObjectNode node) throws MissingPropertyException;
		}
		interface BiNodeParser<T> {
			T parseNode(JsonObjectNode node1, JsonObjectNode node2) throws MissingPropertyException;
		}
		
		private final T[] data;
		
		@SuppressWarnings("unchecked")
		private DataList(Class<T> clazz, String filename, SingleNodeParser<T> parser) {
			try {
				JsonArrayNode arrayNode = new JsonArrayNode(Core.jsonMapper.readTree(new File(filename)));
				data = (T[]) Array.newInstance(clazz, arrayNode.getLength());
				for(int i = 0; i < data.length; i++)
					data[i] = parser.parseNode(arrayNode.getObjectNode(i));
			} catch(IOException | MissingPropertyException e) {
				throw new RuntimeException(e);
			}
		}
		@SuppressWarnings("unchecked")
		private DataList(Class<T> clazz, String filename1, String filename2, BiNodeParser<T> parser) {
			try {
				JsonArrayNode anode1 = new JsonArrayNode(Core.jsonMapper.readTree(new File(filename1)));
				JsonArrayNode anode2 = new JsonArrayNode(Core.jsonMapper.readTree(new File(filename2)));
				data = (T[]) Array.newInstance(clazz, anode1.getLength());
				for(int i = 0; i < data.length; i++)
					data[i] = parser.parseNode(anode1.getObjectNode(i), anode2.getObjectNode(i));
			} catch(IOException | MissingPropertyException e) {
				throw new RuntimeException(e);
			}
		}
		
		public T get(int id) {
			return data[id-1];
		}
		
		public Ref<T> getRef(int id) {
			return new Ref<>(id, this::get);
		}
		public Ref<T> getRef(JsonObjectNode resourceNode) throws MissingPropertyException {
			return getRef(NodeParser.getResourceId(resourceNode));
		}
	}
	
	public static void init() {}
}
