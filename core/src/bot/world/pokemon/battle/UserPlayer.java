package bot.world.pokemon.battle;

import bot.data.UserData;
import bot.world.pokemon.Pokemon;
import bot.world.pokemon.Pokemon.CaughtPokemon;
import bot.world.pokemon.battle.BattleInstance.Player;

import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class UserPlayer extends Player {
	public final MessageChannel channel;
	public final User user;
	public final UserData data;
	
	private final CaughtPokemon caughtPokemon;
	
	public UserPlayer(MessageChannel channel, UserData userData, CaughtPokemon pokemon) {
		super(pokemon);
		this.channel = channel;
		this.user = userData.getUser();
		this.data = userData;
		this.caughtPokemon = pokemon;
	}
	
	@Override
	Mono<Void> onFinish(BattleResult result) {
		data.onBattleEnd(result);
		return caughtPokemon.addExp(channel)
			.flatMap(change -> {
				if(change) {
					CaughtPokemon pokemon = caughtPokemon;
					CaughtPokemon evolved = data.checkEvolution(pokemon);
					if(evolved == null)
						data.save(); // checkEvolution only saves on success
					else
						return channel.createEmbed(e -> e
							.setTitle(pokemon.getName()+" is evolving!")
							.setDescription(pokemon.getName()+" evolved into "+evolved.species.name+"!\nType `pokemon info` to see the new moves and stats!")
							.setThumbnail(pokemon.species.getSpritePath())
							.setImage(evolved.species.getSpritePath())
						).then();
				}
				return Mono.empty();
			});
	}
	
	@Override
	public String getPlayerName() { return user.getUsername(); }
}
