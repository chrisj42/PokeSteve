package bot.pokemon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
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
	
	public static final DataList<Move> MOVES = new DataList<>(Move.class, "moves.json", Move::new);
	
	public static final DataList<PokemonSpecies> POKEMON = new DataList<>(PokemonSpecies.class, "pokemon-species.json", "pokemon.json", PokemonSpecies::new);
	
	public static class DataList<T> {
		
		interface SingleNodeParser<T> {
			T parseNode(JsonObjectNode node) throws MissingPropertyException;
		}
		interface BiNodeParser<T> {
			T parseNode(JsonObjectNode node1, JsonObjectNode node2) throws MissingPropertyException;
		}
		
		private final T[] data;
		private HashMap<String, T> nameMap;
		
		@SuppressWarnings("unchecked")
		private DataList(Class<T> clazz, String filename, SingleNodeParser<T> parser) {
			try {
				JsonArrayNode arrayNode = new JsonArrayNode(Core.jsonMapper.readTree(new File(filename)));
				data = (T[]) Array.newInstance(clazz, arrayNode.getLength());
				nameMap = new HashMap<>(arrayNode.getLength());
				System.out.println("reading "+data.length+" entries");
				for(int i = 0; i < data.length; i++) {
					if(!arrayNode.getNode().has(i)) {
						System.err.println("array node does not have "+i);
						continue;
					}
					// System.out.println("parsing "+i);
					try {
						data[i] = parser.parseNode(arrayNode.getObjectNode(i));
					} catch(MissingPropertyException e) {
						throw new RuntimeException("Error while parsing data "+i, e);
					}
					nameMap.put(data[i].toString().toLowerCase(), data[i]);
				}
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
				nameMap = new HashMap<>(anode1.getLength());
				for(int i = 0; i < data.length; i++) {
					// System.out.println("parsing "+i);
					try {
						data[i] = parser.parseNode(anode1.getObjectNode(i), anode2.getObjectNode(i));
					} catch(MissingPropertyException e) {
						throw new RuntimeException("Error while parsing data "+i, e);
					}
					nameMap.put(data[i].toString().toLowerCase(), data[i]);
				}
			} catch(IOException | MissingPropertyException e) {
				throw new RuntimeException(e);
			}
		}
		
		public T get(int id) {
			if(id > data.length || id <= 0) return null;
			return data[id-1];
		}
		
		public T get(String name) {
			return nameMap.get(name.toLowerCase());
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
