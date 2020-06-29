package bot.command;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;

public interface AuthCheck {
	boolean isAuthorized(User user);
}
