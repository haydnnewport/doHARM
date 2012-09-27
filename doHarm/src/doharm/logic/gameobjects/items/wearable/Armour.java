package doharm.logic.gameobjects.items.wearable;

import doharm.logic.gameobjects.entities.inventory.SlotType;


/**
 * Armour permanently increases attributes while worn.
 * 
 * 
 * 
 * @author bewickrola
 */

public abstract class Armour extends WearableItem
{

	public Armour(SlotType slotType, int width, int height, int imageID) 
	{
		super(slotType, width, height, imageID);
	}

}
