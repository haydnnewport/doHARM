package doharm.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class FloorTileData extends TileData{
	
	private String name;
	private boolean walkable;//in-file, is 0 for walkable, 1 for not walkable
	private int type; //0=wall 1=grass 2=water 3=object?
	private List<Integer> imageIDs;
	
	
	public FloorTileData(String line) 
	{
		imageIDs = new ArrayList<Integer>();
		
		Scanner scan = new Scanner(line);
		name = scan.next();
		walkable = (scan.nextInt() == 0) ? true : false;
		type = scan.nextInt();
		while (scan.hasNext())
		{
			imageIDs.add(scan.nextInt());
		}
	}


	public String getName() 
	{
		return name;
	}


	public int getType() 
	{
		return type;
	}


	/*public List<Integer> getImageIDs() 
	{
		return Collections.unmodifiableList(imageIDs);
	}*/


	public int getNumImages() 
	{
		return imageIDs.size();
	}
	
	public int getImageID(int imageNumber)
	{
		return imageIDs.get(imageNumber);
	}


	public int getNumFramesPerImage() {
		return 160; //TODO!
	}
	
	public boolean isWalkable(){
		return walkable;
	}
	
	
	
}