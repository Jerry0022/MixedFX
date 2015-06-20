package de.mixedfx.logging;

import java.net.URISyntaxException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * Turn all Loggers on / off by calling {@link #turnAllOn()} or {@link #turnAllOff()}! Turn the
 * Logger off by calling {@link Logger#setLevel(Level)} with {@link Level#OFF}! See Level logic
 * here: <a href="http://www.tutorialspoint.com/log4j/log4j_logging_levels.htm"
 * >http://www.tutorialspoint.com/log4j/log4j_logging_levels.htm</a>
 *
 * @author Jerry
 */
public class Log
{
	public static final LoggerContext	CONTEXT;
	public static final Logger			network;

	static
	{
		CustomSysOutErr.init();
		CONTEXT = new LoggerContext("MixedFX");
		try
		{
			Log.CONTEXT.setConfigLocation(Log.class.getResource("log4j.xml").toURI());
		}
		catch (final URISyntaxException e)
		{}
		network = Log.CONTEXT.getLogger("Network");
	}

	public void turnAllOn()
	{
		for (final Logger l : Log.CONTEXT.getLoggers())
		{
			l.setLevel(Level.ALL);
		}
	}

	public void turnAllOff()
	{
		for (final Logger l : Log.CONTEXT.getLoggers())
		{
			l.setLevel(Level.OFF);
		}
	}
}