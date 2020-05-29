package bot.pokemon.definiton;

public class PokemonSpecies {
	
	private final int dex;
	private final String name;
	private final Type[] types;
	
	
	private PokemonSpecies(int dex, String name, Type[] types) {
		this.dex = dex;
		this.name = name;
		this.types = types;
	}
	
	public static class PokemonBuilder {
		
		private final int dex;
		private final String name;
		private final Type[] types;
		
		public PokemonBuilder(int dex, String name, Type... types) {
			this.dex = dex;
			this.name = name;
			this.types = types;
		}
		
		public PokemonBuilder ability() {
			return this;
		}
		
		public PokemonSpecies build() {
			return new PokemonSpecies(dex, name, types);
		}
	}
}
