package bot.world.pokemon;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.TreeSet;

import bot.Core;
import bot.data.DataCore;
import bot.data.UserData;
import bot.util.UsageException;
import bot.world.pokemon.EvolutionChain.LevelUpEvolution;
import bot.world.pokemon.LearnSet.LevelUpMove;
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
	private Move[] moveset;
	private int level;
	private int experience;
	private String nickname;
	
	private Pokemon defeatedCache; // defeated pokemon to be processed
	private final TreeSet<Move> movePool = new TreeSet<>();
	
	// for catching pokemon and loading data
	private Pokemon(@NotNull PokemonSpecies species, int level, int experience, Nature nature, Gender gender, Move[] moveset, EnumMap<Stat, StatData> statData, String nickname) {
		this.species = species;
		this.level = level;
		this.experience = experience;
		this.nature = nature;
		this.gender = gender;
		this.moveset = moveset;
		this.statData = statData;
		this.nickname = nickname;
		species.learnableMoves.getMovePool(level).forEach(lMove -> movePool.add(lMove.move));
	}
	// wild pokemon
	public Pokemon(@NotNull PokemonSpecies species, int level, Nature nature, Gender gender) {
		this(species, level,
			species.growthRate.getExpRequirement(level),
			nature, gender,
			species.learnableMoves.getDefaultMoveset(level),
			new EnumMap<>(Stat.class), null
		);
		
		/*for(Move move: moveset) {
			if(move == null)
				System.err.println("pokemon "+species+" has nulls in moveset");
		}*/
		
		for(Stat stat: Stat.persistStats)
			statData.put(stat, new StatData(this, stat));
	}
	// on evolution
	private Pokemon(Pokemon prevForm, @NotNull PokemonSpecies evo) {
		this(evo, prevForm.level, prevForm.experience, prevForm.nature, prevForm.gender, prevForm.moveset, new EnumMap<>(Stat.class), prevForm.nickname);
		prevForm.statData.forEach((stat, data) -> statData.put(stat, new StatData(this, stat, data)));
	}
	public Pokemon(SerialPokemon data) {
		this(DataCore.POKEMON.get(data.dex),
			data.level, data.experience,
			Utils.values(Nature.class)[data.nature],
			Utils.values(Gender.class)[data.gender],
			new Move[data.moveset.length],
			new EnumMap<>(Stat.class),
			data.nickname
		);
		
		for(int i = 0; i < moveset.length; i++)
			moveset[i] = DataCore.MOVES.get(data.moveset[i]);
		
		for(int i = 0; i < Stat.persistStats.length; i++)
			statData.put(Stat.persistStats[i], new StatData(this, Stat.persistStats[i], data.statData[i]));
	}
	
	public String getName() {
		return nickname == null ? species.name : nickname;
	}
	public boolean hasNickname() { return nickname != null; }
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public int getStat(Stat stat) {
		return statData.get(stat).getStatValue();
	}
	
	public int getLevel() { return level; }
	
	public int getMoveCount() { return moveset.length; }
	public Move getMove(int idx) { return moveset[idx]; }
	
	public void replaceMove(Move toLearn, Move toReplace) {
		if(!movePool.contains(toLearn))
			throw new UsageException(getName()+" cannot learn the move "+toLearn.getName()+".");
		if(Arrays.asList(moveset).contains(toLearn))
			throw new UsageException(getName()+" already knows the move "+toLearn.getName()+".");
		int idx = -1;
		for(int i = 0; i < moveset.length; i++) {
			if(moveset[i] == toReplace) {
				idx = i;
				break;
			}
		}
		if(idx < 0)
			throw new UsageException(getName()+" does not have the move "+toReplace.getName()+" in its move set.");
		
		moveset[idx] = toLearn;
	}
	
	// when this pokemon defeats the given one
	public void onDefeat(Pokemon other) {
		defeatedCache = other;
	}
	
	public Mono<Boolean> addExp(MessageChannel channel) {
		if(defeatedCache == null) return Mono.just(false);
		final Pokemon defeated = defeatedCache;
		defeatedCache = null;
		final int expGain = (int) (defeated.species.baseDefeatExp * defeated.level / 5 * Math.pow(2*defeated.level + 10, 2.5) / Math.pow(defeated.level + level + 10, 2.5) + 1);
		
		experience += expGain;
		statData.forEach((stat, data) -> defeated.species.addDefeatEV(data));
		
		StringBuilder str = new StringBuilder();
		
		// boolean changed = false;
		// EnumMap<Stat, Integer> statChanges = new EnumMap<>(Stat.class);
		LinkedList<String> statChanges = new LinkedList<>();
		while(level < MAX_LEVEL && experience >= species.growthRate.getExpRequirement(level+1)) {
			level++;
			// changed = true;
			str.append("\n").append(getName()).append(" grew to level ").append(level).append("!");
			// statChanges.clear();
			statData.forEach((stat, data) -> {
				int prev = data.getStatValue();
				data.onLevelUp();
				int cur = data.getStatValue();
				final int diff = cur - prev;
				if(diff != 0) {
					// statChanges.put(stat, diff);
					statChanges.add("+"+diff+" "+stat);
				}
			});
			str.append("\n").append(String.join(", ", statChanges));
			statChanges.clear();
			for(LevelUpMove lMove: species.learnableMoves.getNewMoves(level)) {
				final Move move = lMove.move;
				movePool.add(move);
				str.append("\n").append(getName()).append(" learned ").append(move).append("!");
				if(moveset.length < 4) {
					moveset = Utils.append(moveset, move);
					str.append("\n").append(move.getName()).append(" has been added to the move set.");
				} else
					str.append("\nUse `pokemon learn \"").append(move.getName()).append("\" \"<move to replace>\"` to add it to the move set.");
			}
		}
		// if(changed)
		statData.forEach((stat, data) -> data.recalcStat());
		
		if(this instanceof CaughtPokemon)
			((CaughtPokemon)this).owner.save();
		
		return channel.createEmbed(emb -> emb
			.setTitle("Gained "+expGain+" exp!")
			.setDescription(str.toString())
		).map(msg -> true);
	}
	
	// private static final DecimalFormat statFormat = new DecimalFormat("00");
	// private static final int MAX_STAT_NAME_LEN = 18;
	public void buildEmbed(EmbedCreateSpec e) {
		e.setTitle("Lv. "+level+" "+species.name+(nickname != null ? " AKA "+nickname : ""));
		e.setImage(species.getSpritePath());
		
		// if(nickname != null)
		// 	e.addField(, false);
		
		// e.addField("Level", String.valueOf(level), true);
		if(level < 100) {
			final int expMin = species.growthRate.getExpRequirement(level);
			final int expNext = species.growthRate.getExpRequirement(level + 1) - expMin;
			e.addField("Experience", (experience - expMin)+" / "+expNext + " exp", false);
		}
		
		e.addField("Gender", gender.name(), true);
		e.addField("Nature", nature.name(), true);
		
		e.addField("Move set", String.join(", ", Utils.map(String[].class, moveset, Move::getName)), false);
		
		e.addField("Stats", ""
				+"Health - "+getStat(Stat.Health)
				+"\nAttack - "+getStat(Stat.Attack)
				+"\nDefense - "+getStat(Stat.Defense)
				+"\nSp. Attack - "+getStat(Stat.SpAttack)
				+"\nSp. Defense - "+getStat(Stat.SpDefense)
				+"\nSpeed - "+getStat(Stat.Speed),
			true);
		e.addField("IVs", String.join("\n", Utils.map(String[].class, statData.values(),
			data -> data.getIV()+" / 31"
		)), true);
		e.addField("EVs", String.join("\n", Utils.map(String[].class, statData.values(),
			data -> "+"+data.getEV()
		)), true);
		
		Iterable<String> moveNames = Utils.map(movePool, move -> move.name);
		e.addField("Move pool", String.join(", ", moveNames), false);
		// e.addField("Move pool", "Pound, Slam, Leer, Absorb, Mega Drain, Agility, Quick Attack, Screech, Detect, Giga Drain, Pursuit, Endeavor, Energy Ball, Quick Guard", false);
		
		NavigableSet<LevelUpMove> nextMoves = species.learnableMoves.getFutureMoves(level);
		LevelUpMove nextMove = nextMoves.isEmpty() ? null : nextMoves.first();
		if(nextMove != null)
			e.addField("Next move at:", "Level "+nextMove.level, true);
		LevelUpEvolution evo = species.getEvolution();
		if(evo != null)
			e.addField("Evolves into "+DataCore.POKEMON.get(evo.nextSpeciesDex)+":", "Level "+evo.minLevel, true);
	}
	
	
	public static class CaughtPokemon extends Pokemon implements Comparable<CaughtPokemon> {
		private final UserData owner;
		public final int catchId;
		
		// catch constructor
		public CaughtPokemon(Pokemon pokemon, UserData ownerData, int catchId) {
			super(pokemon.species, pokemon.level, pokemon.experience, pokemon.nature, pokemon.gender, pokemon.moveset, pokemon.statData, pokemon.nickname);
			this.owner = ownerData;
			this.catchId = catchId;
		}
		// evo constructor
		public CaughtPokemon(CaughtPokemon prevForm, @NotNull PokemonSpecies evo) {
			super(prevForm, evo);
			this.owner = prevForm.owner;
			this.catchId = prevForm.catchId;
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
			e.setFooter("Owned by "+owner.getUser().getUsername(), Core.self.getAvatarUrl());
			// e.setAuthor(owner.getUser().getUsername(), null, owner.getUser().getAvatarUrl());
		}
		
		public void buildListEntry(StringBuilder str) {
			str.append("**").append(getName()).append("**");
			str.append(" - ID: ").append(catchId);
			if(hasNickname()) str.append(" - ").append(species.name);
			str.append(" - Lv. ").append(super.level);
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
		public String nickname;
		
		public SerialPokemon() {}
		public SerialPokemon(CaughtPokemon caught) {
			catchId = caught.catchId;
			Pokemon pokemon = caught;
			dex = pokemon.species.dex;
			nature = pokemon.nature.ordinal();
			gender = pokemon.gender.ordinal();
			level = pokemon.level;
			experience = pokemon.experience;
			nickname = pokemon.nickname;
			moveset = Utils.map(int[].class, pokemon.moveset, m -> m.id);
			
			statData = new SerialStatData[Stat.persistStats.length];
			for(int i = 0; i < statData.length; i++)
				statData[i] = new SerialStatData(pokemon.statData.get(Stat.persistStats[i]));
		}
	}
}
