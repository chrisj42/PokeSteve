package bot.command;

import java.util.LinkedList;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

public class CommandContext {
	
	public final MessageCreateEvent event;
	public final User user;
	public final MessageChannel channel;
	
	private final LinkedList<String> args;
	private int curArg = 0;
	
	public CommandContext(MessageCreateEvent e, User user, MessageChannel channel, String text) {
		this.event = e;
		this.user = user;
		this.channel = channel;
		
		this.args = parseArguments(text);
		System.out.println("from user "+user.getUsername()+": args: "+args);
	}
	
	String nextArgument() {
		return curArg < args.size() ? args.get(curArg++) : null;
	}
	
	void rewindArgument() {
		if(curArg > 0) curArg--;
	}
	
	/*public String getCallString() {
		return String.join(" ", args.subList(0, curArg));
	}*/
	
	public List<String> getRemainingArgs() {
		return args.subList(curArg, args.size());
	}
	
	private static LinkedList<String> parseArguments(String text) {
		final LinkedList<String> args = new LinkedList<>();
		
		var cur = new StringBuilder();
		var quote = false;
		var skip = false;
		for(var i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if(c == '"' && !skip) {
				// quote behavior
				if(quote) {
					// end the quote
					quote = false;
					// push regardless of size
					args.add(cur.toString());
					cur = new StringBuilder();
				} else {
					// start the quote
					quote = true;
					// separate any previous text in the word
					if(cur.length() > 0) {
						args.add(cur.toString());
						cur = new StringBuilder();
					}
				}
			}
			else if(c == '\\' && !skip && i < text.length()-1 && (text.charAt(i+1) == '\\' || text.charAt(i+1) == '"')) {
				// considered only sometimes removing the backslash, but it's probably best to be consistent.
				// changed my mind, the use case is likely to be more often backslashes of other things that the bot shouldn't parse; the bot will only parse \\ and \" into \ and ", all other \X will be left alone.
				skip = true;
			}
			else if((c == ' ' || c == '\n') && !skip && !quote) {
				if(cur.length() > 0) {
					args.add(cur.toString());
					cur = new StringBuilder();
				}
			}
			else {
				cur.append(c);
				skip = false;
			}
		}
		
		// add any extra
		if(cur.length() > 0)
			args.add(cur.toString());
		
		return args;
	}
	
	@Override
	public String toString() {
		return String.format("CommandContext{user=%s#%s(%s), message=\"%s\", parsedMessage=%s, curArg=%s}", user.getUsername(), user.getTag(), user.getId(), event.getMessage().getContent(), args, curArg);
	}
}
