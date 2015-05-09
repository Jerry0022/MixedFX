package de.mixedfx.network.p2pAgain;

import java.io.IOException;
import java.net.UnknownHostException;

import rice.environment.Environment;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.testing.CommonAPITest;

public class MyApplicationTest extends CommonAPITest
{

	protected MyApplication	newApps[];

	public MyApplicationTest(final Environment env) throws IOException
	{
		super(env);
		this.newApps = new MyApplication[this.NUM_NODES];
	}

	@Override
	protected void processNode(final int num, final Node node)
	{
		this.newApps[num] = new MyApplication(node, num, this);
	}

	@Override
	protected void runTest()
	{
		for (int i = 0; i < this.NUM_NODES; i++)
			this.newApps[i].startApp();
	}

	@Override
	public void simulate()
	{
		super.simulate();
	}

	@Override
	public void kill(final int n)
	{
		super.kill(n);
	}

	/**
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main(final String[] args) throws UnknownHostException
	{

		try
		{
			final String[] i = new String[] { "-this.nodes 5", "-protocol", "direct" };
			final MyApplicationTest myAppTest = new MyApplicationTest(CommonAPITest.parseArgs(i));
			myAppTest.start();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
