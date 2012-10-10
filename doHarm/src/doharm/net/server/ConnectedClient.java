package doharm.net.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import doharm.net.ClientState;
import doharm.net.packets.Command;
import doharm.net.packets.Snapshot;

/**
 * The servers view of a Client
 */
public class ConnectedClient {
	private InetSocketAddress address;
	public Command latestCommandPacket;
	private int counter;	// counter used by various.
	private static int RESEND_DELAY;
	
	// Last time we received a packet from this client.
	private int latestTime;
	
	private ClientState state;
	
	// Holds on to all unack'd Snapshots we've sent the client.
	private LinkedList<Snapshot> snapsBuffer;
	
	public ConnectedClient(InetSocketAddress address)
	{
		this.address = address;
		state = ClientState.READY;
	}
	
	public InetSocketAddress getAddress() {	return address; }
	
	public ClientState getState() { return state; }

	public void setState(ClientState newState)
	{
		state = newState;
		switch (state)
		{
		case READY:
			counter = 0;
		}
	}
	
	/**
	 * Update what the latest command packet from the client is.
	 * @param data
	 */
	public void updateClientCommandPacket(byte[] data)
	{
		if (state == ClientState.READY)		// TODO can probably optimise this by having a special kind of command packet sent on first try.
		{
			setState(ClientState.INGAME);
		}
		
		// Extract the timestamp from the packet.
		int seqnum = Command.getSeqNum(data);
		
		// If this packet isn't more recent than the latest command we've received, discard.
		if ( seqnum <= latestTime )
			return;
		
		latestTime = seqnum;
		latestCommandPacket = new Command(data);
	}
	
	/** Add a new snapshot to the snap buffer. */
	public void addSnapshot(Snapshot snap)
	{
		snapsBuffer.add(snap);
	}
	
	/**
	 * Builds the Snapshot to actually transmit to the client.
	 * Combines all unack'd snapshots into one.
	 * @return
	 */
	public Snapshot buildTransmissionSnapshot()
	{
		// Remove all acknowledged snaps from the Snapshot buffer.
		while (snapsBuffer.peek().serverTime <= latestCommandPacket.serverTimeAckd)
			snapsBuffer.poll();
		
		/* Build the snapshot to send.
		
		So we use the latest snapshot as a base, and from there we go through the rest in order from newest to oldest,
		and if entities from the snap we are looking at aren't in our transmission snap, add them.
		
		TODO not the most efficient way at the moment; eventually should keep the latest transmission packet 
		and just make changes to it based on what has been removed and what has been added, not going thru all of them.
		*/
		Iterator<Snapshot> iter = snapsBuffer.descendingIterator();
		
		Snapshot transSnap = new Snapshot(iter.next());	// will never be null, a snapshot was just added earlier in the thread of execution
		
		while (iter.hasNext())
			transSnap.addMissing(iter.next());
		
		return transSnap;
	}

	/**
	 * Removes all the unack'd snaps from the snapsBuffer.
	 * Used when sending a full GameState. 
	 */
	public void flushSnaps() {
		snapsBuffer.clear();
	}

	public boolean resendGamestate() {
		if (--counter == 0)
		{
			counter = RESEND_DELAY;
			return true;
		}
		return false;
	}
}
