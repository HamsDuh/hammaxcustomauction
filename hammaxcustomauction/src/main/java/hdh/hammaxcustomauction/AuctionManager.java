package hdh.hammaxcustomauction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AuctionManager {

    private final Hammaxcustomauction plugin;

    private boolean running = false;
    private boolean waiting = false;
    private int cyclesSinceCheck = 0;

    BukkitTask task;

    public AuctionManager(Hammaxcustomauction plugin){
        this.plugin = plugin;
    }

    public void start(){
        if (running) return;
        running = true;

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::tick,
                20L * 60L,
                20L * 60L
        );
    }

    public void stop(){
        running = false;
        if(task != null){
            task.cancel();
        }
    }

    private void tick(){
        if (waiting) return;

        waiting = true;

        //Hier die Update funktion
        //System.out.println("AuctionManagerDebug");


        updateAuctions(() -> {
            waiting = false;
        });

        //spätere regelmäßige ausführung
        /*
        if (cyclesSinceCheck >= SettingsManager.getDBCheckTimer()){
            cyclesSinceCheck = 0;
            //System.out.println("DBcheckDebug");
        }else {
            cyclesSinceCheck ++;
        }
        */

    }

    public void updateAuctions(Runnable callback){

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

            Connection connection = SQLSetup.getConnection();

            try{
                connection.setAutoCommit(false);
                long now = System.currentTimeMillis();

                //offene auktion holen
                PreparedStatement auctionStmt = connection.prepareStatement(
                        "SELECT auctionID, itemID, startingOwner, deadline " +
                                "FROM Auctions WHERE closed = 0"
                );

                ResultSet auctionRS = auctionStmt.executeQuery();

                while(auctionRS.next()){
                    long auctionID = auctionRS.getLong("auctionID");
                    long itemID = auctionRS.getLong("itemID");
                    String startingOwner = auctionRS.getString("startingOwner");
                    long deadline = auctionRS.getLong("deadline");

                    //Höchste Bid holen
                    PreparedStatement bidStmt = connection.prepareStatement(
                            "SELECT bidID, bidOwner, bidAmount, bidDate " +
                                    "FROM Bids WHERE auctionID = ? AND bidCanceled = 0 " +
                                    "ORDER BY bidAmount DESC LIMIT 1"
                    );
                    bidStmt.setLong(1, auctionID);

                    ResultSet bidRS = bidStmt.executeQuery();

                    Long highestBidID = null;
                    String highestBidOwner = null;
                    long highestBidDate = 0;
                    double highestBidAmount = 0;

                    if (bidRS.next()){
                        highestBidID = bidRS.getLong("bidID");
                        highestBidOwner = bidRS.getString("bidOwner");
                        highestBidDate = bidRS.getLong("bidDate");
                        highestBidAmount = bidRS.getDouble("bidAmount");
                    }

                    boolean auctionShouldClose = false;
                    //ist die auktion abgelaufen?
                    if (highestBidID == null){
                        if (deadline <= now) auctionShouldClose = true;
                    }else {
                        long extendedTime = highestBidDate + (24L * 60L * 60L * 1000L);
                        if (extendedTime < deadline){
                            extendedTime = deadline;
                        }
                        if (extendedTime <= now){
                            auctionShouldClose = true;
                        }
                    }

                    //auktion schließen
                    if (auctionShouldClose){
                        PreparedStatement closeAuctionStmt = connection.prepareStatement(
                                "UPDATE Auctions SET closed = 1 WHERE auctionID = ?"
                        );
                        closeAuctionStmt.setLong(1, auctionID);
                        closeAuctionStmt.executeUpdate();

                        List<Boolean> bootsRecieved = new ArrayList<>();                                        //fml
                        List<String> recipiants = new ArrayList<>();                                             //early boot init
                        List<Double> linkedAmount = new ArrayList<>();

                        //Auktions auszahlung
                        if (highestBidID != null){
                            double auctionPayout = SettingsManager.alterAuctionPayout(highestBidAmount);

                            UUID sellerUUID = UUID.fromString(startingOwner);
                            OfflinePlayer seller = Bukkit.getOfflinePlayer(sellerUUID);
                            Player onlineSeller = Bukkit.getPlayer(sellerUUID);

                            //geldauszahlung an itembesitzer
                            if (seller != null){
                                if (Hammaxcustomauction.getInstance().getEconomyManager().reimburseSuccess(seller.getUniqueId(), auctionPayout)){
                                    if (onlineSeller != null && onlineSeller.isOnline()) {
                                        onlineSeller.sendMessage("§eDeine Auktion wurde abgeschlossen und du hast " + auctionPayout + " erhalten");
                                    }

                                        String paymentLogSql = "INSERT INTO Payments (playerUUID, auctionID, bidID, amount, type, paymentDate, paymentComplete) VALUES (?, ?, ?, ?, ?, ?, ?)";

                                        try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentLogSql)){
                                            paymentStmt.setString(1,sellerUUID.toString());
                                            paymentStmt.setLong(2, auctionID);
                                            paymentStmt.setLong(3, -1);
                                            paymentStmt.setDouble(4,auctionPayout);
                                            paymentStmt.setString(5,"Auction_Payout");
                                            paymentStmt.setLong(6, System.currentTimeMillis());
                                            paymentStmt.setBoolean(7,true);

                                            paymentStmt.executeUpdate();
                                        }
                                } else {
                                    String paymentLogSql = "INSERT INTO Payments (playerUUID, auctionID, bidID, amount, type, paymentComplete) VALUES (?, ?, ?, ?, ?, ?)";

                                    try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentLogSql)){
                                        paymentStmt.setString(1,sellerUUID.toString());
                                        paymentStmt.setLong(2, auctionID);
                                        paymentStmt.setLong(3, -1);
                                        paymentStmt.setDouble(4,auctionPayout);
                                        paymentStmt.setString(5,"Auction_Payout");
                                        paymentStmt.setBoolean(6,false);

                                        paymentStmt.executeUpdate();
                                    }
                                }
                                //SettingsManager.reimburse(seller, auctionPayout);
                            }

                            //Item übertragen
                            PreparedStatement updateItemOwner = connection.prepareStatement(
                                    "UPDATE ItemStorage SET itemOwner = ? WHERE itemID = ?"
                            );
                            updateItemOwner.setString(1, highestBidOwner);
                            updateItemOwner.setLong(2, itemID);
                            updateItemOwner.executeUpdate();

                            //log erstellen: Item wird von startbesitzer entfernt
                                String itemChangeLogSql = "INSERT INTO ItemChanges (playerUUID, itemID, type, date) VALUES (?, ?, ?, ?)";

                                try (PreparedStatement itemChangeStmt = SQLSetup.getConnection().prepareStatement(itemChangeLogSql)){
                                    itemChangeStmt.setString(1, startingOwner);
                                    itemChangeStmt.setLong(2, itemID);
                                    itemChangeStmt.setString(3, "Auction_Item_Remove");
                                    itemChangeStmt.setLong(4, System.currentTimeMillis());

                                    itemChangeStmt.executeUpdate();
                                }

                            //log erstellen: Item wird neuem Besitzer gegeben
                            String itemChangeLogSql2 = "INSERT INTO ItemChanges (playerUUID, itemID, type, date) VALUES (?, ?, ?, ?)";

                            try (PreparedStatement itemChangeStmt2 = SQLSetup.getConnection().prepareStatement(itemChangeLogSql2)){
                                itemChangeStmt2.setString(1, highestBidOwner);
                                itemChangeStmt2.setLong(2, itemID);
                                itemChangeStmt2.setString(3, "Auction_Item_Add");
                                itemChangeStmt2.setLong(4, System.currentTimeMillis());

                                itemChangeStmt2.executeUpdate();
                            }
                            if(highestBidAmount >= 100000 && auctionShouldClose){     //GlücksSchlapfen

                                String auctionItemSQL = "SELECT itemStack FROM ItemStorage WHERE itemID = ?";

                                PreparedStatement auctionItemSTMT = connection.prepareStatement(auctionItemSQL);
                                auctionItemSTMT.setLong(1,itemID);

                                ResultSet auctionItemRS = auctionItemSTMT.executeQuery();

                                //List<String> recipiants = new ArrayList<>();
                                //List<Double> linkedAmount = new ArrayList<>();

                                long rightNow = System.currentTimeMillis();
                                String dateTime = ConversionManager.formatTime(rightNow);

                                while(auctionItemRS.next()){

                                    ItemStack auctionItem = ConversionManager.itemFromBase64(auctionItemRS.getString("itemStack"));

                                    String otherBidsSQL = "SELECT b.bidOwner, b.bidAmount " +
                                            "FROM Bids b " +
                                            "WHERE b.auctionID = ? " +
                                            "AND b.bidID = ( " +
                                            "SELECT b.bidID FROM Bids b2 " +
                                            "WHERE b2.auctionID = b.auctionID AND b2.bidOwner = b.bidOwner " +
                                            "ORDER BY b2.bidAmount DESC, b2.bidDate DESC LIMIT 1 )";

                                    PreparedStatement otherBidsSTMT = connection.prepareStatement(otherBidsSQL);
                                    otherBidsSTMT.setLong(1,auctionID);

                                    ResultSet otherBidsRS = otherBidsSTMT.executeQuery();

                                    String refferencedItemName = "";
                                    if (auctionItem.hasItemMeta() && auctionItem.getItemMeta().hasDisplayName()){
                                        refferencedItemName = auctionItem.getItemMeta().getDisplayName();
                                    }else if (!SettingsManager.materialToGerman(auctionItem.getType()).equals("ERROR")){
                                        refferencedItemName = SettingsManager.materialToGerman(auctionItem.getType());
                                    } else{
                                        refferencedItemName = auctionItem.getType().toString();
                                    }

                                    if (auctionItem.getAmount() > 1){
                                        refferencedItemName = refferencedItemName + " x" + auctionItem.getAmount();
                                    }

                                    while(otherBidsRS.next()){

                                        String tempRecipiant = otherBidsRS.getString("bidOwner");
                                        double tempOtherBidAmount = otherBidsRS.getDouble("bidAmount");

                                        boolean keepLooking = true;
                                        for(int i = 0; i<recipiants.size()&&keepLooking;i++){
                                            if (recipiants.get(i).equals(tempRecipiant)){
                                                keepLooking = false;
                                                if (linkedAmount.get(i) < tempOtherBidAmount){
                                                    linkedAmount.set(i, tempOtherBidAmount);
                                                }
                                            }
                                        }
                                        if (keepLooking){
                                            recipiants.add(tempRecipiant);
                                            linkedAmount.add(tempOtherBidAmount);
                                            bootsRecieved.add(false);
                                        }

                                        //spieler filter ende

                                    }   //Haupt Itemausgabe \/

                                    for (int i = 0; i < recipiants.size();i++) {
                                        if (!bootsRecieved.get(i)){
                                        String recipiant = recipiants.get(i);
                                        double otherBidAmount = linkedAmount.get(i);

                                        String playername = Bukkit.getOfflinePlayer(UUID.fromString(recipiant)).getName();


                                        String bootsItemName = "§6Trostschlapfen";
                                        String bootsEndText = "§3Gewonnen hat " + "§5" + playername + "§3 leider nicht.";
                                        if (highestBidOwner.equals(recipiant)) {
                                            bootsItemName = "§6Glücksschlapfen";
                                            bootsEndText = "§3Damit hat " + "§5" + playername + "§3 auch gewonnen!";
                                        }

                                        ItemStack trostSchlapfen = new ItemStack(Material.LEATHER_BOOTS);
                                        ItemMeta trostSchlapfenMeta = trostSchlapfen.getItemMeta();
                                        trostSchlapfenMeta.setDisplayName(bootsItemName);
                                        trostSchlapfenMeta.setLore(Arrays.asList(
                                                "§5" + playername + "§3 hat am " + "§5" + dateTime,
                                                "§3auf das Item: " + refferencedItemName,
                                                "§3einen Wert von " + "§5" + otherBidAmount + "§3 geboten.",
                                                "§3-----------------------------------------------",
                                                bootsEndText
                                        ));
                                        trostSchlapfen.setItemMeta(trostSchlapfenMeta);

                                        String insertBoots = "INSERT INTO ItemStorage (itemOwner, itemStack, dateAdded, dateClaimed) VALUES (?, ?, ?, ?)";

                                        try (PreparedStatement bootsstmt = connection.prepareStatement(insertBoots)) {

                                            bootsstmt.setString(1, recipiant);
                                            bootsstmt.setString(2, ConversionManager.itemToBase64(trostSchlapfen));
                                            bootsstmt.setLong(3, System.currentTimeMillis());
                                            bootsstmt.setNull(4, Types.BIGINT);

                                            bootsstmt.executeUpdate();

                                            //log erstellen
                                            ResultSet generatedKey = bootsstmt.getGeneratedKeys();
                                            if (generatedKey.next()) {
                                                long storedItemID = generatedKey.getLong(1);

                                                String bootsitemChangeLogSql = "INSERT INTO ItemChanges (playerUUID, itemID, type, date) VALUES (?, ?, ?, ?)";

                                                try (PreparedStatement bootsitemChangeStmt = connection.prepareStatement(bootsitemChangeLogSql)) {
                                                    bootsitemChangeStmt.setString(1, recipiant);
                                                    bootsitemChangeStmt.setLong(2, storedItemID);
                                                    bootsitemChangeStmt.setString(3, "Auction_Item_Add");
                                                    bootsitemChangeStmt.setLong(4, System.currentTimeMillis());

                                                    bootsitemChangeStmt.executeUpdate();
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        bootsRecieved.set(i,true);
                                    }//ende der if boots ausgegeben nachfrage
                                    }   //itemausgabe hauptschleife

                                }
                            }       //glücksstiefel ende


                        }
                    }

                    //momentane Verlieren auszahlen
                    PreparedStatement refundStmt = connection.prepareStatement(
                            "SELECT bidID, bidOwner, bidAmount FROM Bids " +
                                    "WHERE auctionID = ? AND bidCanceled = 0"
                    );
                    refundStmt.setLong(1,auctionID);

                    ResultSet refundRS = refundStmt.executeQuery();

                    while (refundRS.next()){

                        //einzelne Bids
                        long bidID = refundRS.getLong("bidID");
                        String bidOwner = refundRS.getString("bidOwner");
                        double bidAmount = refundRS.getDouble("bidAmount");

                        UUID uuid = UUID.fromString(bidOwner);
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        Player onlinePlayer = Bukkit.getPlayer(uuid);

                        // höchste bid nicht
                        if (highestBidID != null && bidID != highestBidID) {

                            //bid refund
                            if (player != null) {
                                //SettingsManager.reimburse(player, bidAmount);
                                if (Hammaxcustomauction.getInstance().getEconomyManager().reimburseSuccess(player.getUniqueId(), bidAmount)) {
                                    if (onlinePlayer != null && onlinePlayer.isOnline()) {
                                        onlinePlayer.sendMessage("§eDu wurdest überboten und hast dein Geld zurückbekommen: " + bidAmount);
                                    }
                                    //System.out.println("Rückzahlung erfolgt");

                                    String paymentLogSql = "INSERT INTO Payments (playerUUID, auctionID, bidID, amount, type, paymentDate, paymentComplete) VALUES (?, ?, ?, ?, ?, ?, ?)";

                                    try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentLogSql)){
                                        paymentStmt.setString(1, bidOwner);
                                        paymentStmt.setLong(2, -1);
                                        paymentStmt.setLong(3, bidID);
                                        paymentStmt.setDouble(4, bidAmount);
                                        paymentStmt.setString(5,"Bid_Reimburse");
                                        paymentStmt.setLong(6, System.currentTimeMillis());
                                        paymentStmt.setBoolean(7,true);

                                        paymentStmt.executeUpdate();
                                    }
                                }
                                else {
                                    //player.sendMessage("§cFehler bei einer Auktions-rückzahlung :/");

                                    //System.out.println("Incomplete Payment noted for " + bidID);

                                    String paymentLogSql = "INSERT INTO Payments (playerUUID, auctionID, bidID, amount, type, paymentComplete) VALUES (?, ?, ?, ?, ?, ?)";

                                    try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentLogSql)){
                                        paymentStmt.setString(1, bidOwner);
                                        paymentStmt.setLong(2, -1);
                                        paymentStmt.setLong(3, bidID);
                                        paymentStmt.setDouble(4, bidAmount);
                                        paymentStmt.setString(5,"Bid_Reimburse");
                                        paymentStmt.setBoolean(6,false);

                                        paymentStmt.executeUpdate();
                                    }
                                }

                            }

                            PreparedStatement closeBid = connection.prepareStatement(
                                    "UPDATE Bids SET bidCanceled = 1 WHERE bidID = ?"
                            );
                            closeBid.setLong(1,bidID);
                            closeBid.executeUpdate();

                        }


                    }




                }

            connection.commit();
                connection.setAutoCommit(true);


            }catch (Exception e){
                try{
                    connection.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(),callback);

        });

    }



    public static void adminStopAuctionByID(Player cause, long givenAuctionID){

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

            Connection connection = SQLSetup.getConnection();

            try{
                connection.setAutoCommit(false);
                long now = System.currentTimeMillis();

                //offene auktion holen
                PreparedStatement auctionStmt = connection.prepareStatement(
                        "SELECT auctionID, itemID, startingOwner, deadline " +
                                "FROM Auctions WHERE closed = 0 AND auctionID = ?"
                );
                auctionStmt.setLong(1,givenAuctionID);

                ResultSet auctionRS = auctionStmt.executeQuery();

                while(auctionRS.next()){
                    long auctionID = auctionRS.getLong("auctionID");
                    long itemID = auctionRS.getLong("itemID");
                    String startingOwner = auctionRS.getString("startingOwner");
                    long deadline = auctionRS.getLong("deadline");

                    //Höchste Bid holen
                    PreparedStatement bidStmt = connection.prepareStatement(
                            "SELECT bidID, bidOwner, bidAmount, bidDate " +
                                    "FROM Bids WHERE auctionID = ? AND bidCanceled = 0 " +
                                    "ORDER BY bidAmount DESC LIMIT 1"
                    );
                    bidStmt.setLong(1, auctionID);

                    ResultSet bidRS = bidStmt.executeQuery();

                    Long highestBidID = null;
                    String highestBidOwner = null;
                    long highestBidDate = 0;
                    double highestBidAmount = 0;

                    if (bidRS.next()){
                        highestBidID = bidRS.getLong("bidID");
                        highestBidOwner = bidRS.getString("bidOwner");
                        highestBidDate = bidRS.getLong("bidDate");
                        highestBidAmount = bidRS.getDouble("bidAmount");
                    }

                    boolean auctionShouldClose = false;
                    //ist die auktion abgelaufen?
                    if (cause.hasPermission("ahteam") && cause.isOp()){
                        cause.sendMessage("§cAuktion " + givenAuctionID + " wird abgebrochen!");
                        auctionShouldClose = true;
                    }


                    //auktion schließen
                    if (auctionShouldClose){
                        PreparedStatement closeAuctionStmt = connection.prepareStatement(
                                "UPDATE Auctions SET closed = 1 WHERE auctionID = ?"
                        );
                        closeAuctionStmt.setLong(1, auctionID);
                        closeAuctionStmt.executeUpdate();

                    }

                    //List<Boolean> bootsRecieved = new ArrayList<>();                                        //fml
                    //List<String> recipiants = new ArrayList<>();                                             //early boot init
                    //List<Double> linkedAmount = new ArrayList<>();

                    //momentane Verlieren auszahlen
                    PreparedStatement refundStmt = connection.prepareStatement(
                            "SELECT bidID, bidOwner, bidAmount FROM Bids " +
                                    "WHERE auctionID = ? AND bidCanceled = 0"
                    );
                    refundStmt.setLong(1,auctionID);

                    ResultSet refundRS = refundStmt.executeQuery();

                    while (refundRS.next()){

                        //einzelne Bids
                        long bidID = refundRS.getLong("bidID");
                        String bidOwner = refundRS.getString("bidOwner");
                        double bidAmount = refundRS.getDouble("bidAmount");

                        UUID uuid = UUID.fromString(bidOwner);
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        Player onlinePlayer = Bukkit.getPlayer(uuid);

                        highestBidID = -1L;

                        // höchste bid nicht
                        if (highestBidID != null && bidID != highestBidID) {

                            //bid refund
                            if (player != null) {
                                //SettingsManager.reimburse(player, bidAmount);
                                if (Hammaxcustomauction.getInstance().getEconomyManager().reimburseSuccess(player.getUniqueId(), bidAmount)) {
                                    if (onlinePlayer != null && onlinePlayer.isOnline()) {
                                        onlinePlayer.sendMessage("§eDie Auktion wurde abgebrochen und hast dein Geld zurückbekommen: " + bidAmount);
                                    }
                                    //System.out.println("Rückzahlung erfolgt");

                                    String paymentLogSql = "INSERT INTO Payments (playerUUID, auctionID, bidID, amount, type, paymentDate, paymentComplete) VALUES (?, ?, ?, ?, ?, ?, ?)";

                                    try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentLogSql)){
                                        paymentStmt.setString(1, bidOwner);
                                        paymentStmt.setLong(2, -1);
                                        paymentStmt.setLong(3, bidID);
                                        paymentStmt.setDouble(4, bidAmount);
                                        paymentStmt.setString(5,"Bid_Reimburse");
                                        paymentStmt.setLong(6, System.currentTimeMillis());
                                        paymentStmt.setBoolean(7,true);

                                        paymentStmt.executeUpdate();
                                    }
                                }
                                else {
                                    //player.sendMessage("§cFehler bei einer Auktions-rückzahlung :/");

                                    //System.out.println("Incomplete Payment noted for " + bidID);

                                    String paymentLogSql = "INSERT INTO Payments (playerUUID, auctionID, bidID, amount, type, paymentComplete) VALUES (?, ?, ?, ?, ?, ?)";

                                    try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentLogSql)){
                                        paymentStmt.setString(1, bidOwner);
                                        paymentStmt.setLong(2, -1);
                                        paymentStmt.setLong(3, bidID);
                                        paymentStmt.setDouble(4, bidAmount);
                                        paymentStmt.setString(5,"Bid_Reimburse");
                                        paymentStmt.setBoolean(6,false);

                                        paymentStmt.executeUpdate();
                                    }
                                }
                            }

                            PreparedStatement closeBid = connection.prepareStatement(
                                    "UPDATE Bids SET bidCanceled = 1 WHERE bidID = ?"
                            );
                            closeBid.setLong(1,bidID);
                            closeBid.executeUpdate();

                        }


                    }




                }

                connection.commit();
                connection.setAutoCommit(true);


            }catch (Exception e){
                try{
                    connection.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }

        });

    }




}
