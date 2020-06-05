package bot.pokemon.battle;

public class PlayerBattle extends BattleInstance {
	
	private final UserPlayer firstPlayer;
	private final UserPlayer secondPlayer;
	
	public PlayerBattle(UserPlayer firstPlayer, UserPlayer secondPlayer) {
		super(player1, player2);
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
	}
}
