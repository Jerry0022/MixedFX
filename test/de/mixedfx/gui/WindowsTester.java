package de.mixedfx.gui;

import de.mixedfx.windows.DefaultNetworkAdapter;
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
		FirewallController.enable();
		System.out.println(FirewallController.isEnabled());
		FirewallController.disable();
		System.out.println(FirewallController.isEnabled());

		System.out.println(ProcessController.isProcessRunning(DefaultPrograms.TUNNGLE));
		ProcessController.run(DefaultPrograms.TUNNGLE);
		System.out.println(ProcessController.isProcessRunning(DefaultPrograms.TUNNGLE));
		ProcessController.stop(DefaultPrograms.TUNNGLE);
		System.out.println(ProcessController.isProcessRunning(DefaultPrograms.TUNNGLE));

		System.out.println(ServiceController.isRunning(DefaultPrograms.TUNNGLE));
		ServiceController.run(DefaultPrograms.TUNNGLE);
		System.out.println(ServiceController.isRunning(DefaultPrograms.TUNNGLE));
		ServiceController.stop(DefaultPrograms.TUNNGLE);

		System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
		NetworkAdapterController.enable(DefaultNetworkAdapter.TUNNGLE);
		System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
		NetworkAdapterController.disable(DefaultNetworkAdapter.TUNNGLE);
		System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
	}

}
