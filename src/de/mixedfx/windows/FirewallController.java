package de.mixedfx.windows;

import de.mixedfx.java.ComplexString;

public class FirewallController
{
	private final static String	enableCommand	= "netsh advfirewall set allprofiles state on";
	private final static String	disableCommand	= "netsh advfirewall set allprofiles state off";
	private final static String	statusCommand	= "netsh advfirewall show allprofiles";

	public static void main(final String[] args)
	{
		System.out.println(FirewallController.isEnabled());
		FirewallController.disable();
		System.out.println(FirewallController.isEnabled());
		FirewallController.enable();
		System.out.println(FirewallController.isEnabled());
	}

	/**
	 * Enables all windows firewalls.
	 */
	public static void enable()
	{
		Executor.runAndWaitForOutput(FirewallController.enableCommand);
		while (!FirewallController.isEnabled())
		{
			;
		}
	}

	/**
	 * Disables all windows firewalls.
	 */
	public static void disable()
	{
		Executor.runAndWaitForOutput(FirewallController.disableCommand);
		while (FirewallController.isEnabled())
		{
			;
		}
	}

	/**
	 * @return False if all firewalls are disabled or status could not be retrieved! True if at
	 *         least one firewall is online.
	 */
	public static boolean isEnabled()
	{
		final ComplexString response = Executor.runAndWaitForOutput(FirewallController.statusCommand);

		try
		{
			return response.containsAllRows("Status", "EIN");
		}
		catch (final Exception e)
		{
			return false;
		}
	}
}
