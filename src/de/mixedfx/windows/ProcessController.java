package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;

public class ProcessController
{
	public static boolean isProcessRunning(final Program program)
	{
		final String[] commands = { "WMIC", "process", "list", "brief" };

		final ComplexString complexString = Executor.runAndWaitForOutput(commands);

		for (final String s : complexString)
		{
			if (s.toUpperCase().contains(program.processName.toUpperCase()))
			{
				return true;
			}
		}
		return false;
	}

	public static void runProcess(final Program program)
	{
		Executor.runAndWaitForOutput("wmic process call create \"" + program.path.setName(program.processName).getFullPath() + "\"");
		// Executor.run(program, "");
		while (!ProcessController.isProcessRunning(program))
		{
			;
		}
	}

	/**
	 * Stops ALL process with the processName!
	 *
	 * @param processName
	 *            E. g. "xxx.exe" The name of the process plus the extension
	 */
	public static void stopProcess(final Program program)
	{
		Executor.runAndWaitForOutput("taskkill /F /IM " + program.processName);
		while (ProcessController.isProcessRunning(program))
		{
			;
		}
	}
}
