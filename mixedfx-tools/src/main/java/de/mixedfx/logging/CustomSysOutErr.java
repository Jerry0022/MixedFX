package de.mixedfx.logging;

import java.io.PrintStream;

import org.apache.logging.log4j.core.Logger;

public class CustomSysOutErr {
    private static Logger logger = Log.CONTEXT.getLogger("stdout");

    /**
     * Use logging via default System.out and System.err!
     */
    public static void init() {
	init(false);
    }

    /**
     * Uses logging via default System.out and System.err or Apache's Log4J!
     *
     * @param useLog4J
     *            If true System.out and System.err are redirected to Apache's
     *            Log4J. Otherwise default console output will be used!
     */
    public static void init(boolean useLog4J) {
	new CustomSysOutErr(useLog4J);
    }

    private CustomSysOutErr(boolean useLog4J) {
	System.setOut(new CustomSysOut(useLog4J));
	System.setErr(new CustomSysErr(useLog4J));
    }

    private class CustomSysErr extends PrintStream {

	public CustomSysErr(boolean useLog4J) {
	    super(System.err);
	}

	@Override
	public void print(final String s) {
	    final StackTraceElement se = Thread.currentThread().getStackTrace()[3];
	    final StringBuilder sb = new StringBuilder();
	    sb.append("(").append(se.getFileName()).append(":").append(se.getLineNumber()).append(")");
	    sb.setLength(40);
	    sb.append(": ").append(s);
	    logger.debug(sb.toString());
	}
    }

    private class CustomSysOut extends PrintStream {

	public CustomSysOut(boolean useLog4J) {
	    super(System.out);
	}

	@Override
	public void print(final String s) {
	    final StackTraceElement se = Thread.currentThread().getStackTrace()[3];
	    final StringBuilder sb = new StringBuilder();
	    sb.append("(").append(se.getFileName()).append(":").append(se.getLineNumber()).append(")");
	    sb.setLength(40);
	    sb.append(": ").append(s);
	    logger.debug(sb.toString());
	}
    }

}
