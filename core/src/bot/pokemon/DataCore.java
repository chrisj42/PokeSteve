package bot.pokemon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;

import bot.io.json.MissingPropertyException;
import bot.io.json.node.JsonArrayNode;
import bot.io.json.node.JsonObjectNode;
import bot.pokemon.move.Move;
import bot.pokemon.move.Moves;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataCore {
	
	private DataCore() {}
	
	public static final ObjectMapper jsonMapper = new ObjectMapper();
	
	public static JsonNode readTree(String file) {
		try {
			return jsonMapper.readTree(new File(file));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static JsonArrayNode readList(String file) {
		try {
			return new JsonArrayNode(readTree(file));
		} catch(MissingPropertyException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final JsonArrayNode MOVE_JSON = readList("moves.json");
	
	public static final JsonArrayNode SPECIES_JSON = readList("pokemon-species.json");
	public static final JsonArrayNode POKEMON_JSON = readList("pokemon.json");
		
	
	public static final DataStore<Move> MOVES = new DataStore<>() {
		private final HashMap<String, Move> nameMap = new HashMap<>(Moves.values.length);
		
		{
			for(Moves m: Moves.values) {
				final Move move = m.getMove();
				nameMap.put(format(m.name()/*move.name*/), move);
			}
		}
		
		private String format(String name) {
			return name.toLowerCase()
				.replaceAll("[ \\-_]", "");
		}
		
		@Override
		public Move get(int id) {
			return Moves.values[id-1].getMove();
		}
		
		@Override
		public Move get(String name) {
			return nameMap.get(format(name));
		}
	};
	
	public static final DataStore<PokemonSpecies> POKEMON = new DataList<>(PokemonSpecies.class, SPECIES_JSON, POKEMON_JSON, PokemonSpecies::new);
	
	public interface DataStore<T> {
		T get(int id);
		T get(String name);
	}
	
	public static class DataList<T> implements DataStore<T> {
		
		interface NodeListParser<T> {
			T parseNode(JsonObjectNode... nodes) throws MissingPropertyException;
		}
		interface SingleNodeParser<T> extends NodeListParser<T> {
			T parseNode(JsonObjectNode node) throws MissingPropertyException;
			
			@Override
			default T parseNode(JsonObjectNode... nodes) throws MissingPropertyException {
				return parseNode(nodes[0]);
			}
		}
		interface BiNodeParser<T> extends NodeListParser<T> {
			T parseNode(JsonObjectNode node1, JsonObjectNode node2) throws MissingPropertyException;
			
			@Override
			default T parseNode(JsonObjectNode... nodes) throws MissingPropertyException {
				return parseNode(nodes[0], nodes[1]);
			}
		}
		
		private final T[] data;
		private final HashMap<String, T> nameMap;
		
		private DataList(Class<T> clazz, JsonArrayNode arrayNode, SingleNodeParser<T> parser) {
			this(clazz, parser, arrayNode);
		}
		private DataList(Class<T> clazz, JsonArrayNode anode1, JsonArrayNode anode2, BiNodeParser<T> parser) {
			this(clazz, parser, anode1, anode2);
		}
		@SuppressWarnings("unchecked")
		private DataList(Class<T> clazz, NodeListParser<T> parser, JsonArrayNode... nodes) {
			data = (T[]) Array.newInstance(clazz, nodes[0].getLength());
			nameMap = new HashMap<>(data.length);
			JsonObjectNode[] dnodes = new JsonObjectNode[nodes.length];
			for(int i = 0; i < data.length; i++) {
				// System.out.println("parsing "+i);
				try {
					for(int j = 0; j < nodes.length; j++)
						dnodes[j] = nodes[j].getObjectNode(i);
					data[i] = parser.parseNode(dnodes);
				} catch(MissingPropertyException e) {
					throw new RuntimeException("Error while parsing data "+i, e);
				}
				nameMap.put(data[i].toString().toLowerCase(), data[i]);
			}
		}
		
		@Override
		public T get(int id) {
			if(id > data.length || id <= 0) return null;
			return data[id-1];
		}
		
		@Override
		public T get(String name) {
			return nameMap.get(name.toLowerCase());
		}
		
		/*public Ref<T> getRef(int id) {
			return new Ref<>(id, this::get);
		}
		public Ref<T> getRef(JsonObjectNode resourceNode) throws MissingPropertyException {
			return getRef(NodeParser.getResourceId(resourceNode));
		}*/
	}
	
	private static class DataEnum<T> implements DataStore<T> {
		
		
		
		@Override
		public T get(int id) {
			return null;
		}
		
		@Override
		public T get(String name) {
			return null;
		}
	}
	
	public static void init() {}
}
