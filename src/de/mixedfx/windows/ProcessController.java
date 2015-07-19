package de.mixedfx.windows;

import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;

public class ProcessController
{
	public static final String	HAMACHIPROCESSNAME	= "Hamachi-2.exe";

	public static void main(final String[] args)
	{
		final String processName = "ts3client_win64.exe";

		System.out.println(ProcessController.isProcessRunning(processName));
		ProcessController.runProcess("C:\\Program Files\\TeamSpeak 3 Client\\", processName);

		// System.out.println(ProcessController.isProcessRunning(processName));
		// ProcessController.stopProcess(processName);
		// System.out.println(ProcessController.isProcessRunning(processName));
	}

	public static boolean isProcessRunning(final String processName)
	{
		final String[] commands = { "WMIC", "process", "list", "brief" };

		final ComplexString complexString = Executor.runAndWaitForOutput(commands);

		for (final String s : complexString)
		{
			if (s.toUpperCase().contains(processName.toUpperCase()))
			{
				return true;
			}
		}
		return false;
	}

	public static void runProcess(final String directory, final String processName)
	{
		final FileObject program = FileObject.create().setPath(directory).setName(processName);
		Executor.runAndWaitForOutput("wmic process call create \"" + program.getFullPath() + "\"");
		// Executor.run(program, "");
		while (!ProcessController.isProcessRunning(processName))
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
	public static void stopProcess(final String processName)
	{
		Executor.runAndWaitForOutput("taskkill /F /IM " + processName);
		while (ProcessController.isProcessRunning(processName))
		{
			;
		}
	}
}
