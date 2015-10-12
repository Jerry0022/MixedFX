package de.mixedfx.network.messages;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

/**
 * Master Message object which contains the information to which next hop (as IP) it is send.
 *
 * @author Jerry
 */
public class Message implements Serializable
{
	/**
	 * The message was sent from this one ip to avoid packet circuits.
	 */
	private
	@Getter
	@Setter
	InetAddress fromIP;

	/**
	 * The message is sent to this one ip to avoid packet circuits.
	 */
	private
	@Getter
	@Setter
	InetAddress toIP;

	private Date timeStamp;
}
