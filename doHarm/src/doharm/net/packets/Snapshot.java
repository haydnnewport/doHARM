package doharm.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/** Struct representing a Server Snapshot, which is then converted into a packet to send over the wire. */
public class Snapshot implements Cloneable {

	public final int serverTime;
	public int seqAckd;
	public final PlayerState pState;
	private final HashMap<Integer,EntityInfo> entityUpdates = new HashMap<Integer,EntityInfo>();
	private final HashMap<Integer,EntityInfo> entityCreates = new HashMap<Integer,EntityInfo>();
	private final ArrayList<Integer> entityDeletes = new ArrayList<Integer>();
	
	public Snapshot(int serverTime)
	{
		this.serverTime = serverTime;
		
		pState = null;
	}
	
	/**
	 * Constructs a Snapshot object out of a Snapshot packet byte array.
	 * @param packet
	 * @return
	 */
	public Snapshot(byte[] packet)
	{	
		ByteBuffer buff = ByteBuffer.wrap(packet);
		
		buff.position(1);	// Skip packet type, as we obviously already know what it is.
		
		serverTime = buff.getInt();
		
		seqAckd = buff.getInt();
		
		pState = null;
	}
	
	/**
	 * Translates the Snapshot object into a byte-array for transmission.
	 * @param snap Snapshot object to convert
	 * @return 
	 */
	public byte[] convertToBytes()
	{	
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		// Packet type
		buff.write((byte) ServerPacket.SNAPSHOT.ordinal());		// TODO Do we need the byte cast?
		// Servertime
		try { buff.write(ByteBuffer.allocate(4).putInt(serverTime).array()); }
		catch (IOException e) {	e.printStackTrace(); }
		
		return buff.toByteArray();
	}
	
	/** Extracts the timestamp from the byte array form of a Snapshot */
	public static int getTimestamp(byte[] data)
	{
		ByteBuffer buff = ByteBuffer.wrap(data);
		buff.position(1);
		return buff.getInt();
	}
	
	public Snapshot clone()
	{
		return this.clone();
	}

	/**
	 * Adds any fields that are in the given snapshot that are not present in this snapshot.
	 * This WILL NOT overwrite any fields that this snapshot already has (even if their values differ). 
	 * @param other Snapshot to copy missing fields from.
	 */
	public void addMissing(Snapshot other)
	{
		// add deletes first, as we'll want to check when adding creates/updates
		// if it's already been deleted, in which case shouldn't bother adding it. 
		for (int eID : other.entityDeletes)
			if (!entityDeletes.contains(eID))
				entityDeletes.add(eID);
		
		for (int eID : other.entityCreates.keySet())
			if (!entityCreates.containsKey(eID) && !entityDeletes.contains(eID))
				entityCreates.put(eID, other.entityCreates.get(eID));
		
		for (int eID : other.entityUpdates.keySet())
			if (!entityUpdates.containsKey(eID) && !entityDeletes.contains(eID))
				entityUpdates.put(eID, other.entityUpdates.get(eID));
	}
	
	/** Holds changes in player state */
	private class PlayerState
	{
		
	}
	
	/** Holds changes in entity state */
	private class EntityInfo
	{
		public int id;
	}
	
	
}

