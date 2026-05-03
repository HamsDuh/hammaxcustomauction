package hdh.hammaxcustomauction;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static java.sql.Types.NULL;

public class StorageItem {

    private final long itemID;
    private final String ownerUUID;
    private final ItemStack item;
    private final long dateAdded;
    private final long dateClaimed;


    public StorageItem(long givenItemID, String givenUUID, ItemStack givenStack, long givenDateAdded, long givenDateClaimed){
        this.itemID = givenItemID;
        this.ownerUUID = givenUUID;
        this.item = givenStack;
        this.dateAdded = givenDateAdded;
        this.dateClaimed = givenDateClaimed;

    }
    /*
        ItemStorage
            itemID INTEGER
            itemOwner VARCHAR(36)
            itemStack TEXT
            dateAdded BIGINT
            dateClaimed BIGINT
    */
    public long getItemID(){
        return  itemID;
    }
    public String getOwnerUUID(){
        return ownerUUID;
    }
    public ItemStack getStack(){
        return item;
    }
    public long getDateAdded() {
        return dateAdded;
    }
    public long getDateClaimed() {
        return dateClaimed;
    }
}
