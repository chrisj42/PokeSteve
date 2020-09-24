package bot.world.pokemon.battle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bot.util.Utils;
import bot.world.pokemon.Pokemon;
import bot.world.pokemon.Stat;
import bot.world.pokemon.Stat.StageEquation;
import bot.world.pokemon.battle.Flag.BoolFlag;
import bot.world.pokemon.battle.Flag.ValueFlag;
import bot.world.pokemon.move.ChargeState;
import bot.world.pokemon.move.PersistentEffect;

import reactor.util.annotation.Nullable;

public class BattlePokemon {
	
	// this class is used when battling; holds things like stat stage changes and other temporary effects.
	
	public static final int MAX_STAGE = 6;
	public static final int MIN_STAGE = -6;
	
	public final Pokemon pokemon;
	
	private int health;
	private final int[] movePp;
	
	private final EnumMap<Stat, Integer> statStages;
	private final ArrayList<PersistentEffect> effects;
	
	private final HashSet<BoolFlag> boolFlags = new HashSet<>();
	private final HashMap<ValueFlag<?>, Object> valueFlags = new HashMap<>();
	
	public BattlePokemon(Pokemon pokemon) {
		this.pokemon = pokemon;
		health = pokemon.getStat(Stat.Health);
		
		statStages = new EnumMap<>(Stat.class);
		for(Stat stat: Stat.stageStats)
			statStages.put(stat, 0);
		
		effects = new ArrayList<>(4);
		
		movePp = new int[pokemon.getMoveCount()];
		for(int i = 0; i < movePp.length; i++)
			movePp[i] = pokemon.getMove(i).pp;
	}
	
	public List<Integer> getAvailableMoves() {
		ArrayList<Integer> availableMoves = new ArrayList<>(pokemon.getMoveCount());
		
		if(hasFlag(Flag.FORCED_MOVE)) {
			availableMoves.add(getFlag(Flag.FORCED_MOVE));
		} else {
			for(int i = 0; i < pokemon.getMoveCount(); i++) {
				if(getPp(i) > 0)
					availableMoves.add(i);
			}
		}
		
		if(hasFlag(Flag.DISABLED_MOVE))
			availableMoves.remove(getFlag(Flag.DISABLED_MOVE));
		
		return availableMoves;
	}
	
	public int getStage(Stat stat) {
		return statStages.get(stat);
	}
	
	// returns whether there was a change
	public boolean alterStatStage(Stat stat, int amount) {
		final int cur = statStages.get(stat);
		final int next = Utils.clamp(cur + amount, MIN_STAGE, MAX_STAGE);
		statStages.put(stat, next);
		return cur != next;
	}
	
	public int getPp(int move) {
		return movePp[move];
	}
	
	public void subtractPp(int move) {
		movePp[move]--;
	}
	
	public int getHealth() { return health; }
	
	public int alterHealth(int amount) {
		final int prev = health;
		health = Utils.clamp(health + amount, 0, pokemon.getStat(Stat.Health));
		return health - prev;
	}
	
	public int getSpeed() {
		return StageEquation.Main.modifyStat(pokemon.getStat(Stat.Speed), getStage(Stat.Speed));
	}
	
	public boolean hasFlag(BoolFlag flag) { return boolFlags.contains(flag); }
	public void setFlag(BoolFlag flag) { boolFlags.add(flag); }
	public void clearFlag(BoolFlag flag) { boolFlags.remove(flag); }
	
	@SuppressWarnings("unchecked")
	public <T> T getFlag(ValueFlag<T> flag) {
		return (T) valueFlags.get(flag);
	}
	public <T> boolean hasFlag(ValueFlag<T> flag) {
		return valueFlags.containsKey(flag);
	}
	public <T> void setFlag(ValueFlag<T> flag, T value) {
		valueFlags.put(flag, value);
	}
	public <T> void clearFlag(ValueFlag<T> flag) {
		valueFlags.remove(flag);
	}
	
	public void addEffect(PersistentEffect effect) {
		effects.add(effect);
	}
	
	public void processEffects(PlayerContext context) {
		effects.removeIf(effect -> {
			if(!effect.apply(context)) {
				effect.onEffectEnd(context);
				return false;
			}
			return true;
		});
	}
	
	@Nullable
	public ChargeState getChargeState() {
		Integer chargeMove = getFlag(Flag.FORCED_MOVE);
		if(chargeMove == null) return null;
		return pokemon.getMove(chargeMove).chargeState;
	}
}
