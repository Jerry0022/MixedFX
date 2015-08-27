package de.mixedfx.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;

import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

public class Executor
{
	/**
	 * Runs a file in a separate process maybe with parameter.
	 *
	 * @param fullPath
	 *            Path+file+extension, can be also a cmd command. May contain also parameters
	 * @return Returns true if launched, false if file couldn't be launched / not available
	 */
	public static boolean run(final FileObject fullPath)
	{
		try
		{
			Log.windows.trace("Run following file: " + fullPath.getFullPathWithParameter());
			final List<String> full = new ArrayList<>();
			full.add(fullPath.getFullPath());
			for (final String parameter : fullPath.getParameter())
				full.add(parameter);
			final ProcessBuilder builder = new ProcessBuilder(full.toArray(new String[full.size()]));
			builder.start();
			return true;
		} catch (final IOException e1)
		{
			return false;
		}
	}

	/**
	 * @param command
	 *            To run commands which you would enter in cmd.exe
	 * @return Returns the return of cmd or an empty {@link ComplexString}.
	 */
	public static ComplexString runAndWaitForOutput(final String command)
	{
		Log.windows.trace("Run following command and wait for output: " + command);
		final ComplexString result = new ComplexString();
		try
		{
			final Process process = Runtime.getRuntime().exec(command);
			final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String text;
			while ((text = in.readLine()) != null)
			{
				result.add(text);
			}
			in.close();
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * To run commands which you would enter in cmd.exe
	 *
	 * @param commands
	 *            To run commands which you would enter in cmd.exe
	 * @return Returns the return of cmd or an empty {@link ComplexString}.
	 */
	public static ComplexString runAndWaitForOutput(final String[] commands)
	{
		Log.windows.trace("Run following command and wait for output: " + Arrays.asList(commands));
		final ComplexString result = new ComplexString();
		try
		{
			final Process process = Runtime.getRuntime().exec(commands);
			final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String text;
			while ((text = in.readLine()) != null)
			{
				result.add(text);
			}
			in.close();
		} catch (final Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static void runAsAdministrator(final String command, final String args)
	{
		final Shell32X.SHELLEXECUTEINFO execInfo = new Shell32X.SHELLEXECUTEINFO();
		execInfo.lpFile = new WString(command);
		if (args != null)
			execInfo.lpParameters = new WString(args);
		execInfo.nShow = Shell32X.SW_SHOWDEFAULT;
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
}
