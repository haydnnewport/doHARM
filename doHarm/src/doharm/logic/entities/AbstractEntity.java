package doharm.logic.entities;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import doharm.logic.physics.Vector;
import doharm.logic.world.Layer;
import doharm.logic.world.World;
import doharm.logic.world.tiles.Tile;

public abstract class AbstractEntity
{
	
	private Vector position;
	
	private Vector velocity;
	private Dimension size;
	/** Angle this entity is facing */
	private float angle;
	private float friction = 0.6f;
	
	private Layer currentLayer;
	private Tile currentTile;
	
	private int id; //unique id used for networking
	private World world;

	private final EntityType entityType;
	

	public AbstractEntity(EntityType entityType)
	{
		this.entityType = entityType;
		size = new Dimension(32,32); //hmm..	
		reset();
	}
	
	private void reset() 
	{
		position = new Vector();
		velocity = new Vector();
		angle = 0;
	}

	public void spawn(Tile spawnTile)
	{
		reset();
		setPosition(spawnTile.getX(),spawnTile.getY(), spawnTile.getLayer());
		
		
		
		
		angle = 0;
		
	}
	
	
	
	public void setPosition(float x, float y, Layer layer) 
	{
		position.set(x, y);
		currentLayer = layer;
		Tile tile = currentLayer.getTileAt(position.getX(), position.getY());
		
		if (currentTile == tile || tile == null)
			return;
		
		if (currentTile != null)
			currentTile.removeEntity(this);
		
		this.currentTile = tile;
		currentLayer = currentTile.getLayer();
		currentTile.addEntity(this);
	}

	public World getWorld()
	{
		return world;
	}
	
	public void setID(int id)
	{
		this.id = id;
	}
	public int getID()
	{
		return this.id;
	}
	
	
	public Dimension getSize()
	{
		return new Dimension(size);
	}
	
	public Vector getPosition() 
	{
		return new Vector(position);
	}
	
	

	public Vector getVelocity() 
	{
		return new Vector(velocity);
	}
	public void setVelocity(Vector velocity)
	{
		this.velocity.set(velocity);
	}
	
	

	public float getAngle() 
	{
		return angle;
	}
	public void setAngle(float angle)
	{
		this.angle = angle;
	}
	
	
	
	
	public void move()
	{
		position.add(velocity.getX(),velocity.getY()*0.5f); //move half speed up/down due to isometric view
		
		//Tile newTile = currentLayer.getTileAt(position.getX()+velocity.getX(),position.getY()+velocity.getY());


		
		setPosition(position.getX(), position.getY(), currentTile.getLayer());
		
		
		checkCollisions();
		
		
		velocity.multiply(friction);
	}
	
	

	private void checkCollisions() 
	{
		//TODO
		
	}
	
	
	
	
	public Layer getCurrentLayer()
	{
		return currentTile.getLayer();
	}
	
	public Tile getCurrentTile()
	{
		return currentTile;
	}

	public void setWorld(World world) 
	{
		this.world = world;
	}

	public EntityType getEntityType() 
	{
		return entityType;
	}
	
	
	
	
}
