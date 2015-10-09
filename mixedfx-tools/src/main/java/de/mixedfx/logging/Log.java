package de.mixedfx.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;

/**
 * Turn all Loggers on / off by calling {@link #turnAllOn()} or
 * {@link #turnAllOff()}! Turn the Logger off by calling
 * {@link Logger#setLevel(Level)} with {@link Level#OFF}! See Level logic here:
 * <a href="http://www.tutorialspoint.com/log4j/log4j_logging_levels.htm" >http:
 * //www.tutorialspoint.com/log4j/log4j_logging_levels.htm</a>
 *
 * @author Jerry
 */
public class Log {
    public static final LoggerContext CONTEXT;
    public static final Logger DEFAULT;
    public static final Logger network;
    public static final Logger assets;
    public static final Logger windows;
    public static final Logger textAndSpeech;

    static {
	CONTEXT = new LoggerContext("MixedFX");
	try {
	    Log.CONTEXT.setConfigLocation(Log.class.getResource("log4j.xml").toURI());
	} catch (final URISyntaxException e) {
	}
	DEFAULT = Log.CONTEXT.getLogger("Default");
	network = Log.CONTEXT.getLogger("Network");
	assets = Log.CONTEXT.getLogger("Assets");
	windows = Log.CONTEXT.getLogger("Windows");
	textAndSpeech = Log.CONTEXT.getLogger("TextAndSpeech");
	CustomSysOutErr.init(true);
    }

    @Produces
    public Logger loggerProducer(InjectionPoint point) {
	if (point.getAnnotated().isAnnotationPresent(LoggerTopic.class)) {
	    for (Annotation annotation : point.getAnnotated().getAnnotations()) {
		if (annotation instanceof LoggerTopic) {
		    return CONTEXT.getLogger(((LoggerTopic) annotation).name());
		}
	    }
	}

	return CONTEXT.getLogger(point.getBean().getBeanClass().getPackage().getName());
    }

    public static void turnAllOn() {
	for (final Logger l : Log.CONTEXT.getLoggers()) {
	    l.setLevel(Level.ALL);
	}
    }

    public static void turnAllOff() {
	for (final Logger l : Log.CONTEXT.getLoggers()) {
	    l.setLevel(Level.OFF);
	}
    }
}