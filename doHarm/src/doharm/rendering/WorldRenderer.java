package doharm.rendering;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import doharm.logic.Game;
import doharm.logic.camera.Camera;
import doharm.logic.world.Layer;
import doharm.logic.world.Tile;
import doharm.logic.world.World;
import doharm.storage.TilesetLoader;
import doharm.storage.WorldLoader;

public class WorldRenderer 
{
	private BufferedImage worldImage;
	private Graphics2D graphics;
	private Dimension canvasSize;
	private BufferedImage[] images;
	
	private BufferedImage[] imagesIso;//As images, but the isometric versions.
	
	private AffineTransform transform;
	private Game game;
	
	
	
	private PlayerRenderer playerRenderer;
	
	
	private int imgSize;//Tiles assumed to be square.
	
	
	private int imgIsoW;
	private int imgIsoH;
	
	public WorldRenderer(Game game)
	{
		this.game = game;
		playerRenderer = new PlayerRenderer(game);
		canvasSize = new Dimension();
		transform = new AffineTransform();
		loadTileSets();
		newLoadTileSets();
		//createIsoImages();
	}
	


	public void createImage(Dimension canvasSize)
	{
		this.canvasSize = canvasSize;

		worldImage = new BufferedImage(canvasSize.width, canvasSize.height, BufferedImage.TYPE_INT_ARGB);
		graphics = worldImage.createGraphics();
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
		
		//clear the screen
		graphics.setColor(Color.black);
		graphics.fillRect(0, 0, canvasSize.width, canvasSize.height);
		
		transform.translate(-camera.getRenderPosition().getX(), -camera.getRenderPosition().getY());
		graphics.setTransform(transform);
		//draw the current game, based on the camera, etc.
	
		renderWorldIso();
	
		playerRenderer.redraw(graphics);
		
	
	}

	
	private void renderTiles(){
		World world = game.getWorld();
		Layer layer = world.getLayer(0);
		
		Tile[][] tiles = layer.getTiles();
		
		for(int r = 0; r < tiles[0].length; r++)
		{
			for(int c = 0; c < tiles.length; c++)
			{
				Tile tile = tiles[c][r];
				graphics.drawImage(images[tile.getImageID()], c*imgSize, r*imgSize, null);
			}
		}
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
			
			for(int r = 0; r < tiles[0].length; r++)
			{
				for(int c = 0; c < tiles.length; c++)
				{
					Tile tile = tiles[c][r];
					
					graphics.drawImage(images[tile.getImageID()], (-r*(imgIsoW/2-1))+(c*(imgIsoW/2-1)), (r*(imgIsoH/2-1))+(c*(imgIsoH/2-1))-(layerCount*imgIsoH), null);
				}
			}
		
		}
	}

	/**
	 * rotates and scales the elements of "images" for the isometric view
	 * and adds them to "imagesIso".
	 */
	private void createIsoImages(){
		imagesIso = new BufferedImage[images.length];
		int c = 0;
		for(BufferedImage img : images){
			BufferedImage b = new BufferedImage(imgIsoW, imgIsoW, BufferedImage.TYPE_INT_ARGB);
		
			AffineTransform trans = AffineTransform.getRotateInstance(Math.PI/4, ((double)imgIsoW)/2.0, ((double)imgIsoW)/2.0);
			
			Graphics2D g = (Graphics2D)b.getGraphics();
			g.setTransform(trans);
			g.drawImage(img, img.getWidth()/4, img.getHeight()/4, null);
			b = RenderUtil.scaleImage(b, imgIsoW, imgIsoH);
			imagesIso[c] = b;
			c++;
		}
		
	}
	
	private void loadTileSets(){
		World world = game.getWorld();
		BufferedImage tileSet = null;
		WorldLoader wl = world.getWorldLoader();
		
		
		TilesetLoader tsl = wl.getTilesetLoader();
		imgSize = tsl.getTileWidth();
		imgIsoW = tsl.getTileWidth();
		imgIsoH = tsl.getTileHeight();
		
		
		images = new BufferedImage[tsl.getNumTiles()];
		
		int width = tsl.getTileWidth();
		int height = tsl.getTileHeight();
		
		try{
			tileSet = ImageIO.read(new File("res/tilesets/"+tsl.getTileSetImage())); 
			
			for(int r = 0; r < tileSet.getHeight()/height; r++)
			{
				for(int c = 0; c < tileSet.getWidth()/width; c++)
				{
					BufferedImage n = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = n.createGraphics();
					g.drawImage(tileSet,0, 0,width, height,	c*width, r*height, width*(c+1), (r+1)*height, null);
					
					images[((tileSet.getHeight()/height)*r) + c] = n;
				}
			}
			
		}catch(Exception e){}
		
	}	
	
	private void newLoadTileSets(){
		World world = game.getWorld();
		BufferedImage tileSet = null;
		WorldLoader wl = world.getWorldLoader();
		
		
		TilesetLoader tsl = wl.getTilesetLoader();
		imgSize = tsl.getTileWidth();
		imgIsoW = tsl.getTileWidth();
		imgIsoH = 23;
		
		
		images = new BufferedImage[tsl.getNumTiles()];
		
		int width = tsl.getTileWidth();
		int height = tsl.getTileHeight();
		
		try{
			tileSet = ImageIO.read(new File("res/tilesets/"+tsl.getTileSetImage()));
			
			
			/*
			BufferedImage transparentImage = new BufferedImage(tileSet.getWidth(),tileSet.getHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics2D transparentGraphics = transparentImage.createGraphics();
			transparentGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			transparentGraphics.drawImage(tileSet, 0,0,null);
			*/
			
			/*
			 * 
			 * 
			 * 
			 * 
			 * Composite old = backbufferGraphics.getComposite();
			backbufferGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			//backbufferGraphics.drawImage(boardImage, 0, 0,boardImage.getWidth(),boardImage.getHeight(),null);	
			//backbufferGraphics.setComposite(old);
			drawBoard();
			backbufferGraphics.setComposite(old);
			 * 
			 * 
			 */
			
			for(int r = 0; r < tileSet.getHeight()/height; r++)
			{
				for(int c = 0; c < tileSet.getWidth()/width; c++)
				{
					BufferedImage n = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = n.createGraphics();
					g.drawImage(tileSet,0, 0,width, height,	c*width, r*height, width*(c+1), (r+1)*height, null);
					
					images[((tileSet.getHeight()/height)*r) + c] = n;
				}
			}
			
		}catch(Exception e){}
		
	}

	
}
