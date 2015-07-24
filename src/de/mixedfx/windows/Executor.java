package de.mixedfx.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;

public class Executor
{
	/**
	 * @param command
	 *            To run commands which you would enter in cmd.exe
	 * @return Returns the return of cmd or an empty {@link ComplexString}.
	 */
	public static ComplexString runAndWaitForOutput(final String command)
	{
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
		}
		catch (final Exception e)
		{}
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
		}
		catch (final Exception e)
		{}
		return result;
	}

	/**
	 * Runs a file in a separate process.
	 *
	 * @param fullPath
	 *            Path+file+extension, can be also a cmd command
	 * @return Returns true if launched, false if file couldn't be launched / not available
	 */
	public static boolean run(final FileObject fullPath, final String parameter)
	{
		try
		{
			Runtime.getRuntime().exec(fullPath.getFullPath().concat(" " + parameter));
			return true;
		}
		catch (final IOException e1)
		{
			return false;
		}
	}
}