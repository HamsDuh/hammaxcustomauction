package hdh.hammaxcustomauction;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class UIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        //ist player?
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        //Hat es den auktionUI als holder
        if (!(event.getView().getTopInventory().getHolder() instanceof AuctionUIHolder)) return;
        AuctionUIHolder holder = (AuctionUIHolder) event.getView().getTopInventory().getHolder();
        boolean local = holder.isLocal();       //wurde AH mit befehl oder NPC geöffnet
        boolean real = holder.isReal();         //schutz vor falschem UI -> sicherheitscheck

        //ist in UI -> klick wird abgebrochen -> item wird nicht genommen/verschoben
        event.setCancelled(true);

        //wenn luft geklickt wird passiert nix
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;

        //nur items mit meta (beispielsweise "ui_item" als marker) sollen etwas machen.
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        //Tag lesen, empfehle ich weiter zu nutzen
        String tag = clickedItemMeta.getPersistentDataContainer().get(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING
        );
        NamespacedKey key = new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id");
        long itemID = -1;       //itemID wird hauptsächlich für IDs genutzt, hier und da aber auch einfach als reminder (bsp: auf welcher Seite ist man)
        if (clickedItemMeta.getPersistentDataContainer().has(key,PersistentDataType.LONG)) {
            itemID = clickedItemMeta.getPersistentDataContainer().get(key,
                    PersistentDataType.LONG);
        }

        //der spieler sollte bid_price_input nur außerhalb des UIs haben, sonst können sachen kompliziert werden
        if(player.hasMetadata("bid_price_input")){
            player.removeMetadata("bid_price_input", Hammaxcustomauction.getInstance());
        }

        //ist teil meines UI? falls nicht, vielleicht adminUI?
        int uiListNumber = isInUIList(event, tag);
        int adminUiListNumber = -1;                     //haupt orientierung in welchem UI man ist. Bei erweiterungen einfach isInUIList array erweitern
        if (uiListNumber == -1){                        //nach array erweitern, einfach ein weiteres else if mit höherer uiListNumber nutzen
            if (real && local){
                adminUiListNumber = isInAdminUIList(event,tag);     //gleiches für admin Ui-List
            }
            if (adminUiListNumber == -1){
                return;
            }
        }

        if (tag == null) return;

        if (!player.hasPermission("ahuse") && !player.hasPermission("ahteam")){
            player.sendMessage("§cDu hast nicht die Berechtigungen, dies zu nutzen");
        }

        //Alle SpielerUI Funktionen sind hier \/ in der if - else if schleife
        if (uiListNumber == 0 && player.hasPermission("ahuse")) {                                   //user UI 0(das was bei /ah kommt)
            if (tag.equals("your_items_button")) {
                AuctionUI.openYourItemsUI(player, local, real);
            } else if (tag.equals("your_listings_button")) {
                AuctionUI.openYourListingsUI(player, local, real);
            } else if (tag.equals("your_bets_button")) {
                AuctionUI.openYourBidsUI(player,0, local, real);
            } else if (tag.equals("all_listings_button")) {
                if (!player.hasMetadata("auction_filter")){
                    AuctionFilter auctionFilter = new AuctionFilter();
                    player.setMetadata(
                            "auction_filter",
                            new FixedMetadataValue(Hammaxcustomauction.getInstance(),auctionFilter)
                    );
                }
                AuctionUI.openAllListingsSelectionUI(player, local, real);
            } else if (tag.equals("logs_and_info")) {
                AuctionUI.openLogOrInfoSelection(player, local,real);
            }
        } else if (uiListNumber == 1) {                                                             //Itemlager UI 1
            if (tag.equals("to_previous_button")) {
                AuctionUI.openUI(player, local, real);
            } else if (tag.equals("add_items_button")) {
                if(local && real) {
                    AuctionUI.addItemToStorageUI(player, local, real);
                } else if (!local) {
                    player.sendMessage("§aBitte gehe für diese Funktion zum Auktionshaus NPC");
                } else{
                    player.sendMessage("§aWie bist du hierher gekommen?");
                }
            } else if (itemID != -1 && local && real) {
                AuctionUI.removeItemFromStorageUI(player, local, real, itemID);
            }
        } else if (uiListNumber == 2) {                                                           //Deine listings 2
            if (tag.equals("to_previous_button")) {
                AuctionUI.openUI(player, local, real);
            } else if (tag.equals("add_items_button")) {
                AuctionUI.listingsAddableItemsUI(player, local, real);
            } else if (itemID != -1){
                AuctionUI.auctionOwnerAuctionInfoUI(player, itemID, local, real);
            }
        } else if (uiListNumber == 3) {                                                             //Deine Gebote 3
            if (tag.equals("to_previous_button")) {
                AuctionUI.openUI(player, local, real);
            } else if (tag.equals("refresh_page")) {
                AuctionUI.openYourBidsUI(player,(int) itemID,local,real);
            } else if (tag.equals("prev_page")) {
                AuctionUI.openYourBidsUI(player, (int) itemID - 1, local,real);
            } else if (tag.equals("next_page")) {
                AuctionUI.openYourBidsUI(player,(int) itemID +1, local,real);
            } else if (tag.equals("auction_item")) {    //-1 als Standard der sagt: "ergenze den Preis automatisch"
                AuctionUI.openBidCreationUI(player,itemID,-1,local,real);
            }
        } else if (uiListNumber == 4) {                                                             //alle Auktionen Liste 4
            AuctionFilter auctionFilter = new AuctionFilter();
            if (!player.hasMetadata("auction_filter")){
                player.setMetadata(
                        "auction_filter",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),auctionFilter)
                );
            } else if (player.hasMetadata("auction_filter")) {
                auctionFilter = (AuctionFilter) player.getMetadata("auction_filter").get(0).value();
            }

            if (tag.equals("to_previous_button")) {
                AuctionUI.openUI(player, local, real);
            }else if (tag.equals("prev_offers_button")) {
                auctionFilter.setPage(auctionFilter.getPage() -1);
                player.setMetadata(
                        "auction_filter",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),auctionFilter)
                );
                AuctionUI.openAllListingsSelectionUI(player, local, real);
            }else if (tag.equals("next_offers_button")) {
                auctionFilter.setPage(auctionFilter.getPage() +1);
                player.setMetadata(
                        "auction_filter",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),auctionFilter)
                );
                AuctionUI.openAllListingsSelectionUI(player, local, real);
            }else if (tag.equals("refresh_offers_button")) {
                AuctionUI.openAllListingsSelectionUI(player, local, real);
            } else if (tag.equals("set_filter_button")) {
                AuctionUI.openYourFilterSettingsUI(player, local, real);
            }else if (tag.equals("auction_item")){
                if (itemID != -1) {
                    AuctionUI.openBidCreationUI(player,itemID,-1,local,real);
                }
            }
        } else if (uiListNumber == 5) {                                                                             //item to storage 5
            if (tag.equals("to_previous_button")){
                AuctionUI.openYourItemsUI(player,local,real);
            } else if (tag.equals("addable_item_ui_element") && local && !SettingsManager.isAddItemAllowed()) {
                player.closeInventory();                                        //item darf aber funktion ist aus
                player.sendMessage("§cMomentan können keine neuen Items in das Auktionshaus gelegt werden");
            } else if (tag.equals("addable_item_ui_element") && local && SettingsManager.isAddItemAllowed()) {
                ItemStack tempItem = clickedItem;                               //item darf und funktion ist an
                ItemMeta tempItemMeta = tempItem.getItemMeta();
                tempItemMeta.getPersistentDataContainer().remove(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"));
                tempItem.setItemMeta(tempItemMeta);
                //es wird gesucht bis ein item gefunden und gelöscht wird
                boolean itemDeleted = false;

                //item ist nicht problematisch
                if (SettingsManager.isProblematicMaterial(tempItem.getType()) == -1) {//standardwert für kein problem
                    for (int i = 0; i < player.getInventory().getSize() && !itemDeleted; i++) {
                        ItemStack inventoryItem = player.getInventory().getItem(i);
                        if (inventoryItem == null) continue;       //leere slots überspringen

                        //item gleicht genau dem gesuchten stack. erst wenn der stack gefunden wurde, wird er gelöscht und hinzugefügt.
                        //sollen cheater versuchen mit package delay items zu duplizieren sollte, weil es nicht async läuft der stack nicht gefunden werden,
                        //-> item wird nicht hinzugefügt -> kein dupe
                        if (/*inventoryItem.isSimilar(tempItem)*/ inventoryItem.serialize().equals(tempItem.serialize())
                                && inventoryItem.getAmount() == tempItem.getAmount()) {

                            player.getInventory().setItem(i, null);
                            Hammaxcustomauction.getInstance().getDbManager().addItemToStorage(player, inventoryItem, local, real);
                            //player.sendMessage("§eItem Hinzugefügt");
                            itemDeleted = true;                                 //item wurde gelöscht, Schleife endet
                        }

                    }
                } else if (SettingsManager.isProblematicMaterial(tempItem.getType()) != -1) {   //item ist problematisch... keine ahnung warum, schau am besten
                    //System.out.println("checkProblematic");                                   //im SettingsManager nach
                    int tempProblematicNr = SettingsManager.isProblematicMaterial(tempItem.getType());
                    for (int i = 0; i < player.getInventory().getSize() && !itemDeleted; i++){          //
                        ItemStack inventoryItem = player.getInventory().getItem(i);
                        if (inventoryItem == null) continue;
                        if (SettingsManager.isProblematicMaterial(inventoryItem.getType()) != tempProblematicNr) continue;

                        //SettingsManager.problematicsMatch ist weniger streng wie .equals aber sollte genauso funktionieren.
                        //Sicherheitsmäßig sollte es genauso wie oben passen
                        if (SettingsManager.problematicsMatch(tempItem, inventoryItem)){
                            player.getInventory().setItem(i, null);
                            Hammaxcustomauction.getInstance().getDbManager().addItemToStorage(player, inventoryItem, local, real);
                            //player.sendMessage("§eItem Hinzugefügt");
                            itemDeleted = true;
                        }

                    }


                }
                AuctionUI.openYourItemsUI(player,local,real);       //zurück zum Itemlager
            }
        } else if (uiListNumber == 6) {                                                                 //Item aus storage abholen 6
            if (tag.equals("to_previous_button")){
                AuctionUI.openYourItemsUI(player,local,real);
            } else if (tag.equals("confirm_button") && local && real){

                //Slot reserviert damit Item nicht verloren geht. Die Items aus dem AH sind in meisten fällen wichtiger als random items die man
                //zugeworfen bekommt
                int guaranteedSlot = player.getInventory().firstEmpty();
                //falls inventar voll ist und das gesehen wird, wird nix gelöscht und man wird darauf hingewiesen
                if(guaranteedSlot == -1){
                    player.sendMessage("§cDein Inventar ist voll");
                }
                else {
                    //Ist das Inventar nicht voll, dann versuche das Item zu geben
                    AuctionUI.openYourItemsUI(player,local,real);
                    if (itemID != -1) { //nur wenn ein richtiges Item gewählt wurde (und versuche out of bounds zu vermeiden)

                        long tempIDvalue = itemID;      //claimstorageItem wollte einen final wert haben und war mit itemID nicht zufrieden
                        Hammaxcustomauction.getInstance().getDbManager().getItemFromID(itemID, storageItem -> {

                            //Wäre problematisch wenn das auftritt aber sollte nix weiter kaputt machen als esin dem fall ist
                            if (storageItem == null || storageItem.getStack() == null || storageItem.getStack().getType() == Material.AIR) {
                                player.sendMessage("§cEin Fehler beim Laden des Items ist aufgetreten");
                                return;
                            }

                            ItemStack giveItem = storageItem.getStack();    //erst iteminfos holen

                            //itemstatus als geclaimed setzen und auf änderungsbestätigung warten
                            //stellt ABSOLUT sicher, dass Items nicht dupliziert werden
                            Hammaxcustomauction.getInstance().getDbManager().claimStorageItem(player, tempIDvalue, success ->{
                                if (success){   //wenn in der DB etwas geändert wurde (item wurde erfolgreich als geclaimed markiert)
                                    player.getInventory().setItem(guaranteedSlot, giveItem);    //item geben
                                }
                                else {  //keine Änderung: bsp durch lag/ package delay
                                    player.sendMessage("cItem wurde schon geclaimed"); //item dupe verhindert
                                }
                            });
                            AuctionUI.openYourItemsUI(player, local, real);
                        });
                    }
                }
            }

        } else if (uiListNumber == 7) {                                                                         // Anbietbare Items 7
            if (tag.equals("to_previous_button")){
                AuctionUI.openYourListingsUI(player,local,real);
            }else if (itemID != -1){
                if (SettingsManager.isAuctionCreationAllowed()) {   //auktionserstellung ist an
                    Auction auction = new Auction();
                    auction.setItemID(itemID);
                    player.setMetadata(                     //erstellt auktionserstellungsdaten
                            "active_auction_creation",
                            new FixedMetadataValue(Hammaxcustomauction.getInstance(), auction)
                    );
                    AuctionUI.itemAuctionCreationUI(auction, player, local, real);  //öffnet auktionserstellungsmenü
                }else {
                    player.closeInventory();       //auktionserstellung ist aus, weist spieler darauf hin, schließt menü
                    player.sendMessage("§cMomentan können keine neuen Auktionen erstellt werden");
                }
            }

        } else if (uiListNumber == 8 && real) {                                         //Auktion erstellen UI
            Auction auction = (Auction) player.getMetadata("active_auction_creation").get(0).value();

            if (tag.equals("to_previous_button")){
                player.removeMetadata("active_auction_creation", Hammaxcustomauction.getInstance());
                AuctionUI.openYourListingsUI(player,local,real);
            }else if(tag.equals("meh_button")){
                player.sendMessage("§cEs sind noch nicht alle Werte gesetzt");
            } else if (tag.equals("confirm_button")) {
                if (Hammaxcustomauction.getInstance().getEconomyManager().hasEnoughMoney(player.getUniqueId(),SettingsManager.getDurationPrice(auction.getDeadline()))
                    /*SettingsManager.checkMoney(player, SettingsManager.getDurationPrice(auction.getDeadline()))*/) {
                    //geldcheck vor endgültiger bestätigung
                    //ist auch angenehmer, weil man die Zeit direkt anpassen kann
                    AuctionUI.itemAuctionConfirmationUI(auction, player, local,real);
                }else{
                    player.sendMessage("§cDu hast nicht genug Geld um die Auktionsgebüren zu bezahlen");
                }
                
            } else if (tag.equals("starting_price_button")) {
                player.setMetadata(
                        "auction_step",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),"auction_set_price")
                );
                player.sendMessage("§aBitte gib den Startpreis deiner Auktion mit /aset <Preis>");
                player.sendMessage("§aAlternativ kannst du alle Werte setzen mit: /aset <Preis> <Erhöhungswert> <Zeit in Tagen>");
                player.closeInventory();

            } else if (tag.equals("increase_price_button")) {
                player.setMetadata(
                        "auction_step",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),"auction_set_increase")
                );
                player.sendMessage("§aBitte gib den Erhöhungswert deiner Auktion mit /aset <Erhöhungswert>");
                player.sendMessage("§aAlternativ kannst du alle Werte setzen mit: /aset <Preis> <Erhöhungswert> <Zeit in Tagen>");
                player.closeInventory();
                
            } else if (tag.equals("duration_button")) {
                player.setMetadata(
                        "auction_step",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),"auction_set_deadline")
                );
                player.sendMessage("§aBitte gib das Zeitlimit deiner Auktion " + "§cin Tagen" + "§a mit /aset <Tage>");
                player.sendMessage("§aAlternativ kannst du alle Werte setzen mit: /aset <Preis> <Erhöhungswert> <Zeit in Tagen>");
                player.closeInventory();
            }

        } else if (uiListNumber == 9 && real) {                                                     //Auktion erstellen bestätigung 9


            if (tag.equals("to_previous_button")){
                //player.removeMetadata("active_auction_creation", Hammaxcustomauction.getInstance());
                AuctionUI.itemAuctionCreationUI((Auction) player.getMetadata("active_auction_creation").get(0).value(), player, false, true);
            } else if (tag.equals("confirm_button")) {  //Auktionserstellungs metadata wird entfernt und für das Item wird versucht eine auktion zu erstellen
                AuctionUI.openUI(player, local, real);
                Auction tempAuction = (Auction) player.getMetadata("active_auction_creation").get(0).value();
                Hammaxcustomauction.getInstance().getDbManager().addItemToAuction(player,
                        tempAuction.getItemID(),tempAuction.getStartingPrice(),tempAuction.getBidIncrease(),tempAuction.getDeadline(),local, real);
                player.removeMetadata("active_auction_creation", Hammaxcustomauction.getInstance());
            }


        } else if (uiListNumber == 10 && real) {                                                        //Genauere Auktionsinfo 10
            if (tag.equals("to_previous_button")){
                AuctionUI.openYourListingsUI(player,local,real);
            } else if (tag.equals("cancel_auction_button")) {
                if (itemID != -1) {
                    Hammaxcustomauction.getInstance().getDbManager().attemptAuctionCancelByPlayer(player, itemID, real);
                }
                AuctionUI.openYourListingsUI(player, local, real);
            }

        } else if (uiListNumber == 11 && real) {                                                            //Filter einstellungen 11
            AuctionFilter auctionFilter = new AuctionFilter();
            if (!player.hasMetadata("auction_filter")){
                player.setMetadata(
                        "auction_filter",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),auctionFilter)
                );
            } else if (player.hasMetadata("auction_filter")) {
                auctionFilter = (AuctionFilter) player.getMetadata("auction_filter").get(0).value();
            }
            if (tag.equals("to_previous_button")){
                AuctionUI.openAllListingsSelectionUI(player,local,real);
            } else if(tag.equals("reset_filter_button")){
                player.removeMetadata("auction_filter", Hammaxcustomauction.getInstance());
                auctionFilter = new AuctionFilter();
                player.setMetadata(
                        "auction_filter",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),auctionFilter)
                );
                AuctionUI.openYourFilterSettingsUI(player, local,real);
            } else if (tag.equals("set_sort_direction")) {
                if (auctionFilter.getAscending()){
                    auctionFilter.setAscending(false);
                } else if (!auctionFilter.getAscending()) {
                    auctionFilter.setAscending(true);
                }
                player.removeMetadata("auction_filter", Hammaxcustomauction.getInstance());
                player.setMetadata(
                        "auction_filter",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),auctionFilter)
                );
                AuctionUI.openYourFilterSettingsUI(player, local,real);
            } else if (tag.equals("set_sort_style")) {
                if (auctionFilter.getSortStyle().equals("Preis")){
                    auctionFilter.setSortStyle("Zeit");
                } else if (auctionFilter.getSortStyle().equals("Zeit")) {
                    auctionFilter.setSortStyle("Preis");
                }
                player.removeMetadata("auction_filter", Hammaxcustomauction.getInstance());
                player.setMetadata(
                        "auction_filter",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),auctionFilter)
                );
                AuctionUI.openYourFilterSettingsUI(player, local,real);
            } else if (tag.equals("set_filter_name")) {
                player.setMetadata(
                        "filter_edit",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),"set_filter_name")
                );
                player.sendMessage("§aBitte gib mit /aset <Filter> einen Text ein, nach dem Items gefiltert werden sollen");
                player.closeInventory();
            } else if (tag.equals("open_team_ui")&& player.hasPermission("ahteam")) {
                AdminUI.openAdminUI(player, true, true);
            }

        } else if (uiListNumber == 12 && real) {                                                            //Gebot erstellen 12
            if (tag.equals("to_previous_button")){
                AuctionUI.openUI(player, local, real);
            } else if (tag.equals("bid_set_price_button")) {
                if (itemID != -1){                                  //spieler beommt keinen Marker für /aset
                    player.setMetadata("bid_price_input", new FixedMetadataValue(Hammaxcustomauction.getInstance(), itemID));
                    player.sendMessage("§aBitte setze den gewollten Preis mit /aset <Preis>");
                    player.closeInventory();
                }
            } else if (tag.equals("bid_confirm")) {
                if (itemID != -1){
                    long auctionID = itemID;
                    double price = clickedItemMeta.getPersistentDataContainer().get(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_price"),
                            PersistentDataType.DOUBLE);
                    Hammaxcustomauction.getInstance().getDbManager().attemptBidCreation(player,itemID,price,local,real);
                    AuctionUI.openUI(player,local,real);
                }
            } else if (tag.equals("ignore_bid_button")) {
                if (itemID != -1 && real){    //entfernt bids die schon verloren sind aus der Ansicht
                    Hammaxcustomauction.getInstance().getDbManager().ignoreThisAuction(player,itemID);
                    AuctionUI.openYourBidsUI(player,0,local,real);
                }
            }
        } else if (uiListNumber == 13 && real) {                                                            //Logs/info UI 13
            if (tag.equals("plugin_info_button")){
                AuctionUI.openGeneralPluginInfoMenu(player, local, real);
            } else if (tag.equals("player_logs_button")) {
                AuctionUI.openPlayerLogs(player, player.getUniqueId().toString(), 0, false, local, real);
            } else if (tag.equals("to_previous_button")) {
                AuctionUI.openUI(player, local, real);
            }
        } else if (uiListNumber == 14 && real) {                                                            //Pligin info UI 14
            if (tag.equals("to_previous_button")) {
                AuctionUI.openLogOrInfoSelection(player,local,real);
                //AuctionUI.openUI(player, local, real);
            }
        } else if (uiListNumber == 15 && real){                                                             // spieler logs 15
            if (tag.equals("to_previous_button")) {
                AuctionUI.openLogOrInfoSelection(player,local,real);
                //AuctionUI.openUI(player, local, real);
            } else if (tag.equals("next_page")){
                String pID = clickedItemMeta.getPersistentDataContainer().get(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING);
                long page = clickedItemMeta.getPersistentDataContainer().get(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG);
                AuctionUI.openPlayerLogs(player, pID,(int) page +1,false,local, real);

            } else if (tag.equals("prev_page")) {
                String pID = clickedItemMeta.getPersistentDataContainer().get(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING);
                long page = clickedItemMeta.getPersistentDataContainer().get(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG);
                AuctionUI.openPlayerLogs(player, pID,(int) page -1,false,local, real);
            }
        }
        //Ich bin das ende der else if schleife
        //bei UI-Erweiterungen, bitte an die klammer "}" direkt hierüber /\ else if(uiListNumber == <NeuerWert>){ }


        //TeamUI funktionen
        if (player.hasPermission("ahteam") && player.hasPermission("ahuse")){
            if (adminUiListNumber != -1 && local && real) {
                if (adminUiListNumber == 0) {                                                                   //Base admin ui 0
                    if (tag.equals("admin_auction_list")) {
                        AdminUI.openDetailedAuctionList(player, local, real);
                    } else if (tag.equals("auction_settings")) {
                        AdminUI.openAuctionPluginSettings(player,local,real);
                    } else if (tag.equals("player_actions")) {                  //neuer eintrag in ASetCommand
                        player.setMetadata(
                                "admin_player_search_input",
                                new FixedMetadataValue(Hammaxcustomauction.getInstance(),"name_input")
                        );
                        player.closeInventory();
                        player.sendMessage("§eBitte Spielernamen mit \"/aset <Spielername>\" eingeben");
                    }
                } else if (adminUiListNumber == 1) {                                                //admin auktionsliste 1

                    AuctionFilter auctionFilter = new AuctionFilter();
                    if (!player.hasMetadata("auction_filter")) {
                        player.setMetadata(
                                "auction_filter",
                                new FixedMetadataValue(Hammaxcustomauction.getInstance(), auctionFilter)
                        );
                    } else if (player.hasMetadata("auction_filter")) {
                        auctionFilter = (AuctionFilter) player.getMetadata("auction_filter").get(0).value();
                    }
                    if (tag.equals("to_previous_button")) {
                        AdminUI.openAdminUI(player, local, real);
                    } else if (tag.equals("prev_offers_button")) {
                        auctionFilter.setPage(auctionFilter.getPage() - 1);
                        player.setMetadata(
                                "auction_filter",
                                new FixedMetadataValue(Hammaxcustomauction.getInstance(), auctionFilter)
                        );
                        AdminUI.openDetailedAuctionList(player, local, real);
                        AdminUI.openDetailedAuctionList(player, local, real);
                    } else if (tag.equals("next_offers_button")) {
                        auctionFilter.setPage(auctionFilter.getPage() + 1);
                        player.setMetadata(
                                "auction_filter",
                                new FixedMetadataValue(Hammaxcustomauction.getInstance(), auctionFilter)
                        );
                        AdminUI.openDetailedAuctionList(player, local, real);
                    } else if (tag.equals("refresh_offers_button")) {
                        AdminUI.openDetailedAuctionList(player, local, real);            //Refresh auctions
                    } else if (tag.equals("set_filter_button")) {                   //geht in das filtermenü des normalen spieler UIs
                        AuctionUI.openYourFilterSettingsUI(player, false, real);
                    } else if (tag.equals("auction_item")) {
                        if (itemID != -1) {
                            AdminUI.openAdminAuctionActions(player, itemID, local, real);
                        }
                    }
                } else if (adminUiListNumber == 2) {                                              //Admin auktions aktionen 2
                    if (tag.equals("to_previous_button")) {
                        AdminUI.openDetailedAuctionList(player, local, real);
                    } else if (tag.equals("player_info")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerActions(player, pID, local, real);
                    } else if (tag.equals("end_auction_button") && player.hasPermission("ahteam") && player.isOp()){
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminAuctionCancelConfirmUI(player,pID,itemID,local,real);
                    }
                } else if (adminUiListNumber == 3) {                                            //plugin settings 3
                    if (tag.equals("to_previous_button")){
                        AdminUI.openAdminUI(player,local,real);
                    } else if (tag.equals("item_add_status")) {
                        if (player.isOp()){
                            if (itemID == 0){
                                SettingsManager.setAddItemAllowed(player, true);
                            } else if (itemID == 1) {
                               SettingsManager.setAddItemAllowed(player, false);
                            }
                            AdminUI.openAuctionPluginSettings(player, local, real);
                        }
                    } else if (tag.equals("auction_create_status")) {
                        if (player.isOp()){
                            if (itemID == 0){
                                SettingsManager.setAuctionCreationAllowed(player, true);
                            } else if (itemID == 1) {
                                SettingsManager.setAuctionCreationAllowed(player, false);
                            }
                            AdminUI.openAuctionPluginSettings(player, local, real);
                        }
                    }
                } else if (adminUiListNumber == 4) {                                            //spieler aktionen 4
                    if (tag.equals("to_previous_button")){
                        AdminUI.openAdminUI(player, local, real);
                    } else if (tag.equals("player_view_storage")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openPlayerStorage(player, pID,false, local, real);
                    } else if (tag.equals("player_edit_storage") && player.isOp()) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openPlayerStorage(player,pID,true,local, real);
                    } else if (tag.equals("player_logs")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AuctionUI.openPlayerLogs(player, pID,0,true,local,real);
                    } else if (tag.equals("player_pending_payments")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerPendingPayments(player, pID, local,real);
                    } else if (tag.equals("player_auction_list")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerAuctions(player, pID,local,real);
                    } else if (tag.equals("player_bid_list")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerBids(player,pID,0,local,real);
                    }
                } else if (adminUiListNumber == 5) {                                            //admin spieler lager ansicht 5
                    String pID = clickedItemMeta.getPersistentDataContainer().get(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                            PersistentDataType.STRING);
                    if (tag.equals("to_previous_button")){
                        AdminUI.openAdminPlayerActions(player, pID, local, real);
                    } else if (tag.equals("item_from_storage")) {
                        boolean pPerms = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_perms"),
                                PersistentDataType.BOOLEAN);
                        if (player.isOp() && pPerms){
                            AdminUI.openAdminStorageitemRemoveConfirm(player, pID,itemID,local, real);
                        }
                    }
                } else if (adminUiListNumber == 6) {                                    //admin spieler lager item entfernen 6
                    String pID = clickedItemMeta.getPersistentDataContainer().get(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                            PersistentDataType.STRING);
                    if (tag.equals("to_previous_button")){
                        AdminUI.openPlayerStorage(player, pID,true, local, real);
                    } else if (tag.equals("confirm_button") && player.isOp()) {
                        int guaranteedSlot = player.getInventory().firstEmpty();
                        //player.sendMessage("§a Confirm gelesen");
                        if(guaranteedSlot == -1){
                            player.sendMessage("§7Dein inventar ist voll");
                        }
                        else {

                            AuctionUI.openYourItemsUI(player,local,real);
                            if (itemID != -1) {
                                //normaler item claim vorgang
                                long tempIDvalue = itemID;
                                Hammaxcustomauction.getInstance().getDbManager().getItemFromID(itemID, storageItem -> {

                                    if (storageItem == null || storageItem.getStack() == null || storageItem.getStack().getType() == Material.AIR) {
                                        player.sendMessage("§cEin Fehler beim Laden des Items ist aufgetreten");
                                        return;
                                    }

                                    ItemStack giveItem = storageItem.getStack();

                                    Hammaxcustomauction.getInstance().getDbManager().claimStorageItem(player, tempIDvalue, success ->{
                                        if (success){
                                            player.getInventory().setItem(guaranteedSlot, giveItem);
                                        }
                                        else {
                                            player.sendMessage("cItem wurde schon geclaimed");
                                        }
                                    });
                                    AdminUI.openPlayerStorage(player,pID,true,local,real);
                                });

                            }

                        }
                    }
                } else if (adminUiListNumber == 7) {                                            //admin spieler Logs 7
                    if (tag.equals("to_previous_button")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerActions(player,pID,local, real);
                        //AuctionUI.openUI(player, local, real);
                    } else if (tag.equals("next_page")){
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        long page = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                                PersistentDataType.LONG);
                        AuctionUI.openPlayerLogs(player, pID,(int) page +1,true,local, real);

                    } else if (tag.equals("prev_page")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        long page = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                                PersistentDataType.LONG);
                        AuctionUI.openPlayerLogs(player, pID,(int) page -1,true,local, real);
                    }
                } else if (adminUiListNumber == 8) {                                                    //pending payments list 8
                    if (tag.equals("to_previous_button")){
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerActions(player, pID,local,real);
                    }
                } else if (adminUiListNumber == 9) {                                        //auktion abbrechen bestätigung 9
                    if (tag.equals("end_auction_button") && player.hasPermission("ahteam") && player.isOp()){
                        AuctionManager.adminStopAuctionByID(player,itemID);
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerActions(player,pID,local,real);
                    }else if (tag.equals("to_previous_button")){
                        AdminUI.openAdminAuctionActions(player,itemID,local,real);
                    }
                } else if (adminUiListNumber == 10) {                           //Admin Aktive Auktionen des spielers 10
                    if (tag.equals("to_previous_button")){
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerActions(player,pID,local,real);
                    } else if (tag.equals("to_old_auctions")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerOldAuctions(player,pID,0,local,real);
                    } else if (tag.equals("auction_item")) {
                        AdminUI.openAdminAuctionActions(player,itemID,local,real);
                    }
                }else if (adminUiListNumber == 11){                                     //Admin Alte Auktionen des Spielers 11
                    if (tag.equals("to_previous_button")){
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerActions(player,pID,local,real);
                    } else if (tag.equals("to_new_auctions")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerAuctions(player,pID,local,real);
                    } else if (tag.equals("prev_page")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerOldAuctions(player,pID,(int)itemID-1,local,real);
                    }else if (tag.equals("next_page")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerOldAuctions(player,pID,(int)itemID+1,local,real);
                    }
                } else if (adminUiListNumber == 12) {                                               //Admin New player bids 12
                    if (tag.equals("to_previous_button")){
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerActions(player,pID,local,real);
                    } else if (tag.equals("to_old_bids")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerOldBids(player,pID,0,local,real);
                    } else if (tag.equals("prev_page")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerBids(player,pID,(int) itemID -1,local,real);
                    }else if (tag.equals("next_page")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerBids(player,pID,(int) itemID +1,local,real);
                    }
                } else if (adminUiListNumber == 13) {                                           //Admin Old player bids 13
                    if (tag.equals("to_previous_button")){
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerActions(player,pID,local,real);
                    } else if (tag.equals("to_new_bids")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerBids(player,pID,0,local,real);
                    } else if (tag.equals("prev_page")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerOldBids(player,pID,(int) itemID -1,local,real);
                    }else if (tag.equals("next_page")) {
                        String pID = clickedItemMeta.getPersistentDataContainer().get(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING);
                        AdminUI.openAdminPlayerOldBids(player,pID,(int) itemID +1,local,real);
                    }
                }
                //else if ende
                //bei UI-Erweiterungen, bitte an die klammer "}" direkt hierüber /\ else if(adminUiListNumber == <NeuerWert>){ }
            }
        }
    }
        private int isInUIList(InventoryClickEvent event, String tag) {         //erkennt WELCHES menü gerade offen ist
            int result = -1; //Default wert = -1                                //OB es ein Menü ist wird am Holder erkannt
            String[] currentUIs = {
                    "User UI", //0
                    "Deine Items", //1
                    "Deine Angebote", //2
                    "Deine Gebote", //3
                    "Verfügbare Auktionen", //4
                    "Items zu Storage", //5
                    "Item aus Storage abholen", //6
                    "Anbietbare Items", //7
                    "Auktion erstellen", //8
                    "Auktion bestätigen", //9
                    "Auktion-übersicht", //10
                    "Setze Filter", //11
                    "Gebot erstellen", //12
                    "Logs/Info",  //13
                    "General Info", //14
                    "Spieler Logs"  //15
                    //Bei Erweiterungen bitte hier das array erweitern und oben die entsprechende uiListNumber nutzen
            };

            for (int i = 0; i < currentUIs.length; i++) {
                if (event.getView().getTitle().equals(currentUIs[i])) {
                    result = i;
                }
            }
            return result;
        }

    private int isInAdminUIList(InventoryClickEvent event, String tag) {            //erkennt WELCHES menü gerade offen ist
        int result = -1; //Default wert = -1                                        //OB es ein Menü ist wird am Holder erkannt
        String[] currentUIs = {
                "§cAdmin UI", //0
                "§cAdmin Auktions-Liste", //1
                "§cAdmin Auktions-Aktionen", //2
                "§cPlugin Einstellungen", //3
                "§cAdmin Spieler-Aktionen",  //4
                "§cAdmin-Spielerstorage Ansicht",   //5
                "§cAdmin Spieler-Item entfernen",    //6
                "§cAdmin Spieler-Logs",               //7
                "§cAdmin-Ausstehendezahlungen Ansicht",  //8
                "§cAdmin Auktion abbrechen bestätigung",   //9
                "§cAdmin Spieler-Auktionen",         //10
                "§cAdmin Alte Spieler-Auktionen",        //11
                "§cAdmin Spieler-Gebote",               //12
                "§cAdmin Alte Spieler-Gebote"           //13
                //Bei Erweiterungen bitte hier das array erweitern und oben die entsprechende uiListNumber nutzen
        };

        for (int i = 0; i < currentUIs.length; i++) {
            if (event.getView().getTitle().equals(currentUIs[i])) {
                result = i;
            }
        }
        return result;
    }


    //alter chat listener, vielleicht reperierbar aber momentan mit /aset ersetzt
    //(ich empfehle /aset wenigstens obtional zu behalten)
