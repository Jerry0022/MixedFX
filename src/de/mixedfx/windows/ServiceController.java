package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;
import de.mixedfx.java.TimeoutController;
import de.mixedfx.java.TimeoutController.TimeoutException;
import de.mixedfx.logging.Log;

public class ServiceController
{

	public static boolean isRunning(final Program service)
	{
		final ComplexString result = Executor.runAndWaitForOutput("sc query " + "\"" + service.serviceName + "\"", MasterController.TIMEOUT);

		try
		{
			Log.windows.debug("Service " + service.serviceName + " is " + (result.containsAllRows("STATE", "RUNNING", "4") ? "enabled!" : "disabled!"));
			return result.containsAllRows("STATE", "RUNNING", "4");
		}
		catch (final Exception e)
		{
			Log.windows.debug("Service " + service.serviceName + " status couldn't be determined!");
			return false;
		}
	}

	public static void run(final Program service) throws TimeoutException
	{
		if (ServiceController.isRunning(service))
			return;
		Executor.runAsAdmin("sc", "start " + "\"" + service.serviceName + "\"", false);
		TimeoutController.execute(() ->
		{
			while (!ServiceController.isRunning(service))
				;
		} , MasterController.TIMEOUT);
		Log.windows.debug("Service " + service.serviceName + " was started!");
	}

	public static void stop(final Program service) throws TimeoutException
	{
		if (!ServiceController.isRunning(service))
			return;
		Executor.runAsAdmin("sc", "stop " + "\"" + service.serviceName + "\"", false);
		TimeoutController.execute(() ->
		{
			while (ServiceController.isRunning(service))
				;
		} , MasterController.TIMEOUT);
		Log.windows.debug("Service " + service.serviceName + " was stopped!");
	}
}
