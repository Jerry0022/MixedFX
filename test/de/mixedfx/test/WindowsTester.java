package de.mixedfx.test;

import java.io.FileNotFoundException;

import de.mixedfx.java.TimeoutController.TimeoutException;
import de.mixedfx.windows.DefaultPrograms;
import de.mixedfx.windows.MasterController;
import de.mixedfx.windows.NetworkAdapterNotFoundException;

public class WindowsTester
{

	public static void main(final String[] args)
	{
		// System.out.println(MasterController.isRunningAsAdmin());
		// System.out.println(MasterController.hasCurrentUserAdminRights());

		try
		{
			DefaultPrograms.TUNNGLE.fullPath.setParameter("");
			MasterController.enableTunngle();
		}
		catch (FileNotFoundException | IllegalStateException | NetworkAdapterNotFoundException | TimeoutException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		// System.out.println(NetworkAdapterController.getList());
		// System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
		// NetworkPriorityController.toTop(DefaultNetworkAdapter.TUNNGLE);
		// NetworkAdapterController.enable(DefaultNetworkAdapter.TUNNGLE);
		// System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
		// NetworkAdapterController.disable(DefaultNetworkAdapter.TUNNGLE);
		// System.out.println(NetworkAdapterController.isEnabled(DefaultNetworkAdapter.TUNNGLE));
	}

}
