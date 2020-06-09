package bot.pokemon.move.format;

import bot.pokemon.Type;

public class MoveFormat {
	
	private String name;
	private int id;
	private MoveDescription description;
	private Type type;
	private int pp;
	private int accuracy;
	private int priority;
	
	private MoveEffect[] effects;
	private int drain; // positive is drain, negative is recoil
	private int healing;
	
	public MoveFormat() {}
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public MoveDescription getDescription() {
		return description;
	}
	
	public Type getType() {
		return type;
	}
	
	public int getPp() {
		return pp;
	}
	
	public int getAccuracy() {
		return accuracy;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public MoveEffect[] getEffects() {
		return effects;
	}
}
