package doharm.rendering;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.attribute.standard.MediaSize.ISO;

import doharm.logic.Game;
import doharm.logic.camera.Camera;
import doharm.logic.physics.Vector;
import doharm.logic.world.Layer;
import doharm.logic.world.World;
import doharm.logic.world.tiles.Direction;
import doharm.logic.world.tiles.Tile;
import doharm.storage.TilesetLoader;
import doharm.storage.WorldLoader;

public class WorldRenderer 
{
	private BufferedImage worldImage;
	private Graphics2D graphics;
	
	private BufferedImage pickImage;
	private Graphics2D pickGraphics;
	
	private Dimension canvasSize;
	//private BufferedImage[] images;
	
	private BufferedImage[] floorImages;
	private BufferedImage[] wallImages;
	
	private BufferedImage[] floorImagesTrans;//transparent versions of flootImages and wallImages.
	private BufferedImage[] wallImagesTrans;
	
	private AffineTransform transform;
	private Game game;

	private PlayerRenderer playerRenderer;
	
	
	
	private  int fTileW;
	private  int fTileH;

	private int wTileW;
	private  int wTileH;
	
	public WorldRenderer(Game game)
	{
		this.game = game;
		playerRenderer = new PlayerRenderer(game);
		canvasSize = new Dimension();
		transform = new AffineTransform();
		
		newLoadTileSets();
		RenderUtil.setImgDimensions(fTileW, fTileH);
		System.out.println("Wall tile width:"+ wTileW+ "    wall tile height: "+wTileH);
		
		
	}
	


	public void createImage(Dimension canvasSize)
	{
		this.canvasSize = canvasSize;

		worldImage = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
		graphics = worldImage.createGraphics();
		
		pickImage = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
		pickGraphics = pickImage.createGraphics();
	}
	
	
	public BufferedImage getImage() 
	{
		return worldImage;
	}

	public void redraw(Dimension canvasSize) 
	{
		if (!this.canvasSize.equals(canvasSize))
			createImage(canvasSize); //resize the canvas
	
		Camera camera = game.getCamera();
		
		//give the camera the canvas size so we can calculate the centre of the screen
		camera.setCanvasDimensions(canvasSize);
		
		transform.setToIdentity();
		
		graphics.setTransform(transform);
		pickGraphics.setTransform(transform);
		
		//clear the screen
		graphics.setColor(Color.black);
		graphics.fillRect(0, 0, canvasSize.width, canvasSize.height);
		
		
		//clear the mouse pick image
		pickGraphics.setColor(Color.black);
		pickGraphics.fillRect(0, 0, canvasSize.width, canvasSize.height);
		
		//transform.translate(v.getX(),v.getY());
		transform.translate(-camera.getRenderPosition().getX(), -camera.getRenderPosition().getY());
		
		graphics.setTransform(transform);
		pickGraphics.setTransform(transform);
		//draw the current game, based on the camera, etc.
	
		
		renderWorldIso();
	
		playerRenderer.redraw(graphics, fTileW, fTileH);
		
		
		//TODO REMOVE
		transform.setToIdentity();
		graphics.setTransform(transform);
		
		Composite old = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		
		graphics.drawImage(pickImage, 0, 0, null);
		graphics.setComposite(old);
	}

