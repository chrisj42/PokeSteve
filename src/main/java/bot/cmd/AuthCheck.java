package bot.cmd;

import discord4j.common.util.Snowflake;

public interface AuthCheck {
	boolean isAuthorized(Snowflake id);
}
