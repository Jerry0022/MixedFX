package de.mixedfx.network.p2p;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;

public class Listener implements Application
{
	private Endpoint endpoint;

	private Node node;

	public Listener(Node node)
	{
		this.endpoint = node.buildEndpoint(this, "myinstance");

		this.node = node;

		this.endpoint.register();
	}

	public Node getNode()
	{
		return node;
	}

	public void routeMyMsg(Id id)
	{
		System.out.println(this + " sending to " + id);
		Message msg = new P2PMessage(endpoint.getId(), id);
		endpoint.route(id, msg, null);
	}

	public void routeMyMsgDirect(NodeHandle nh)
	{
		System.out.println(this + " sending direct to " + nh);
		Message msg = new P2PMessage(endpoint.getId(), nh.getId());
		endpoint.route(null, msg, nh);
	}

	@Override
	public void deliver(Id arg0, Message message)
	{
		System.out.println(this + " received " + message);
	}

	@Override
	public boolean forward(RouteMessage arg0)
	{
		return true;
	}

	@Override
	public void update(NodeHandle arg0, boolean arg1)
	{
		// TODO Auto-generated method stub

	}

	public String toString()
	{
		return "MyApp " + endpoint.getId();
	}

}
