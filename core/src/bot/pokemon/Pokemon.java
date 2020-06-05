package bot.pokemon;

import java.util.EnumMap;

import org.jetbrains.annotations.NotNull;

public class Pokemon {
	
	@NotNull
	private final PokemonSpecies species;
	private final Nature nature;
	private final Gender gender;
	
	private final EnumMap<Stat, Integer> stats;
	private final Move[] moveset;
	private int level;
	private int experience;
	
	// wild pokemon
	public Pokemon(@NotNull PokemonSpecies species, int level, Nature nature, Gender gender, EnumMap<Stat, Integer> stats, Move[] moveset) {
		this.species = species;
		this.level = level;
		this.experience = species.growthRate.getXpRequirement(level);
		this.nature = nature;
		this.gender = gender;
		this.stats = new EnumMap<>(stats);
		this.moveset = moveset;
	}
	
}
