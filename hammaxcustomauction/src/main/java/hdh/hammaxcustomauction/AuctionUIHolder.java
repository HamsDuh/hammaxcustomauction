package hdh.hammaxcustomauction;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AuctionUIHolder implements InventoryHolder {

    private final boolean local;
    private final boolean real;

    public AuctionUIHolder(boolean local, boolean real){
        this.local = local;
        this.real = real;
    }

    public boolean isLocal(){
        return local;
    }

    public boolean isReal(){
        return real;
    }

    @Override
    public Inventory getInventory(){
        return Bukkit.createInventory(this, 9);
    }

}
