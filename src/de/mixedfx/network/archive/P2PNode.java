package de.mixedfx.network.archive;

import java.io.IOException;
import java.net.InetSocketAddress;

import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

public class P2PNode
{
	public P2PNode(final int bindport, final InetSocketAddress bootaddress, final Environment env) throws IOException, InterruptedException
	{
		// Generate the NodeIds Randomly
		final NodeIdFactory nidFactory = new RandomNodeIdFactory(env);

		// construct the PastryNodeFactory, this is how we use rice.pastry.socket
		final PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindport, env);
		// final InternetPastryNodeFactory fact = new InternetPastryNodeFactory(nidFactory,
		// bindport, env);

		// construct a node, but this does not cause it to boot
		final PastryNode node = factory.newNode();

		// in later tutorials, we will register applications before calling boot
		node.boot(bootaddress);

		// the node may require sending several messages to fully boot into the ring
		synchronized (node)
		{
			while (!node.isReady() && !node.joinFailed())
			{
				// delay so we don't busy-wait
				node.wait(500);

				// abort if can't join
				if (node.joinFailed())
					throw new IOException("Could not join the FreePastry ring.  Reason:" + node.joinFailedReason());
			}
		}

		System.out.println("Finished creating new node " + node);
	}
}
