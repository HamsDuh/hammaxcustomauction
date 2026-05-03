package hdh.hammaxcustomauction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AdminUI {

    //HauptUI des Admin-Menüs
    public static void openAdminUI(Player player, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 27, "§cAdmin UI");

        //detailed auction list
        ItemStack detailedAuctionList = new ItemStack(Material.BOOK);
        ItemMeta detailedAuctionListMeta = detailedAuctionList.getItemMeta();
        detailedAuctionListMeta.setDisplayName("§eAdmin-Auktionsliste");
        detailedAuctionListMeta.setLore(Arrays.asList(
                "§7Hier werden zu den auktionen auch die Spieler angezeigt.",
                "§7Auswählen bringt dich zu einer Info-Seite",
                "§cOperatoren können laufende Auktionen Abbrechen"
        ));
        detailedAuctionListMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "admin_auction_list"
        );
        detailedAuctionList.setItemMeta(detailedAuctionListMeta);


        //player info (namenseingabe -> playerActionsUI)
        ItemStack playerInfo = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerInfoMeta = playerInfo.getItemMeta();
        playerInfoMeta.setDisplayName("§eSpieler-Info");
        playerInfoMeta.setLore(Arrays.asList(
                "§7Nach klicken bitte " + "§e/aset [Spielername]" + "§7 nutzen",
                "§7um die Spielerübersicht zu öffnen",
                "§7Inhalte:",
                "§7Spielerauktionen/Gebote",
                "§7Spielerlogs/Zahlungen",
                "§7Spieler-Itemlager"
        ));
        playerInfoMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "player_actions"
        );
        playerInfo.setItemMeta(playerInfoMeta);

        //AuctionSettings
        ItemStack auctionSettings = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta auctionSettingsMeta = auctionSettings.getItemMeta();
        auctionSettingsMeta.setDisplayName("§ePlugin-Einstellungen");
        auctionSettingsMeta.setLore(Arrays.asList(
                "§7Momentan nur diese Funktionen:",
                "§7Auktionen erstellen an/aus schalten",
                "§7Items INS LAGER legen an/aus schalten",
                "§7-------------------------------------------------------------",
                "§7Weitere anpassungen können in der \"config\"-datei gemacht werden",
                "§7Erweiterungen könnten in Zukunft kommen"
        ));
        auctionSettingsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "auction_settings"
        );
        auctionSettings.setItemMeta(auctionSettingsMeta);


        inv.setItem(10, detailedAuctionList);
        inv.setItem(13, playerInfo);
        inv.setItem(16, auctionSettings);

        player.openInventory(inv);
    }


    //kopie der originalen Auktionsliste
    public static void openDetailedAuctionList(Player player, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 54, "§cAdmin Auktions-Liste");

        //Auktionsfilter metadata wird gesucht, wenn nicht neu erstellt
        AuctionFilter filter;

        if (player.hasMetadata("auction_filter")){
            filter = (AuctionFilter) player.getMetadata("auction_filter").get(0).value();
        }else {
            filter = new AuctionFilter();
        }

        //Erhalte alle offenen Auktionen für Spieler null (Spieler null wird ausgeschlossen -> alle, auch eigene werden angezeigt)
        Hammaxcustomauction.getInstance().getDbManager().getPublicAuctionsForPlayer("", auctions -> {

            //Anzeigelogik: erhält alle auktionen, zeigt aber nur im Bereich countStart -> shownElements an
            int countStart = filter.getPage() * 36;
            int shownElements = (filter.getPage() + 1) * 36;
            int totalElements;

            List<Auction> filteredAuctions = auctions;


            //Sortiert alle Auktionen um
            //Weitere Sortieroptionen können hier eingebaut werden
            if ("Zeit".equals(filter.getSortStyle())){
                filteredAuctions = AuctionUI.sortAuctionsByTime(filteredAuctions, filter.getAscending());
            } else if ("Preis".equals(filter.getSortStyle())) {
                filteredAuctions = AuctionUI.sortAuctionsByPrice(filteredAuctions, filter.getAscending());
            }

            if(filter.getNameFilter() != null && !filter.getNameFilter().isEmpty()) {
                filteredAuctions = AuctionUI.filterAuctionsByName(filteredAuctions, filter.getNameFilter());
            }

            //neue gefilterte List
            totalElements = filteredAuctions.size();

            //Seitenwechsel knöpfe
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
                //Falls hier was wäre, wäre es ein Fehler, ist also eher zum abfangen
                }else {
                    //Auktions UI-Item erstellen
                    ItemStack stack = auction.getStorageItem().getStack();
                    ItemMeta meta = stack.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    if (meta.hasLore()) {
                        lore.addAll(meta.getLore());
                    }
                    lore.add("§8AuktionID: " + auction.getAuctionID());
                    lore.add("§8ItemID: " + auction.getItemID());
                    lore.add("§cGeplante Deadline: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getDeadline())));
                    lore.add("Owner: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getStartingOwner())).getName());

                    //Prüfen ob es eine Bid gibt, falls ja, anpassen
                    if (auction.getBid() != null) {
                        if (auction.getBid().getBidID() != -1) {
                            long tempValue = auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L;
                            if (tempValue < auction.getDeadline()) tempValue = auction.getDeadline();
                            int[] timeLeft = ConversionManager.convertToTimeLeft(tempValue);
                            lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");

                            double amount = auction.getBid().getBidAmount();// + auction.getBidIncrease();
                            lore.add("§7Momentan leitendes Gebot: " + amount);
                            lore.add("Höchst bietender: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getBid().getBidOwner())).getName());
                        }
                    } else {
                        int[] timeLeft = ConversionManager.convertToTimeLeft(auction.getDeadline());
                        lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");
                        lore.add("§7Mindestpreis des nächsten gebotes: " + auction.getStartingPrice());
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
            //eigendlich unnötig aber warum nicht
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
                "§cDas ist die normale Filter Funktion!",
                "§cNach Änderungen bitte AdminUI neu Öffnen",
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


        //AdminAuctionActions

    }

    //Hier können zu Stand des Kommentars nur die standard admin infos aus dem vorherigen UI gezeigt werden :/
    //Hat aber nen netten zugriff auf das Spieler-Aktionen UI und kann Auktionen beenden
    public static void openAdminAuctionActions(Player player, long givenID, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 27, "§cAdmin Auktions-Aktionen");

        //Holt sich nochmal die gegebene Auktionsdetails
        Hammaxcustomauction.getInstance().getDbManager().getAuctionFromID(givenID, auction -> {

            ItemStack aItem = auction.getStorageItem().getStack();

            ItemStack aInfo = new ItemStack(Material.BOOK);
            ItemMeta aInfoMeta = aInfo.getItemMeta();
            aInfoMeta.setDisplayName("§eAuktionID: " + auction.getAuctionID());
            List<String> lore = new ArrayList<>();
            if (aInfoMeta.hasLore()) {
                lore.addAll(aInfoMeta.getLore());
            }
            lore.add("§8ItemID: " + auction.getItemID());
            lore.add("§cGeplante Deadline: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getDeadline())));
            lore.add("Owner: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getStartingOwner())).getName());

            if (auction.getBid() != null) {
                if (auction.getBid().getBidID() != -1) {
                    long tempValue = auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L;
                    if (tempValue < auction.getDeadline()) tempValue = auction.getDeadline();
                    int[] timeLeft = ConversionManager.convertToTimeLeft(tempValue);
                    lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");

                    double amount = auction.getBid().getBidAmount() + auction.getBidIncrease();
                    lore.add("§7Momentan leitendes Gebot: " + amount);
                    lore.add("Höchst bietender: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getBid().getBidOwner())).getName());
                }
            } else {
                int[] timeLeft = ConversionManager.convertToTimeLeft(auction.getDeadline());
                lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");
                double amount = auction.getStartingPrice() + auction.getBidIncrease();
                lore.add("§7Mindestpreis des nächsten gebotes: " + amount);
            }
            aInfoMeta.setLore(lore);
            aInfo.setItemMeta(aInfoMeta);

            inv.setItem(10, aItem);
            inv.setItem(12, aInfo);


            //open auction owner info shortcut
            ItemStack pHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta pHeadMeta = (SkullMeta) pHead.getItemMeta();
            pHeadMeta.setDisplayName("§e"+ Bukkit.getOfflinePlayer(UUID.fromString(auction.getStartingOwner())).getName());
            pHeadMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(auction.getStartingOwner())));
            pHeadMeta.setLore(Arrays.asList(
                    "§7Öffne für diesen Spieler",
                    "§7die Admin Spieler-Aktionen Seite"
            ));
            pHeadMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "player_info"
            );
            pHeadMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                    PersistentDataType.STRING, auction.getStartingOwner()
            );
            pHead.setItemMeta(pHeadMeta);

            inv.setItem(14, pHead);

            //op check
            if (player.isOp() && local && real){
                ItemStack endAuction = new ItemStack(Material.REDSTONE_BLOCK);
                ItemMeta endAuctionMeta = endAuction.getItemMeta();
                endAuctionMeta.setDisplayName("§cAuktion Beenden");
                endAuctionMeta.setLore(Arrays.asList(
                        "§7Schließt diese Auktion,",
                        "§7Überträgt " + "§cNICHT" + "§7 das Item",
                        "§7und zahlt alle bietenden zurück",
                        "-----------------------------------",
                        "§cBITTE NUR BEI ERNSTFÄLLEN NUTZEN",
                        "-----------------------------------",
                        "§cDIESE FUNKTION SOLL NICHT ZUM VORTEIL",
                        "§cEINES SPIELERS GENUTZT WERDEN"
                ));
                endAuctionMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                        PersistentDataType.STRING, "end_auction_button"
                );
                endAuctionMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                        PersistentDataType.LONG, givenID
                );
                endAuctionMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, auction.getStartingOwner()
                        );
                endAuction.setItemMeta(endAuctionMeta);

                inv.setItem(16, endAuction);
            }

        });

        //bidlist   //meh                                                           kommt irgendwann anders vllt

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

        player.openInventory(inv);  //ItemInfo fehlt (Zeigt alle infos über Item) kommt auch iwann später vllt
    }

    //extra nachfrage damit nicht aus Versehen auktionen durch misklicks geschlossen werden
    public static void openAdminAuctionCancelConfirmUI(Player player, String targetPlayer, long givenID, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 27, "§cAdmin Auktion abbrechen bestätigung");

        if (player.isOp() && local && real){
            ItemStack endAuction = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta endAuctionMeta = endAuction.getItemMeta();
            endAuctionMeta.setDisplayName("§cAuktion Beenden");
            endAuctionMeta.setLore(Arrays.asList(
                    "§7Schließt diese Auktion,",
                    "§7Überträgt " + "§cNICHT" + "§7 das Item",
                    "§7und zahlt alle bietenden zurück",
                    "-----------------------------------",
                    "§cBITTE NUR BEI ERNSTFÄLLEN NUTZEN",
                    "-----------------------------------",
                    "§cDIESE FUNKTION SOLL NICHT ZUM VORTEIL",
                    "§cEINES SPIELERS GENUTZT WERDEN"
            ));
            endAuctionMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "end_auction_button"
            );
            endAuctionMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                    PersistentDataType.LONG, givenID
            );
            endAuctionMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                    PersistentDataType.STRING, targetPlayer
            );
            endAuction.setItemMeta(endAuctionMeta);

            inv.setItem(11, endAuction);
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
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                PersistentDataType.LONG, givenID
        );
        toPrevious.setItemMeta(toPreviousMeta);

        inv.setItem(15, toPrevious);

        player.openInventory(inv);
    }

    //Wahrscheinlich der wichtigste teil des Admin-UI: Alle nötigen Spielerinfos
    public static void openAdminPlayerActions(Player player, String uuid, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 27, "§cAdmin Spieler-Aktionen");

        //spieler auktionen
        ItemStack playerAuctions = new ItemStack(Material.BOOK);
        ItemMeta playerAuctionsMeta = playerAuctions.getItemMeta();
        playerAuctionsMeta.setDisplayName("§eÖffne Auktionsliste des Spielers");
        playerAuctionsMeta.setLore(Arrays.asList(
                "§7Hier werden alle aktiven und beendeten Auktionen des Spielers gezeigt",
                "§7Beim wählen einer Auktion wird das \"Admin Auktions-Aktionen\" menü geöffnet"
        ));
        playerAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "player_auction_list"
        );
        playerAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        playerAuctions.setItemMeta(playerAuctionsMeta);

        //spieler gebote                                                    momentan nicht optimal, zeigt gerade gebote an wo spieler #1 ist, kp warum
        ItemStack playerBids = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta playerBidsMeta = playerBids.getItemMeta();
        playerBidsMeta.setDisplayName("§eÖffne Gebotsliste des Spielers");
        playerBidsMeta.setLore(Arrays.asList(
                "§7Hier werden alle aktiven und beendeten Gebote des Spielers gezeigt",
                "§7Beim wählen eines Gebotes wird eine übersicht für die Auktion "
        ));
        playerBidsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "player_bid_list"
        );
        playerBidsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        playerBids.setItemMeta(playerBidsMeta);

        //open auction owner info
        ItemStack pHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta pHeadMeta = (SkullMeta) pHead.getItemMeta();
        pHeadMeta.setDisplayName("§e"+ Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
        pHeadMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        pHeadMeta.setLore(Arrays.asList(
                "§7Öffne für diesen Spieler",
                "§7die Admin Spieler-Aktionen Seite"
        ));
        pHeadMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "player_info"
        );
        pHeadMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        pHead.setItemMeta(pHeadMeta);

        //spieler logs
        ItemStack playerLogs = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta playerLogsMeta = playerLogs.getItemMeta();
        playerLogsMeta.setDisplayName("§eÖffne Spieler Logs");
        playerLogsMeta.setLore(Arrays.asList(
                "§7Öffnet die geschichte von übertragungen für:",
                "§7" + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()

        ));
        playerLogsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "player_logs"
        );
        playerLogsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        playerLogs.setItemMeta(playerLogsMeta);

            //pending payments                   ist der Spieler offline, so wird er beim nächsten beitreten ausgezahlt. Farmwelt ETC gilt auch als offline
            ItemStack playerPendingPayments = new ItemStack(Material.PAPER);
            ItemMeta playerPendingPaymentsMeta = playerPendingPayments.getItemMeta();
            playerPendingPaymentsMeta.setDisplayName("§eÖffne ausstehende Auszahlungen");
            playerPendingPaymentsMeta.setLore(Arrays.asList(
                    "§7Öffne die Liste an ausstehenden Auszahlungen an: " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName(),
                    "§7Die einträge der Liste werden beim nächsten Spielbeitritt ausgezahlt"
            ));
            playerPendingPaymentsMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "player_pending_payments"
            );
            playerPendingPaymentsMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                    PersistentDataType.STRING, uuid
            );
            playerPendingPayments.setItemMeta(playerPendingPaymentsMeta);

        ItemStack viewStorage = new ItemStack(Material.CHEST);
        ItemMeta viewStorageMeta = viewStorage.getItemMeta();
        viewStorageMeta.setDisplayName("§eItemlager-übersicht");
        viewStorageMeta.setLore(Arrays.asList(
                "§7Öffnet eine Übersicht für die Inhalte des Itemlagers von: " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName(),
                "§7Hier können keine Änderungen vorgenommen werden"
        ));
        viewStorageMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "player_view_storage"
        );
        viewStorageMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        viewStorage.setItemMeta(viewStorageMeta);

        //nur mit OP kann man items aus dem lager anderer spieler nehmen, als teamler kann man es sich aber anschauen
        if (player.isOp()) {
            ItemStack editStorage = new ItemStack(Material.CHEST_MINECART);
            ItemMeta editStorageMeta = editStorage.getItemMeta();
            editStorageMeta.setDisplayName("§cSpielerlager Bearbeiten");
            editStorageMeta.setLore(Arrays.asList(
                    "§7Erlaubt zugriff auf das Itemlager des Spielers: " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName(),
                    "---------------------------------------------------------",
                    "§cNur gedacht um illegale/duplizierte Items zu entfernen",
                    "---------------------------------------------------------"
            ));
            editStorageMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                    PersistentDataType.STRING, "player_edit_storage"
            );
            editStorageMeta.getPersistentDataContainer().set(
                    new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                    PersistentDataType.STRING, uuid
            );
            editStorage.setItemMeta(editStorageMeta);

            inv.setItem(25, editStorage);
        }

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur Hauptseite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur \"Admin UI\" Seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        //items im inventar verteilen
        inv.setItem(4, pHead);

        inv.setItem(10, playerAuctions);
        inv.setItem(12, playerBids);
        inv.setItem(14, playerLogs);
        inv.setItem(23, playerPendingPayments);
        inv.setItem(16, viewStorage);

        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }

    //zeigt alle laufende  auktionen des Spielers
    public static void openAdminPlayerAuctions(Player player, String uuid, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 27, "§cAdmin Spieler-Auktionen");

        //erhält alle laufenden auktionen
        Hammaxcustomauction.getInstance().getDbManager().getAuctionsFromPlayer(uuid,auctions -> {
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
                lore.add("§7Besitzer: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getStartingOwner())).getName());
                lore.add("§cPlannedDeadline: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getDeadline())));
                lore.add("§7Startpreis: " + auction.getStartingPrice());
                lore.add("§7Mindesterhöhung: " + auction.getBidIncrease());

                if(auction.getBid() != null){
                    if (auction.getBid().getBidID() != -1){
                        //lore.add("§7Momentan leitendes Gebot: " + auction.getBid().getBidAmount());
                        lore.add("§7Gebot-Besitzer: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getBid().getBidOwner())).getName());
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
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, uuid
                );

                stack.setItemMeta(meta);
                inv.setItem(slot, stack);

                slot++;
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
                PersistentDataType.STRING, uuid
        );
        toPrevious.setItemMeta(toPreviousMeta);

        inv.setItem(8,toPrevious);

        //old auctions swap
        ItemStack oldAuctions = new ItemStack(Material.LIME_DYE);
        ItemMeta  oldAuctionsMeta = oldAuctions.getItemMeta();
        oldAuctionsMeta.setDisplayName("§aLaufende Auktionen");
        oldAuctionsMeta.setLore(Arrays.asList(
                "§7Du siehst gerade aktuelle Auktionen",
                "§eKlicken öffnet abgelaufende Auktionen"
        ));
        oldAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_old_auctions"
        );
        oldAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        oldAuctions.setItemMeta(oldAuctionsMeta);

        inv.setItem(0,oldAuctions);

        player.openInventory(inv);
    }

    //zeigt alle alten Auktionen des Spielers
    public static void openAdminPlayerOldAuctions(Player player, String uuid, int page, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 45, "§cAdmin Alte Spieler-Auktionen");

        Hammaxcustomauction.getInstance().getDbManager().getOldAuctionsFromPlayer(uuid,auctions -> {
            int slot = 9;
            int countstart = page*36;
            int shownElements = (page+1) * 36;
            int totalElements = auctions.size();

            for (int  i = 0; i < 45 && i+countstart < totalElements; i++) {
                Auction auction = auctions.get(i + countstart);

                ItemStack stack = auction.getStorageItem().getStack();
                ItemMeta meta = stack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if (meta.hasLore()) {
                    lore.addAll(meta.getLore());
                }
                lore.add("§8AuktionID: " + auction.getAuctionID());
                lore.add("§8ItemID: " + auction.getItemID());
                lore.add("§7Besitzer: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getStartingOwner())).getName());
                lore.add("§cPlannedDeadline: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getDeadline())));
                lore.add("§7Startpreis: " + auction.getStartingPrice());
                lore.add("§7Mindesterhöhung: " + auction.getBidIncrease());

                if(auction.getBid() != null){
                    if (auction.getBid().getBidID() != -1){
                        lore.add("Letztes Gebot: " + auction.getBid().getBidAmount());
                        lore.add("§7Gebot-Besitzer: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getBid().getBidOwner())).getName());
                        lore.add("§7Gebot erstellt am: "+ ConversionManager.formatTime(ConversionManager.correctTimeOffset(auction.getBid().getBidDate())));
                    }
                } else {
                    lore.add("§7Es gab auf diese Auktion keine Gebote");
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
                meta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, uuid
                );

                stack.setItemMeta(meta);
                inv.setItem(slot, stack);

                slot++;
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
                        PersistentDataType.STRING, uuid
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
                        PersistentDataType.STRING, uuid
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
                PersistentDataType.STRING, uuid
        );
        toPrevious.setItemMeta(toPreviousMeta);

        inv.setItem(8,toPrevious);

        //old auctions swap
        ItemStack oldAuctions = new ItemStack(Material.GRAY_DYE);
        ItemMeta  oldAuctionsMeta = oldAuctions.getItemMeta();
        oldAuctionsMeta.setDisplayName("§7Alte Auktionen");
        oldAuctionsMeta.setLore(Arrays.asList(
                "§7Du siehst gerade alte Auktionen",
                "§eKlicken öffnet laufende Auktionen"
        ));
        oldAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_new_auctions"
        );
        oldAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        oldAuctions.setItemMeta(oldAuctionsMeta);

        inv.setItem(0,oldAuctions);

        player.openInventory(inv);
    }


    public static void openAdminPlayerBids(Player player, String uuid, int page,  boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 45, "§cAdmin Spieler-Gebote");

        Hammaxcustomauction.getInstance().getDbManager().getIgnoredAndOldBidsForPlayer(uuid,false,auctions -> {

            //int[] availableSlots = {9,10,11,12,13,14,15,16,17,27,28,29,30,31,32,33,34,35};
            int placedItemCounter = 0;

            int countStart = page * 36;
            int countStop = (page + 1) * 36;

            //player.sendMessage("§cErgebnisse: " + auctions.size());

            /*
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
            */


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
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, uuid
                );
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
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, uuid
                );
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
                    lore.add("§7Auktionsbesitzer: "+ Bukkit.getOfflinePlayer(UUID.fromString(auction.getStartingOwner())).getName());

                    if (auction.getBid() != null) {
                        if (auction.getBid().getBidID() != -1) {
                            long tempValue = auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L;
                            if (tempValue < auction.getDeadline()) tempValue = auction.getDeadline();
                            int[] timeLeft = ConversionManager.convertToTimeLeft(tempValue);
                            lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");

                            lore.add("§7Dieses Gebot: " + auction.getBid().getBidAmount());
                            lore.add("§7Leitendes Gebot: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getBid().getBidOwner())).getName());
                            if (auction.getBid().getBidCanceled() != 0){
                                lore.add("§7Dieses Gebot wurde rückerstattet");
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

                    /*

                    ItemStack statusGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    if (auction.getBid() != null && auction.getBid().getBidID() != -1) {
                        if (auction.getBid().getBidOwner().equals(player.getUniqueId().toString())) {
                            statusGlass.setType(Material.LIME_STAINED_GLASS_PANE);
                        }
                    }
                    statusGlass.setItemMeta(meta);

                    */

                    inv.setItem(9+placedItemCounter/*availableSlots[placedItemCounter]*/, stack);

                    //inv.setItem(availableSlots[placedItemCounter] + 9, statusGlass);
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
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        toPrevious.setItemMeta(toPreviousMeta);

        inv.setItem(8,toPrevious);

        //old auctions swap
        ItemStack oldAuctions = new ItemStack(Material.LIME_DYE);
        ItemMeta  oldAuctionsMeta = oldAuctions.getItemMeta();
        oldAuctionsMeta.setDisplayName("§aAktuelle Gebote");
        oldAuctionsMeta.setLore(Arrays.asList(
                "§7Du siehst gerade aktuelle Gebote",
                "§eKlicken öffnet alte Gebote"
        ));
        oldAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_old_bids"
        );
        oldAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        oldAuctions.setItemMeta(oldAuctionsMeta);

        inv.setItem(0,oldAuctions);

        player.openInventory(inv);
    }

    public static void openAdminPlayerOldBids(Player player, String uuid, int page, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 45, "§cAdmin Alte Spieler-Gebote");

        Hammaxcustomauction.getInstance().getDbManager().getIgnoredAndOldBidsForPlayer(uuid,true,auctions -> {

            //int[] availableSlots = {9,10,11,12,13,14,15,16,17,27,28,29,30,31,32,33,34,35};
            int placedItemCounter = 0;

            int countStart = page * 36;
            int countStop = (page + 1) * 36;

            /*
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
            */


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
                prevPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, uuid
                );
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
                nextPageMeta.getPersistentDataContainer().set(
                        new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                        PersistentDataType.STRING, uuid
                );
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
                    lore.add("§7Auktionsbesitzer: "+ Bukkit.getOfflinePlayer(UUID.fromString(auction.getStartingOwner())).getName());

                    if (auction.getBid() != null) {
                        if (auction.getBid().getBidID() != -1) {
                            long tempValue = auction.getBid().getBidDate() + 24L * 60L * 60L * 1000L;
                            if (tempValue < auction.getDeadline()) tempValue = auction.getDeadline();
                            int[] timeLeft = ConversionManager.convertToTimeLeft(tempValue);
                            //lore.add("§7Die Auktion läuft noch " + "§c" + timeLeft[0] + "Tage, " + timeLeft[1] + "Stunden, " + timeLeft[2] + "Minuten");
                            if (auction.getBid().getBidCanceled() != 0){
                                lore.add("§7Dieses Gebot wurde rückerstattet");
                            }
                            lore.add("§7Dieses Gebot: " + auction.getBid().getBidAmount());
                            lore.add("§7Leitendes Gebot: " + Bukkit.getOfflinePlayer(UUID.fromString(auction.getBid().getBidOwner())).getName());
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

                    /*

                    ItemStack statusGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    if (auction.getBid() != null && auction.getBid().getBidID() != -1) {
                        if (auction.getBid().getBidOwner().equals(player.getUniqueId().toString())) {
                            statusGlass.setType(Material.LIME_STAINED_GLASS_PANE);
                        }
                    }
                    statusGlass.setItemMeta(meta);

                    */

                    inv.setItem(9+placedItemCounter/*availableSlots[placedItemCounter]*/, stack);

                    //inv.setItem(availableSlots[placedItemCounter] + 9, statusGlass);
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
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        toPrevious.setItemMeta(toPreviousMeta);

        inv.setItem(8,toPrevious);

        //old auctions swap
        ItemStack oldAuctions = new ItemStack(Material.GRAY_DYE);
        ItemMeta  oldAuctionsMeta = oldAuctions.getItemMeta();
        oldAuctionsMeta.setDisplayName("§7Alte Gebote");
        oldAuctionsMeta.setLore(Arrays.asList(
                "§7Du siehst gerade alte Gebote",
                "§eKlicken öffnet laufende Gebote"
        ));
        oldAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_new_bids"
        );
        oldAuctionsMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, uuid
        );
        oldAuctions.setItemMeta(oldAuctionsMeta);

        inv.setItem(0,oldAuctions);

        player.openInventory(inv);

    }

    public static void openAdminPlayerPendingPayments(Player player, String uuid, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 54, "§cAdmin-Ausstehendezahlungen Ansicht");

        Hammaxcustomauction.getInstance().getDbManager().getPlayerpendingPayments(uuid,hammaxPaymentLogs -> {
            for (int i = 9; i-9 < hammaxPaymentLogs.size() && i < 54; i++){

                int simplify = i-9;

                Material itemMat = Material.BRICKS;
                String displayname = "zahlung";
                String description = "Zahlung";
                long linkedID = 0;
                String uiItemType = "";     //auction_log / bid_log

                if (hammaxPaymentLogs.get(simplify).getType().equals("Auction_Payout")){
                    displayname = "§eAuktions-Auszahlung";
                    itemMat = Material.BOOK;
                    description = "Du hast von deiner Auktion "+ "§e" + hammaxPaymentLogs.get(simplify).getAmount() + "§7" +" erhalten";
                    linkedID = hammaxPaymentLogs.get(simplify).getAuctionID();
                    uiItemType = "auction_log";
                } else if (hammaxPaymentLogs.get(simplify).getType().equals("Bid_Reimburse")) {
                    displayname = "§eGebots-Rückzahlung";
                    itemMat = Material.FILLED_MAP;
                    description = "Dein Gebot wurde überboten. Du hast " + "§e" + hammaxPaymentLogs.get(simplify).getAmount()+ "§7" + " zurückbekommen";
                    linkedID = hammaxPaymentLogs.get(simplify).getBidID();
                    uiItemType = "bid_log";
                }


                ItemStack pItem = new ItemStack(itemMat);
                ItemMeta pItemMeta = pItem.getItemMeta();
                pItemMeta.setDisplayName(displayname);
                pItemMeta.setLore(Arrays.asList(
                        "§7" + description,
                        "§7ZahlungsID: " +hammaxPaymentLogs.get(simplify).getPaymentID(),
                        "§7Datum: " + ConversionManager.formatTime(ConversionManager.correctTimeOffset(hammaxPaymentLogs.get(simplify).getPaymentDate()))
                ));
                if (hammaxPaymentLogs.get(simplify).isBookmark()){
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
                        PersistentDataType.STRING, uuid
                );
                pItem.setItemMeta(pItemMeta);

                inv.setItem(i, pItem);

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
                PersistentDataType.STRING, uuid
        );
        toPrevious.setItemMeta(toPreviousMeta);

        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }

    public static void openPlayerStorage(Player player, String target, boolean editPerms, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 54, "§cAdmin-Spielerstorage Ansicht");

        Hammaxcustomauction.getInstance().getDbManager().getItemsFromStorage(target,
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
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_perms"),
                                PersistentDataType.BOOLEAN, editPerms
                        );
                        meta.getPersistentDataContainer().set(
                                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                                PersistentDataType.STRING, target
                        );

                        stack.setItemMeta(meta);
                        inv.setItem(slot, stack);

                        slot++;
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
                PersistentDataType.STRING, target
        );
        toPrevious.setItemMeta(toPreviousMeta);

        //UI Placement
        //inv.setItem(4, addItem);
        inv.setItem(8, toPrevious);

        player.openInventory(inv);

    }

    public static void openAdminStorageitemRemoveConfirm(Player player, String target, long itemID, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 27, "§cAdmin Spieler-Item entfernen");

        ItemStack confirmRemoval = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmRemovalMeta = confirmRemoval.getItemMeta();
        confirmRemovalMeta.setDisplayName("§aBestätigen");
        confirmRemovalMeta.setLore(Arrays.asList(
                "§7Entfernt das Item aus dem Itemlager des Spielers",
                "§cBitte nicht missbrauchen :/"
        ));
        confirmRemovalMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "confirm_button"
        );
        confirmRemovalMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "player_id"),
                PersistentDataType.STRING, target
        );
        confirmRemovalMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                PersistentDataType.LONG, itemID
        );
        confirmRemoval.setItemMeta(confirmRemovalMeta);

        Hammaxcustomauction.getInstance().getDbManager().getItemFromID(itemID, storageItem->{

            ItemStack item = storageItem.getStack();

            inv.setItem(13, item);
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
                PersistentDataType.STRING, target
        );
        toPrevious.setItemMeta(toPreviousMeta);

        //UI Placement
        inv.setItem(10, confirmRemoval);
        inv.setItem(16, toPrevious);

        player.openInventory(inv);
    }

    public static void openAuctionPluginSettings(Player player, boolean local, boolean real){
        AuctionUIHolder holder = new AuctionUIHolder(local, real);
        Inventory inv = Bukkit.createInventory(holder, 27, "§cPlugin Einstellungen");

        long auctionCreateStatusVar = -1;
        long itemAddStatusVar = -1;
        if (player.isOp()){
            if (SettingsManager.isAuctionCreationAllowed()){
                auctionCreateStatusVar = 1;
            }else {
                auctionCreateStatusVar = 0;
            }

            if (SettingsManager.isAddItemAllowed()){
                itemAddStatusVar = 1;
            } else {
                itemAddStatusVar = 0;
            }
        }

        String yourPerms = "§7Nur operatoren können diese Einstellung ändern";
        if (player.isOp()){
            yourPerms = "§cHier klicken aktiviert/deaktiviert diese Funktion";
        }

        Material auctionMat = Material.RED_STAINED_GLASS_PANE;
        String auctionStatus = "§cMomentaner Status: Deaktiviert";
        if (SettingsManager.isAuctionCreationAllowed()){
            auctionStatus = "§aMomentanerStatus: Aktiviert";
            auctionMat = Material.LIME_STAINED_GLASS_PANE;
        }

        //Auktion erstellen (!!!!!!OP ONLY!!!!!!!)
        ItemStack auctionCreateStatus = new ItemStack(Material.BOOK);
        ItemMeta auctionCreateStatusMeta = auctionCreateStatus.getItemMeta();
        auctionCreateStatusMeta.setDisplayName("§eAuktion Erstellen");
        auctionCreateStatusMeta.setLore(Arrays.asList(
                auctionStatus,
                "§7Solange diese Funktion deaktiviert ist, können keine neuen Auktionen erstellt werden",
                yourPerms
        ));
        auctionCreateStatusMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "auction_create_status"
        );
        auctionCreateStatusMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                PersistentDataType.LONG, auctionCreateStatusVar
        );
        auctionCreateStatus.setItemMeta(auctionCreateStatusMeta);

        ItemStack auctionCreationGlass = new ItemStack(auctionMat);
        auctionCreationGlass.setItemMeta(auctionCreateStatusMeta);

            //glassPane



        Material itemsAddMat = Material.RED_STAINED_GLASS_PANE;
        String itemsAddStatus = "§cMomentaner Status: Deaktiviert";
        if (SettingsManager.isAddItemAllowed()){
            itemsAddStatus = "§aMomentanerStatus: Aktiviert";
            itemsAddMat = Material.LIME_STAINED_GLASS_PANE;
        }

        //Item Hinzufügen   (!!!!!!OP ONLY!!!!!!!!)
        ItemStack itemAddStatus = new ItemStack(Material.BOOK);
        ItemMeta itemAddStatusMeta = itemAddStatus.getItemMeta();
        itemAddStatusMeta.setDisplayName("§eItem in DB legen");
        itemAddStatusMeta.setLore(Arrays.asList(
                itemsAddStatus,
                "§7Solange diese Funktion deaktiviert ist, können keine neuen Items ins ItemStorage gelegt werden",
                yourPerms
        ));
        itemAddStatusMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "item_add_status"
        );
        itemAddStatusMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item_with_id"),
                PersistentDataType.LONG, itemAddStatusVar
        );
        itemAddStatus.setItemMeta(itemAddStatusMeta);

        ItemStack itemAddGlass = new ItemStack(itemsAddMat);
        itemAddGlass.setItemMeta(itemAddStatusMeta);
            //glassPane

        //UI Previous page
        ItemStack toPrevious = new ItemStack(Material.RED_WOOL);
        ItemMeta toPreviousMeta = toPrevious.getItemMeta();
        toPreviousMeta.setDisplayName("§cZur Hauptseite");
        toPreviousMeta.setLore(Arrays.asList(
                "§7Gehe zurück zur \"Admin UI\" Seite",
                "§e"
        ));
        toPreviousMeta.getPersistentDataContainer().set(
                new NamespacedKey(Hammaxcustomauction.getPlugin(Hammaxcustomauction.class), "ui_item"),
                PersistentDataType.STRING, "to_previous_button"
        );
        toPrevious.setItemMeta(toPreviousMeta);

        //11
        inv.setItem(11, auctionCreateStatus);
        inv.setItem(20, auctionCreationGlass);

        //15
        inv.setItem(15, itemAddStatus);
        inv.setItem(24, itemAddGlass);

        inv.setItem(8, toPrevious);

        player.openInventory(inv);
    }





}
