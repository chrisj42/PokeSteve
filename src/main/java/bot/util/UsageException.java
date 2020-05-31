package bot.util;

// caught by main command executor, which sends the error message as a discord message.
// meant to be user friendly messages.
public class UsageException extends RuntimeException {
	
	public UsageException(String message) {
		super(message);
	}
	
	public UsageException(Throwable cause) {
		this(cause.getMessage(), cause);
	}
	
	public UsageException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
