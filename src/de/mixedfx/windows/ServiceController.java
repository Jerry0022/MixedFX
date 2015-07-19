package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;

public class ServiceController
{
	public static void runService(final Program program)
	{
		Executor.runAndWaitForOutput("sc start " + program.serviceName);
		while (!ServiceController.isServiceRunning(program))
		{
			;
		}
	}

	public static void stopService(final Program program)
	{
		Executor.runAndWaitForOutput("sc stop " + program.serviceName);
		while (ServiceController.isServiceRunning(program))
		{
			;
		}
	}

	public static boolean isServiceRunning(final Program program)
	{
		final ComplexString result = Executor.runAndWaitForOutput("sc query " + program.serviceName);

		try
		{
			return result.containsAllRows("STATE", "RUNNING", "4");
		}
		catch (final Exception e)
		{
			return false;
		}
	}
}
