package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;

public class ServiceController
{

	public static boolean isRunning(final Program service)
	{
		final ComplexString result = Executor.runAndWaitForOutput("sc query " + service.serviceName);

		try
		{
			return result.containsAllRows("STATE", "RUNNING", "4");
		}
		catch (final Exception e)
		{
			return false;
		}
	}

	public static void run(final Program service)
	{
		if (ServiceController.isRunning(service))
			return;
		Executor.runAndWaitForOutput("sc start " + service.serviceName);
		while (!ServiceController.isRunning(service))
		{
			;
		}
	}

	public static void stop(final Program service)
	{
		if (!ServiceController.isRunning(service))
			return;
		Executor.runAndWaitForOutput("sc stop " + service.serviceName);
		while (ServiceController.isRunning(service))
		{
			;
		}
	}
}
