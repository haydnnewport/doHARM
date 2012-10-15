package doharm.logic.entities.inventory;

import doharm.logic.entities.items.Item;
import doharm.logic.world.tiles.Tile;

/**
 * A stash can store items in a grid like behaviour.
 * A player has a stash, so does a chest, etc.
 * 
 * @author bewickrola
 */

public class Stash implements ItemContainer
{
	private Item[][] items;
	private final int numRows;
	private final int numCols;
	
	public Stash(int numRows, int numCols)
	{
		this.numRows = numRows;
		this.numCols = numCols;
		
		items = new Item[numRows][numCols];
	}
	
	public Item[][] getItems()
	{
		return items;
	}


	@Override
	public void drop(Item item, Tile tile) {
		
		
	}
	@Override
	public boolean pickup(Item item) 
	{
		
		for (int row = 0; row < numRows; row++)
		{
			for (int col = 0; col < numCols; col++)
			{
				if (add(item, row,col))
					return true;
			}
		}
		
		return false;
	}
	
	private boolean add(Item item, int row, int col) 
	{
		for (int y = row; y < row+item.getStashSize().height; y++)
		{
			for (int x = col; x < col+item.getStashSize().width; x++)
			{
				if (y >= numRows || x >= numCols)
					return false;
				if (items[y][x] != null)
					return false;
			}
		}
		
		items[row][col] = item;
		
		return true;
	}

	
	public void reorder()
	{
		//TODO
	}
	
}
