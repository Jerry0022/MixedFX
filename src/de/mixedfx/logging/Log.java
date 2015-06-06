package de.mixedfx.logging;

import java.net.URISyntaxException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * Turn the Logger off by calling {@link Logger#setLevel(org.apache.logging.log4j.Level))} with
 * {@link Level#OFF}!
 *
 * @author Jerry
 */
public class Log
{
	public static final LoggerContext	CONTEXT;
	public static final Logger			network;

	static
	{
		CONTEXT = new LoggerContext("MixedFX");
		try
		{
			Log.CONTEXT.setConfigLocation(Log.class.getResource("log4j.xml").toURI());
		}
		catch (final URISyntaxException e)
		{}
		network = Log.CONTEXT.getLogger("Network");
	}
}