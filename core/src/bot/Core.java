package bot;

import java.io.IOException;
import java.util.HashMap;

import bot.command.Command;
import bot.command.CommandContext;
import bot.command.CommandSet;
import bot.io.DataFile;
import bot.io.json.MissingPropertyException;
import bot.pokemon.DataCore;
import bot.util.UsageException;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel.Type;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Core {
	
	/*
		something to keep in mind: I definitely want to maintain relative order of messages in the same DM, but across multiple it shouldn't matter (until servers perhaps)
	 */
	
	private Core() {}
	
	public static final ObjectMapper jsonMapper = new ObjectMapper();
	
	static {
		DataCore.init();
	}
	
	/*private static final HashMap<String, BiPredicate<String, Member>> prefixMap = new HashMap<>(3);
	private static void addPrefix(String prefix, Predicate<Member> userCheck) {
		Predicate<String> regexTest = Pattern.compile("^"+prefix).asPredicate();
		prefixMap.put(prefix, (str, member) -> regexTest.test(str) && userCheck.test(member));
	}
	static {
		addPrefix("!ps", Utils::matchBot);
	}*/
	
	public static DiscordClient client;
	public static GatewayDiscordClient gateway;
	public static BotData data;
	
	public static CommandSet getRootCommands(User user) {
		return UserState.getState(user).commands;
	}
	
	// private static final Map<Snowflake, SyncQueue<MessageCreateEvent>> waitingMessages = Collections.synchronizedMap(new HashMap<>());
	
	public static void main(String[] args) throws IOException, MissingPropertyException {
		// ReadOnlyJsonTraversal auth = new ReadOnlyJsonTraversal(DataFile.AUTH);
		// final String token = auth.getPropertyValue("token", JsonNode::textValue);
		final String token = DataFile.AUTH.readJson().getValueNode("token").parseValue(JsonNode::textValue);
		
		// read bot config
		data = null;//BotData.load();
		
		// addPrefix(data.prefix, member -> Utils.matchBot(member)/* || Owners.Chris.is(member)*/);
		
		// create client
		client = DiscordClient.create(token);
		gateway = client.login().block();
			// .setInitialPresence(Presence.online(Activity.playing("with pokemans")))
		
		if(gateway == null)
			throw new NullPointerException("gateway is null");
		
		gateway.on(ReadyEvent.class)
			.subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));
		
		gateway.on(MessageCreateEvent.class)
			.flatMap(Core::createContext)
			.flatMap(context -> Mono.just(context).flatMap(Core::parseMessage).onErrorResume(e -> {
				if(e instanceof UsageException)
					return context.channel.createMessage(e.getMessage()).then();
				System.err.println("exception occurred while parsing command "+context);
				e.printStackTrace();
				return Mono.empty();
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
		// System.out.println("DM message");
		
		// return context.channel.createMessage("hello!").then();
		Command cmd = Command.tryParseSubCommand(getRootCommands(context.user), context);
		// System.out.println("basic command: "+cmd);
		if(cmd == null)
			return Mono.empty();
		
		return Mono.just(cmd)
			.flatMap(command -> command.execute(context));
		
		/*final Snowflake uid = author.getId();
		SyncQueue<MessageCreateEvent> messageQueue;
		synchronized (waitingMessages) {
			messageQueue = waitingMessages.computeIfAbsent(uid, key -> new SyncQueue<>());
		}*/
		
		/*
			first entry in queue is current command
			if the message queue is empty, then add it and process
		 */
		
		// if(messageQueue.queueCheckEmpty(e))
		// 	parseMessage(e, author, messageQueue);
		// otherwise added to queue
	}
	
	/*private static void parseMessage(MessageCreateEvent e, User author, SyncQueue<MessageCreateEvent> messageQueue) {
		e.getMessage().getChannel()
			.flatMap(channel -> {
				tryParseMessage(e, channel, author);
				// check for queued messages
				MessageCreateEvent next = messageQueue.nextInQueue();
				return next == null ? Mono.empty() : Mono.just(next);
			})
			.subscribe(messageEvent -> parseMessage(messageEvent, author, messageQueue));
	}*/
	
	// -> first test if message events will overlap if each one takes a while; I assume they will in order to serve multiple people at once. But probably at certain boundaries, like the various filters and maps. 
	
	/*private static Mono<Void> tryParseMessage(MessageCreateEvent e, MessageChannel channel, User author) {
		if(channel.getType() != Type.DM) {
			// channel.createMessage("DMs are the only way to communicate at the moment.").then();
			return Mono.empty();
		}
		
		// final User author = e.getMessage().getAuthor().orElse(null);
		System.out.println("message received in DM with "+author);
		if(author == null)
			return Mono.empty();
		
		final String content = e.getMessage().getContent();
		final MessageContext context = new MessageContext(e, author, channel, content);
		return Command.tryParseSubcommand(rootCommands, context);
	}*/
	
	/*private static void tryParseCommand(final MessageCreateEvent e, final MessageChannel channel, final User user, String text) {
		return Mono.fromCallable(() -> {
			if(user.isBot())
				return null;
			
			for(Map.Entry<String, BiPredicate<String, Member>> entry: prefixMap.entrySet()) {
				if(entry.getValue().test(text, user)) {
					return text.replaceFirst(entry.getKey(), "");
				}
			}
			return null;
		}).filter(Objects::nonNull).flatMap(content ->
			// parse arguments and quotes
			CommandContext.createContext(e, user).flatMap(context -> {
				parseArguments(content, context);
				return Command.tryParseSubcommand(rootCommands, context);
			})
		).filter(Boolean::booleanValue).hasElement();
	}*/
}
