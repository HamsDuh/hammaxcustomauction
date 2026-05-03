package hdh.hammaxcustomauction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AHAdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(!(sender instanceof Player)){
            //gleiches wie bei "TestCommand", es wird ein Spieler-Objekt benötigt und ich mache mir sorgen was passiert, wenn etwas anderes das nutzt
            sender.sendMessage("Avoided crashout: Please only run this command as a Player");
            //command wird abgebrochen
            return true;
        }

        Player player = (Player) sender;
        if (player.hasPermission("ahteam")) {
            AdminUI.openAdminUI(player, true, true);
        } else {
            player.sendMessage("§cDu hast nicht die Berechtigung dies zu nutzen");
        }

        return true;
    }



}
