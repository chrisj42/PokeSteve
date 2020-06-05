package bot.pokemon.battle;

import bot.pokemon.Pokemon;
import bot.pokemon.battle.BattleInstance.Player;

import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class UserPlayer extends Player {
	public final MessageChannel channel;
	public final User user;
	public final BattlePokemon pokemon;
	
	public UserPlayer(MessageChannel channel, User user, Pokemon pokemon) {
		super(user.getUsername());
		this.channel = channel;
		this.user = user;
		this.pokemon = new BattlePokemon(pokemon);
	}
}
