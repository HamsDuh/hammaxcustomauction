package hdh.hammaxcustomauction;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class AuctionUI {

    public static void openUI(Player player, boolean local, boolean isReal) {
        //player.sendMessage("Opening UI for Local =" + local);       //debug
        //User UI
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "User UI");

        //UI Your Items
        ItemStack yourItems = new ItemStack(Material.CHEST);
        ItemMeta yourItemsMeta = yourItems.getItemMeta();
        yourItemsMeta.setDisplayName("§eDeine Items");
        yourItemsMeta.setLore(Arrays.asList(
                "§7Deine durch die Auktion erworbenen Items",
                "§cLade..."
        ));
        yourItemsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "your_items_button"
        );
        yourItems.setItemMeta(yourItemsMeta);

        Hammaxcustomauction.getInstance().getDbManager().getStoredItemNumber(player, amount -> {
            ItemMeta tempStorageMeta = yourItems.getItemMeta();
            tempStorageMeta.setLore(Arrays.asList(
                    "§7Deine durch die Auktion erworbenen Items",
                    "§aDu hast gerade " + amount + " items im Lager"
            ));
            yourItems.setItemMeta(tempStorageMeta);
            inv.setItem(16, yourItems);
            player.updateInventory();
        });

        //UI Your Listings
        ItemStack yourListings = new ItemStack(Material.PAPER);
        ItemMeta yourListingsMeta = yourListings.getItemMeta();
        yourListingsMeta.setDisplayName("§eDeine Angebote");
        yourListingsMeta.setLore(Arrays.asList(
                "§7Deine laufenden angebote",
                "§cLade..."
        ));
        yourListingsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "your_listings_button"
        );
        yourListings.setItemMeta(yourListingsMeta);

        Hammaxcustomauction.getInstance().getDbManager().getActiveAuctionNumber(player, amount -> {
            ItemMeta tempListingMeta = yourListings.getItemMeta();
            tempListingMeta.setLore(Arrays.asList(
                    "§7Deine Laufenden Auktionen",
                    "§aDu hast gerade " + amount + " offene Auktionen"
            ));
            yourListings.setItemMeta(tempListingMeta);
            inv.setItem(14, yourListings);
            player.updateInventory();
        });


        //UI Your Bids
        ItemStack yourBets = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta yourBetsMeta = yourBets.getItemMeta();
        yourBetsMeta.setDisplayName("§eDeine aktiven Gebote");
        yourBetsMeta.setLore(Arrays.asList(
                "§cDeine laufgenden Gebote",
                "§cLade..."
        ));
        yourBetsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "your_bets_button"
        );
        yourBets.setItemMeta(yourBetsMeta);

        Hammaxcustomauction.getInstance().getDbManager().countActiveBidsForPlayer(player, givenValues ->{

            yourBetsMeta.setLore(Arrays.asList(
                    "§aDu bist top Bieter auf " + givenValues[0] + " Angeboten",
                    "§cDu bist nicht top Bieter auf " + givenValues[1] + " Angeboten"
            ));
            yourBets.setItemMeta(yourBetsMeta);
            inv.setItem(12, yourBets);
            player.updateInventory();
        });


        //UI AllListings
        ItemStack allListings = new ItemStack(Material.BOOK);
        ItemMeta allListingsMeta = allListings.getItemMeta();
        allListingsMeta.setDisplayName("§eAngebote");
        //itemLore
        allListingsMeta.setLore(Arrays.asList(
                "§7Alle zur Verfügungstehenden Angebote",
                "§7"
        ));
        allListingsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "all_listings_button"
        );
        allListings.setItemMeta(allListingsMeta);

        //logs and info
        ItemStack logsAndInfo = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta logsAndInfoMeta = logsAndInfo.getItemMeta();
        logsAndInfoMeta.setDisplayName("§eLogs und info");
        logsAndInfoMeta.setLore(Arrays.asList(
                "Hier sind Infos zum Plugin",
                "und dein Verlauf (zahlungen, und itemverlauf)"
        ));
        logsAndInfoMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "logs_and_info"
        );
        logsAndInfo.setItemMeta(logsAndInfoMeta);

        //UI Placement
        inv.setItem(16, yourItems);
        inv.setItem(14, yourListings);
        inv.setItem(12, yourBets);
        inv.setItem(10, allListings);
        inv.setItem(26, logsAndInfo);

        player.openInventory(inv);
    }


    public static void openYourItemsUI(Player player, boolean local, boolean isReal) {
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 54, "Deine Items");

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        Hammaxcustomauction.getInstance().getDbManager().getStoredItemNumber(player, amount -> {

            //UI Item hinzufügen
            ItemStack addItem = new ItemStack(Material.FEATHER);
            ItemMeta addItemMeta = addItem.getItemMeta();

            if(amount < SettingsManager.getMaxPlayerStorage()){

                //UI Item hinzufügen
                addItemMeta.setDisplayName("§aItem hinzufügen");
                addItemMeta.setLore(Arrays.asList(
                        "§7Du kannst bis zu " + SettingsManager.getMaxPlayerStorage() + " Items im Auktionshaus haben"//,
                        //"§7Du hast gerade " + getYourItemAnmount(player)
                ));
                addItemMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "add_items_button"
                );
            } else{
                //UI Item hinzufügen
                addItemMeta.setDisplayName("§aItem hinzufügen");
                addItemMeta.setLore(Arrays.asList(
                        "§cDu kannst nur " + SettingsManager.getMaxPlayerStorage() + " Items im Auktionshaus haben",
                        "§cMach bitte etwas Platz bevor du weitere Items hinzufügst"
                ));
                addItemMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "add_too_many_items"
                );
            }

            addItem.setItemMeta(addItemMeta);

            inv.setItem(4, addItem);
            player.updateInventory();
        });
        /*
        //UI Item hinzufügen
        ItemStack addItem = new ItemStack(Material.FEATHER);
        ItemMeta addItemMeta = addItem.getItemMeta();
        addItemMeta.setDisplayName("§aItem hinzufügen");
        addItemMeta.setLore(Arrays.asList(
                "§cDu kannst bis zu " + SettingsManager.getMaxPlayerStorage() + " Items im Auktionshaus haben"//,
                //"§7Du hast gerade " + getYourItemAnmount(player)
        ));
        addItemMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "add_items_button"
        );
        addItem.setItemMeta(addItemMeta);
        */

        Hammaxcustomauction.getInstance().getDbManager().getItemsFromStorage(player.getUniqueId().toString(),
                items -> {
                    int slot = 9;
                    for (StorageItem storageItem : items) {
                        if (slot > 50) {
                            break;
                        }
                        ItemStack stack = storageItem.getStack();
                        ItemMeta meta = stack.getItemMeta();
                        List<String> lore = new ArrayList<>();
                        if (meta.hasLore()) {
                            lore.addAll(meta.getLore());
                        }
                        lore.add("§8ItemID: " + storageItem.getItemID());
                        lore.add("§7Item added at " + ConversionManager.formatTime(storageItem.getDateAdded()));
                        meta.setLore(lore);

                        //Tag
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                                PersistentDataType.LONG, storageItem.getItemID());
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                                PersistentDataType.STRING, "item_from_storage"
                        );

                        stack.setItemMeta(meta);
                        inv.setItem(slot, stack);

                        slot++;
                    }
                });


            //UI Placement
        //inv.setItem(4, addItem);
        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }

    public static void addItemToStorageUI(Player player,boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 45, "Items zu Storage");

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);
        int availableSlot = 9;
        for(int i = 0; i < player.getInventory().getSize(); i++){
            if(player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getType() != Material.AIR)
                if(SettingsManager.allowedItemForStorage(player.getInventory().getItem(i))){
                    ItemStack addableItemUIElement =new ItemStack(player.getInventory().getItem(i));
                    ItemMeta addableItemUIElementMeta = addableItemUIElement.getItemMeta();

                    addableItemUIElementMeta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                            PersistentDataType.STRING, "addable_item_ui_element"
                    );
                    addableItemUIElement.setItemMeta(addableItemUIElementMeta);

                    inv.setItem(availableSlot, addableItemUIElement);
                    availableSlot++;
                }
        }

        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }

    public static void removeItemFromStorageUI (Player player, boolean local, boolean save, long givenID){
        AuctionUIHolder holder = new AuctionUIHolder(local, save);
        Inventory inv = Bukkit.createInventory(holder, 27, "Item aus Storage abholen");

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        /*
        //UI Confirm Button
        ItemStack confirm = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName("§cBestätige");
        confirmMeta.setLore(Arrays.asList(
                "§aHole das item aus dem ab",
                "§e"
        ));
        confirmMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "confirm_button"
        );
    */

        //convert itemID -> givenItem


        inv.setItem(16, toPrevious);
        //inv.setItem(10, confirm);

        Hammaxcustomauction.getInstance().getDbManager().getItemFromID(givenID, storageItem -> {

            ItemStack givenItem = storageItem.getStack().clone();

            ItemMeta givenitemMeta = givenItem.getItemMeta();
            givenitemMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                    PersistentDataType.LONG, storageItem.getItemID()
            );
            givenItem.setItemMeta(givenitemMeta);



            //UI Confirm Button
            ItemStack confirm = new ItemStack(Material.LIME_WOOL);
            ItemMeta confirmMeta = confirm.getItemMeta();
            confirmMeta.setDisplayName("§aBestätige");
            confirmMeta.setLore(Arrays.asList(
                    "§7Hole das item aus dem Storage ab",
                    "§e"
            ));
            confirmMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "confirm_button"
            );

            confirmMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                    PersistentDataType.LONG, storageItem.getItemID()
            );

            inv.setItem(13, givenItem);
            confirm.setItemMeta(confirmMeta);
            inv.setItem(10, confirm);
        });

    /*
        confirm.setItemMeta(confirmMeta);
        inv.setItem(10, confirm);
    */
        player.openInventory(inv);
    }


    public static void openYourListingsUI(Player player, boolean local, boolean isReal) {
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "Deine Angebote");

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        Hammaxcustomauction.getInstance().getDbManager().getActiveAuctionNumber(player, amount -> {

            //UI Item hinzufügen
            ItemStack addItem = new ItemStack(Material.FEATHER);
            ItemMeta addItemMeta = addItem.getItemMeta();
            if(amount < SettingsManager.getMaxPlayerAuctions()) {

                addItemMeta.setDisplayName("§aItem hinzufügen");
                addItemMeta.setLore(Arrays.asList(
                        "§7Du kannst bis zu " + SettingsManager.getMaxPlayerAuctions() + " aktive Auktionen haben"//,
                        //"§7Du hast gerade " + "§c Noch Nicht eingebaut :/"
                ));
                addItemMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "add_items_button"
                );

            } else{
                addItemMeta.setDisplayName("§aItem hinzufügen");
                addItemMeta.setLore(Arrays.asList(
                        "§cDu kannst nur " + SettingsManager.getMaxPlayerAuctions() + " aktive Auktionen haben",
                        "§7Du hast gerade " + "§c Noch Nicht eingebaut :/"
                ));
                addItemMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "add_too_many_items"
                );
            }

            addItem.setItemMeta(addItemMeta);

            inv.setItem(4, addItem);
            player.updateInventory();
        });
        /*
        //UI Item hinzufügen
        ItemStack addItem = new ItemStack(Material.FEATHER);
        ItemMeta addItemMeta = addItem.getItemMeta();
        addItemMeta.setDisplayName("§aItem hinzufügen");
        addItemMeta.setLore(Arrays.asList(
                "§cDu kannst bis zu " + SettingsManager.getMaxPlayerAuctions() + " Items im Auktionshaus haben",
                "§7Du hast gerade " + "§c Noch Nicht eingebaut :/"
        ));
        addItemMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "add_items_button"
        );
        addItem.setItemMeta(addItemMeta);
        */

        Hammaxcustomauction.getInstance().getDbManager().getAuctionsFromPlayer(player.getUniqueId().toString(),
                auctions -> {
                    int slot = 9;

                    for (Auction auction : auctions) {
                        if (slot > 26) {
                            break;
                        }
                        ItemStack stack = auction.getStorageItem().getStack();
                        ItemMeta meta = stack.getItemMeta();
                        List<String> lore = new ArrayList<>();
                        if (meta.hasLore()) {
                            lore.addAll(meta.getLore());
                        }
                        lore.add("§8AuktionID: " + auction.getAuctionID());
                        lore.add("§8ItemID: " + auction.getItemID());
                        lore.add("§cPlannedDeadline: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getDeadline())));
                        lore.add("§7Startpreis: " + auction.getStartingPrice());
                        lore.add("§7Mindesterhöhung: " + auction.getBidIncrease());

                        if(auction.getBid() != null){
                            if (auction.getBid().getBidID() != -1){
                                lore.add("§7Momentan leitendes Gebot: " + auction.getBid().getBidAmount());
                                lore.add("§7Gebot erstellt am: "+ ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getBid().getBidDate())));
                            }
                        } else {
                            lore.add("§7Noch gibt es auf diese Auktion keine Gebote");
                        }


                        meta.setLore(lore);

                        //Tag
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                                PersistentDataType.LONG, auction.getAuctionID());
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                                PersistentDataType.STRING, "auction_item"
                        );

                        stack.setItemMeta(meta);
                        inv.setItem(slot, stack);

                        slot++;
                    }
                });


        //UI Placement
        //inv.setItem(4, addItem);
        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }

    public static void listingsAddableItemsUI(Player player, boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "Anbietbare Items");

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);


        Hammaxcustomauction.getInstance().getDbManager().getItemsFromStorage(player.getUniqueId().toString(),
                items -> {
                    int slot = 9;
                    for (StorageItem storageItem : items) {
                        if (slot > 26) {
                            break;
                        }
                        ItemStack stack = storageItem.getStack();
                        ItemMeta meta = stack.getItemMeta();
                        List<String> lore = new ArrayList<>();
                        if (meta.hasLore()) {
                            lore.addAll(meta.getLore());
                        }
                        lore.add("§8ItemID: " + storageItem.getItemID());
                        lore.add("§7Item added at " + ConversionManager.formatTime(storageItem.getDateAdded()));
                        meta.setLore(lore);

                        //Tag
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                                PersistentDataType.LONG, storageItem.getItemID());
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                                PersistentDataType.STRING, "item_from_storage"
                        );

                        stack.setItemMeta(meta);
                        inv.setItem(slot, stack);

                        slot++;
                    }
                });


        inv.setItem(8, toPrevious);
        player.openInventory(inv);
    }

    public static void itemAuctionCreationUI(Auction auction, Player player, boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 36, "Auktion erstellen");

        if(auction.getItemID()!= -1 && auction.getStartingPrice() >= SettingsManager.getMinimumAuctionPrice()
                && auction.getBidIncrease() >= SettingsManager.getMinimumAuctionIncrease(auction.getStartingPrice())
                && auction.getDeadline() >= SettingsManager.getMinimumDeadline()){

            // Falls bedingungen passen Confirm knopf

            //UI Confirm Button
            ItemStack confirm = new ItemStack(Material.LIME_WOOL);
            ItemMeta confirmMeta = confirm.getItemMeta();
            confirmMeta.setDisplayName("§aBestätige");
            confirmMeta.setLore(Arrays.asList(
                    "§7Startpreis: " + auction.getStartingPrice(),
                    "§7Preiserhöhung: " + auction.getBidIncrease(),
                    "---",
                    "§7Deadline: in " + ConversionManager.durationToDays(auction.getDeadline())+ " §7Tagen",
                    "§cAuktionskosten: " + SettingsManager.getDurationPrice(auction.getDeadline())
            ));
            confirmMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "confirm_button"
            );
            confirm.setItemMeta(confirmMeta);

            inv.setItem(0, confirm);



        } else {

            // sonst hinweis: bedingungen
            ItemStack conditionsNotMet = new ItemStack(Material.ORANGE_WOOL);
            ItemMeta conditionsNotMetMeta = conditionsNotMet.getItemMeta();
            conditionsNotMetMeta.setDisplayName("§cNicht alle nötigen Bedingungen sind erfüllt");
            conditionsNotMetMeta.setLore(Arrays.asList(
                    "§eMindeststartpreis ist " + SettingsManager.getMinimumAuctionPrice(),
                    "§7Startpreis: " + auction.getStartingPrice(),
                    "----------------------",
                    "§eMindesterhöhung ist " + SettingsManager.getMinimumAuctionIncrease(auction.getStartingPrice()),
                    "§7Preiserhöhung: " + auction.getBidIncrease(),
                    "----------------------",
                    "§eMinimaler Zeitraum ist " + SettingsManager.getMinimumDeadline(),
                    "§7Deadline: in " + ConversionManager.durationToDays(auction.getDeadline()) + "§7Tagen",
                    "§cAuktionskosten: " + SettingsManager.getDurationPrice(auction.getDeadline())
            ));
            conditionsNotMetMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "meh_button"
            );
            conditionsNotMet.setItemMeta(conditionsNotMetMeta);

            inv.setItem(0, conditionsNotMet);
        }


        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);


        // Starting price
        ItemStack startingPrice = new ItemStack(Material.GOLD_INGOT);
        ItemMeta startingPriceMeta = startingPrice.getItemMeta();
        startingPriceMeta.setDisplayName("§eSetze den Startpreis");
        startingPriceMeta.setLore(Arrays.asList(
                "§7Beim anklicken schließt sich dieses Menü",
                "§7Bitte trage mit /aset <preis> den Mindestpreis ein",
                "§7Alternativ können mit /aset <Stertpreis> <Erhöhung> <Dauer> alle werte gesetzt werden",
                "§7Mindeststartpreis ist " + SettingsManager.getMinimumAuctionPrice(),
                "§7Momentaner Startpreis: " +auction.getStartingPrice()
        ));
        startingPriceMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "starting_price_button"
        );
        startingPrice.setItemMeta(startingPriceMeta);


        // Preiserhöhung
        ItemStack bidIncrease = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta bidIncreaseMeta = bidIncrease.getItemMeta();
        bidIncreaseMeta.setDisplayName("§eSetze den Erhöhungswert");
        bidIncreaseMeta.setLore(Arrays.asList(
                "§7Beim anklicken schließt sich dieses Menü",
                "§7Bitte trage mit /aset <Erhöhungswert> den Erhöhungswert ein",
                "§7Alternativ können mit /aset <Stertpreis> <Erhöhung> <Dauer> alle werte gesetzt werden",
                "§7Mindest-Erhöhungswert ist " + SettingsManager.getMinimumAuctionIncrease(auction.getStartingPrice()),
                "§7Momentaner Arhöhungswert: " + auction.getBidIncrease()
        ));
        bidIncreaseMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "increase_price_button"
        );
        bidIncrease.setItemMeta(bidIncreaseMeta);


        //Dauer
        ItemStack duration = new ItemStack(Material.REDSTONE);
        ItemMeta durationMeta = duration.getItemMeta();
        durationMeta.setDisplayName("§eSetze auktions Zeit");
        durationMeta.setLore(Arrays.asList(
                "§7Beim anklicken schließt sich dieses Menü",
                "§7Bitte setze die Auktionszeit mit /aset <Zeit> (in Tagen)",
                "§7Alternativ können mit /aset <Stertpreis> <Erhöhung> <Dauer> alle werte gesetzt werden",
                "§7Maximale Auktionsdauer ist " + SettingsManager.getMaximumDeadlineInDays() + "Tage",
                "------------------",
                "§eAktuelle Auktionsdauer kostet: " + SettingsManager.getDurationPrice(ConversionManager.durationToDays(auction.getDeadline()))
        ));
        durationMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "duration_button"
        );
        duration.setItemMeta(durationMeta);


        //tempItem
        Hammaxcustomauction.getInstance().getDbManager().getItemFromID(auction.getItemID(), storageItem -> {

                    ItemStack givenItem = storageItem.getStack().clone();

                    ItemMeta givenitemMeta = givenItem.getItemMeta();
                    givenitemMeta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                            PersistentDataType.LONG, storageItem.getItemID()
                    );
                    givenItem.setItemMeta(givenitemMeta);

                    inv.setItem(19, givenItem);
                });

        inv.setItem(8, toPrevious);

        inv.setItem(15, startingPrice);
        inv.setItem(24, bidIncrease);
        inv.setItem(33, duration);

        player.openInventory(inv);
    }

    public static void itemAuctionConfirmationUI(Auction auction, Player player,boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "Auktion bestätigen");

        if(auction.getItemID()!= -1 && auction.getStartingPrice() >= SettingsManager.getMinimumAuctionPrice()
                && auction.getBidIncrease() >= SettingsManager.getMinimumAuctionIncrease(auction.getStartingPrice())
                && auction.getDeadline() >= SettingsManager.getMinimumDeadline() &&isReal){ //bestätigen erstellen

            //UI Confirm Button
            ItemStack confirm = new ItemStack(Material.LIME_WOOL);
            ItemMeta confirmMeta = confirm.getItemMeta();
            confirmMeta.setDisplayName("§aBestätige");
            confirmMeta.setLore(Arrays.asList(
                    "§7Startpreis: " + auction.getStartingPrice(),
                    "§7Preiserhöhung: " + auction.getBidIncrease(),
                    "---",
                    "§7Deadline: in " + ConversionManager.durationToDays(auction.getDeadline())+ " §7Tagen",
                    "§cAuktionskosten: " + SettingsManager.getDurationPrice(auction.getDeadline())
            ));
            confirmMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "confirm_button"
            );
            confirm.setItemMeta(confirmMeta);

            inv.setItem(10, confirm);
        }

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        //tempItem
        Hammaxcustomauction.getInstance().getDbManager().getItemFromID(auction.getItemID(), storageItem -> {

            ItemStack givenItem = storageItem.getStack().clone();

            ItemMeta givenitemMeta = givenItem.getItemMeta();
            givenitemMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                    PersistentDataType.LONG, storageItem.getItemID()
            );
            givenItem.setItemMeta(givenitemMeta);

            inv.setItem(13, givenItem);
        });

        inv.setItem(16, toPrevious);


        player.openInventory(inv);

    }

    public static void auctionOwnerAuctionInfoUI(Player player, long auctionID, boolean local, boolean isReal){

        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "Auktion-übersicht");

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        Hammaxcustomauction.getInstance().getDbManager().getAuctionFromID(auctionID, auction -> {

            if(auction == null){

            }else {
                ItemStack stack = auction.getStorageItem().getStack();
                ItemMeta meta = stack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if (meta.hasLore()) {
                    lore.addAll(meta.getLore());
                }
                lore.add("§8AuktionID: " + auction.getAuctionID());
                lore.add("§8ItemID: " + auction.getItemID());
                lore.add("§cGeplante Deadline: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getDeadline())));
                lore.add("§7Startpreis: " + auction.getStartingPrice());
                lore.add("§7 Mindesterhöhung: " + auction.getBidIncrease());

                //UI Cancel auction Button
                ItemStack cancelAuction = new ItemStack(Material.REDSTONE_TORCH);
                ItemMeta cancelAuctionMeta = cancelAuction.getItemMeta();
                cancelAuctionMeta.setDisplayName("§cAuktion abbrechen");

                if (auction.getBid() != null) {
                    if (auction.getBid().getBidID() != -1) {
                        //original item
                        lore.add("§7Momentan leitendes Gebot: " + auction.getBid().getBidAmount());
                        lore.add("§7Gebot erstellt am: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getBid().getBidDate())));

                        //cancel Item
                        cancelAuctionMeta.setLore(Arrays.asList(
                                "§cDu kannst eine Auktion nur abbrechen, wenn nicht auf sie geboten wird",
                                "§cAuf diese Auktion wurde schon geboten"
                        ));
                        cancelAuctionMeta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                                PersistentDataType.LONG, auction.getAuctionID());
                        cancelAuctionMeta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                                PersistentDataType.STRING, "dont_cancel_auction_button"
                        );
                        cancelAuction.setItemMeta(cancelAuctionMeta);
                        inv.setItem(10, cancelAuction);
                        player.updateInventory();
                    }
                } else {
                    lore.add("§7Noch gibt es auf diese Auktion keine Gebote"); //original item

                    //cancel Item
                    cancelAuctionMeta.setLore(Arrays.asList(
                            "§aDu kannst eine Auktion nur abbrechen, wenn nicht auf sie Geboten wird",
                            "§aZum zeitpunkt dieser Abfrage hat diese Auktion noch kein Gebot"
                    ));
                    cancelAuctionMeta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                            PersistentDataType.LONG, auction.getAuctionID());
                    cancelAuctionMeta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                            PersistentDataType.STRING, "cancel_auction_button"
                    );
                    cancelAuction.setItemMeta(cancelAuctionMeta);
                    inv.setItem(10, cancelAuction);
                    player.updateInventory();
                }

                meta.setLore(lore);

                //Tag
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG, auction.getAuctionID());
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "auction_item"
                );

                stack.setItemMeta(meta);
                inv.setItem(12, stack);
                player.updateInventory();
            }
        });



        inv.setItem(16, toPrevious);

        player.openInventory(inv);
    }




    public static void openAllListingsSelectionUI(Player player, boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 54, "Verfügbare Auktionen");

        AuctionFilter filter;

        if (player.hasMetadata("auction_filter")){
            filter = (AuctionFilter) player.getMetadata("auction_filter").get(0).value();
        }else {
            filter = new AuctionFilter();
        }

        Hammaxcustomauction.getInstance().getDbManager().getPublicAuctionsForPlayer(player.getUniqueId().toString(), auctions -> {

            int countStart = filter.getPage() * 36;
            int shownElements = (filter.getPage() + 1) * 36;
            int totalElements;

            List<Auction> filteredAuctions = auctions;

            //player.sendMessage("Debug ListenLänge: "+ filteredAuctions.size());

            if ("Zeit".equals(filter.getSortStyle())){
                filteredAuctions = sortAuctionsByTime(filteredAuctions, filter.getAscending());
            } else if ("Preis".equals(filter.getSortStyle())) {
                filteredAuctions = sortAuctionsByPrice(filteredAuctions, filter.getAscending());
            }

            //player.sendMessage("Debug ListenLänge: "+ filteredAuctions.size());

            if(filter.getNameFilter() != null && !filter.getNameFilter().isEmpty()) {
                filteredAuctions = filterAuctionsByName(filteredAuctions, filter.getNameFilter());
            }

            //player.sendMessage("Debug ListenLänge: "+ filteredAuctions.size());

            totalElements = filteredAuctions.size();

            if(filter.getPage() == 0){
                //UI Keine Vorherige Auktions Seite
                ItemStack prevPage = new ItemStack(Material.BRICKS);
                ItemMeta prevPageMeta = prevPage.getItemMeta();
                prevPageMeta.setDisplayName("§eVorherige Angebots-Seite");
                prevPageMeta.setLore(Arrays.asList(
                        "§7Es Gibt keine vorherige Angebots-Seite",
                        "§e"
                ));
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "no_prev_offers_button"
                );
                prevPage.setItemMeta(prevPageMeta);
                inv.setItem(2, prevPage );
            }else {
                //UI Vorherige Auktions Seite
                ItemStack prevPage = new ItemStack(Material.ARROW);
                ItemMeta prevPageMeta = prevPage.getItemMeta();
                prevPageMeta.setDisplayName("§eVorherige Angebots-Seite");
                prevPageMeta.setLore(Arrays.asList(
                        "§7Schaue dir Angebote auf der vorherigen Seite an",
                        "§e"
                ));
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "prev_offers_button"
                );
                prevPage.setItemMeta(prevPageMeta);
                inv.setItem(2, prevPage );
            }

            if (totalElements > shownElements){
                //UI Nächste Auktions Seite
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta nextPageMeta = nextPage.getItemMeta();
                nextPageMeta.setDisplayName("§eNächste Angebots Seite");
                nextPageMeta.setLore(Arrays.asList(
                        "§7Schaue dir Angebote auf der nächsten Seite an",
                        "§e"
                ));
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "next_offers_button"
                );
                nextPage.setItemMeta(nextPageMeta);
                inv.setItem(6, nextPage);
            } else {
                //UI Keine Nächste Auktions Seite
                ItemStack nextPage = new ItemStack(Material.BRICKS);
                ItemMeta nextPageMeta = nextPage.getItemMeta();
                nextPageMeta.setDisplayName("§eNächste Angebots Seite");
                nextPageMeta.setLore(Arrays.asList(
                        "§7Es gibt keine weiteren Seiten",
                        "§e"
                ));
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "no_next_offers_button"
                );
                nextPage.setItemMeta(nextPageMeta);
                inv.setItem(6, nextPage);
            }



            //Auktionen anzeigen
            for (int i = countStart; i < shownElements && i < totalElements; i++){

                Auction auction = filteredAuctions.get(i);

                if(auction == null){

                }else {
                    ItemStack stack = auction.getStorageItem().getStack();
                    ItemMeta meta = stack.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    if (meta.hasLore()) {
                        lore.addAll(meta.getLore());
                    }
                    //lore.add("§8AuktionID: " + auction.getAuctionID());
                    //lore.add("§8ItemID: " + auction.getItemID());
                    lore.add("§cGeplante Deadline: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getDeadline())));


                    if (auction.getBid() != null) {
                        if (auction.getBid().getBidID() != -1) {
                            long tempValue = auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L;
                            if (tempValue < auction.getDeadline()) tempValue = auction.getDeadline();
                            int[] timeLeft = ConversionManager.convertToTimeLeft(tempValue);
                            lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");

                            double amount = auction.getBid().getBidAmount() + auction.getBidIncrease();
                            lore.add("§7Momentan leitendes Gebot: " + auction.getBid().getBidAmount());
                            lore.add("§eMindest Gebotskosten: " + amount);
                            if (auction.getBid().getBidOwner().equals(player.getUniqueId().toString())){
                                lore.add("§aDu bist der höchst bietende");
                            }
                        }
                    } else {
                        int[] timeLeft = ConversionManager.convertToTimeLeft(auction.getDeadline());
                        lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");
                        double amount = auction.getStartingPrice() + auction.getBidIncrease();
                        lore.add("§eMindestpreis des nächsten gebotes: " + amount);
                    }

                    meta.setLore(lore);

                    //Tag
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                            PersistentDataType.LONG, auction.getAuctionID());
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                            PersistentDataType.STRING, "auction_item"
                    );

                    stack.setItemMeta(meta);
                    inv.setItem(9 + i - countStart, stack);
                }

            }
            player.updateInventory();




        });

        //UI Filter setzen
        ItemStack filterButton = new ItemStack(Material.COBWEB);
        ItemMeta filterButtonMeta = filterButton.getItemMeta();
        filterButtonMeta.setDisplayName("§eFilter-einstellungen");
        String nameFilter = "Kein Filter";
        if (!(filter.getNameFilter() == null) && !filter.getNameFilter().isEmpty()) nameFilter = filter.getNameFilter();
        String aufsteigend = "Aufsteigend";
        if (!filter.getAscending()) aufsteigend = "absteigend";
        filterButtonMeta.setLore(Arrays.asList(
                "§7Momentane Sortierung nach: " + filter.getSortStyle(),
                "§7Reihenfolge: " + aufsteigend ,
                "--------------------",
                "§7Namens Filter: " + nameFilter
        ));
        filterButtonMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "set_filter_button"
        );
        filterButton.setItemMeta(filterButtonMeta);
        inv.setItem(0, filterButton);

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        //UI Refresh offers
        ItemStack refreshOffers = new ItemStack(Material.SAND);
        ItemMeta refreshOffersMeta = refreshOffers.getItemMeta();
        refreshOffersMeta.setDisplayName("§eSeite Aktualisieren");
        refreshOffersMeta.setLore(Arrays.asList(
                "§7Aktuallisiere Angebote auf dieser Seite",
                "§e"
        ));
        refreshOffersMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "refresh_offers_button"
        );
        refreshOffers.setItemMeta(refreshOffersMeta);

        inv.setItem(4, refreshOffers);
        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }

    public static void openYourFilterSettingsUI(Player player, boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "Setze Filter");

        AuctionFilter filter = new AuctionFilter();

        if (player.hasMetadata("auction_filter")){
            filter = (AuctionFilter) player.getMetadata("auction_filter").get(0).value();
        }

        //UI Filter setzen
        ItemStack filterButton = new ItemStack(Material.COBWEB);
        ItemMeta filterButtonMeta = filterButton.getItemMeta();
        filterButtonMeta.setDisplayName("§eFilter-Info");
        String nameFilter = "Kein Filter";
        if (!(filter.getNameFilter() == null) && !filter.getNameFilter().isEmpty()) nameFilter = filter.getNameFilter();
        String aufsteigend = "Aufsteigend";
        if (!filter.getAscending()) aufsteigend = "Absteigend";
        filterButtonMeta.setLore(Arrays.asList(
                "§7Momentane Sortierung nach: " + filter.getSortStyle(),
                "§7Reihenfolge: " + aufsteigend ,
                "--------------------",
                "Namens Filter: " + nameFilter
        ));
        filterButtonMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "filter_info"
        );
        filterButton.setItemMeta(filterButtonMeta);
        inv.setItem(0, filterButton);

        //UI SortByName
        ItemStack sortByName = new ItemStack(Material.OAK_SIGN);
        ItemMeta sortByNameMeta = sortByName.getItemMeta();
        sortByNameMeta.setDisplayName("§eSetze Namens-Filter");
        sortByNameMeta.setLore(Arrays.asList(
                "§7Nach klicken bitte den Filter in den chat schreiben",
                //"§7Itemnamen sind zu dieser Version noch " + "§cEnglisch",
                "§7Beispiel: dragon -> zeigt Dracheneier",
                "Momentaner Namens-Filter: " + nameFilter
        ));
        sortByNameMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "set_filter_name"
        );
        sortByName.setItemMeta(sortByNameMeta);
        inv.setItem(10, sortByName);

        //UI SortStyle
        ItemStack sortStyle = new ItemStack(Material.PAPER);
        ItemMeta sortStyleMeta = sortStyle.getItemMeta();
        sortStyleMeta.setDisplayName("§eSortiere nach: " + filter.getSortStyle());
        if (filter.getSortStyle().equals("Zeit")){
            sortStyleMeta.setLore(Arrays.asList(
                    "§7Klicke um Sortierart zu ändern",
                    "§7Preis"
            ));
        } else if (filter.getSortStyle().equals("Preis")) {
            sortStyleMeta.setLore(Arrays.asList(
                    "§7Klicke um Sortierart zu ändern",
                    "§7Zeit"
            ));
        }
        sortStyleMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "set_sort_style"
        );
        sortStyle.setItemMeta(sortStyleMeta);
        inv.setItem(12, sortStyle);

        //UI auf/absteigend
        ItemStack asc = new ItemStack(Material.POINTED_DRIPSTONE);
        ItemMeta ascMeta = asc.getItemMeta();
        ascMeta.setDisplayName("§eSortierweise: "+ aufsteigend);
        ascMeta.setLore(Arrays.asList(
                "§7Drücke um zwischen aufsteigend und absteigend zu wechseln",
                "§e"
        ));
        ascMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "set_sort_direction"
        );
        asc.setItemMeta(ascMeta);
        inv.setItem(14, asc);

        //UI Reset Filter
        ItemStack resetFilter = new ItemStack(Material.GLASS);
        ItemMeta resetFilterMeta = resetFilter.getItemMeta();
        resetFilterMeta.setDisplayName("§cFilter zurücksetzen");
        resetFilterMeta.setLore(Arrays.asList(
                "§7Setze den Filter zum Standard zurück",
                "§e"
        ));
        resetFilterMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "reset_filter_button"
        );
        resetFilter.setItemMeta(resetFilterMeta);
        inv.setItem(16, resetFilter);

        String teamWarn = "";
        if (player.hasPermission("ahteam")){
            teamWarn = "Hiermit gelangst du " + "§cNICHT" + "§e zum Team-UI";
        }

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"+teamWarn
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        if (player.hasPermission("ahteam")){
            //Teamler Filter zu ahadmin
            ItemStack teamMenu = new ItemStack(Material.ORANGE_WOOL);
            ItemMeta teamMenuMeta = teamMenu.getItemMeta();
            teamMenuMeta.setDisplayName("§cZum Team UI");
            teamMenuMeta.setLore(Arrays.asList(
                    "§7Schickt dich zur Startseite des Team-Menüs",
                    ""
            ));
            teamMenuMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "open_team_ui"
            );
            teamMenu.setItemMeta(teamMenuMeta);

            inv.setItem(26,teamMenu);
        }

        inv.setItem(8, toPrevious);
        player.openInventory(inv);
    }

    public static void openBidCreationUI(Player player, long givenAuctionID, double givenValue, boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "Gebot erstellen");

        Hammaxcustomauction.getInstance().getDbManager().getAuctionFromID(givenAuctionID, auction ->{

            double auctionMinPrice = auction.getStartingPrice() + auction.getBidIncrease();
            long maxDuration = auction.getDeadline();
            if (auction.getBid() != null){
                auctionMinPrice = auction.getBid().getBidAmount() + auction.getBidIncrease();
                if(auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L > maxDuration){
                    maxDuration = auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L;
                }
            }
            double price = givenValue;
            int[] durationInDays = ConversionManager.convertToTimeLeft(ConversionManager.correctTimeOffset(maxDuration));

            if(givenValue == -1.0){
                price = auctionMinPrice;
            }

            if (price < auctionMinPrice){

                //UI Bid Meh
                ItemStack bidMeh = new ItemStack(Material.ORANGE_WOOL);
                ItemMeta bidMehMeta = bidMeh.getItemMeta();
                bidMehMeta.setDisplayName("§cZu niedriger Preis :/");
                bidMehMeta.setLore(Arrays.asList(
                        "§7Der Eingabewert muss mindestens " + "§c" + auctionMinPrice + "§7 sein",
                        "§eMomentaner Eingabewert: " + "§c" +price,
                        "§7Die auktion läuft noch: " + durationInDays[0] + "Tage, " + durationInDays[1] + "Stunden, " + durationInDays[2] + "Minuten"
                ));
                bidMehMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "bid_meh"
                );
                bidMeh.setItemMeta(bidMehMeta);

                inv.setItem(10, bidMeh);
            } else if (price >= auctionMinPrice) {

                //UI Bid confirm
                ItemStack bidConfirm = new ItemStack(Material.LIME_WOOL);
                ItemMeta bidConfirmMeta = bidConfirm.getItemMeta();
                bidConfirmMeta.setDisplayName("§aBestätige: Gebot erstellen");
                bidConfirmMeta.setLore(Arrays.asList(
                        "§cACHTING! Das Geld wird direkt beim erstellen des Gebotes abgezogen",
                        "§eDein Eingabewert ist: " + "§c" + price,
                        "§7Die auktion läuft noch: " + durationInDays[0] + "Tage, " + durationInDays[1] + "Stunden, " + durationInDays[2] + "Minuten",
                        "-------------------------------------------------------------------------------",
                        "§7Momentanes Vermögen: " + Hammaxcustomauction.getInstance().getEconomyManager().getBalance(player.getUniqueId()),
                        "§7Vermögen nach Transaktion: " + (Hammaxcustomauction.getInstance().getEconomyManager().getBalance(player.getUniqueId()) - price)
                ));
                bidConfirmMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "bid_confirm"
                );
                bidConfirmMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG, auction.getAuctionID());
                bidConfirmMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_price"),
                        PersistentDataType.DOUBLE, price
                );
                bidConfirm.setItemMeta(bidConfirmMeta);

                inv.setItem(10, bidConfirm);
            }

            //UI setPriceButton
            ItemStack setPrice = new ItemStack(Material.FEATHER);
            ItemMeta setPriceMeta = setPrice.getItemMeta();
            setPriceMeta.setDisplayName("§ePreis selbst eingeben");
            setPriceMeta.setLore(Arrays.asList(
                    "§7Standardmäßig sind Gebote auf den Mindestbetrag eingestellt",
                    "§7Wenn der eingegebene Betrag niedriger ist als der mindestwert wird er automatisch erhöht",
                    "§7Momentan liegt der Mindestbetrag bei: " + "§e" + auctionMinPrice
            ));
            setPriceMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                    PersistentDataType.LONG, auction.getAuctionID());
            setPriceMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "bid_set_price_button"
            );
            setPrice.setItemMeta(setPriceMeta);

            //inv.setItem(12, setPrice);

            ItemStack itemInfo = auction.getStorageItem().getStack();
            ItemMeta itemInfoMeta = itemInfo.getItemMeta();
            itemInfoMeta.setLore(Arrays.asList(
                    "§cACHTING! Das Geld wird direkt beim erstellen des Gebotes abgezogen",
                    "§7Dein Eingabewert ist: " + "§c" + price,
                    "§7Die auktion läuft noch: " + durationInDays[0] + "Tage, " + durationInDays[1] + "Stunden, " + durationInDays[2] + "Minuten"
            ));
            itemInfoMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "info_item"
            );
            //inv.setItem(4, itemInfo);

            Hammaxcustomauction.getInstance().getDbManager().getPlayerIgnorableBid(player,givenAuctionID, bid -> {

                if (bid.getBidID() != -1) {
                    //UI ignore bid
                    ItemStack ignoreBid = new ItemStack(Material.REDSTONE_TORCH);
                    ItemMeta ignoreBidMeta = ignoreBid.getItemMeta();
                    ignoreBidMeta.setDisplayName("§cGebot aus Liste entfernen");
                    ignoreBidMeta.setLore(Arrays.asList(
                            "§7Entfernt dieses Gebot aus deiner Gebotliste",
                            "§7Die Auktion kann im Auktionsmenü immernoch gefunden werden"
                    ));
                    ignoreBidMeta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                            PersistentDataType.STRING, "ignore_bid_button"
                    );
                    ignoreBidMeta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                            PersistentDataType.LONG, givenAuctionID);
                    ignoreBid.setItemMeta(ignoreBidMeta);

                    inv.setItem(14, ignoreBid);
                    inv.setItem(12, setPrice);
                    inv.setItem(3, itemInfo);

                }else{
                    inv.setItem(13, setPrice);
                    inv.setItem(4, itemInfo);
                }

            });

        });

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        inv.setItem(16, toPrevious);
        player.openInventory(inv);
    }

    public static void openYourBidsUI(Player player, int page, boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 45, "Deine Gebote");

        Hammaxcustomauction.getInstance().getDbManager().getPlayerBidAuctions(player.getUniqueId().toString(), auctions -> {

            int[] availableSlots = {9,10,11,12,13,14,15,16,17,27,28,29,30,31,32,33,34,35};
            int placedItemCounter = 0;

            int countStart = page * 18;
            int countStop = (page + 1) * 18;


            //UI refresh page
            ItemStack refreshPage = new ItemStack(Material.SAND);
            ItemMeta refreshPageMeta = refreshPage.getItemMeta();
            refreshPageMeta.setDisplayName("§eSeite Aktuallisieren");
            refreshPageMeta.setLore(Arrays.asList(
                    "§7Aktuallisiert diese seite",
                    "§e"
            ));
            refreshPageMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "refresh_page"
            );
            refreshPageMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                    PersistentDataType.LONG, (long) page);
            refreshPage.setItemMeta(refreshPageMeta);
            inv.setItem(4, refreshPage);

            if(page == 0){
                //UI Keine Vorherige Auktions Seite
                ItemStack prevPage = new ItemStack(Material.BRICKS);
                ItemMeta prevPageMeta = prevPage.getItemMeta();
                prevPageMeta.setDisplayName("§eVorherige Gebots-Seite");
                prevPageMeta.setLore(Arrays.asList(
                        "§7Es Gibt keine vorherige Gebots-Seite",
                        "§e"
                ));
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "no_prev_offers_button"
                );
                prevPage.setItemMeta(prevPageMeta);
                inv.setItem(2, prevPage );
            }else {
                //UI Vorherige Auktions Seite
                ItemStack prevPage = new ItemStack(Material.ARROW);
                ItemMeta prevPageMeta = prevPage.getItemMeta();
                prevPageMeta.setDisplayName("§eVorherige Gebots-Seite");
                prevPageMeta.setLore(Arrays.asList(
                        "§7Schaue dir Angebote auf der vorherigen Seite an",
                        "§e"
                ));
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "prev_page"
                );
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG, (long) page);
                prevPage.setItemMeta(prevPageMeta);
                inv.setItem(2, prevPage );
            }

            if (auctions.size() > countStop){
                //UI Nächste Auktions Seite
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta nextPageMeta = nextPage.getItemMeta();
                nextPageMeta.setDisplayName("§eNächste Gebots Seite");
                nextPageMeta.setLore(Arrays.asList(
                        "§7Schaue dir Angebote auf der nächsten Seite an",
                        "§e"
                ));
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "next_page"
                );
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG, (long) page);
                nextPage.setItemMeta(nextPageMeta);
                inv.setItem(6, nextPage);
            } else {
                //UI Keine Nächste Auktions Seite
                ItemStack nextPage = new ItemStack(Material.BRICKS);
                ItemMeta nextPageMeta = nextPage.getItemMeta();
                nextPageMeta.setDisplayName("§eNächste Gebots Seite");
                nextPageMeta.setLore(Arrays.asList(
                        "§7Es gibt keine weiteren Seiten",
                        "§e"
                ));
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "no_next_offers_button"
                );
                nextPage.setItemMeta(nextPageMeta);
                inv.setItem(6, nextPage);
            }



            for(int i = countStart; i< countStop && i < auctions.size(); i++){


                Auction auction = auctions.get(i);

                if(auction == null){

                }else {
                    ItemStack stack = auction.getStorageItem().getStack();
                    ItemMeta meta = stack.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    if (meta.hasLore()) {
                        lore.addAll(meta.getLore());
                    }
                    lore.add("§8AuktionID: " + auction.getAuctionID());
                    lore.add("§8ItemID: " + auction.getItemID());
                    lore.add("§cGeplante Deadline: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getDeadline())));


                    if (auction.getBid() != null) {
                        if (auction.getBid().getBidID() != -1) {
                            long tempValue = auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L;
                            if (tempValue < auction.getDeadline()) tempValue = auction.getDeadline();
                            int[] timeLeft = ConversionManager.convertToTimeLeft(tempValue);
                            lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");

                            double amount = auction.getBid().getBidAmount() + auction.getBidIncrease();
                            lore.add("§7Momentan leitendes Gebot: " + amount);
                            if (auction.getBid().getBidOwner().equals(player.getUniqueId().toString())){
                                lore.add("§aDu bietest momentan am meisten");
                            }else {
                                lore.add("§cDu bietest momentan nicht am meisten");
                            }
                        }
                    } else {
                        int[] timeLeft = ConversionManager.convertToTimeLeft(auction.getDeadline());
                        lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");
                        double amount = auction.getStartingPrice() + auction.getBidIncrease();
                        lore.add("§7Mindestpreis des nächsten gebotes: " + amount);
                    }

                    meta.setLore(lore);

                    //Tag
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                            PersistentDataType.LONG, auction.getAuctionID());
                    meta.getPersistentDataContainer().set(
                            new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                            PersistentDataType.STRING, "auction_item"
                    );


                    stack.setItemMeta(meta);

                    ItemStack statusGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    if (auction.getBid() != null && auction.getBid().getBidID() != -1) {
                        if (auction.getBid().getBidOwner().equals(player.getUniqueId().toString())) {
                            statusGlass.setType(Material.LIME_STAINED_GLASS_PANE);
                        }
                    }
                    statusGlass.setItemMeta(meta);

                    inv.setItem(availableSlots[placedItemCounter], stack);

                    inv.setItem(availableSlots[placedItemCounter] + 9, statusGlass);
                }





                placedItemCounter++;
            }





        });




        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);


        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }

    //Menu selection logs/info
    public static void openLogOrInfoSelection(Player player, boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "Logs/Info");

        //Plugin Info
        ItemStack pluginInfo = new ItemStack(Material.OAK_SIGN);
        ItemMeta pluginInfoMeta = pluginInfo.getItemMeta();
        pluginInfoMeta.setDisplayName("§ePlugin Info");
        pluginInfoMeta.setLore(Arrays.asList(
                "§7Hier sind Informationen über Funktionen,",
                "§7Kosten und Einstellungen des Plugins"
                ));
        pluginInfoMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "plugin_info_button"
        );
        pluginInfo.setItemMeta(pluginInfoMeta);

        //Player Logs
        ItemStack playerLogs = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta playerLogsMeta = playerLogs.getItemMeta();
        playerLogsMeta.setDisplayName("§eDeine Auktions-Logs");
        playerLogsMeta.setLore(Arrays.asList(
                "§7Hier ist der Verlauf aller deiner Transaktionen",
                ""
        ));
        playerLogsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "player_logs_button"
        );
        playerLogs.setItemMeta(playerLogsMeta);


        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);


        inv.setItem(8, toPrevious);
        inv.setItem(11, pluginInfo);
        inv.setItem(15, playerLogs);

        player.openInventory(inv);
    }

    //infoMenu
    public static void openGeneralPluginInfoMenu(Player player, boolean local, boolean isReal){
        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 27, "General Info");

        //items ablegen info
        ItemStack depositInfo = new ItemStack(Material.CHEST);
        ItemMeta depositInfoMeta = depositInfo.getItemMeta();
        depositInfoMeta.setDisplayName("§eItemlager");
        depositInfoMeta.setLore(Arrays.asList(
                "§7Beim Auktionshaus-NPC können wertvollere Items abgelegt oder abgeholt werden.",
                "§7Welche Items abgelegt werden können wird dir in dem Menü angezeigt.",
                "§7Du kannst bis zu " + SettingsManager.getMaxPlayerStorage() + " Items ablegen.",
                "§7Wenn eine Auktion für ein Item aktiv ist, kann dieses Item nicht entfernt werden."
        ));
        depositInfo.setItemMeta(depositInfoMeta);
        inv.setItem(16, depositInfo);

        //auktionen info
        ItemStack auctionCreateInfo = new ItemStack(Material.PAPER);
        ItemMeta auctionCreateInfoMeta = auctionCreateInfo.getItemMeta();
        auctionCreateInfoMeta.setDisplayName("§eAuktionen");
        auctionCreateInfoMeta.setLore(Arrays.asList(
                "§7Auktionen haben einen Startpreis, eine Mindesterhöhung und eine Dauer.",
                "§7Solange Spieler auf eine Auktion bieten, endet diese nicht.",
                "§7Beim erstellen einer Auktion muss man einmalig basierend auf der Auktionsdauer einen Betrag zahlen.",
                "§7Du kannst Auktionen nur abbrechen solange keiner auf diese bietet.",
                "§7Es können pro Spieler maximal " + SettingsManager.getMaxPlayerAuctions() +" Auktionen laufen.",
                "§cAm Ende deiner Auktion bekommst du " + SettingsManager.alterAuctionPayout(100) + "% des gewinns"
        ));
        auctionCreateInfo.setItemMeta(auctionCreateInfoMeta);
        inv.setItem(14,auctionCreateInfo);

        //Erstellen info

        //gebote info
        ItemStack bidCreateInfo = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta bidCreateInfoMeta = bidCreateInfo.getItemMeta();
        bidCreateInfoMeta.setDisplayName("§eGebote erstellen");
        bidCreateInfoMeta.setLore(Arrays.asList(
                "§7Beim erstellen eines Gebotes wird dir zur sicherheit " + "§c DIREKT" + "§7 das Geld entfernt",
                "§7Solange du höchstbietender bist, kannst du nicht erneut auf eine Auktion bieten.",
                "§7Wirst du überboten, bekommst du nach spätestens 3 Minuten dein Geld zurück."
        ));
        bidCreateInfo.setItemMeta(bidCreateInfoMeta);
        inv.setItem(12,bidCreateInfo);

        //kosten info
        ItemStack auctionListInfo = new ItemStack(Material.BOOK);
        ItemMeta auctionListInfoMeta = auctionListInfo.getItemMeta();
        auctionListInfoMeta.setDisplayName("§eAuktionsliste");
        auctionListInfoMeta.setLore(Arrays.asList(
                "§7In der Auktionsliste werden dir alle Auktionen außer deine eigenen angezeigt.",
                "§7Oben links kann ein Filter gesetzt werden, welcher beim schließen und öffnen des Menüs bleibt.",
                "§7Es kann dort nach Preis oder Zeit, aufsteigend oder absteigend sortiert werden.",
                "§7Es können auch nach Itemnamen gefiltert werden (in Deutsch/Englisch).",
                "§7Eine Auktion endet nur, wenn die Auktionsdauer endet und seit 24Stunden kein Gebot erstellt wurde."
        ));
        auctionListInfo.setItemMeta(auctionListInfoMeta);
        inv.setItem(10,auctionListInfo);


        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);


        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }




    //logMenu
    public static void openPlayerLogs(Player player, String target, int page,boolean teamAction, boolean local, boolean isReal){
        String pagename = "Spieler Logs";
        if (teamAction){
            pagename = "§cAdmin Spieler-Logs";
        }

        AuctionUIHolder holder = new AuctionUIHolder(local, isReal);
        Inventory inv = Bukkit.createInventory(holder, 54, pagename);


        Hammaxcustomauction.getInstance().getDbManager().getPlayerAuctionLogsFromDB(target, hammaxPlayerLogs -> {

            List<HammaxPaymentLog> payments = hammaxPlayerLogs.getPayments();
            List<HammaxItemChangeLog> changes = hammaxPlayerLogs.getItemChanges();

            List<Integer[]> sort = hammaxPlayerLogs.getSortList();


            int countStart = page * 36;
            int shownElements = (page + 1) * 36;
            int totalElements = payments.size() + changes.size();

                for (int i = countStart; i < shownElements && i < totalElements; i++){
                        if (sort.get(i)[0] == 0){
                            //Payment
                            HammaxPaymentLog activePayment = payments.get(sort.get(i)[1]);

                            Material itemMat = Material.BRICKS;
                            String displayname = "zahlung";
                            String description = "Zahlung";
                            long linkedID = 0;
                            String uiItemType = "";     //auction_log / bid_log

                            if (activePayment.getType().equals("Auction_Payout")){
                                displayname = "§eAuktions-Auszahlung";
                                itemMat = Material.BOOK;
                                description = "Du hast von deiner Auktion "+ "§e" + activePayment.getAmount() + "§7" +" erhalten";
                                linkedID = activePayment.getAuctionID();
                                uiItemType = "auction_log";
                            } else if (activePayment.getType().equals("Bid_Reimburse")) {
                                displayname = "§eGebots-Rückzahlung";
                                itemMat = Material.FILLED_MAP;
                                description = "Dein Gebot wurde überboten. Du hast " + "§e" + activePayment.getAmount()+ "§7" + " zurückbekommen";
                                linkedID = activePayment.getBidID();
                                uiItemType = "bid_log";
                            } else if (activePayment.getType().equals("Bid_Create")) {
                                displayname = "§eGebotserstellung";
                                itemMat = Material.FEATHER;
                                description = "Du hast ein Gebot erstellt und " + "§e" + activePayment.getAmount() + "§7" +  " bezahlt";
                                linkedID = activePayment.getBidID();
                                uiItemType = "bid_log";
                            } else if (activePayment.getType().equals("Auction_Create")) {
                                displayname = "§eAuktionserstellung";
                                itemMat = Material.WRITABLE_BOOK;
                                description = "Du hast eine Auktion erstellt und " + "§e" + activePayment.getAmount() + "§7" + " als gebüren bezahlt";
                                linkedID = activePayment.getAuctionID();
                                uiItemType = "auction_log";
                            }

                            ItemStack pItem = new ItemStack(itemMat);
                            ItemMeta pItemMeta = pItem.getItemMeta();
                            pItemMeta.setDisplayName(displayname);
                            pItemMeta.setLore(Arrays.asList(
                                    "§7" + description,
                                    "§7ZahlungsID: " +activePayment.getPaymentID(),
                                    "§7Datum: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(activePayment.getPaymentDate()))
                            ));
                            if (activePayment.isBookmark()){
                                pItemMeta.setEnchantmentGlintOverride(true);
                            }
                            pItemMeta.getPersistentDataContainer().set(
                                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                                    PersistentDataType.LONG, linkedID);
                            pItemMeta.getPersistentDataContainer().set(
                                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                                    PersistentDataType.STRING, uiItemType
                            );
                            pItemMeta.getPersistentDataContainer().set(
                                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                    PersistentDataType.STRING, target.toString()
                            );
                            pItem.setItemMeta(pItemMeta);

                            inv.setItem(i - countStart + 9, pItem);


                        } else if (sort.get(i)[0] == 1) {
                            //changes
                            HammaxItemChangeLog activeChange = changes.get(sort.get(i)[1]);

                            Material itemMat = Material.BRICKS;
                            String displayname = "ItemÄnderung";
                            String description = "ItemÄnderung";
                            long linkedID = activeChange.getItemID();
                            String uiItemType = "item_change";     //Auction_Item_Add / Auction_Item_Remove / Item_Claim / Item_Add

                            if (activeChange.getType().equals("Item_Add")){
                                displayname = "§aHinzugefügtes Item";
                                description = "Du hast einen Gegenstand in das Itemlager gelegt";
                                itemMat = Material.OAK_CHEST_BOAT;
                            } else if (activeChange.getType().equals("Item_Claim")) {
                                displayname = "§cEntferntes Item";
                                description = "Du hast einen Gegenstand abgeholt";
                                itemMat = Material.OAK_BOAT;
                            } else if (activeChange.getType().equals("Auction_Item_Add")) {
                                displayname = "§aHinzugefügtes Item";
                                description = "Du hast durch eine Auktion ein Item erhalten";
                                itemMat = Material.CHEST_MINECART;
                            } else if (activeChange.getType().equals("Auction_Item_Remove")) {
                                displayname = "§cEntferntes Item";
                                description = "Du hast durch eine Auktion ein Item abgegeben";
                                itemMat = Material.MINECART;
                            }

                            ItemStack cItem = new ItemStack(itemMat);
                            ItemMeta cItemMeta = cItem.getItemMeta();
                            cItemMeta.setDisplayName(displayname);
                            cItemMeta.setLore(Arrays.asList(
                                    "§7" + description,
                                    "§7ChangeID: " + activeChange.getChangeID(),
                                    "§7ItemID: " + activeChange.getItemID(),
                                    "§7Datum: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(activeChange.getDate()))
                            ));
                            if (activeChange.getBookmark()){
                                cItemMeta.setEnchantmentGlintOverride(true);
                            }
                            cItemMeta.getPersistentDataContainer().set(
                                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                                    PersistentDataType.LONG, linkedID);
                            cItemMeta.getPersistentDataContainer().set(
                                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                                    PersistentDataType.STRING, uiItemType
                            );
                            cItemMeta.getPersistentDataContainer().set(
                                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                    PersistentDataType.STRING, target.toString()
                            );
                            cItem.setItemMeta(cItemMeta);

                            inv.setItem(i - countStart + 9, cItem);

                        }
                }

            if(page == 0){
                //UI Keine Vorherige Auktions Seite
                ItemStack prevPage = new ItemStack(Material.BRICKS);
                ItemMeta prevPageMeta = prevPage.getItemMeta();
                prevPageMeta.setDisplayName("§eVorherige Log-Seite");
                prevPageMeta.setLore(Arrays.asList(
                        "§7Es Gibt keine vorherige Log-Seite",
                        "§e"
                ));
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "no_prev_offers_button"
                );
                prevPage.setItemMeta(prevPageMeta);
                inv.setItem(2, prevPage );
            }else {
                //UI Vorherige Auktions Seite
                ItemStack prevPage = new ItemStack(Material.ARROW);
                ItemMeta prevPageMeta = prevPage.getItemMeta();
                prevPageMeta.setDisplayName("§eVorherige Log-Seite");
                prevPageMeta.setLore(Arrays.asList(
                        "§7Schaue dir Logs auf der vorherigen Seite an",
                        "§e"
                ));
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "prev_page"
                );
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, target.toString()
                );
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG, (long) page);
                prevPage.setItemMeta(prevPageMeta);
                inv.setItem(2, prevPage );
            }

            if (totalElements > shownElements){
                //UI Nächste Auktions Seite
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta nextPageMeta = nextPage.getItemMeta();
                nextPageMeta.setDisplayName("§eNächste Log-Seite");
                nextPageMeta.setLore(Arrays.asList(
                        "§7Lade weitere Logs",
                        "§e"
                ));
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "next_page"
                );
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, target.toString()
                );
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG, (long) page);
                nextPage.setItemMeta(nextPageMeta);
                inv.setItem(6, nextPage);
            } else {
                //UI Keine Nächste Auktions Seite
                ItemStack nextPage = new ItemStack(Material.BRICKS);
                ItemMeta nextPageMeta = nextPage.getItemMeta();
                nextPageMeta.setDisplayName("§eNächste Log-Seite");
                nextPageMeta.setLore(Arrays.asList(
                        "§7Es gibt keine Logs",
                        "§e"
                ));
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "no_next_offers_button"
                );
                nextPage.setItemMeta(nextPageMeta);
                inv.setItem(6, nextPage);
            }






        });





        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur vorherigen Seite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur vorherigen seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );

        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, target.toString()
        );
        toPrevious.setItemMeta(toPreviousMeta);


        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }






    //sortLogsByDate



    public static List<Auction> filterAuctionsByName(List<Auction> input, String filterText){

        if(filterText == null || filterText.isEmpty()){
            return new ArrayList<>(input);
        }

        String lowerFilter = filterText.toLowerCase();

        List<Auction> filtered = new ArrayList<>();

        for (Auction auction : input){
            boolean added = false;
            if (auction == null) continue;
            if (auction.getStorageItem() == null) continue;
            if (auction.getStorageItem().getStack() == null) continue;

            ItemStack item = auction.getStorageItem().getStack();
            String germanName = SettingsManager.materialToGerman(item.getType());

            if (item.hasItemMeta()) {
                if (item.getItemMeta().hasDisplayName()) {
                    String displayName = item.getItemMeta().getDisplayName().toString().toLowerCase();
                    if (displayName != null && displayName.contains(lowerFilter)){
                        filtered.add(auction);
                        added = true;
                    }
                }
            }
            if (ConversionManager.normaliseMaterial(item.getType().toString()).toLowerCase().contains(filterText) && !added) {
                filtered.add(auction);
                added = true;
            } else if (!germanName.equals("ERROR") && germanName.contains(filterText) && !added || germanName.toLowerCase().contains(filterText) && !added && !germanName.equals("ERROR")) {
                filtered.add(auction);
                added = true;
            }
        }
        return filtered;
    }

    public static List<Auction> sortAuctionsByPrice(List<Auction> input, boolean ascending){
        List<Auction> sorted = new ArrayList<>(input);

        sorted.sort((a1, a2) -> {
            double value1 = calculatePriceSortValue(a1);
            double value2 = calculatePriceSortValue(a2);

            int result = Double.compare(value1, value2);

            return ascending ? result : -result;
        });
        return sorted;
    }

    private static double calculatePriceSortValue(Auction auction){
        if (auction == null) return 0;
        if (auction.getBid() != null){
            return auction.getBid().getBidAmount() + auction.getBidIncrease();
        } else {
            return auction.getStartingPrice() + auction.getBidIncrease();
        }

    }

    public static List<Auction> sortAuctionsByTime(List<Auction> input, boolean ascending){
        List<Auction> sorted = new ArrayList<>(input);

        sorted.sort((a1, a2) -> {
            long value1 = calculateTimeSortValue(a1);
            long value2 = calculateTimeSortValue(a2);

            int result = Long.compare(value1, value2);

            return ascending ? result : -result;
        });
        return sorted;
    }

    private static long calculateTimeSortValue(Auction auction){
        if (auction == null) return 0;
        if (auction.getBid() != null){
            long tempValue = auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L;
            if (tempValue < auction.getDeadline()) tempValue = auction.getDeadline();
            return tempValue;
        } else {
            return auction.getDeadline();
        }

    }

}
