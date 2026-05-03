package hdh.hammaxcustomauction;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class ASetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Avoided crashout: Please only run this command as a Player");
            return true;
        }



        Player player = (Player) sender;

        if (!player.hasMetadata("active_auction_creation") && !player.hasMetadata("auction_step") &&
                !player.hasMetadata("filter_edit") && !player.hasMetadata("bid_price_input") &&
                !player.hasMetadata("admin_player_search_input")){
            player.sendMessage("§cKeine Eingabe erwartet");
            return true;
        }

        if (args.length != 1 && args.length != 3) {

            player.sendMessage("§cKeine gültige eingabe gefunden");
            player.sendMessage("§7Bitte nur einen Wert eingeben (Bsp: \"/aset 10\" -> aktuelle Eingabe wird auf 10 gesetzt)");
            player.sendMessage("§7Wird mit dem Befehl eine Auktion erstellt, können alle 3 Werte auf einmal gegeben werden");
            player.sendMessage("§7(Bsp: \"/aset 1000 100 7\" -> Startpreis = 1000, Erhöhung = 100, Dauer = 7Tage");
            return true;
        }

        if (args.length == 3) {

            if (player.hasMetadata("active_auction_creation")) {
                if (player.hasMetadata("auction_step")) {

                    Auction auction = (Auction) player.getMetadata("active_auction_creation").get(0).value();

                    String step = player.getMetadata("auction_step").get(0).value().toString();

                    //player.sendMessage("input ist: " + input);          //debug

                    if (step.equals("auction_set_price") || step.equals("auction_set_increase") || step.equals("auction_set_deadline")) {
                        double price = ConversionManager.signToMoneyDouble(args[0]);
                        auction.setStartingPrice(price);
                        player.sendMessage("§7Startpreis gesetzt: " + auction.getStartingPrice());

                        auction.setBidIncrease(ConversionManager.signToMoneyDouble(args[1]));
                        player.sendMessage("§7Erhöhungspreis gesetzt: " + auction.getBidIncrease());

                        auction.setDeadline(ConversionManager.chatToDuration(args[2]));
                        player.sendMessage("§7Auktionsdauer gesetzt auf "
                                //+ auction.getDeadline()
                                + ConversionManager.durationToDays(ConversionManager.correctTimeOffset(auction.getDeadline()))
                                + " Tage");
                    }


                    player.removeMetadata("auction_step", Hammaxcustomauction.getInstance());
                    Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> {
                        player.closeInventory();
                        AuctionUI.itemAuctionCreationUI(auction, player, false, true);
                    });
                    return true;
                }

            }

        }

        if (args.length == 1) {

                String input = args[0];

                if (player.hasMetadata("active_auction_creation")) {
                    if (player.hasMetadata("auction_step")) {

                        Auction auction = (Auction) player.getMetadata("active_auction_creation").get(0).value();

                        String step = player.getMetadata("auction_step").get(0).value().toString();

                        //player.sendMessage("input ist: " + input);          //debug

                        if (step == "auction_set_price") {
                            double price = ConversionManager.signToMoneyDouble(input);
                            auction.setStartingPrice(price);
                            player.sendMessage("§7Startpreis gesetzt: " + auction.getStartingPrice());
                        } else if (step.equals("auction_set_increase")) {
                            auction.setBidIncrease(ConversionManager.signToMoneyDouble(input));
                            player.sendMessage("§7Erhöhungspreis gesetzt: " + auction.getBidIncrease());

                        } else if (step.equals("auction_set_deadline")) {
                            auction.setDeadline(ConversionManager.chatToDuration(input));
                            player.sendMessage("§7Auktionsdauer gesetzt auf "
                                    //+ auction.getDeadline()
                                    + ConversionManager.durationToDays(ConversionManager.correctTimeOffset(auction.getDeadline()))
                                    + " Tage");
                        }


                        player.removeMetadata("auction_step", Hammaxcustomauction.getInstance());
                        Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> {
                            player.closeInventory();
                            AuctionUI.itemAuctionCreationUI(auction, player, false, true);
                        });
                        return true;
                    }
                }

                if (player.hasMetadata("auction_filter")) {
                    if (player.hasMetadata("filter_edit")) {    //set_filter_name


                        AuctionFilter filter = (AuctionFilter) player.getMetadata("auction_filter").get(0).value();


                        if (player.getMetadata("filter_edit").get(0).value().toString().equals("set_filter_name")) {
                            filter.setNameFilter(input);
                        }

                        player.removeMetadata("filter_edit", Hammaxcustomauction.getInstance());
                        player.removeMetadata("auction_filter", Hammaxcustomauction.getInstance());
                        player.setMetadata(
                                "auction_filter",
                                new FixedMetadataValue(Hammaxcustomauction.getInstance(), filter)
                        );
                        Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> {
                            player.closeInventory();
                            AuctionUI.openYourFilterSettingsUI(player, false, true);
                        });
                        return true;
                    }

                }

                if (player.hasMetadata("bid_price_input")) {

                    long auctionID = Long.valueOf(player.getMetadata("bid_price_input").get(0).value().toString());


                    double convertedValue = ConversionManager.signToMoneyDouble(input);

                    player.removeMetadata("bid_price_input", Hammaxcustomauction.getInstance());
                    Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> {
                        player.closeInventory();
                        AuctionUI.openBidCreationUI(player, auctionID, convertedValue, false, true);
                    });
                    return true;
                }

            }

        if(player.hasPermission("ahteam")){
            if (player.hasMetadata("admin_player_search_input")) {

                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                if (target == null) {
                    sender.sendMessage("§cSpieler nicht gefunden");
                    return true;
                }

                player.removeMetadata("admin_player_search_input", Hammaxcustomauction.getInstance());
                AdminUI.openAdminPlayerActions(player,target.getUniqueId().toString(),true, true);
                return true;
            }
        }

            return true;
    }

}
