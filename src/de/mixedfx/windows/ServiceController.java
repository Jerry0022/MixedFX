package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;

public class ServiceController
{
	public static final String	HAMACHISERVICENAME	= "Hamachi2Svc";

	public static void main(final String[] args)
	{
		System.out.println(ServiceController.isServiceRunning(ServiceController.HAMACHISERVICENAME));
		ServiceController.runService(ServiceController.HAMACHISERVICENAME);
		System.out.println(ServiceController.isServiceRunning(ServiceController.HAMACHISERVICENAME));
		ServiceController.stopService(ServiceController.HAMACHISERVICENAME);
		System.out.println(ServiceController.isServiceRunning(ServiceController.HAMACHISERVICENAME));
	}

	public static void runService(final String serviceName)
	{
		Executor.runAndWaitForOutput("sc start " + serviceName);
		while (!ServiceController.isServiceRunning(serviceName))
		{
			;
		}
	}

	public static void stopService(final String serviceName)
	{
		Executor.runAndWaitForOutput("sc stop " + serviceName);
		while (ServiceController.isServiceRunning(serviceName))
		{
			;
		}
	}

	private static boolean isServiceRunning(final String serviceName)
	{
		final ComplexString result = Executor.runAndWaitForOutput("sc query " + serviceName);

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
