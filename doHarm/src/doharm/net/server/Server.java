package doharm.net.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import doharm.logic.entities.AbstractEntity;
import doharm.logic.entities.characters.players.Player;
import doharm.logic.entities.characters.players.PlayerType;
import doharm.logic.world.World;
import doharm.net.ClientState;
import doharm.net.UDPReceiver;
import doharm.net.packets.ClientPacket;
import doharm.net.packets.Gamestate;
import doharm.net.packets.Join;
import doharm.net.packets.ServerPacket;
import doharm.net.packets.Snapshot;
import doharm.net.packets.entityinfo.CharacterCreate;
import doharm.net.packets.entityinfo.CharacterUpdate;
import doharm.net.packets.entityinfo.EntityCreate;
import doharm.net.packets.entityinfo.EntityUpdate;

/**
 * Class for handling networking from the Server end.
 * @author Adam McLaren (300248714)
 */
public class Server {

	private final int maxPlayers = 16;
	private ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();
	private UDPReceiver receiver;
	private DatagramSocket udpSock;
	private int serverTime = 0;
	private static int CLIENT_CHECK_INTERVAL = 60, TIMEOUT_DELAY = 200;
	private int checkClientsCounter = 0;
	
	private World world;
	
	/**
	 * Create a new Server.
	 * @param port Port number to run the server on.
	 * @param world World to use for the Servers game.
	 */
	public Server(int port, World world)
	{
		this.world = world;
		
		// Setup the UDP socket.
		try {
			udpSock = new DatagramSocket(null);
			InetSocketAddress address = new InetSocketAddress(port);
			udpSock.bind(address);
			receiver = new UDPReceiver(udpSock);
			receiver.start();
		} catch (SocketException e) { e.printStackTrace(); }
	}
	
	/**
	 * Processes all packets received. Handles player join requests and updating what the latest actions from the Clients are.
	 */
	public void processIncomingPackets()
	{		
		while (!receiver.isEmpty())
		{
			System.out.println("Dealing with packet...");
			DatagramPacket packet = receiver.poll();
			byte[] data = packet.getData();
			
			// Check what type of packet it is.			
			switch (ClientPacket.values()[data[0]&0xff])
			{
			case ACTION:
				for (ConnectedClient c : clients)
					if ( c.getAddress().equals(packet.getSocketAddress()) )
					{
						c.updateClientActionPacket(data, serverTime);
						break;
					}
				break;
				
			case JOIN:
				Join request = new Join(data);
				byte[] response = new byte[2];
				response[0] = (byte) ServerPacket.RESPONSE.ordinal();
				
				if (clients.size() < maxPlayers)
				{
					boolean goodToGo = true; 
					for (ConnectedClient c : clients)
					{
						if (c.getName().equals(request.name))
						{
							goodToGo = false;
							response[1] = (byte)2;
							break;
						}
					}
					if (goodToGo)
					{
						createClient(new InetSocketAddress(packet.getAddress(), packet.getPort()), request);
						response[1] = (byte)0;
					}
					transmit(response, new InetSocketAddress(packet.getAddress(), packet.getPort()));
				}
				else
				{
					// Reject, inform them so.
					response[1] = (byte)1;	// 1 = NO server is full.
					transmit(response, new InetSocketAddress(packet.getAddress(), packet.getPort()));
				}
				break;
			}
		}
	}
	
	/**
	 * Sends a UDP Packet out.
	 * @param data Packet contents.
	 * @param address IP and Port to send to.
	 * @return If the transmit succeeded.
	 */
	public boolean transmit(byte[] data, InetSocketAddress address)
	{
		System.out.println("Transmitting packet to " + address.toString());
		try {
			udpSock.send(new DatagramPacket(data, data.length, address.getAddress(), address.getPort()));
			return true;
		}
		catch (SocketException e) { e.printStackTrace(); }
		catch (IOException e) {	e.printStackTrace(); }
		return false;
	}
	
	/**
	 * Create a new client and the player he will control. Note this method adds the created client to the client list as well as returns the object.
	 * @param address Address the client is connecting from.
	 * @param settings User settings for applying to the player entity.
	 * @return The resulting Client object.
	 */
	private ConnectedClient createClient(InetSocketAddress address, Join settings)
	{
		ConnectedClient oldClient = null;
		for (ConnectedClient c : clients)
		{
			if (c.getAddress().equals(address))
			{
				oldClient = c;
				break;
			}
		}
		if (oldClient != null)
		{
			oldClient.kill(world);
			clients.remove(oldClient);
		}
		
		Player player = world.getPlayerFactory().createPlayer(world.getRandomEmptyTile(), settings.name, 
				settings.classType, world.getIDManager().takeID(), PlayerType.NETWORK, settings.colour, true);
		ConnectedClient client = new ConnectedClient(address, player, serverTime);
		clients.add(client);
		return client;
	}
	
