package de.mixedfx.java;

import java.io.PrintStream;

public class CustomSysOutErr
{
	public static void init()
	{
		new CustomSysOutErr();
	}

	private CustomSysOutErr()
	{
		System.setOut(new CustomSysOut());
		System.setErr(new CustomSysErr());
	}

	private class CustomSysErr extends PrintStream
	{
		public CustomSysErr()
		{
			super(System.err);
		}

		@Override
		public void print(final String s)
		{
			final StackTraceElement se = Thread.currentThread().getStackTrace()[3];
			final StringBuilder sb = new StringBuilder();
			sb.append("(").append("Error: ").append(se.getFileName()).append(":").append(se.getLineNumber()).append(")");
			sb.setLength(40);
			sb.append(": ").append(s);
			super.print(sb.toString());
		}
	}

	private class CustomSysOut extends PrintStream
	{
		public CustomSysOut()
		{
			super(System.out);
		}

		@Override
		public void print(final String s)
		{
			final StackTraceElement se = Thread.currentThread().getStackTrace()[3];
			final StringBuilder sb = new StringBuilder();
			sb.append("(").append(se.getFileName()).append(":").append(se.getLineNumber()).append(")");
			sb.setLength(40);
			sb.append(": ").append(s);
			super.print(sb.toString());
		}
	}

}
