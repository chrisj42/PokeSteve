package bot.pokemon;

public class PokemonForm {
	
	// specific data that changes for pokemon of the same species
	// covers alternate visual forms, mega evolutions, regional forms, battle forms, etc
	
	private static class Field<T> {
		private static int idCounter = 0;
		
		private final int fieldId;
		
		private Field() {
			fieldId = idCounter++;
		}
		
		public T getValue(PokemonForm form) {
			return null;
		}
	}
	
	private static final Field<String> NAME = new Field<>();
	private static final Field<Integer> DEX = new Field<>();
	private static final Field<Byte> CATCH_RATE = new Field<>();
	
}
