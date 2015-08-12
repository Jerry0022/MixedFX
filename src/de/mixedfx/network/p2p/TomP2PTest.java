package de.mixedfx.network.p2p;

import java.io.IOException;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

public class TomP2PTest
{
	final private Peer peer;

	public TomP2PTest(int peerId) throws Exception
	{
		peer = new PeerMaker(Number160.createHash(peerId)).setPorts(4000 + peerId).makeAndListen();
		FutureBootstrap fb = peer.bootstrap().setBroadcast().setPorts(4002).start(); // Should be the port of the other one
		fb.awaitUninterruptibly();
		if (fb.getBootstrapTo() != null)
		{
			peer.discover().setPeerAddress(fb.getBootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}
	}

	public static void main(String[] args) throws NumberFormatException, Exception
	{
		String[] errorSoon =
		{ "1", "testme", "0.0.0.0" };

		TomP2PTest dns = new TomP2PTest(Integer.parseInt(errorSoon[0]));
		if (errorSoon.length == 3)
		{
			dns.store(errorSoon[1], errorSoon[2]);
		}
		if (errorSoon.length == 2)
		{
			System.out.println("Name:" + errorSoon[1] + " IP:" + dns.get(errorSoon[1]));
		}
	}

	private String get(String name) throws ClassNotFoundException, IOException
	{
		FutureDHT futureDHT = peer.get(Number160.createHash(name)).start();
		futureDHT.awaitUninterruptibly();
		if (futureDHT.isSuccess())
		{
			return futureDHT.getData().getObject().toString();
		}
		return "not found";
	}

	private void store(String name, String ip) throws IOException
	{
		peer.put(Number160.createHash(name)).setData(new Data(ip)).start().awaitUninterruptibly();
	}

}
