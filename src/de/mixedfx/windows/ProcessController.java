package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

public class ProcessController {
	public static boolean isProcessRunning(final Program process) {
		final String[] commands = { "WMIC", "process", "list", "brief" };

		final ComplexString complexString = Executor.runAndWaitForOutput(commands);
		for (final String s : complexString) {
			if (s.toUpperCase().contains(process.processName.toUpperCase())) {
				Log.windows.debug("Process " + process.processName + " is enabled!");
				return true;
			}
		}
		Log.windows.debug("Process " + process.processName + " is disabled! Fullpath: " + process.fullPath.getFullPath());
		return false;
	}

	public static void run(final Program process) {
		if (ProcessController.isProcessRunning(process))
			return;
		if (process.fullPath.getParameter().length == 0)
			Executor.runAndWaitForOutput("wmic process call create \"" + process.fullPath.getFullPathWithParameter() + "\"");
		else
			Executor.run(process.fullPath);
		while (!ProcessController.isProcessRunning(process)) {
			;
		}
		Log.windows.debug("Process " + process.processName + " was started! Fullpath with parameters: " + process.fullPath.getFullPathWithParameter());
	}

	/**
	 * Stops ALL process with the processName!
	 *
	 * @param process
	 *            E. g. "xxx.exe" The name of the process plus the extension
	 */
	public static void stop(final Program process) {
		if (!ProcessController.isProcessRunning(process))
			return;
		Executor.runAndWaitForOutput("taskkill /F /IM " + process.processName);
		while (ProcessController.isProcessRunning(process)) {
			;
		}
		Log.windows.debug("Process " + process.processName + " was stopped!");
	}
}
