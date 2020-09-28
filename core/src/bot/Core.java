package bot;

import java.io.IOException;
import java.util.HashSet;

import bot.command.Command;
import bot.command.CommandContext;
import bot.command.RootCommands;
import bot.data.DataCore;
import bot.data.DataFile;
import bot.data.UserData;
import bot.data.json.MissingPropertyException;
import bot.util.UsageException;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel.Type;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Core {
	/*
		something to keep in mind: I definitely want to maintain relative order of messages in the same DM, but across multiple it shouldn't matter (until servers perhaps)
	 */
	
	private Core() {}
	
	public static final ObjectMapper jsonMapper = new ObjectMapper();
	
	public static final Snowflake devId = Snowflake.of("281644307553845248");
	
	static {
		DataCore.init();
		System.gc(); // lots of objects are created in all the various builders; ensure that the setup objects have all been thrown away
	}
	
	public static DiscordClient client;
	public static GatewayDiscordClient gateway;
	public static User self;
	
	// used for tasks where the user must wait for something to occur, and during that time,
	// commands should not be processed.
	private static final HashSet<Snowflake> USERS_WAITING = new HashSet<>();
	public static boolean isUserWaiting(Snowflake id) {
		return USERS_WAITING.contains(id);
	}
	public static void setUserWaiting(Snowflake id, boolean waiting) {
		if(waiting) USERS_WAITING.add(id);
		else USERS_WAITING.remove(id);
	}
	
	public static void main(String[] args) throws IOException, MissingPropertyException {
		final String token = DataFile.AUTH.readJson().getValueNode("token").parseValue(JsonNode::textValue);
		
		// create client
		System.out.println("creating client...");
		client = DiscordClient.create(token);
		System.out.println("logging in...");
		gateway = client.login().block();
		
		if(gateway == null)
			throw new NullPointerException("gateway is null");
		
		UserData.load();
		
		gateway.on(ReadyEvent.class)
			.subscribe(ready -> {
				Core.self = ready.getSelf();
				System.out.println("Logged in as " + self.getUsername());
				gateway.updatePresence(Presence.online(Activity.playing("with pokemans")))
					.then(Mono.fromRunnable(() -> System.out.println("presence updated")))
					.subscribe();
			});
		
		gateway.on(MessageCreateEvent.class)
			.flatMap(Core::createContext)
			.flatMap(context -> Mono.just(context).flatMap(Core::parseMessage).onErrorResume(e -> {
				if(e instanceof UsageException)
					return context.channel.createMessage(e.getMessage()).then();
				System.err.println("exception occurred while parsing command "+context);
				e.printStackTrace();
				return context.channel.createMessage("internal error.").then();
			}))
			.subscribe();
		
		gateway.onDisconnect().block();
	}
	
	private static Mono<CommandContext> createContext(MessageCreateEvent e) {
		User author = e.getMessage().getAuthor().orElse(null);
		if(author == null || author.isBot()) return Mono.empty();
		
		return e.getMessage().getChannel()
			.filter(channel -> channel.getType() == Type.DM)
			.map(channel -> new CommandContext(e, author, channel, e.getMessage().getContent()));
	}
	
	// pretty sure it only runs one of these at a time i.e. there is only actually one thread for the event dispatcher
	private static Mono<Void> parseMessage(CommandContext context) {
		
		if(isUserWaiting(context.user.getId()))
			// leave a reaction to show that the message was not and will not be processed
			return context.event.getMessage().addReaction(ReactionEmoji.unicode("⏱"));
		
		Command cmd = Command.tryParseSubCommand(RootCommands.getCommandsFor(context.user), context);
		if(cmd == null)
			return Mono.empty();
		
		return Mono.just(cmd)
			.flatMap(command -> command.execute(context));
	}
}
