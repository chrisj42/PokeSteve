package bot.world.pokemon;

import java.text.DecimalFormat;
import java.util.EnumMap;

import bot.data.DataCore;
import bot.data.UserData;
import bot.world.pokemon.StatData.SerialStatData;
import bot.world.pokemon.move.Move;
import bot.util.POJO;
import bot.util.Utils;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

import reactor.core.publisher.Mono;

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
	
	private int expCache; // exp to be added
	
	// for catching pokemon and loading data
	private Pokemon(@NotNull PokemonSpecies species, int level, int experience, Nature nature, Gender gender, Move[] moveset, EnumMap<Stat, StatData> statData) {
		this.species = species;
		this.level = level;
		this.experience = experience;
		this.nature = nature;
		this.gender = gender;
		this.moveset = moveset;
		this.statData = statData;
	}
	// wild pokemon
	public Pokemon(@NotNull PokemonSpecies species, int level, Nature nature, Gender gender, Move[] moveset) {
		this(species, level,
			species.growthRate.getExpRequirement(level),
			nature, gender, moveset, new EnumMap<>(Stat.class)
		);
		
		for(Move move: moveset) {
			if(move == null)
				System.err.println("pokemon "+species+" has nulls in moveset");
		}
		
		for(Stat stat: Stat.persistStats)
			statData.put(stat, new StatData(this, stat));
	}
	// on evolution
	private Pokemon(Pokemon prevForm, @NotNull PokemonSpecies evo) {
		this(evo, prevForm.level, prevForm.experience, prevForm.nature, prevForm.gender, prevForm.moveset, new EnumMap<>(Stat.class));
		prevForm.statData.forEach((stat, data) -> statData.put(stat, new StatData(this, stat, data)));
	}
	public Pokemon(SerialPokemon data) {
		species = DataCore.POKEMON.get(data.dex);
		nature = Utils.values(Nature.class)[data.nature];
		gender = Utils.values(Gender.class)[data.gender];
		level = data.level;
		experience = data.experience;
		
		moveset = new Move[data.moveset.length];
		for(int i = 0; i < moveset.length; i++)
			moveset[i] = DataCore.MOVES.get(data.moveset[i]);
		
		statData = new EnumMap<>(Stat.class);
		for(int i = 0; i < Stat.persistStats.length; i++)
			statData.put(Stat.persistStats[i], new StatData(this, Stat.persistStats[i], data.statData[i]));
	}
	
	public int getStat(Stat stat) {
		return statData.get(stat).getStatValue();
	}
	
	public int getLevel() { return level; }
	
	// when this pokemon defeats the given one
	public void onDefeat(Pokemon other) {
		statData.forEach((stat, data) -> other.species.addDefeatEV(data));
		
		expCache = (int) (other.species.baseDefeatExp * other.level / 5 * Math.pow(2*other.level + 10, 2.5) / Math.pow(other.level + level + 10, 2.5) + 1);
	}
	
	public Mono<Boolean> addExp(MessageChannel channel) {
		if(expCache == 0) return Mono.just(false);
		experience += expCache;
		
		StringBuilder str = new StringBuilder("Gained ").append(expCache).append(" exp!");
		
		boolean changed = false;
		while(level < MAX_LEVEL && experience >= species.growthRate.getExpRequirement(level+1)) {
			level++;
			changed = true;
			str.append("\n").append(species).append(" grew to level ").append(level).append("!");
			Move[] newMoves = species.learnableMoves.getNewMoves(level);
			for(Move move: newMoves)
				str.append("\n").append(species).append(" learned ").append(move).append("!");
		}
		if(changed)
			statData.forEach((stat, data) -> data.recalcStat());
		
		expCache = 0;
		
		return channel.createEmbed(emb -> emb
			.setTitle("You win!")
			.setDescription(str.toString())
		).map(msg -> true);
	}
	
	// private static final DecimalFormat statFormat = new DecimalFormat("00");
	// private static final int MAX_STAT_NAME_LEN = 18;
	public void buildEmbed(EmbedCreateSpec e) {
		e.setTitle("Lv. "+level+" "+species.name);
		e.setThumbnail(species.getSpritePath());
		// e.addField("Level", String.valueOf(level), true);
		if(level < 100) {
			final int expMin = species.growthRate.getExpRequirement(level);
			final int expNext = species.growthRate.getExpRequirement(level + 1) - expMin;
			e.addField("Experience", (experience - expMin)+" / "+expNext + " exp", false);
		}
		
		e.addField("Gender", gender.name(), true);
		e.addField("Nature", nature.name(), true);
		
		e.addField("Move set", String.join(", ", Utils.map(String[].class, moveset, Move::getName)), false);
		
		e.addField("Stats", String.join("\n", Utils.map(String[].class, statData.entrySet(), entry -> { 
			final String statName = entry.getKey().toString();
			final String statValue = String.valueOf(entry.getValue().getStatValue());
			return statName + ": "
				// + " ".repeat(MAX_STAT_NAME_LEN - statName.length() - statValue.length())
				+ statValue;
		})), true);
		e.addField("IVs", String.join("\n", Utils.map(String[].class, statData.values(),
			data -> data.getIV()+" / 31"
		)), true);
		e.addField("EVs", String.join("\n", Utils.map(String[].class, statData.values(),
			data -> "+"+data.getEV()
		)), true);
		
		Iterable<String> moveNames = Utils.map(species.learnableMoves.getMovePool(level), lmove -> lmove.move.name);
		e.addField("Move pool", String.join(", ", moveNames), false);
	}
	
	
	public static class CaughtPokemon extends Pokemon implements Comparable<CaughtPokemon> {
		private final UserData owner;
		public final int catchId;
		
		// catch constructor
		public CaughtPokemon(Pokemon pokemon, UserData ownerData, int catchId) {
			super(pokemon.species, pokemon.level, pokemon.experience, pokemon.nature, pokemon.gender, pokemon.moveset, pokemon.statData);
			this.owner = ownerData;
			this.catchId = catchId;
		}
		// load constructor
		public CaughtPokemon(SerialPokemon data, UserData ownerData) {
			super(data);
			this.owner = ownerData;
			this.catchId = data.catchId;
		}
		
		@Override
		public void buildEmbed(EmbedCreateSpec e) {
			super.buildEmbed(e);
			e.setDescription("Catch ID - #" + catchId);
			e.setFooter("Owned by "+owner.getUser().getUsername(), owner.getUser().getAvatarUrl());
		}
		
		@Override
		public int compareTo(@NotNull Pokemon.CaughtPokemon o) {
			return Integer.compare(catchId, o.catchId);
		}
	}
	
	@POJO
	public static class SerialPokemon {
		public int dex;
		public int nature;
		public int gender;
		public int level;
		public int experience;
		public int[] moveset;
		public SerialStatData[] statData;
		public int catchId;
		
		public SerialPokemon() {}
		public SerialPokemon(CaughtPokemon caught) {
			catchId = caught.catchId;
			Pokemon pokemon = caught;
			dex = pokemon.species.dex;
			nature = pokemon.nature.ordinal();
			gender = pokemon.gender.ordinal();
			level = pokemon.level;
			experience = pokemon.experience;
			moveset = Utils.map(int[].class, pokemon.moveset, m -> m.id);
			
			statData = new SerialStatData[Stat.persistStats.length];
			for(int i = 0; i < statData.length; i++)
				statData[i] = new SerialStatData(pokemon.statData.get(Stat.persistStats[i]));
		}
	}
}
