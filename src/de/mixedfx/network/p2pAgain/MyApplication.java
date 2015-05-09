package de.mixedfx.network.p2pAgain;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.NodeHandleSet;
import rice.p2p.commonapi.RouteMessage;

public class MyApplication implements Application
{

	protected Endpoint			endpoint;
	protected Node				myNode;
	protected MyDialog			dialog;
	protected MyApplicationTest	test;
	protected int				num;

	public MyApplication(final Node node, final int num, final MyApplicationTest test)
	{
		this.myNode = node;
		final String instance = "MyApplication";
		this.endpoint = node.buildEndpoint(this, instance);
		this.endpoint.register();
		System.out.println(instance + " registered");
		this.test = test;
		this.num = num;
	}

	@Override
	public boolean forward(final RouteMessage message)
	{
		System.out.println("[" + this.endpoint.getId() + "] RouteMessage forwarded");
		return true;
	}

	@Override
	public void deliver(final Id id, final Message message)
	{
		System.out.println("[" + this.endpoint.getId() + "] Got new message: [" + ((MyMessage) message).getMessage() + "]");
		this.dialog.receivedMessage(((MyMessage) message).getMessage());
	}

	@Override
	public void update(final NodeHandle handle, final boolean joined)
	{
		System.out.println("[" + this.endpoint.getId() + "] NodeHandle joined: " + joined);
		if (this.dialog != null)
			this.dialog.updateRoutingTable(this.getLeafSet());
	}

	// ----- EXTERNALLY AVAILABLE METHODS -----

	public void startApp()
	{
		this.dialog = new MyDialog(this, this.endpoint.getId().toString());
		this.dialog.updateRoutingTable(this.getLeafSet());
	}

	public void put(final String key, final String msg)
	{
		final Id keyId = this.myNode.getIdFactory().buildId(key);
		System.out.println("[" + this.endpoint.getId() + "] Key maps to nodeId " + keyId.toStringFull());
		final Message message = new MyMessage("From: " + this.endpoint.getId() + ", Key: " + key + ", Msg: " + msg);
		this.endpoint.route(keyId, message, null);
		this.test.simulate(); // only needed for protocol direct
	}

	public String getLeafSet()
	{
		final StringBuffer s = new StringBuffer();
		final NodeHandleSet nhs = this.endpoint.neighborSet(16);
		for (int i = 0; i < nhs.size(); i++)
			s.append(nhs.getHandle(i).getId() + "\n");
		return s.toString();
	}

	public void quit()
	{
		this.dialog.dispose();
		class KillThread extends Thread
		{
			@Override
			public void run()
			{
				MyApplication.this.test.kill(MyApplication.this.num);
			}
		}
		final KillThread killer = new KillThread();
		killer.start();
	}

}