	/**
	 * Builds new snapshots from the game state then sends them out.
	 */
	public void dispatchSnapshots()
	{
		HashMap<Integer,EntityUpdate> entityUpdates = new HashMap<Integer,EntityUpdate>();
		HashMap<Integer,EntityCreate> entityCreates = new HashMap<Integer,EntityCreate>();
		ArrayList<Integer> entityDeletes = new ArrayList<Integer>();
		
		// get game changes.
		
		// Created Entities.
		for (AbstractEntity e : world.getEntityFactory().getAddedEntities() )
		{
			if (e instanceof Player)
			{
				entityCreates.put(e.getID(), new CharacterCreate((Player)e));
			}
//			else if (e instanceof Item)
//			{
//				entityCreates.put(e.getID(), new ItemCreate((Item)e));
//			}
		}
		world.getEntityFactory().clearAddedEntities();
		
		// Updated Entities (presently is just ALL entities)
		for (AbstractEntity e : world.getEntityFactory().getEntities() )
		{
			if (e instanceof Player)
			{
				entityUpdates.put(e.getID(), new CharacterUpdate((Player)e));
			}
		}
		
		// Removed Entities.
		for (AbstractEntity e : world.getEntityFactory().getRemovedEntities() )
			entityDeletes.add(e.getID());
		world.getEntityFactory().clearRemovedEntities();
		
		
		for (ConnectedClient c : clients)
		{
			if (c.getState() == ClientState.INGAME)
			{
				buildSnapshot(c, new Snapshot(serverTime, c.getLatestActionPacket().seqNum, world), entityUpdates, entityCreates, entityDeletes);
				
				// build transmission snap and send
				transmit( c.buildTransmissionSnapshot().convertToBytes() , c.getAddress() );
			}
			else if ( c.getState() == ClientState.READY )
			{
				if (c.resendGamestate())
					sendGamestate(c);
				else
					buildSnapshot(c, new Snapshot(serverTime, -1, world), entityUpdates, entityCreates, entityDeletes);
			}
		}
	}
	
	/**
	 * Adds entity deletes, creates and updates to a Snapshot, and then adds it to the clients snap buffer.
	 * @param client Client to build the snapshot for.
	 * @param snap Freshly constructed snapshot (does not include entity info).
	 * @param entityUpdates List of entity updates.
	 * @param entityCreates List of entity creates.
	 * @param entityDeletes List of entity deletes.
	 */
	private void buildSnapshot(ConnectedClient client, Snapshot snap, HashMap<Integer,EntityUpdate> entityUpdates, HashMap<Integer,EntityCreate> entityCreates, ArrayList<Integer> entityDeletes)
	{		
		for (int eID : entityDeletes)
			snap.addEDelete(eID);
		
		for (int eID : entityCreates.keySet())
			snap.addECreate(entityCreates.get(eID));
		
		for (int eID : entityUpdates.keySet())
			snap.addEUpdate(entityUpdates.get(eID));
		
		client.addSnapshot(snap);
	}
	
	/**
	 * Sends out a "Gamestate" snapshot, it contains all the information.
	 * @param client Client to send Gamestate to.
	 */
	private void sendGamestate(ConnectedClient client)
	{
		client.flushSnaps();
		
		Gamestate gamestate = new Gamestate(serverTime, -1, world, client);
		
		for (AbstractEntity e : world.getEntityFactory().getEntities() )
		{
			if (e instanceof Player)
			{
				gamestate.addECreate(new CharacterCreate((Player)e));
				gamestate.addEUpdate(new CharacterUpdate((Player)e));
			}
		}
		byte[] send = gamestate.convertToBytes();
		transmit(send, client.getAddress() );
	}
	
	/**
	 * Periodically checks the last time we received a packet from any clients, and if it has been too long, drop them from the server.
	 */
	public void checkClients()
	{
		++checkClientsCounter;
		if (checkClientsCounter > CLIENT_CHECK_INTERVAL)
		{
			checkClientsCounter = 0;
			ArrayList<ConnectedClient> toRemove = new ArrayList<ConnectedClient>(clients.size());
			for ( ConnectedClient c : clients )
			{
				if (serverTime - c.getLatestTime() > TIMEOUT_DELAY)
					toRemove.add(c);
			}
			for ( ConnectedClient c : toRemove )
			{
				System.out.println("Dropping " + c.getName() + " from server.");
				clients.remove(c);
			}
		}
	}

	/**
	 * Tick the server time.
	 */
	public void tick()
	{
		++serverTime;		
	}

	/**
	 * Perform client desired moves from the received Action packets.
	 */
	public void moveClients()
	{
		for (ConnectedClient c : clients)
		{
			if (c.getLatestActionPacket() != null)
				c.getPlayerEntity().updateFromAction(c.getLatestActionPacket());
		}
	}
}
