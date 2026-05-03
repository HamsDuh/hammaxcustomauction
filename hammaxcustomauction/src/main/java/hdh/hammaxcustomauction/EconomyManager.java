package hdh.hammaxcustomauction;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EconomyManager {

    private final Economy economy;

    public EconomyManager(Economy economy){
        this.economy = economy;
    }

    public boolean hasEnoughMoney(UUID uuid, double amount){
        //OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(uuid);

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()){
            return false;
        }

        return economy.getBalance(player) >= amount;
    }

    public double getBalance(UUID uuid){
        OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        return economy.getBalance(player);
    }

    public boolean withdrawlSuccess(UUID uuid, double amount){
        //OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(uuid);

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()){
            return false;
        }

        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean reimburseSuccess(UUID uuid, double amount){

        //OfflinePlayer player = org.bukkit.Bukkit.getOfflinePlayer(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()){
            //System.out.println("reimbursement please send false");
            return false;
        }

        return economy.depositPlayer(player, amount).transactionSuccess();
    }





}
