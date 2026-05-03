package hdh.hammaxcustomauction;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NpcOpenAHCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){


        if (sender instanceof Player){
            sender.sendMessage("§cDieser befehl kann nur von NPCs genutzt werden");
            //sender.sendMessage("§cBitte return entKommentieren");
            return true;
        }


        if (args.length != 1){
            sender.sendMessage("§cBitte gebe EINEN spielernamen an (/npcOpenAH <player>)");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null){
            sender.sendMessage("§cSpieler nicht gefunden");
            return true;
        }

        AuctionUI.openUI(target, true, true);
        return true;
    }





}
