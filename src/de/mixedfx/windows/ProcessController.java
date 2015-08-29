package de.mixedfx.windows;

import java.io.FileNotFoundException;

import de.mixedfx.java.ComplexString;
import de.mixedfx.java.TimeoutController;
import de.mixedfx.java.TimeoutController.TimeoutException;
import de.mixedfx.logging.Log;

public class ProcessController
{
	public static boolean isProcessRunning(final Program program)
	{
		final String[] commands = { "WMIC", "process", "list", "brief" };

		final ComplexString complexString = Executor.runAndWaitForOutput(commands, MasterController.TIMEOUT);

		final boolean result = complexString.containsOneRow("", program.processName);
		Log.windows.debug("Process " + program.processName + " is " + (result ? "enabled" : "disabled") + "!");
		return result;
	}

	public static void run(final Program program) throws FileNotFoundException, TimeoutException
	{
		if (!program.fullPath.toFile().exists())
			throw new FileNotFoundException("File not found: " + program.fullPath.toString());
		if (ProcessController.isProcessRunning(program))
			return;

		Executor.runAsAdmin(program.fullPath, true);
		TimeoutController.execute(() ->
		{
			try
			{
				while (!ProcessController.isProcessRunning(program))
				{
					System.out.println(Thread.currentThread().isInterrupted());
					if (Thread.currentThread().isInterrupted())
						break;
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}

		} , MasterController.TIMEOUT);
		Log.windows.debug("Program " + program.programName + " was started! Fullpath with parameters: " + program.fullPath);
	}

	/**
	 * Stops ALL process with the processName!
	 *
	 * @param program
	 *            E. g. "xxx.exe" The name of the process plus the extension
	 * @throws TimeoutException
	 * @throws FileNotFoundException
	 */
	public static void stop(final Program program) throws TimeoutException
	{
		if (!ProcessController.isProcessRunning(program))
			return;

		Executor.runAsAdmin("taskkill", "/F /IM " + program.processName, false);
		TimeoutController.execute(() ->
		{
			while (ProcessController.isProcessRunning(program))
				;
		} , MasterController.TIMEOUT);
		Log.windows.debug("Program " + program.programName + " was stopped!");
	}
}
