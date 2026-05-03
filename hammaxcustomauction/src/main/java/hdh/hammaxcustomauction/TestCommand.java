package hdh.hammaxcustomauction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
//TestCommand ist der eigendliche /ah oder /auction befehl, muss aber keiner wissen :p
public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    if(!(sender instanceof Player)){
        //kein plan was passieren würde wenn ein nicht-spieler das nutzen würde, gruselig
        sender.sendMessage("Avoided crashout: Please only run this command as a Player");
        //befehl wird in dem fall abgebrochen
        return true;
    }


        Player player = (Player) sender;
        //Permission check für Spieler
        if (player.hasPermission("ahuse")) {
            //es ist das command menü, desshalb local = false (für local = true zum NPC). local = false verhindert Lager-änderungen
            AuctionUI.openUI(player, false, true);
        }else {
            player.sendMessage("§cDu hast nicht die Berechtigung, dies zu nutzen.");
        }

        return true;
    }



}
