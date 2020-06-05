package bot.pokemon.move;

public enum MoveLearnMethod {
	
	// I'm not going to simulate all these ways so light ball will probably be a random chance or purchasable for breeding, and machine/tutor will both be made purchasable.
	LevelUp, Egg, Machine, LightBallEgg;
	
	public static MoveLearnMethod getLearnMethod(String name) {
		if(name == null) return null;
		switch(name) {
			case "level-up": return LevelUp;
			case "egg": return Egg;
			case "light-ball-egg": return LightBallEgg;
			default: return Machine;
		}
	}
}
