package de.mixedfx.gui;

import de.mixedfx.windows.DefaultNetworkAdapter;
import de.mixedfx.windows.MasterController;
import de.mixedfx.windows.NetworkPriorityController;

public class WindowsTester
{

	public static void main(final String[] args)
	{
		System.out.println(MasterController.isRunningAsAdmin());
		System.out.println(MasterController.hasCurrentUserAdminRights());
//		MasterController.enableTunngle();
		NetworkPriorityController.toTop(DefaultNetworkAdapter.TUNNGLE);
		
		// System.out.println(FirewallController.isEnabled());
		// FirewallController.enable();
		// System.out.println(FirewallController.isEnabled());
		// FirewallController.disable();
		// System.out.println(FirewallController.isEnabled());
		//
		// System.out.println(ProcessController.isProcessRunning(DefaultPrograms.TUNNGLE));
		// ProcessController.run(DefaultPrograms.TUNNGLE);
		// System.out.println(ProcessController.isProcessRunning(DefaultPrograms.TUNNGLE));
		// ProcessController.stop(DefaultPrograms.TUNNGLE);
		// System.out.println(ProcessController.isProcessRunning(DefaultPrograms.TUNNGLE));
		//
		// System.out.println(ServiceController.isRunning(DefaultPrograms.TUNNGLE));
		// ServiceController.run(DefaultPrograms.TUNNGLE);
		// System.out.println(ServiceController.isRunning(DefaultPrograms.TUNNGLE));
		// ServiceController.stop(DefaultPrograms.TUNNGLE);
		//
		// System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
		// NetworkAdapterController.enable(DefaultNetworkAdapter.TUNNGLE);
		// System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
		// NetworkAdapterController.disable(DefaultNetworkAdapter.TUNNGLE);
		// System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
	}

}
