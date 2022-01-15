package bot.world.pokemon.battle.status;

import java.util.EnumSet;

import bot.world.pokemon.battle.BattlePokemon;
import bot.world.pokemon.battle.Flag;
import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.battle.PlayerContext;
import bot.world.pokemon.move.EffectResult;
import bot.world.pokemon.move.PersistentEffect;
import bot.world.pokemon.move.PersistentEffect.TimedPersistentEffect;
import bot.world.pokemon.move.StatusEffect;

public enum StatusAilment {
	Paralysis(1.5f, " was paralyzed!", " is paralyzed! It can't move!", " is no longer paralyzed.")/* {
		@Override
		public EffectResult applyStatus(MoveContext context, BattlePokemon pokemon) {
			if(!Paralysis.tryApplyStatus(pokemon))
				return EffectResult.FAILURE;
			context.line(pokemon).append(" was paralyzed!");
		}
	}*/,
	Sleep(2, " fell asleep!", " is fast asleep.", " woke up!"),
	Freeze(2, " has been frozen!", " is frozen solid!", " thawed out."),
	Burn(1.5f, " was burned!", " is hurt by its burn!", " is no longer on fire :D"),
	Poison(1.5f, " was poisoned!", " is hurt by the poison!", " is no longer poisoned."),
	Bad_Poison(1.6f, " was badly poisoned!", " is hurt by the poison!", " is no longer poisoned."),
	Confusion(1, " became confused!", " is confused!", " snapped out of its confusion.");
	// most effects past here will be relocated / turned into flags
	// Infatuation, Trap, Nightmare, Torment, Disable, Yawn,
	// HealBlock, NoTypeImmunity, LeechSeed, Embargo, PerishSong,
	// Ingrain, Silence;
	
	public static final EnumSet<StatusAilment> PERSISTENT_AILMENTS = EnumSet.of(
		Paralysis, Sleep, Freeze, Burn, Poison, Bad_Poison
	);
	
	public static final StatusAilment[] values = StatusAilment.values();
	
	public final float catchModifier;
	private final String statusAttain, statusAffect, statusRemove;
	private final PersistentEffect effect;
	
	StatusAilment(float catchModifier, String statusAttain, String statusAffect, String statusRemove) {
		this(catchModifier, statusAttain, statusAffect, statusRemove, null);
	}
	StatusAilment(float catchModifier, String statusAttain, String statusAffect, String statusRemove, PersistentEffect effect) {
		this.catchModifier = catchModifier;
		this.statusAttain = statusAttain;
		this.statusAffect = statusAffect;
		this.statusRemove = statusRemove;
		this.effect = effect;
	}
	
	public StatusEffect getEffect() { return getEffect(true); }
	public StatusEffect getEffect(boolean onEnemy) {
		return StatusEffect.get(onEnemy, this);
	}
	
	private boolean tryApplyStatus(BattlePokemon pokemon) {
		if(pokemon.hasFlag(Flag.STATUS_EFFECT))
			return false;
		pokemon.setFlag(Flag.STATUS_EFFECT, this);
		return true;
	}
	
	public EffectResult applyStatus(MoveContext context, BattlePokemon pokemon) {
		if(PERSISTENT_AILMENTS.contains(this)) {
			if(pokemon.hasFlag(Flag.STATUS_EFFECT))
				return EffectResult.FAILURE;
			pokemon.setFlag(Flag.STATUS_EFFECT, this);
		}
		// if(effect != null)
		// 	pokemon.addEffect(effect);
		context.line(pokemon.pokemon.getName()).append(statusAttain);
		return EffectResult.RECORDED;
	}
}
