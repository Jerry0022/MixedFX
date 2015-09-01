package de.mixedfx.logging;

import java.io.PrintStream;

public class CustomSysOutErr {
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
	 *            If true System.out and System.err are redirected to Apache's Log4J. Otherwise default console output will be used!
	 */
	public static void init(boolean useLog4J) {
		new CustomSysOutErr(useLog4J);
	}

	private CustomSysOutErr(boolean useLog4J) {
		System.setOut(new CustomSysOut(useLog4J));
		System.setErr(new CustomSysErr(useLog4J));
	}

	private class CustomSysErr extends PrintStream {
		private boolean useLog4J = false;

		public CustomSysErr(boolean useLog4J) {
			super(System.err);
			this.useLog4J = useLog4J;
		}

		@Override
		public void print(final String s) {
			if (useLog4J)
				Log.DEFAULT.fatal(s);
			else {
				final StackTraceElement se = Thread.currentThread().getStackTrace()[3];
				final StringBuilder sb = new StringBuilder();
				sb.append("(").append(se.getFileName()).append(":").append(se.getLineNumber()).append(")");
				sb.setLength(40);
				sb.append(": ").append(s);
				super.print(sb.toString());
			}
		}
	}

	private class CustomSysOut extends PrintStream {
		private boolean useLog4J = false;

		public CustomSysOut(boolean useLog4J) {
			super(System.out);
			this.useLog4J = useLog4J;
		}

		@Override
		public void print(final String s) {
			if (useLog4J) {
				Log.DEFAULT.debug(s);
			} else {
				final StackTraceElement se = Thread.currentThread().getStackTrace()[3];
				final StringBuilder sb = new StringBuilder();
				sb.append("(").append(se.getFileName()).append(":").append(se.getLineNumber()).append(")");
				sb.setLength(40);
				sb.append(": ").append(s);
				super.print(sb.toString());
			}
		}
	}

}
