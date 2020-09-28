package bot.world.pokemon.battle.status;

import bot.world.pokemon.battle.MoveContext;
import bot.world.pokemon.move.StatusEffect;

public enum StatusAilment {
	Paralysis(1.5f, "was paralyzed!", "is paralyzed! It can't move!", "is no longer paralyzed."),
	Sleep(2, "fell asleep!", "is fast asleep.", "woke up!"),
	Freeze(2, "has been frozen!", "is frozen solid!", "thawed out."),
	Burn(1.5f, "was burned!", "is hurt by its burn!", "is no longer on fire :D"),
	Poison(1.5f, "was poisoned!", "is hurt by the poison!", "is no longer poisoned."),
	Bad_Poison(1.6f, "was badly poisoned!", "is hurt by the poison!", "is no longer poisoned."),
	Confusion("became confused!", "is confused!", "snapped out of its confusion.");
	// most effects past here will be relocated / turned into flags
	// Infatuation, Trap, Nightmare, Torment, Disable, Yawn,
	// HealBlock, NoTypeImmunity, LeechSeed, Embargo, PerishSong,
	// Ingrain, Silence;
	
	public static final StatusAilment[] values = StatusAilment.values();
	
	public final float catchModifier;
	public final String statusAttain, statusAffect, statusRemove;
	
	// this constructor will be phased out
	// StatusAilment() { this(null, null, null); }
	
	StatusAilment(String statusAttain, String statusAffect, String statusRemove) {
		this(1, statusAttain, statusAffect, statusRemove);
	}
	StatusAilment(float catchModifier, String statusAttain, String statusAffect, String statusRemove) {
		this.catchModifier = catchModifier;
		this.statusAttain = statusAttain;
		this.statusAffect = statusAffect;
		this.statusRemove = statusRemove;
	}
	
	public StatusEffect getEffect() { return getEffect(true); }
	public StatusEffect getEffect(boolean onEnemy) {
		return StatusEffect.get(onEnemy, this);
	}
}
