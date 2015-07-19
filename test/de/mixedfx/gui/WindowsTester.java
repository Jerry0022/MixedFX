package de.mixedfx.gui;

import de.mixedfx.windows.DefaultPrograms;
import de.mixedfx.windows.FirewallController;
import de.mixedfx.windows.NetworkAdapterController;
import de.mixedfx.windows.ProcessController;
import de.mixedfx.windows.ServiceController;

public class WindowsTester
{

	public static void main(final String[] args)
	{
		System.out.println(FirewallController.isEnabled());
		FirewallController.disable();
		System.out.println(FirewallController.isEnabled());
		FirewallController.enable();
		System.out.println(FirewallController.isEnabled());

		System.out.println(NetworkAdapterController.isAdapterEnabled("HAMACHI"));
		NetworkAdapterController.enableAdapter("HAMACHI");
		System.out.println(NetworkAdapterController.isAdapterEnabled("HAMACHI"));
		NetworkAdapterController.disableAdaptar("HAMACHI");
		System.out.println(NetworkAdapterController.isAdapterEnabled("HAMACHI"));

		System.out.println(ProcessController.isProcessRunning(DefaultPrograms.HAMACHI));
		ProcessController.runProcess(DefaultPrograms.HAMACHI);

		System.out.println(ProcessController.isProcessRunning(DefaultPrograms.HAMACHI));
		ProcessController.stopProcess(DefaultPrograms.HAMACHI);
		System.out.println(ProcessController.isProcessRunning(DefaultPrograms.HAMACHI));

		System.out.println(ServiceController.isServiceRunning(DefaultPrograms.HAMACHI));
		ServiceController.runService(DefaultPrograms.HAMACHI);
		System.out.println(ServiceController.isServiceRunning(DefaultPrograms.HAMACHI));
		ServiceController.stopService(DefaultPrograms.HAMACHI);
	}

}