	public int getPickColourAt(int mouseX, int mouseY)
	{
		if (mouseX < 0) mouseX = 0;
		if (mouseY < 0) mouseY = 0;
		if (mouseX > pickImage.getWidth()-1) mouseX = pickImage.getWidth()-1;
		if (mouseY > pickImage.getHeight()-1) mouseY = pickImage.getHeight()-1;
		
		return game.getWorld().pickRGBToTileRGB(pickImage.getRGB(mouseX, mouseY));
	}
	
	
	private void renderWorldIso(){
		World world = game.getWorld();
	
		Layer[] layers = world.getLayers();
		for(int layerCount = 0; layerCount < layers.length; layerCount++){
			

			Tile[][] tiles = layers[layerCount].getTiles();
			
			boolean isTransparent = false;
			//TODO this must be changed when camera views are implemented.
			//if(tile above the player with respect to the isometric view, 
			//ie. the tile(s) obscuring view of the player, is not an invisible tile, make this entire layer transparent.
			//and dont draw any subsequent layers.
			
			graphics.setColor(new Color(1,0,1,0.4f));
			for(int row = 0; row < tiles.length; row++){
				
				for(int col = 0; col < tiles[row].length; col++){
					Tile tile = tiles[row][col];
					
					BufferedImage image = floorImages[tile.getImageID()];

					
					Vector vector = RenderUtil.convertCoordsToIso(col, row, layerCount);
					int x = vector.getXAsInt() - fTileW/2; //fTileW/2 added PLEASE leave in here
					int y = vector.getYAsInt() - fTileH/2; //fTileH/2 added PLEASE leave in here
					graphics.drawImage(image,x,y, null);
					
					if (tile.isWalkable()) //TODO remove
						graphics.fillRect(x-1+fTileW/2, y-1+fTileH/2, 2, 2);
					
					
					

					if(tile.isWalkable() && layerCount==0){
						
						
						pickGraphics.drawImage(tile.getPickImage(), x,y,null);
					
					
					}
					
					if(tile.getImageID() != 2 ){

						int imgID = 2;//tile.getWallImageID(Direction.UP);
						
						
						image = wallImages[imgID++];
						y+=fTileH/2;
						
						graphics.drawImage(image,x,y, null);
						image = wallImages[imgID];
						x+=wTileW;
						graphics.drawImage(image,x,y, null);
					}
					
					
				
					
					
					
					
				}
			}
		
		}
		
	}
	
	private void createTransparentImages(){
		
		/*
			BufferedImage transparentImage = new BufferedImage(tileSet.getWidth(),tileSet.getHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics2D transparentGraphics = transparentImage.createGraphics();
			transparentGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			transparentGraphics.drawImage(tileSet, 0,0,null);
		*/
		/*
		 * 
		 * 	Composite old = backbufferGraphics.getComposite();
			backbufferGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			backbufferGraphics.drawImage(boardImage, 0, 0,boardImage.getWidth(),boardImage.getHeight(),null);	
			backbufferGraphics.setComposite(old);
			drawBoard();
			backbufferGraphics.setComposite(old);
		 * 
		 * 
		 */
		
		
		
		
		
	}
	
	
	private void newLoadTileSets(){
		World world = game.getWorld();
		BufferedImage tileSet = null;
		WorldLoader wl = world.getWorldLoader();
		//TODO remove
	
		
		TilesetLoader tsl = wl.getTilesetLoader();
		
		fTileW = tsl.getFloorTileWidth();
		fTileH = tsl.getFloorTileHeight();
		
		
		wTileW = tsl.getWallTileWidth();
		wTileH = tsl.getWallTileHeight();
		
		
		//TODO not right
		floorImages = new BufferedImage[10];
		wallImages = new BufferedImage[20];
		
		try{
			tileSet = ImageIO.read(new File("res/tilesets/"+tsl.getFloorTileSetImage()));
			
		
			
			for(int r = 0; r < tileSet.getHeight()/fTileH; r++){
				for(int c = 0; c < tileSet.getWidth()/fTileW; c++)
				{
			
					
					BufferedImage n = new BufferedImage(fTileW, fTileH, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = n.createGraphics();
					g.drawImage(tileSet,0, 0,fTileW, fTileH, c*fTileW, r*fTileH, fTileW*(c+1), (r+1)*fTileH, null);
					floorImages[((tileSet.getHeight()/fTileH)*r) + c] = n;
				}
			}
			
			
			//load the wall tiles
		
			tileSet = ImageIO.read(new File("res/tilesets/"+tsl.getWallTileSetImage()));
		
			for(int r = 0; r < tileSet.getHeight()/wTileH; r++)
			{
				for(int c = 0; c < tileSet.getWidth()/wTileW; c++)
				{
					BufferedImage n = new BufferedImage(wTileW, wTileH, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = n.createGraphics();
					g.drawImage(tileSet,0, 0,wTileW, wTileH, c*wTileW, r*wTileH, wTileW*(c+1), (r+1)*wTileH, null);
					
					wallImages[((tileSet.getHeight()/wTileH)*r) + c] = n;
				}
			}
			
		}catch(IOException e){
			System.out.println(e);
		}
		
	}

	
}

