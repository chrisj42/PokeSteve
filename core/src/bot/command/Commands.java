package bot.command;

import bot.command.group.battle.AttackCommand;
import bot.command.group.battle.FleeCommand;
import bot.command.group.info.DexCommand;
import bot.command.group.info.MoveCommand;
import bot.command.group.world.AcceptCommand;
import bot.command.group.system.HelpCommand;
import bot.command.group.world.RejectCommand;
import bot.command.group.world.BattleCommand;
import bot.command.group.world.DuelCommand;
import bot.command.group.world.SpawnCommand;

public class Commands {
	
	public static final Command HELP = new HelpCommand();
	public static final Command DEX = new DexCommand();
	public static final Command MOVE = new MoveCommand();
	
	public static final Command ACCEPT = new AcceptCommand();
	public static final Command REJECT = new RejectCommand();
	
	public static final Command SPAWN = new SpawnCommand();
	public static final Command BATTLE = new BattleCommand();
	public static final Command DUEL = new DuelCommand();
	public static final Command ATTACK = new AttackCommand();
	public static final Command FLEE = new FleeCommand();
	
	private Commands() {}
	
}
