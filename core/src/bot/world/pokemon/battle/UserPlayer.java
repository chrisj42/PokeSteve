package bot.world.pokemon.battle;

import bot.data.UserData;
import bot.world.pokemon.Pokemon;
import bot.world.pokemon.battle.BattleInstance.Player;

import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class UserPlayer extends Player {
	public final MessageChannel channel;
	public final User user;
	public final UserData data;
	
	public UserPlayer(MessageChannel channel, UserData userData, Pokemon pokemon) {
		super(userData.getUser().getUsername(), pokemon);
		this.channel = channel;
		this.user = userData.getUser();
		this.data = userData;
	}
	
	@Override
	Mono<Void> onFinish(BattleResult result) {
		data.onBattleEnd(result);
		return pokemon.pokemon.addExp(channel)
			.flatMap(change -> {
				if(change) data.save();
				return Mono.empty();
			});
	}
	
	@Override
	public String getDescriptor() {
		return "("+user.getUsername()+") "+super.getDescriptor();
	}
}
