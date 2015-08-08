package de.mixedfx.windows;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.io.FileUtils;

import de.mixedfx.file.FileObject;
import de.mixedfx.java.ComplexString;
import de.mixedfx.logging.Log;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class NetworkPriorityController
{
	private static FutureTask<Void>	t;
	private static BooleanProperty	result	= new SimpleBooleanProperty(false);

	/**
	 * DOES NOT WORK IF USER DOES NOT USE THE PREDEFINED ADMIN ACCOUNT IN WINDOWS!
	 * @param adapterName
	 *            The name of the network adapter, may be user specific.
	 * @return A property which is false but turns to true or invalidates false if the async thread is finished (the thread waits for a dialogue to
	 *         close, if there is the default windows dialogue to set this priority open). (The async thread is not a daemon and only called once,
	 *         doesn't matter how often this method is called)
	 */
	public static BooleanProperty toTop(String adapterName)
	{
		if (t == null)
		{
			t = new FutureTask<Void>(new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					try
					{
						FileObject tempFullPath = FileObject.create().setPath(File.createTempFile("temp-file", "tmp").getParent()).setFullName("nvspbind.exe");
						if (!tempFullPath.toFile().exists())
						{
							File nvspbind = new File(NetworkPriorityController.class.getResource("nvspbind.exe").toURI());
							FileUtils.copyInputStreamToFile(new FileInputStream(nvspbind), tempFullPath.toFile());
						}
						String command = "cmd /c start \"\" \"" + tempFullPath + "\"" + " /++ \"" + adapterName + "\" \"" + "ms_tcpip\"";
						ComplexString string = Executor.runAndWaitForOutput(command);
						result.set(string.containsAllRows("finished", "(0)") || string.containsAllRows("finished", "(14)"));
						t = null;
						Log.windows.debug("NetworkAdapter " + adapterName + " was set to top!");
						return null;
					}
					catch (Exception e)
					{
						result.set(false);
						t = null;
						return null;
					}
				}
			});
			new Thread(t).start();
		}

		return result;
	}
}
