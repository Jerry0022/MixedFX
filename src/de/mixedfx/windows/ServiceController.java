package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

public class ServiceController
{

	public static boolean isRunning(final Program service)
	{
		final ComplexString result = Executor.runAndWaitForOutput("sc query " + service.serviceName);

		try
		{
			Log.windows.debug("Process " + service.processName + " is " + (result.containsAllRows("STATE", "RUNNING", "4")?"enabled!":"disabled!"));
			return result.containsAllRows("STATE", "RUNNING", "4");
		}
		catch (final Exception e)
		{
			Log.windows.debug("Service " + service.processName + " status couldn't be determined!");
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
		Log.windows.debug("Service " + service.processName + " was started!");
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
		Log.windows.debug("Service " + service.processName + " was stopped!");
	}
}
