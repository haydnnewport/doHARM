package doharm.logic.entities.items;

import doharm.logic.entities.AbstractEntity;
import doharm.logic.entities.EntityType;
import doharm.logic.entities.characters.classes.attributes.Attributes;

/**
 * 
 * Uses the visitor pattern 
 * 
 * An item is an entity as it can be dropped on the ground
 * 
 * 
 * @author bewickrola
 */


public abstract class Item extends AbstractEntity
{
	private Attributes minimumAttributes;
	private int width; //in stash
	private int height; //in stash
	private int imageID;
	private ItemType itemType;
	
	
	/*public boolean canUse(Player player)
	{
		
	}*/
	
	protected Item(ItemType type, int width, int height, int imageID)
	{
		super(EntityType.ITEM);
		this.itemType = type;
		this.width = width;
		this.height = height;
		this.imageID = imageID; 
	}
	
	public ItemType getItemType()
	{
		return itemType;
	}

	public int getHeight() {
		return height; //TODO
	}

	public int getWidth() {
		return width; //TODO
	}
	
	/**
	 * NOTE: this will map to two different images (Each in an array):
	 * -The inventory image, and
	 * -The world image (when the item is lying on the ground)
	 * 
	 * @return the the image id of this item.
	 */
	public int getImageID()
	{
		return imageID;
	}
}
