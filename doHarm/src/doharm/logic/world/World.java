package doharm.logic.world;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import doharm.logic.AbstractGame;
import doharm.logic.camera.Camera;
import doharm.logic.chat.Message;
import doharm.logic.chat.MessagePart;
import doharm.logic.entities.AbstractEntity;
import doharm.logic.entities.EntityFactory;
import doharm.logic.entities.EntityType;
import doharm.logic.entities.IDManager;
import doharm.logic.entities.characters.Character;
import doharm.logic.entities.characters.alliances.AllianceManager;
import doharm.logic.entities.characters.classes.CharacterClassType;
import doharm.logic.entities.characters.monsters.MonsterFactory;
import doharm.logic.entities.characters.players.HumanPlayer;
import doharm.logic.entities.characters.players.Player;
import doharm.logic.entities.characters.players.PlayerFactory;
import doharm.logic.entities.characters.players.PlayerType;
import doharm.logic.entities.items.Item;
import doharm.logic.entities.items.ItemFactory;
import doharm.logic.entities.items.ItemQuality;
import doharm.logic.entities.items.ItemType;
import doharm.logic.entities.items.misc.MiscItemType;
import doharm.logic.entities.items.misc.dragonballs.DragonBall;
import doharm.logic.entities.items.misc.dragonballs.DragonRadar;
import doharm.logic.entities.objects.GameObjectFactory;
import doharm.logic.entities.objects.ObjectType;
import doharm.logic.entities.objects.furniture.Chest;
import doharm.logic.time.Time;
import doharm.logic.weather.Weather;
import doharm.logic.world.tiles.Direction;
import doharm.logic.world.tiles.Tile;
import doharm.logic.world.tiles.TileType;
import doharm.net.NetworkMode;
import doharm.storage.TilesetLoader;
import doharm.storage.WallTileData;
import doharm.storage.WorldLoader;


public class World 
{
	private static final int NUM_MONSTERS = 20;
	private static final int NUM_TREES = 50;
	private static final int NUM_CHESTS = 5;
	private static final double NUM_CHEST_ITEMS = 6;

	private Layer[] layers;  
	
	private EntityFactory entityFactory;
	private PlayerFactory playerFactory;
	private MonsterFactory monsterFactory;
	private GameObjectFactory objectFactory;
	private ItemFactory itemFactory;
	
	private HumanPlayer humanPlayer;
	private Camera camera;
	private WorldLoader worldLoader;
	private int tileWidth;
	private int tileHeight;
	private int numRows;
	private int numCols;

	private IDManager idManager;

	private NetworkMode networkMode;
	
	private Time time;
	private Weather weather;
	private List<Message> messages;

	private DragonRadar dragonRadar;

	private AbstractGame game;

	private String worldName;
	private AllianceManager allianceManager;
	private List<Character> characters;

	
	
	
	
