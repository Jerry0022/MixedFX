package de.mixedfx.windows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;

public class NetworkPriorityController
{
	/**
	 * Windows 10 is unsupported!
	 *
	 * @param adapterName
	 *            The name of the network adapter, may be user specific.
	 * @throws NetworkAdapterNotFoundException
	 *             If network adapter could not be found!
	 * @throws IllegalStateException
	 *             If Windows 10 is the current operation system!
	 */
	public static void toTop(final String adapterName) throws NetworkAdapterNotFoundException, IllegalStateException
	{
		// Windows 10? If yes, it is unsupported!
		final ComplexString result = Executor.runAndWaitForOutput("systeminfo", MasterController.TIMEOUT);
		if (result.containsOneRow("Betriebssystemname", "10"))
			throw new IllegalStateException("Windows 10 is not supported for this action. See also: "
					+ "http://answers.microsoft.com/en-us/windows/forum/windows_10-networking/windows-10-tunngle-network-adapter-priority-cannot/e9df566a-4dfe-4d3e-ae6e-623f39e31661");

		// Does NetworkAdapter exists?
		if (!NetworkAdapterController.exists(adapterName))
			throw new NetworkAdapterNotFoundException(adapterName);

		try
		{
			// Set it to top!
			final FileObject nvspBind = DataHandler.getTempFolder().setFullName("nvspbind.exe");
			if (!nvspBind.toFile().exists())
			{
				final File nvspbind = new File(NetworkPriorityController.class.getResource("nvspbind.exe").toURI());
				FileUtils.copyInputStreamToFile(new FileInputStream(nvspbind), nvspBind.toFile());
			}
			nvspBind.setParameter("/++", "\"" + adapterName + "\"", "ms_tcpip");
			Executor.runAsAdmin(nvspBind, false);
		}
		catch (final IOException | URISyntaxException e)
		{
			Log.windows.error(e);
		}
	}
}
