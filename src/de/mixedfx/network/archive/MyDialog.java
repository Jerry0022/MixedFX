package de.mixedfx.network.archive;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MyDialog extends JDialog implements ActionListener
{

	private static final long	serialVersionUID	= 1L;

	protected MyApplication		itsListener;

	protected JPanel			dialogPanel, midPanel, midUpperPanel, midLowerPanel, bottomPanel,
	midLeftPanel, midRightPanel;

	protected JLabel			topLabel, leftLabel, rightLabel, midLabel, midAreaLabel;

	protected JTextField		leftField, rightField, midField;

	protected JTextArea			midArea;

	protected JButton			okButton, quitButton;

	public MyDialog(final MyApplication listener, final String title)
	{
		super();
		this.setTitle(title);
		this.setSize(300, 300);
		this.itsListener = listener;
		this.dialogPanel = new JPanel();
		this.dialogPanel.setLayout(new BorderLayout());
		this.midPanel = new JPanel();
		this.midPanel.setLayout(new GridLayout(2, 1));
		this.midUpperPanel = new JPanel();
		this.midUpperPanel.setLayout(new GridLayout(5, 1));
		this.midLowerPanel = new JPanel();
		this.midLowerPanel.setLayout(new GridLayout(1, 1));
		this.bottomPanel = new JPanel();
		this.bottomPanel.setLayout(new FlowLayout());
		this.midLeftPanel = new JPanel();
		this.midLeftPanel.setLayout(new GridLayout(1, 2));
		this.midRightPanel = new JPanel();
		this.midRightPanel.setLayout(new GridLayout(1, 2));
		this.topLabel = new JLabel("Enter a key and a message:");
		this.leftLabel = new JLabel("Key");
		this.rightLabel = new JLabel("Message");
		this.leftField = new JTextField();
		this.rightField = new JTextField();
		this.midLabel = new JLabel("Received message:");
		this.midField = new JTextField();
		this.midAreaLabel = new JLabel("My leafset:");
		this.midArea = new JTextArea(10, 20);
		this.okButton = new JButton("ok");
		this.quitButton = new JButton("quit");

		this.okButton.addActionListener(this);
		this.quitButton.addActionListener(this);

		this.dialogPanel.add(this.topLabel, BorderLayout.NORTH);
		this.midLeftPanel.add(this.leftLabel);
		this.midLeftPanel.add(this.rightLabel);
		this.midPanel.add(this.midUpperPanel);
		this.midPanel.add(this.midLowerPanel);
		this.midUpperPanel.add(this.midLeftPanel);
		this.midRightPanel.add(this.leftField);
		this.midRightPanel.add(this.rightField);
		this.midUpperPanel.add(this.midRightPanel);
		this.midUpperPanel.add(this.midLabel);
		this.midUpperPanel.add(this.midField);
		this.midUpperPanel.add(this.midAreaLabel);
		this.midLowerPanel.add(this.midArea);
		this.dialogPanel.add(this.midPanel, BorderLayout.CENTER);
		this.bottomPanel.add(this.okButton);
		this.bottomPanel.add(this.quitButton);
		this.dialogPanel.add(this.bottomPanel, BorderLayout.SOUTH);
		this.setContentPane(this.dialogPanel);
		super.setVisible(true);
	}

	@Override
	public void actionPerformed(final ActionEvent event)
	{

		final String command = event.getActionCommand();

		if (command.equals("ok"))
			this.itsListener.put(this.leftField.getText(), this.rightField.getText());
		else if (command.equals("quit"))
			this.itsListener.quit();

	}

	public void receivedMessage(final String msg)
	{
		this.midField.setText(msg);
	}

	public void updateRoutingTable(final String msg)
	{
		this.midArea.setText(msg);
	}

}