/*
    @EventHandler
    public void onActionChatInput(AsyncPlayerChatEvent event){

        //async player chat muss anders

        Player player = event.getPlayer();

        if (player.hasMetadata("active_auction_creation")) {
            if (player.hasMetadata("auction_step")) {
                event.setCancelled(true);

                Auction auction = (Auction) player.getMetadata("active_auction_creation").get(0).value();

                String step = player.getMetadata("auction_step").get(0).value().toString();

                String input = event.getMessage();
                //player.sendMessage("input ist: " + input);          //debug

                    if(step == "auction_set_price"){
                        double price = ConversionManager.signToMoneyDouble(input);
                        auction.setStartingPrice(price);
                        player.sendMessage("§7Startpreis gesetzt: " + auction.getStartingPrice());
                    } else if (step.equals("auction_set_increase")) {
                        auction.setBidIncrease(ConversionManager.signToMoneyDouble(input));
                        player.sendMessage("§7Erhöhungspreis gesetzt: " + auction.getBidIncrease());

                    } else if (step.equals("auction_set_deadline")) {
                        auction.setDeadline(ConversionManager.chatToDuration(input));
                        player.sendMessage("§7Auktionsdauer gesetzt auf "
                                + ConversionManager.durationToDays(ConversionManager.correctTimeOffset(auction.getDeadline()))
                                + " Tage");
                    }



                player.removeMetadata("auction_step", Hammaxcustomauction.getInstance());
                Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> {
                    player.closeInventory();
                    AuctionUI.itemAuctionCreationUI(auction, player, false, true);
                });

            }
        }

        if (player.hasMetadata("auction_filter")){
            if (player.hasMetadata("filter_edit")) {    //set_filter_name
                event.setCancelled(true);

                AuctionFilter filter = (AuctionFilter) player.getMetadata("auction_filter").get(0).value();

                String input = event.getMessage();

                if (player.getMetadata("filter_edit").get(0).value().toString().equals("set_filter_name")){
                    filter.setNameFilter(input);
                }

                player.removeMetadata("filter_edit", Hammaxcustomauction.getInstance());
                player.removeMetadata("auction_filter", Hammaxcustomauction.getInstance());
                player.setMetadata(
                        "auction_filter",
                        new FixedMetadataValue(Hammaxcustomauction.getInstance(),filter)
                );
                Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> {
                    player.closeInventory();
                    AuctionUI.openYourFilterSettingsUI(player, false, true);
                });

            }

        }

        if (player.hasMetadata("bid_price_input")){

            long auctionID = Long.valueOf(player.getMetadata("bid_price_input").get(0).value().toString());

            event.setCancelled(true);
            String input = event.getMessage();

            double convertedValue = ConversionManager.signToMoneyDouble(input);

            player.removeMetadata("bid_price_input", Hammaxcustomauction.getInstance());
            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> {
                player.closeInventory();
                AuctionUI.openBidCreationUI(player,auctionID,convertedValue, false,true);
            });
        }


    } */

    @EventHandler
    public void onJoin(PlayerJoinEvent event){              //Versuche fehlgeschlagene offlinezahlungen bei join nach zu holen
        UUID uuid = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();
        Hammaxcustomauction.getInstance().getDbManager().getOpenReimbursements(uuid,hammaxPaymentLogs -> {

            if (hammaxPaymentLogs == null || hammaxPaymentLogs.isEmpty()){
                return;         //keine ausstehenden zahlungen -> beende
            }

            for (int i = 0; i < hammaxPaymentLogs.size(); i++){     //bei ausstehenden zahlungen alle auszahlen

                if (Hammaxcustomauction.getInstance().getEconomyManager().reimburseSuccess(uuid, hammaxPaymentLogs.get(i).getAmount())){
                    //zahlung abhaken (ist erfolgt)
                    Hammaxcustomauction.getInstance().getDbManager().closeReimbursement(hammaxPaymentLogs.get(i).getPaymentID());
                    //spieler infonachricht
                    if (hammaxPaymentLogs.get(i).getType().equals("Auction_Payout")){
                        player.sendMessage("§eDeine Auktion wurde beendet. Du hast " + hammaxPaymentLogs.get(i).getAmount() + " erhalten.");
                    }else if (hammaxPaymentLogs.get(i).getType().equals("Bid_Reimburse")){
                        player.sendMessage("§eDu wurdest überboten und hast "+ hammaxPaymentLogs.get(i).getAmount() + " zurückbekommen.");
                    }

                }

            }

        });

    }

}
