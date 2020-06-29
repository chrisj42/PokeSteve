package bot.world.pokemon.battle;

public class PlayerBattle extends BattleInstance {
	
	private final UserPlayer firstPlayer;
	private final UserPlayer secondPlayer;
	
	public PlayerBattle(UserPlayer firstPlayer, UserPlayer secondPlayer) {
		super(firstPlayer, secondPlayer);
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
	}
}
