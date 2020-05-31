package bot.io.json;

import java.io.File;

public class MissingPropertyException extends Exception {
	public MissingPropertyException(File file, String message) {
		super(String.format(
			"Error reading data file '%s': %s", file.getName(), message)
		);
	}
	public MissingPropertyException(String message) {
		super(message);
	}
}
