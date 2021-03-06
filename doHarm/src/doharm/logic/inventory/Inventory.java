package doharm.logic.inventory;

import doharm.logic.entities.items.Item;
import doharm.logic.entities.items.ItemType;
import doharm.logic.entities.items.wearable.WearableItem;
import doharm.logic.world.tiles.Tile;

public class Inventory implements ItemContainer
{
	public static final int STASH_ROWS = 6;
	public static final int STASH_COLS = 10;
	
	private WearableItem[] slots;
	private Stash stash;
	private Belt belt;
	
	

	public Inventory()
	{
		slots= new WearableItem[SlotType.values().length];
		stash = new Stash(STASH_ROWS, STASH_COLS);
		belt = new Belt();
	}
	
	public WearableItem[] getSlots()
	{
		return slots;
	}
	
	public Stash getStash()
	{
		return stash;
	}
	
	/**
	 * @param item
	 * @return true if the item was picked up. Otherwise print inventory full!
	 */
	public boolean pickup(Item item)
	{
		//First check if can add to a slot.
		boolean pickedup = false;
		if (item.getItemType() == ItemType.WEARABLE)
		{
			addItemToSlot((WearableItem)item);
			pickedup = true;
		}
		
		if (!pickedup)
			pickedup = stash.pickup(item);
		
		
		if (pickedup)
		{
			item.setOnGround(false);
		}
		
		return pickedup;
	}

	@Override
	public boolean drop(Item item, ItemContainer destination)
	{
		if (destination.pickup(item))
		{
			stash.drop(item, destination);
			belt.drop(item, destination);
			deleteItem(item);
			return true;
		}
		return false;
	}
	
	@Override
	public void deleteItem(Item item) 
	{
		for (int i = 0; i < slots.length; i++)
		{
			if (slots[i] == item)
				slots[i] = null;
		}
	}

	


	private boolean addItemToSlot(WearableItem item) 
	{
		//special case for weapon slots
		if (item.getSlotType() == SlotType.WEAPON || item.getSlotType() == SlotType.WEAPON2)
		{
			
			//TODO two handed weapons and shield in slot 2
			if (slots[SlotType.WEAPON.ordinal()] == null)
			{
				slots[SlotType.WEAPON.ordinal()] = item;
				return true;
			}
			else if (slots[SlotType.WEAPON2.ordinal()] == null)
			{
				slots[SlotType.WEAPON2.ordinal()] = item;
				return true;
			}
		}
		else if (item.getSlotType() == SlotType.RING1)
		{
			if (slots[SlotType.RING1.ordinal()] == null)
			{
				slots[SlotType.RING1.ordinal()] = item;
				return true;
			}
			else if (slots[SlotType.RING2.ordinal()] == null)
			{
				slots[SlotType.RING2.ordinal()] = item;
				return true;
			}
		}
		else if (slots[item.getSlotType().ordinal()] == null)
		{
			slots[item.getSlotType().ordinal()] = item;
			return true;
		}
		
		
		return false;
	}

	public Belt getBelt() {
		return belt;
	}

	@Override
	public void dropAll(Tile dropTile) 
	{
		for (Item item: slots)
		{
			if (item != null)
				drop(item, dropTile);
		}
		stash.dropAll(dropTile);
		belt.dropAll(dropTile);
		
		
	}

	public boolean hasLight() 
	{
		return false;
	}

	


	
	
}
