package bot.pokemon;

import java.util.Arrays;
import java.util.EnumMap;

import bot.util.Utils;

import org.jetbrains.annotations.NotNull;

public class Pokemon {
	
	public static final int MAX_LEVEL = 100;
	
	@NotNull
	public final PokemonSpecies species;
	public final Nature nature;
	public final Gender gender;
	private final EnumMap<Stat, StatData> statData;
	public final Move[] moveset;
	private int level;
	private int experience;
	
	// wild pokemon
	public Pokemon(@NotNull PokemonSpecies species, int level, Nature nature, Gender gender, Move[] moveset) {
		this.species = species;
		this.level = level;
		this.experience = species.growthRate.getExpRequirement(level);
		this.nature = nature;
		this.gender = gender;
		this.moveset = moveset;
		
		statData = new EnumMap<>(Stat.class);
		for(Stat stat: Stat.values)
			statData.put(stat, new StatData(this, stat));
		
	}
	
	// TODO constructor taking a pre-evolved form to handle evolutions and pass on movesets and EVs
	
	public int getStat(Stat stat) {
		return statData.get(stat).getStatValue();
	}
	
	public int getLevel() { return level; }
	
	public void onDefeat(Pokemon other) {
		statData.forEach((stat, data) -> other.species.addDefeatEV(data));
		
		int expGain = (int) (other.species.baseDefeatExp * other.level / 5 * Math.pow(2*other.level + 10, 2.5) / Math.pow(other.level + level + 10, 2.5) + 1);
		addExp(expGain);
	}
	
	public void addExp(int exp) {
		experience += exp;
		boolean changed = false;
		while(level < MAX_LEVEL && experience >= species.growthRate.getExpRequirement(level+1)) {
			level++;
			changed = true;
		}
		if(changed)
			statData.forEach((stat, data) -> data.recalcStat());
	}
	
	public String buildInfo() {
		StringBuilder info = new StringBuilder("__Pokemon info__");
		info.append("\nname: ").append(species.name);
		info.append("\ndex: ").append(species.dex);
		info.append("\ngender: ").append(gender);
		info.append("\nnature: ").append(nature);
		info.append("\nlevel: ").append(level);
		info.append("\nexperience: ").append(experience);
		info.append("\nstats: ");
		statData.forEach((stat, data) -> info.append(stat).append('=').append(data.getStatValue()).append(", "));
		info.append("\nmoves: ").append(
			String.join(", ", Utils.map(Arrays.asList(moveset), move -> move.name))
		);
		info.append("\n\nsprite coming soon once I feel like added richEmbeds.");
		return info.toString();
	}
}
