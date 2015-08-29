package de.mixedfx.windows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;

import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

public class Executor
{
	/**
	 * @param command
	 *            To run commands which you would enter in cmd.exe
	 * @return Returns the print outs of cmd or an empty {@link ComplexString}.
	 */
	public static ComplexString runAndWaitForOutput(final String command, final long timout)
	{
		return Executor.start(new String[] { command }, timout);
	}

	/**
	 * To run a batch of commands which you would enter in cmd.exe
	 *
	 * @param commands
	 *            To run commands which you would enter in cmd.exe
	 * @return Returns the return of cmd or an empty {@link ComplexString}.
	 */
	public static ComplexString runAndWaitForOutput(final String[] commands, final long timout)
	{
		return Executor.start(commands, timout);
	}

	/**
	 * @param program
	 *            Fullpath with parameters!
	 * @param show
	 *            If program shall be hidden.
	 */
	public static void runAsAdmin(final FileObject program, final boolean show)
	{
		String parameters = "";
		for (final String param : program.getParameter())
			parameters += param + " ";
		Executor.runAsAdmin("\"" + program.getFullPath() + "\"", parameters, show);
	}

	public static void runAsAdmin(final String file, final boolean show)
	{
		Executor.runAsAdmin(file, null, show);
	}

	public static void runAsAdmin(final String file, final String args, final boolean show)
	{
		final Shell32X.SHELLEXECUTEINFO execInfo = new Shell32X.SHELLEXECUTEINFO();
		execInfo.lpFile = new WString(file);
		if (args != null)
			execInfo.lpParameters = new WString(args);
		if (show)
			execInfo.nShow = Shell32X.SW_SHOWDEFAULT;
		else
			execInfo.nShow = Shell32X.SW_HIDE;
		execInfo.fMask = Shell32X.SEE_MASK_NOCLOSEPROCESS;
		execInfo.lpVerb = new WString("runas");
		final boolean result = Shell32X.INSTANCE.ShellExecuteEx(execInfo);

		if (!result)
		{
			final int lastError = Kernel32.INSTANCE.GetLastError();
			final String errorMessage = Kernel32Util.formatMessageFromLastErrorCode(lastError);
			throw new RuntimeException("Error performing elevation: " + lastError + ": " + errorMessage + " (apperror=" + execInfo.hInstApp + ")");
		}
	}

	public static ComplexString start(final String[] commands, final long timout)
	{
		final Future<ComplexString> future = Executors.newSingleThreadExecutor().submit(() ->
		{
			Log.windows.trace("Run following command and wait for output: " + Arrays.asList(commands));
			final ComplexString result = new ComplexString();
			try
			{
				Process process;
				if (commands.length == 1)
					process = Runtime.getRuntime().exec(commands[0]);
				else
					process = Runtime.getRuntime().exec(commands);
				final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

				String text;
				while ((text = in.readLine()) != null)
				{
					result.add(text);
				}
				in.close();
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			return result;
		});
		try
		{
			if (timout == 0)
				return future.get();
			else
				return future.get(timout, TimeUnit.MILLISECONDS);
		}
		catch (final Exception e)
		{
			Log.windows.error("An error occured executing the following commands: " + new ComplexString(commands));
			future.cancel(true); // this method will stop the running underlying task
			return new ComplexString();
		}
	}
}
