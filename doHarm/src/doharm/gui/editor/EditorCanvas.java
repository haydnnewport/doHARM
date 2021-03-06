package doharm.gui.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import doharm.gui.editor.EditorLogic.EditorTileSetLoader;
import doharm.storage.WorldLoader;

public class EditorCanvas extends JPanel {
    private ArrayList<EditorLayerData> layers = new ArrayList<EditorLayerData>();
    private int xDim, yDim;
    private int offsetX = 0, offsetY = 0;
    private int currentLayer = 0;
    private EditorTileSetLoader tiles;

    private final int TILE_SIZE = 25;

    public EditorCanvas() {
	super();
	setBackground(Color.black);
	tiles = loadTiles("tileset1.txt");
	setBounds(0, 0, 700, 600);
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	// BufferedImage below = null;
	// // This is commmented out to remove the lower layer drawing effect
	// if (currentLayer > 0 && layers.get(currentLayer - 1) != null) {
	// below = new BufferedImage(getWidth(), getHeight(),
	// BufferedImage.TYPE_INT_ARGB);
	// drawLayer(layers.get(currentLayer - 1), below.createGraphics(),
	// false);
	// }
	// // Do scaling
	// if (below != null) {
	// RenderUtil.scaleImage(below, (int) (getWidth() * .9),
	// (int) (getHeight() * .9));
	//
	// g.drawImage(EditorLogic.makeTransparent(below, .7f),
	// (int) (getWidth() * .05), (int) (getHeight() * .05), null);
	// }
	drawLayer(layers.get(currentLayer), (Graphics2D) g, true);
    }

    private void drawLayer(EditorLayerData layer, Graphics2D g, boolean drawGrid) {
	g.setStroke(new BasicStroke(2));
	g.setColor(new Color(0xAAAAAAAA, true));
	// Draw a grid
	if (drawGrid) {
	    for (int line = 0; line < (Math.max(getWidth(), getHeight()) / TILE_SIZE) + 1; line++) {
		g.drawLine(TILE_SIZE * line, 0, TILE_SIZE * line, getHeight());
		g.drawLine(0, TILE_SIZE * line, getWidth(), TILE_SIZE * line);
	    }
	}
	for (int x = offsetX; x < offsetX + getWidth() / TILE_SIZE + 1; ++x) {
	    if (x >= 0 && x < xDim)
		for (int y = offsetY; y < offsetY + getHeight() / TILE_SIZE + 1; ++y) {
		    if (y > 0 && y < yDim)
			g.drawImage(tiles.getTileImage(layer.getTileID(y, x)), TILE_SIZE * (x - offsetX), TILE_SIZE * (y - offsetY),
				TILE_SIZE, TILE_SIZE, null);
		}
	}
	if (drawGrid) {
	    g.setStroke(new BasicStroke(3));
	    g.setColor(new Color(0xFFFFFFFF, true));
	    g.drawLine(TILE_SIZE * (xDim + offsetX), 0, TILE_SIZE * (xDim + offsetX), TILE_SIZE * (yDim + offsetY));
	    g.drawLine(0, TILE_SIZE * (yDim + offsetY), TILE_SIZE * (xDim + offsetX), TILE_SIZE * (yDim + offsetY));
	    g.drawLine(TILE_SIZE * offsetX, 0, TILE_SIZE * offsetX, TILE_SIZE * (yDim + offsetY));
	    g.drawLine(0, TILE_SIZE * offsetY, TILE_SIZE * (xDim + offsetX), TILE_SIZE * offsetY);
	}

    }

    public EditorTileSetLoader loadTiles(String fname) {
	EditorTileSetLoader t = null;
	try {
	    t = new EditorTileSetLoader(fname);
	} catch (FileNotFoundException e) {
	    System.out.println("Tileset could not be found at " + fname);
	    e.printStackTrace();
	}
	return t;
    }

    public void setWorld(WorldLoader w) {
	xDim = w.getNumTilesX();
	yDim = w.getNumTilesY();
	layers.clear();
	for (int i = 0; i < w.getNumLayers(); i++) {
	    layers.add(i, new EditorLayerData(w.getLayerData(i), xDim, yDim));
	}
    }

    public Dimension getTileUnder(int x, int y) {
	return new Dimension(x / TILE_SIZE, y / TILE_SIZE);
    }

    public boolean setTileUnder(int x, int y, int tileType) {
//	if (x > (xDim - offsetX) * TILE_SIZE || y > (yDim - offsetY) * TILE_SIZE || y < offsetY * TILE_SIZE || x < offsetX * TILE_SIZE)
//	    return false;
	layers.get(currentLayer).setTileID(y / TILE_SIZE + offsetY, x / TILE_SIZE + offsetX, tileType);
	return true;
    }

    public int getCurrentLayer() {
	return currentLayer;
    }

    void changeLayer(int delta) {
	if (currentLayer + delta >= 0 && currentLayer + delta < layers.size())
	    currentLayer += delta;
    }

    void addLayer() {
	layers.add(currentLayer + 1, new EditorLayerData(xDim, yDim));
    }

    EditorTileSetLoader getTiles() {
	return tiles;
    }

    public void changeMapSize(int newX, int newY) {
	ArrayList<EditorLayerData> newMap = new ArrayList<EditorLayerData>();
	for (EditorLayerData layer : layers) {
	    newMap.add(new EditorLayerData(layer, xDim, yDim, newX, newY));
	}
	layers = newMap;
	xDim = newX;
	yDim = newY;

    }

    public int getXDim() {
	return xDim;
    }

    public int getYDim() {
	return yDim;
    }

    public void changeOffset(int deltaX, int deltaY) {
	offsetX += deltaX;
	offsetY += deltaY;
    }

    public void writeout() {
	JFileChooser jfc = new JFileChooser();
	jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	jfc.setDialogType(JFileChooser.SAVE_DIALOG);
	jfc.setCurrentDirectory(new File("res/worlds"));
	int result = jfc.showSaveDialog(this);
	if (result == JFileChooser.APPROVE_OPTION) {
	    File path = jfc.getSelectedFile();
	    String folderPath = path.getAbsolutePath();
	    System.out.println(folderPath);
	    File world = new File(folderPath + "/world.txt");
	    try {
		new File(folderPath + "/layers").mkdirs();
		world.createNewFile();
		PrintStream worldps = new PrintStream(world);
		worldps.println(xDim);
		worldps.println(yDim);
		worldps.println("tileset_new_format.txt");
		for (int layer = 0; layer < layers.size(); ++layer) {
		    worldps.println("layer" + layer + ".txt");
		    File currentLayer = new File(folderPath + "/layers/" + "layer" + layer + ".txt");
		    currentLayer.createNewFile();
		    new PrintStream(currentLayer).println(layers.get(layer).toString());
		}

	    } catch (FileNotFoundException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