	public World(AbstractGame game, String worldName, NetworkMode networkMode)
	{
		this.game = game;
		this.networkMode = networkMode;
		this.worldName = worldName;
		messages = new ArrayList<Message>();
		idManager = new IDManager();
		dragonRadar = new DragonRadar();
		time = new Time();
		weather = new Weather();
		allianceManager = new AllianceManager(this);
		characters = new ArrayList<Character>();
		
		
		entityFactory = new EntityFactory(this,idManager);
		playerFactory = new PlayerFactory(this,entityFactory);
		monsterFactory = new MonsterFactory(this, entityFactory);
		itemFactory = new ItemFactory(this, entityFactory, dragonRadar);
		objectFactory = new GameObjectFactory(this, entityFactory);
		
		
		
		
		
		try 
		{
			worldLoader = new WorldLoader(worldName);
			numRows = worldLoader.getNumTilesY();
			numCols = worldLoader.getNumTilesX();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		TilesetLoader tsl = worldLoader.getTilesetLoader();
		this.tileWidth = tsl.getFloorTileWidth();
		this.tileHeight = tsl.getFloorTileHeight();
		
		camera = new Camera(tileWidth, tileHeight);
		
		layers = new Layer[worldLoader.getNumLayers()];
		for (int i = 0; i < layers.length; i++)
			layers[i] = new Layer(this, i);
		
		linkTiles();
		
		
		if (networkMode != NetworkMode.CLIENT)
		{
			createHumanPlayer(getRandomEmptyTile(),CharacterClassType.WARRIOR,"Player",Color.white,idManager.takeID());



			//Add some AIs
			for (int i = 0; i < 3; i++)
			{
				Tile tile = null;
				do
				{
					int r = (int)(Math.random()*numRows-2);
					int c = (int)(Math.random()*numCols-2);
					if (r < 2) r = 2;
					if (c < 2) c = 2;
					tile = layers[0].getTiles()[r][c];
				} while(!tile.isWalkable());


				playerFactory.createPlayer(tile, "AI"+(i+1), CharacterClassType.WARRIOR, idManager.takeID(),PlayerType.AI, new Color((i+102)*10213%255,(i+102)*223%255,(i+23)*1013%255),false);

			}


			//add some objects / items
		
			addDragonballs();
			addMonsters();
			addTrees();
			addChests();
			addRandomItems();
			//addDoors();
	
			
			addMessage(new Message(-1, false, new MessagePart("World created.")));
		}
		
	}
	
	
	
	private void addRandomItems() 
	{
		//for (int i = 0; i < NUM_RANDOM_ITEMS; i++)
		{
			
			
		}
	}

	private void addChests() 
	{
		for (int i = 0; i < NUM_CHESTS; i++)
		{
			while (true)
			{
				Tile tile = getRandomEmptyGrassTile();
				if (tile.getType() == TileType.DARK || tile.getType() == TileType.WOOD || tile.getType() == TileType.CONCRETE)
				{
					Chest chest = (Chest)objectFactory.createObject(ObjectType.CHEST, tile, idManager.takeID(), false);
				
					//int items = (int)(Math.random()*NUM_CHEST_ITEMS);
					//for (int j = 0; j < items; j++ )
						//itemFactory.createRandomItem(ItemQuality.RARE, idManager.takeID(), chest);
				}
				
				break;
			}
		}
	}


	private void addTrees() 
	{
		for (int i = 0; i < NUM_TREES; i++)
		{
			Tile tile = getRandomEmptyGrassTile();
			
			objectFactory.createObject(ObjectType.TREE, tile, idManager.takeID(), false);
		}
	}
	
	


	private void addMonsters() 
	{
		for (int i = 0; i < NUM_MONSTERS; i++)
		{
			monsterFactory.createMonster(CharacterClassType.getRandomMonsterClass(), idManager.takeID(),false);
		}
	}
	
	/**
	 * Only called by server!
	 */
	private void addDragonballs() 
	{
		for (int i = 0; i < DragonBall.NUM_DRAGONBALLS; i++)
		{
			itemFactory.setDragonBallStar(i+1);
			Tile tile = getRandomEmptyTile();
			itemFactory.createItem(ItemType.MISC, MiscItemType.DRAGONBALL.ordinal(), ItemQuality.LEGENDARY, idManager.takeID(), tile, false);
		}
	}

	public AllianceManager getAllianceManager()
	{
		return allianceManager;
	}
	
	@Override
	public String toString()
	{
		return worldName;
	}
	
	public DragonRadar getDragonRadar()
	{
		return dragonRadar;
	}
	
	public IDManager getIDManager()
	{
		return idManager;
	}

	

	public void addMessage(Message message)
	{
		messages.add(message);
	}
	
	private void linkTiles() 
	{
		TilesetLoader tilesetLoader = worldLoader.getTilesetLoader();
		WallTileData tempWallData = tilesetLoader.getWallTileData(0);
		
		Tile[][] prevTiles = null;
		for (Layer layer: layers)
		{
			Tile[][] tiles = layer.getTiles();
			for (int row = 0; row < tiles.length; row++)
			{
				for (int col = 0; col < tiles[0].length; col++)
				{
					if (prevTiles != null)
					{
						prevTiles[row][col].setRoof(tiles[row][col]);
					}
					
					for (int x = -1; x <= 1; x++)
					{
						for (int y = -1; y <= 1; y++)
						{
							//if (layer == 0)
							{
								tiles[row][col].setWall(Direction.UP, tempWallData);
								tiles[row][col].setWall(Direction.RIGHT, tempWallData);
								tiles[row][col].setWall(Direction.DOWN, tempWallData);
								tiles[row][col].setWall(Direction.LEFT, tempWallData);
								
							}
							
							//if (x != 0 && y != 0) 
								//continue;
							
							if (x == 0 && y == 0) //can never teleport up/down layers
								continue; 
							
							//TODO check upper/lower levels
							
							if (row + y >= 0 && row + y < tiles.length && 
								col + x >= 0 && col + x < tiles[0].length)
							{
								tiles[row][col].addNeighbour(tiles[row+y][col+x]);
							}
							
						}
					}
				}
			}
			
			
			prevTiles = tiles;
			
		}
	}

	public void process()
	{
		if (networkMode != NetworkMode.CLIENT)
		{
			updateCharacters();
			time.process();
			weather.process();
			allianceManager.process();
			updateLights();
			removeDeadItems();
			respawnEntities();
			moveEntities();
		}
		else
		{
			this.getHumanPlayer().process();
		}
		setCamera();
	}
	
	
	
	

	private void updateCharacters() {
		characters.clear();
		for (AbstractEntity e: entityFactory.getEntities())
		{
			if (e.isAlive() && e.getEntityType() == EntityType.CHARACTER)
				characters.add((Character)e);
		}
	}



	private void updateLights() 
	{
		for (Layer layer: layers)
		{
			for (Tile[] tiles: layer.getTiles())
			{
				for (Tile tile: tiles)
				{
					if (tile.getType() == TileType.DARK)
					{
						tile.updateLights();
					}
				}
			}
		}
		
		
	}



	private void removeDeadItems() 
	{
		List<Item> itemsToRemove = new ArrayList<Item>();
		for (Item item: itemFactory.getEntities())
		{
			if (!item.isAlive())
			{
				itemsToRemove.add(item);
			}
		}
		for (Item item: itemsToRemove)
		{
			itemFactory.removeItem(item);
		}
	}

	

	private void respawnEntities() 
	{
		for (AbstractEntity e: entityFactory.getEntities())
		{
			if (!e.isAlive() && e.getEntityType() == EntityType.CHARACTER)
			{
				Character character = (Character)e;
				character.tryRespawn();
			}
		}
	}

	private void moveEntities() 
	{
		for (AbstractEntity e: entityFactory.getEntities())
		{
			e.process();
		}
	}
	
	private void setCamera() 
	{
		if (humanPlayer != null)
		{
			camera.setPosition(humanPlayer.getPosition().getX(), humanPlayer.getPosition().getY());
		}
	}
	
	public Time getTime()
	{
		return time;
	}
	
	public Weather getWeather()
	{
		return weather;
	}
	
	public PlayerFactory getPlayerFactory()
	{
		return playerFactory;
	}
	
	public ItemFactory getItemFactory()
	{
		return itemFactory;
	}
	
	public EntityFactory getEntityFactory()
	{
		return entityFactory;
	}
	

	public WorldLoader getWorldLoader() 
	{
		return worldLoader;
	}
	
	public Layer getLayer(int number)
	{
		return layers[number];
	}
	
	public Layer[] getLayers()
	{
		return layers;
	}
	
	public int getNumLayers()
	{
		return layers.length;
	}

	public Camera getCamera() {
		return camera;
	}
	
	
	/**
	 * 
	 * @param row
	 * @param col
	 * @param layer
	 * @return a rgb colour to draw a tile on the offscreen mouse picking image
	 * See OpenGL colour picking technique.
	 */
	
	public int getColour(int row, int col, int layer)
	{
		int colour = (row*numRows+col)+(layer*numRows*numCols);
		
		//System.out.println("row="+row +", col="+col + ", layer="+layer +", colour="+colour);
		
		return colour;
	}
	
	public Tile getTile(int colour)
	{
		int layerNumber = colour / (numRows*numCols);
		colour -= layerNumber * (numRows*numCols);
		int row = colour / numRows;
		colour -= row*numRows;
		int col = colour;
		
		//col++;
		
		if (row < 0) row = 0;
		if (col < 0) col = 0;
		if (row > numRows-1) row = numRows-1;
		if (col > numCols-1) col = numCols-1;
		
		Layer layer = getLayer(layerNumber);
		return layer.getTiles()[row][col];
	}

	public HumanPlayer getHumanPlayer() 
	{
		return humanPlayer;
	}

	public float getTileWidth() 
	{
		return tileWidth;
	}
	public float getTileHeight() 
	{
		return tileHeight;
	}

	public void resetTiles(Tile goal) 
	{
		for (Layer layer: layers)
		{
			for (Tile[] row: layer.getTiles())
			{
				for (Tile t: row)
				{
					t.setVisited(false);
					t.calculateHeuristic(goal);
					t.setPathLength(0);
					t.setParent(null);
				}
			}
		}
		
	}

	public int pickRGBToTileRGB(int imageRGB) 
	{
		Color colour = new Color(imageRGB);
		
		int rgb = (colour.getRed() << 16 ) | (colour.getGreen()<<8) | colour.getBlue();
		return rgb;
	}

	public Tile getRandomEmptyTile() 
	{
		while(true)
		{
			int layer = (int) (Math.random()*layers.length);
			int row = (int) (Math.random()*numRows);
			int col = (int) (Math.random()*numCols);
			
			Tile tile = layers[layer].getTiles()[row][col];
			if (tile.isWalkable() && (tile.getRoof() == null || !tile.getRoof().isVisible()) && tile.isEmpty())
				return tile;
		}
	}
	
	private Tile getRandomEmptyGrassTile() 
	{
		while(true)
		{
			Tile tile = getRandomEmptyTile();
			if (tile.getType() == TileType.GRASS)
				return tile;
		}
	}
	
	public NetworkMode getNetworkMode()
	{
		return networkMode;
	}

	public Collection<Message> getAndClearMessages() {
		List<Message> temp = new ArrayList<Message>(messages);
		messages.clear();
		return temp;
	}

	public Player getRandomPlayer() 
	{
		int n = (int) (Math.random() * playerFactory.getEntities().size());
		Iterator<Player> iterator = playerFactory.getEntities().iterator();
		for (int i = 0; i < n; i++)
		{
			iterator.next();
		}
		return iterator.next();
	}

	public void createHumanPlayer(Tile spawnPosition, CharacterClassType type, String playerName, Color colour, int id) 
	{
		humanPlayer = (HumanPlayer)playerFactory.createPlayer(spawnPosition,playerName,type, id,PlayerType.HUMAN,colour,false);
	}

	public AbstractGame getGame() {
		return game;
	}



	public Character getRandomCharacter() 
	{
		if (characters.isEmpty())
			return null;
		
		return characters.get((int)(Math.random()*(characters.size()-1)));
	}
}
