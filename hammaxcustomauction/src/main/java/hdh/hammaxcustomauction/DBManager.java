package hdh.hammaxcustomauction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static java.sql.Types.NULL;


public class DBManager {








    /*
    public static void testVoid() {
        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {
            //SQL code
        });
    }
    */

    //Storage
    //
    //Alle methoden die zum speichern und lesen der Item-Storage DB nötig sind
    //
    public void getStoredItemNumber(Player player, Consumer<Integer> callback){
        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() ->{
            String sql = "SELECT COUNT(*) FROM ItemStorage WHERE itemOwner = ? AND dateClaimed IS NULL";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){

                stmt.setString(1,player.getUniqueId().toString());

                ResultSet rs = stmt.executeQuery();

                int count = 0;

                if (rs.next()){
                    count = rs.getInt(1);
                }

                int finalCount = count;
                Bukkit.getScheduler().runTask(
                        Hammaxcustomauction.getInstance(),
                        () -> callback.accept(finalCount)
                );

            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    //Bedingungen zum hinzufügen zu storage

    public boolean storageAddRequirements(Player player, ItemStack itemToAdd, boolean local, boolean save){
        boolean erg = true;
        boolean conditionsMet = true; // out of 3      ->erweiterbar


        if(!local || !save){
            conditionsMet = false;
        }

        if(!player.getInventory().contains(itemToAdd)){
            conditionsMet = false;
        }
        if(!SettingsManager.allowedItemForStorage(itemToAdd)){
            conditionsMet = false;
        }

        return erg;
    }


    public void addItemToStorage(Player player, ItemStack item, boolean local, boolean save){
        if(storageAddRequirements(player, item, local,save)) {
            Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {
                String sql = "INSERT INTO ItemStorage (itemOwner, itemStack, dateAdded, dateClaimed) VALUES (?, ?, ?, ?)";

                try (PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)) {

                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.setString(2, ConversionManager.itemToBase64(item));
                    stmt.setLong(3, System.currentTimeMillis());
                    stmt.setNull(4, Types.BIGINT);

                    stmt.executeUpdate();
                    player.sendMessage("§aItemAdded");

                    //log erstellen
                    ResultSet generatedKey = stmt.getGeneratedKeys();
                    if (generatedKey.next()){
                        long storedItemID = generatedKey.getLong(1);

                        String itemChangeLogSql = "INSERT INTO ItemChanges (playerUUID, itemID, type, date) VALUES (?, ?, ?, ?)";

                        try (PreparedStatement itemChangeStmt = SQLSetup.getConnection().prepareStatement(itemChangeLogSql)){
                            itemChangeStmt.setString(1, player.getUniqueId().toString());
                            itemChangeStmt.setLong(2, storedItemID);
                            itemChangeStmt.setString(3, "Item_Add");
                            itemChangeStmt.setLong(4, System.currentTimeMillis());

                            itemChangeStmt.executeUpdate();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
        /*
        ItemStorage
            itemID INTEGER
            itemOwner VARCHAR(36)
            itemStack TEXT
            dateAdded BIGINT
            dateClaimed BIGINT
        */

    }

    public void /*List<StorageItem>*/ getItemsFromStorage(String uuid, Consumer<List<StorageItem>> callback){

        List<StorageItem> items = new ArrayList<>();

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {

            String sql = "SELECT i.itemID, i.itemOwner, i.itemStack, i.dateAdded FROM ItemStorage i " +
                    "WHERE i.itemOwner = ? AND i.dateClaimed IS NULL " +
                    "AND NOT EXISTS (" +
                    "SELECT 1 FROM Auctions a " +
                    "WHERE a.itemID = i.itemID " +
                    "AND a.closed = 0" +
                    ")";


            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setString(1, uuid);

                ResultSet rs = stmt.executeQuery();

                while (rs.next()){
                    long itemID = rs.getLong("itemID");

                    String base64Item = rs.getString("itemStack");
                    ItemStack item = ConversionManager.itemFromBase64(base64Item);
                    long addedWhen = rs.getLong("dateAdded");
                    StorageItem disitem = new StorageItem(
                            itemID,
                            uuid,
                            item,
                            addedWhen,
                            NULL
                    );
                    items.add(disitem);
                    //player.sendMessage("§7item listed");
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(),() -> callback.accept(items));

        });

    }

    public void getItemFromID(long givenID, Consumer<StorageItem> callback){

        StorageItem[] storedItem = new StorageItem[1];

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() ->{

            String sql = "SELECT itemID, itemOwner, itemStack, dateAdded, dateClaimed FROM ItemStorage WHERE itemID = ?";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setLong(1, givenID);

                ResultSet rs = stmt.executeQuery();{
                    while(rs.next()){
                        long itemID = rs.getLong("itemID");
                        String owner = rs.getString("itemOwner");
                        String base64item = rs.getString("itemStack");

                        ItemStack item = null;
                        try{
                            item = ConversionManager.itemFromBase64(base64item);
                        }catch(Exception notFound){
                            notFound.printStackTrace();
                            item = new ItemStack(Material.AIR);
                        }
                        long dateAdded = rs.getLong("dateAdded");
                        long dateClaimed = rs.getLong("dateClaimed");
                        if(rs.wasNull()){
                            dateClaimed = NULL;
                        }

                        storedItem[0] = new StorageItem(
                                itemID,
                                owner,
                                item,
                                dateAdded,
                                dateClaimed
                        );

                    }

                };

            }catch(Exception e){
                e.printStackTrace();
            }

            StorageItem result = storedItem[0];
            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> callback.accept(result));

        });

    }


    public void claimStorageItem(Player player, long itemID, Consumer<Boolean> callback){
    Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{
            String sql = "UPDATE ItemStorage SET dateClaimed = ? WHERE itemID = ? AND dateClaimed IS NULL";
            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setLong(1,System.currentTimeMillis());
                stmt.setLong(2,itemID);

                boolean success = true;

                int rows = stmt.executeUpdate();
                if (rows == 0){
                    Bukkit.getLogger().warning("Kein storageItem gefunden");
                    success = false;
                }else{

                        String itemChangeLogSql = "INSERT INTO ItemChanges (playerUUID, itemID, type, date) VALUES (?, ?, ?, ?)";

                        try (PreparedStatement itemChangeStmt = SQLSetup.getConnection().prepareStatement(itemChangeLogSql)){
                            itemChangeStmt.setString(1, player.getUniqueId().toString());
                            itemChangeStmt.setLong(2, itemID);
                            itemChangeStmt.setString(3, "Item_Claim");
                            itemChangeStmt.setLong(4, System.currentTimeMillis());

                            itemChangeStmt.executeUpdate();
                        }

                }

                boolean finalSuccess = success;
                Bukkit.getScheduler().runTask(
                        Hammaxcustomauction.getInstance(),
                        () -> callback.accept(finalSuccess)
                );

        }catch(Exception e){
                e.printStackTrace();
        }
    });
    }


    public void getActiveAuctionNumber(Player player, Consumer<Integer> callback){
        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() ->{
            String sql = "SELECT COUNT(*) FROM Auctions a JOIN ItemStorage i ON a.itemID = i.itemID WHERE i.itemOwner = ? AND i.dateClaimed IS NULL AND a.closed = 0";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){

                stmt.setString(1,player.getUniqueId().toString());

                ResultSet rs = stmt.executeQuery();

                int count = 0;

                if (rs.next()){
                    count = rs.getInt(1);
                }

                int finalCount = count;
                Bukkit.getScheduler().runTask(
                        Hammaxcustomauction.getInstance(),
                        () -> callback.accept(finalCount)
                );

            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }


    public void addItemToAuction(Player player, long itemID, double startingPrice, double bidIncrease, long givenTime, boolean local, boolean save){
        if(save && Hammaxcustomauction.getInstance().getEconomyManager().withdrawlSuccess(player.getUniqueId(), SettingsManager.getDurationPrice(givenTime))) {
            long deadline = System.currentTimeMillis() + givenTime;
            //SettingsManager.pay(player,SettingsManager.getDurationPrice(givenTime));
            Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {
                String sql = "INSERT INTO Auctions (itemID, startingOwner, startingPrice, bidIncrease, deadline, closed) VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)) {

                    stmt.setLong(1, itemID);
                    stmt.setString(2, player.getUniqueId().toString());
                    stmt.setDouble(3, startingPrice);
                    stmt.setDouble(4, bidIncrease);
                    stmt.setLong(5, deadline);
                    stmt.setBoolean(6, false);


                    stmt.executeUpdate();

                    ResultSet generatedKey = stmt.getGeneratedKeys();
                    if (generatedKey.next()){
                        long createdAuctionID = generatedKey.getLong(1);

                        double paymentLogAmount = SettingsManager.getDurationPrice(givenTime);

                        String paymentLogSql = "INSERT INTO Payments (playerUUID, auctionID, bidID, amount, type, paymentDate, paymentComplete) VALUES (?, ?, ?, ?, ?, ?, ?)";

                        try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentLogSql)){
                            paymentStmt.setString(1,player.getUniqueId().toString());
                            paymentStmt.setLong(2, createdAuctionID);
                            paymentStmt.setLong(3,-1);
                            paymentStmt.setDouble(4,paymentLogAmount);
                            paymentStmt.setString(5,"Auction_Create");
                            paymentStmt.setLong(6, System.currentTimeMillis());
                            paymentStmt.setBoolean(7,true);

                            paymentStmt.executeUpdate();
                        }


                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        } else{
            player.sendMessage("§cDu hast nicht genug Geld um die Auktionsgebüren(" + SettingsManager.getDurationPrice(givenTime) +  ") zu bezahlen");
        }

        /*
        Auctions
            auctionID INTEGER primary
            itemID INTEGER NOT NULL
            startingPrice REAL NOT LOLL
            bidIncrease REAL NOT NULL
            deadline BIGINT NOT NULL
            closed BOOLEAN DEFAULT 0
        */

    }


    public void getAuctionFromID(long givenID, Consumer<Auction> callback){

        Auction[] auction = new Auction[1];

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() ->{

            //String sql = "SELECT auctionID, itemID, startingPrice, bidIncrease, deadline, closed FROM Auctions WHERE auctionID = ?";

            String sql = "SELECT a.auctionID, a.itemID, a.startingOwner, a.startingPrice, a.bidIncrease, a.deadline, a.closed, " +
                    "i.itemOwner, i.itemStack, i.dateAdded, i.dateClaimed, " +
                    "b.bidID, b.auctionID, b.bidOwner, b.bidAmount, b.bidDate, b.bidCanceled " +
                    "FROM Auctions a JOIN ItemStorage i ON a.itemID = i.itemID " +
                    "LEFT JOIN Bids b ON b.bidID = ( " +
                    "SELECT bidID FROM Bids WHERE auctionID = a.auctionID AND bidCanceled = 0 " +
                    "ORDER BY bidAmount DESC LIMIT 1 ) " +
                    "WHERE a.auctionID = ? ";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setLong(1, givenID);

                ResultSet rs = stmt.executeQuery();{
                    while(rs.next()){
                        long auctionID = rs.getLong("auctionID");
                        long itemID = rs.getLong("itemID");
                        String startingOwner = rs.getString("startingOwner");
                        double startPrice = rs.getDouble("startingPrice");
                        double priceIncrease = rs.getDouble("bidIncrease");
                        long deadline = rs.getLong("deadline");
                        boolean closed = rs.getBoolean("closed");

                        auction[0] = new Auction(
                                auctionID,
                                itemID,
                                startingOwner,
                                startPrice,
                                priceIncrease,
                                deadline,
                                closed
                        );

                        //StorageItemWerte
                        String owner = rs.getString("itemOwner");
                        ItemStack item = ConversionManager.itemFromBase64(rs.getString("itemStack"));
                        long dateAdded = rs.getLong("dateAdded");
                        long dateClaimed = rs.getLong("dateClaimed");
                        if(rs.wasNull()){
                            dateClaimed = NULL;
                        }

                        StorageItem storageItem = new StorageItem(
                                itemID,
                                owner,
                                item,
                                dateAdded,
                                dateClaimed
                        );

                        auction[0].setStorageItem(storageItem);

                        long bidID = rs.getLong("bidID");
                        //BidWerte
                        if(!rs.wasNull()){
                            Bid bid = new Bid(
                                    bidID,
                                    rs.getLong("auctionID"),
                                    rs.getString("bidOwner"),
                                    rs.getDouble("bidAmount"),
                                    rs.getLong("bidDate"),
                                    rs.getInt("bidCanceled")
                            );

                            auction[0].setBid(bid);

                        }
                    }

                };

            }catch(Exception e){
                e.printStackTrace();
            }

            Auction result = auction[0];
            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> callback.accept(result));

        });

    }


    public void getAuctionsFromPlayer(String uuid, Consumer<List<Auction>> callback){

        List<Auction> auctions = new ArrayList<>();

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {

            String sql = "SELECT a.auctionID, a.itemID, a.startingOwner, a.startingPrice, a.bidIncrease, a.deadline, a.closed, " +
                    "i.itemOwner, i.itemStack, i.dateAdded, i.dateClaimed, " +
                    "b.bidID, b.auctionID, b.bidOwner, b.bidAmount, b.bidDate, b.bidCanceled " +
                    "FROM Auctions a JOIN ItemStorage i ON a.itemID = i.itemID " +
                    "LEFT JOIN Bids b ON b.bidID = (" +
                    "SELECT bidID FROM Bids WHERE auctionID = a.auctionID AND bidCanceled = 0 " +
                    "ORDER BY bidAmount DESC LIMIT 1) " +
                    "WHERE a.closed = 0 AND i.dateClaimed IS NULL AND i.itemOwner = ?";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setString(1, uuid);

                ResultSet rs = stmt.executeQuery();

                while(rs.next()){


                    long auctionID = rs.getLong("auctionID");
                    long itemID = rs.getLong("itemID");
                    String startingOwner = rs.getString("startingOwner");
                    double startPrice = rs.getDouble("startingPrice");
                    double priceIncrease = rs.getDouble("bidIncrease");
                    long deadline = rs.getLong("deadline");
                    boolean closed = rs.getBoolean("closed");

                    Auction auction = new Auction(
                            auctionID,
                            itemID,
                            startingOwner,
                            startPrice,
                            priceIncrease,
                            deadline,
                            closed
                    );

                    //StorageItemWerte
                    String owner = rs.getString("itemOwner");
                    ItemStack item = ConversionManager.itemFromBase64(rs.getString("itemStack"));
                    long dateAdded = rs.getLong("dateAdded");
                    long dateClaimed = rs.getLong("dateClaimed");
                    if(rs.wasNull()){
                        dateClaimed = NULL;
                    }

                    StorageItem storageItem = new StorageItem(
                            itemID,
                            owner,
                            item,
                            dateAdded,
                            dateClaimed
                    );

                    auction.setStorageItem(storageItem);

                    long bidID = rs.getLong("bidID");
                    //BidWerte
                    if(!rs.wasNull()){
                        Bid bid = new Bid(
                                bidID,
                                rs.getLong("auctionID"),
                                rs.getString("bidOwner"),
                                rs.getDouble("bidAmount"),
                                rs.getLong("bidDate"),
                                rs.getInt("bidCanceled")
                        );

                        auction.setBid(bid);
                    }


                    auctions.add(auction);

                }

            }catch (Exception e){
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> callback.accept(auctions));

        });

    }


    public void getOldAuctionsFromPlayer(String uuid, Consumer<List<Auction>> callback){

        List<Auction> auctions = new ArrayList<>();

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {

            String sql = "SELECT a.auctionID, a.itemID, a.startingOwner, a.startingPrice, a.bidIncrease, a.deadline, a.closed, " +
                    "i.itemOwner, i.itemStack, i.dateAdded, i.dateClaimed, " +
                    "b.bidID, b.auctionID, b.bidOwner, b.bidAmount, b.bidDate, b.bidCanceled " +
                    "FROM Auctions a JOIN ItemStorage i ON a.itemID = i.itemID " +
                    "LEFT JOIN Bids b ON b.bidID = (" +
                    "SELECT bidID FROM Bids WHERE auctionID = a.auctionID AND bidCanceled = 0 " +
                    "ORDER BY bidAmount DESC LIMIT 1) " +
                    "WHERE a.closed = 1 AND i.dateClaimed IS NULL AND i.itemOwner = ?";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setString(1, uuid);

                ResultSet rs = stmt.executeQuery();

                while(rs.next()){


                    long auctionID = rs.getLong("auctionID");
                    long itemID = rs.getLong("itemID");
                    String startingOwner = rs.getString("startingOwner");
                    double startPrice = rs.getDouble("startingPrice");
                    double priceIncrease = rs.getDouble("bidIncrease");
                    long deadline = rs.getLong("deadline");
                    boolean closed = rs.getBoolean("closed");

                    Auction auction = new Auction(
                            auctionID,
                            itemID,
                            startingOwner,
                            startPrice,
                            priceIncrease,
                            deadline,
                            closed
                    );

                    //StorageItemWerte
                    String owner = rs.getString("itemOwner");
                    ItemStack item = ConversionManager.itemFromBase64(rs.getString("itemStack"));
                    long dateAdded = rs.getLong("dateAdded");
                    long dateClaimed = rs.getLong("dateClaimed");
                    if(rs.wasNull()){
                        dateClaimed = NULL;
                    }

                    StorageItem storageItem = new StorageItem(
                            itemID,
                            owner,
                            item,
                            dateAdded,
                            dateClaimed
                    );

                    auction.setStorageItem(storageItem);

                    long bidID = rs.getLong("bidID");
                    //BidWerte
                    if(!rs.wasNull()){
                        Bid bid = new Bid(
                                bidID,
                                rs.getLong("auctionID"),
                                rs.getString("bidOwner"),
                                rs.getDouble("bidAmount"),
                                rs.getLong("bidDate"),
                                rs.getInt("bidCanceled")
                        );

                        auction.setBid(bid);
                    }


                    auctions.add(auction);

                }

            }catch (Exception e){
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> callback.accept(auctions));

        });

    }




public void attemptAuctionCancelByPlayer(Player player, long auctionID, boolean save){
    if(save) {
        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{
            String sql = "UPDATE Auctions SET closed = 1 WHERE auctionID = ? AND closed = 0 " +
                    "AND NOT EXISTS (" +
                    "SELECT 1 FROM Bids WHERE Bids.auctionID = ?)";
            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setLong(1,auctionID);
                stmt.setLong(2,auctionID);

                int rows = stmt.executeUpdate();
                if (rows == 0){
                    player.sendMessage("§cDie Auktion " + auctionID +" konnte nicht geschlossen werden");
                } else {
                    player.sendMessage("§aDie Auktion " + auctionID +" wurde erfolgreich geschlossen");
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }
}



    public void getPublicAuctionsForPlayer(String uuid, Consumer<List<Auction>> callback){

        List<Auction> auctions = new ArrayList<>();

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {

            String sql = "SELECT a.auctionID, a.itemID, a.startingOwner, a.startingPrice, a.bidIncrease, a.deadline, a.closed, " +
                    "i.itemOwner, i.itemStack, i.dateAdded, i.dateClaimed, " +
                    "b.bidID, b.auctionID, b.bidOwner, b.bidAmount, b.bidDate, b.bidCanceled " +
                    "FROM Auctions a JOIN ItemStorage i ON a.itemID = i.itemID " +
                    "LEFT JOIN Bids b ON b.bidID = (" +
                    "SELECT bidID FROM Bids WHERE auctionID = a.auctionID AND bidCanceled = 0 " +
                    "ORDER BY bidAmount DESC LIMIT 1) " +
                    "WHERE a.closed = 0 AND i.dateClaimed IS NULL AND i.itemOwner != ?";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setString(1, uuid);

                ResultSet rs = stmt.executeQuery();

                while(rs.next()){


                    long auctionID = rs.getLong("auctionID");
                    long itemID = rs.getLong("itemID");
                    String startingOwner = rs.getString("startingOwner");
                    double startPrice = rs.getDouble("startingPrice");
                    double priceIncrease = rs.getDouble("bidIncrease");
                    long deadline = rs.getLong("deadline");
                    boolean closed = rs.getBoolean("closed");

                    Auction auction = new Auction(
                            auctionID,
                            itemID,
                            startingOwner,
                            startPrice,
                            priceIncrease,
                            deadline,
                            closed
                    );

                    //StorageItemWerte
                    String owner = rs.getString("itemOwner");
                    ItemStack item = ConversionManager.itemFromBase64(rs.getString("itemStack"));
                    long dateAdded = rs.getLong("dateAdded");
                    long dateClaimed = rs.getLong("dateClaimed");
                    if(rs.wasNull()){
                        dateClaimed = NULL;
                    }

                    StorageItem storageItem = new StorageItem(
                            itemID,
                            owner,
                            item,
                            dateAdded,
                            dateClaimed
                    );

                    auction.setStorageItem(storageItem);

                    long bidID = rs.getLong("bidID");
                    //BidWerte
                    if(!rs.wasNull()){
                        Bid bid = new Bid(
                                bidID,
                                rs.getLong("auctionID"),
                                rs.getString("bidOwner"),
                                rs.getDouble("bidAmount"),
                                rs.getLong("bidDate"),
                                rs.getInt("bidCanceled")
                        );

                        auction.setBid(bid);
                    }


                    auctions.add(auction);

                }

            }catch (Exception e){
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> callback.accept(auctions));

        });

    }




    public void attemptBidCreation(Player player, long auctionID, double bidPrice, boolean local, boolean save){
        if (save && /*SettingsManager.checkMoney(player, bidPrice) &&*/ Hammaxcustomauction.getInstance().getEconomyManager().hasEnoughMoney(player.getUniqueId(), bidPrice)){



            Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

                Connection connection = SQLSetup.getConnection();

                try{
                    connection.setAutoCommit(false);

                    //Auktion aktuell?
                    PreparedStatement auctionStmt  = connection.prepareStatement(
                        "SELECT closed FROM Auctions WHERE auctionID = ?"
                    );
                    auctionStmt.setLong(1, auctionID);
                    ResultSet auctionRS = auctionStmt.executeQuery();

                    if (!auctionRS.next() || auctionRS.getInt("closed") != 0){
                        player.sendMessage("§cDie Auktion konnte nicht gefunden werden oder ist schon geschlossen :/");
                        return;
                    }

                    //Noch aktueller Preis, selbst nicht überbieten?
                    PreparedStatement highestBidStmt = connection.prepareStatement(
                            "SELECT bidOwner, bidAmount FROM Bids " +
                                    "WHERE auctionID = ? AND bidCanceled = 0 " +
                                    "ORDER BY bidAmount DESC LIMIT 1"
                    );
                    highestBidStmt.setLong(1, auctionID);
                    ResultSet bidRS = highestBidStmt.executeQuery();

                    String highestBidOwner = null;
                    double highestBid = 0;

                    if (bidRS.next()){
                        highestBidOwner = bidRS.getString("bidOwner");
                        highestBid = bidRS.getDouble("bidAmount");
                    }

                    if (highestBidOwner != null && highestBidOwner.equals(player.getUniqueId().toString())){
                        player.sendMessage("§eDu bist bereits der Höchstbietende bei dieser Auktion");
                        return;
                    }
                    if (bidPrice <= highestBid){
                        player.sendMessage("§cDas Gebot war zu klein, vielleicht hat da jemand vor dir geboten :/");
                        return;
                    }


                    if (Hammaxcustomauction.getInstance().getEconomyManager().withdrawlSuccess(player.getUniqueId(), bidPrice)){
                        PreparedStatement gebotErstellen = connection.prepareStatement(
                                "INSERT INTO Bids (auctionID, bidOwner, bidAmount, bidDate) VALUES (?, ?, ? ,? )"
                        );
                        gebotErstellen.setLong(1, auctionID);
                        gebotErstellen.setString(2, player.getUniqueId().toString());
                        gebotErstellen.setDouble(3, bidPrice);
                        gebotErstellen.setLong(4, System.currentTimeMillis());

                        //SettingsManager.pay(player, bidPrice);
                        gebotErstellen.executeUpdate();

                        ResultSet generatedKey = gebotErstellen.getGeneratedKeys();
                        if (generatedKey.next()){
                            long createdBidID = generatedKey.getLong(1);

                            double paymentLogAmount = bidPrice;

                            String paymentLogSql = "INSERT INTO Payments (playerUUID, auctionID, bidID, amount, type, paymentDate, paymentComplete) VALUES (?, ?, ?, ?, ?, ?, ?)";

                            try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentLogSql)){
                                paymentStmt.setString(1,player.getUniqueId().toString());
                                paymentStmt.setLong(2, -1);
                                paymentStmt.setLong(3,createdBidID);
                                paymentStmt.setDouble(4,paymentLogAmount);
                                paymentStmt.setString(5,"Bid_Create");
                                paymentStmt.setLong(6, System.currentTimeMillis());
                                paymentStmt.setBoolean(7,true);

                                paymentStmt.executeUpdate();
                            }


                        }

                        player.sendMessage("§aGebot erfolgreich erstellt");
                    }else{
                        player.sendMessage("§cBei der Zahlung ist ein Fehler aufgetreten");
                    }

                    connection.commit();
                    connection.setAutoCommit(true);

                }catch (SQLException e){
                    e.printStackTrace();
                    player.sendMessage("§cFehler bei der Erstellung deines Gebotes :/");
                }






            });


        } else if (!Hammaxcustomauction.getInstance().getEconomyManager().hasEnoughMoney(player.getUniqueId(),bidPrice)) {
            player.sendMessage("§cdu hast nicht genügend Geld");
        }

    }


    public void addBidToAuction(Player player, long itemID, double startingPrice, double bidIncrease, long givenTime, boolean local, boolean save){






        if(save) {
            long deadline = System.currentTimeMillis() + givenTime;
            Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {
                String sql = "INSERT INTO Bids (itemID, startingOwner, startingPrice, bidIncrease, deadline, closed) VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)) {

                    stmt.setLong(1, itemID);
                    stmt.setString(2, player.getUniqueId().toString());
                    stmt.setDouble(3, startingPrice);
                    stmt.setDouble(4, bidIncrease);
                    stmt.setLong(5, deadline);
                    stmt.setBoolean(6, false);


                    stmt.executeUpdate();
                    player.sendMessage("§aItemAdded to auction");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }

        /*
        Bids
            bidID INTEGER
            auctionID INTEGER
            bidOwner VARCHAR(36)
            bidAmount REAL
            bidDate BIGINT
            bidCanceled BOOLEAN
        */

    }


    public void getPlayerBidAuctions(String givenUUID, Consumer<List<Auction>> callback){

        List<Auction> auctions = new ArrayList<>();

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {

            String sql = "SELECT a.auctionID, a.itemID, a.startingOwner, a.startingPrice, a.bidIncrease, a.deadline, a.closed, " +
                    "i.itemOwner, i.itemStack, i.dateAdded, i.dateClaimed, " +
                    "b.bidID, b.auctionID, b.bidOwner, b.bidAmount, b.bidDate, b.bidCanceled " +
                    "FROM Auctions a JOIN ItemStorage i ON a.itemID = i.itemID " +
                    "LEFT JOIN Bids b ON b.bidID = (" +
                    "SELECT bidID FROM Bids WHERE auctionID = a.auctionID AND bidCanceled = 0 " +
                    "ORDER BY bidAmount DESC LIMIT 1) " +
                    "WHERE a.closed = 0 AND i.dateClaimed IS NULL " +
                    "AND EXISTS ( " +
                    "SELECT 1 FROM Bids pb WHERE pb.auctionID = a.auctionID " +
                    "AND pb.bidOwner = ? AND pb.bidCanceled != 2 )";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setString(1, givenUUID);

                ResultSet rs = stmt.executeQuery();

                while(rs.next()){


                    long auctionID = rs.getLong("auctionID");
                    long itemID = rs.getLong("itemID");
                    String startingOwner = rs.getString("startingOwner");
                    double startPrice = rs.getDouble("startingPrice");
                    double priceIncrease = rs.getDouble("bidIncrease");
                    long deadline = rs.getLong("deadline");
                    boolean closed = rs.getBoolean("closed");

                    Auction auction = new Auction(
                            auctionID,
                            itemID,
                            startingOwner,
                            startPrice,
                            priceIncrease,
                            deadline,
                            closed
                    );

                    //StorageItemWerte
                    String owner = rs.getString("itemOwner");
                    ItemStack item = ConversionManager.itemFromBase64(rs.getString("itemStack"));
                    long dateAdded = rs.getLong("dateAdded");
                    long dateClaimed = rs.getLong("dateClaimed");
                    if(rs.wasNull()){
                        dateClaimed = NULL;
                    }

                    StorageItem storageItem = new StorageItem(
                            itemID,
                            owner,
                            item,
                            dateAdded,
                            dateClaimed
                    );

                    auction.setStorageItem(storageItem);

                    long bidID = rs.getLong("bidID");
                    //BidWerte
                    if(!rs.wasNull()){
                        Bid bid = new Bid(
                                bidID,
                                rs.getLong("auctionID"),
                                rs.getString("bidOwner"),
                                rs.getDouble("bidAmount"),
                                rs.getLong("bidDate"),
                                rs.getInt("bidCanceled")
                        );

                        auction.setBid(bid);
                    }


                    auctions.add(auction);

                }

            }catch (Exception e){
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> callback.accept(auctions));

        });

    }

    public void getIgnoredAndOldBidsForPlayer(String givenUUID, boolean old, Consumer<List<Auction>> callback){

        List<Auction> auctions = new ArrayList<>();

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(() -> {


            String sql = "SELECT a.auctionID, a.itemID, a.startingOwner, a.startingPrice, a.bidIncrease, a.deadline, a.closed, " +
                    "i.itemOwner, i.itemStack, i.dateAdded, i.dateClaimed, " +
                    "b.bidID, b.auctionID, b.bidOwner, b.bidAmount, b.bidDate, b.bidCanceled " +
                    "FROM Auctions a JOIN ItemStorage i ON a.itemID = i.itemID " +
                    "LEFT JOIN Bids b ON b.bidID = (" +
                    "SELECT bidID FROM Bids WHERE auctionID = a.auctionID AND b.bidOwner = ? " +
                    "ORDER BY bidAmount DESC LIMIT 1) " +
                    "WHERE a.closed = 1 AND b.bidOwner = ? " //+
                    /*"AND EXISTS ( " +
                    "SELECT 1 FROM Bids pb WHERE pb.auctionID = a.auctionID " +
                    "AND pb.bidOwner = ? )"*/;

            if (!old){
                sql = "SELECT a.auctionID, a.itemID, a.startingOwner, a.startingPrice, a.bidIncrease, a.deadline, a.closed, " +
                        "i.itemOwner, i.itemStack, i.dateAdded, i.dateClaimed, " +
                        "b.bidID, b.auctionID, b.bidOwner, b.bidAmount, b.bidDate, b.bidCanceled " +
                        "FROM Auctions a JOIN ItemStorage i ON a.itemID = i.itemID " +
                        "LEFT JOIN Bids b ON b.bidID = (" +
                        "SELECT bidID FROM Bids WHERE auctionID = a.auctionID AND b.bidOwner = ? " +
                        "ORDER BY bidAmount DESC LIMIT 1) " +
                        "WHERE a.closed = 0 AND b.bidOwner = ?" //+
                        /*"AND EXISTS ( " +
                        "SELECT 1 FROM Bids pb WHERE pb.auctionID = a.auctionID " +
                        "AND pb.bidOwner = ? )"*/;
            }

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setString(1, givenUUID);
                stmt.setString(2, givenUUID);

                ResultSet rs = stmt.executeQuery();

                while(rs.next()){


                    long auctionID = rs.getLong("auctionID");
                    long itemID = rs.getLong("itemID");
                    String startingOwner = rs.getString("startingOwner");
                    double startPrice = rs.getDouble("startingPrice");
                    double priceIncrease = rs.getDouble("bidIncrease");
                    long deadline = rs.getLong("deadline");
                    boolean closed = rs.getBoolean("closed");

                    Auction auction = new Auction(
                            auctionID,
                            itemID,
                            startingOwner,
                            startPrice,
                            priceIncrease,
                            deadline,
                            closed
                    );

                    //StorageItemWerte
                    String owner = rs.getString("itemOwner");
                    ItemStack item = ConversionManager.itemFromBase64(rs.getString("itemStack"));
                    long dateAdded = rs.getLong("dateAdded");
                    long dateClaimed = rs.getLong("dateClaimed");
                    if(rs.wasNull()){
                        dateClaimed = NULL;
                    }

                    StorageItem storageItem = new StorageItem(
                            itemID,
                            owner,
                            item,
                            dateAdded,
                            dateClaimed
                    );

                    auction.setStorageItem(storageItem);

                    long bidID = rs.getLong("bidID");
                    //BidWerte
                    if(!rs.wasNull()){
                        Bid bid = new Bid(
                                bidID,
                                rs.getLong("auctionID"),
                                rs.getString("bidOwner"),
                                rs.getDouble("bidAmount"),
                                rs.getLong("bidDate"),
                                rs.getInt("bidCanceled")
                        );

                        auction.setBid(bid);
                    }


                    auctions.add(auction);

                }

            }catch (Exception e){
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> callback.accept(auctions));

        });
    }



    public void getPlayerIgnorableBid(Player player, long auctionID, Consumer<Bid> callback){

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

            Bid resultBid = new Bid();

            String sql =
                    "SELECT bidID, auctionID, bidOwner, bidAmount, bidDate, bidCanceled " +
                            "FROM Bids " +
                            "WHERE auctionID = ? AND bidOwner = ? " +
                            "ORDER BY bidAmount DESC LIMIT 1";

            try (PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setLong(1, auctionID);
                stmt.setString(2, player.getUniqueId().toString());

                ResultSet rs = stmt.executeQuery();

                if (rs.next()){

                    int bidCanceled = rs.getInt("bidCanceled");

                    if (bidCanceled == 1){
                        resultBid = new Bid(
                                rs.getLong("bidID"),
                                rs.getLong("auctionID"),
                                rs.getString("bidOwner"),
                                rs.getDouble("bidAmount"),
                                rs.getLong("bidDate"),
                                bidCanceled
                        );
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Bid finalResultBid = resultBid;

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), ()-> callback.accept(finalResultBid));


        });


    }

    public void ignoreThisAuction(Player player, long auctionID){
        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{
            String sql = "UPDATE Bids SET bidCanceled = 2 WHERE auctionID = ? AND bidOwner = ? AND bidCanceled = 1";
            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setLong(1,auctionID);
                stmt.setString(2,player.getUniqueId().toString());

                int rows = stmt.executeUpdate();
                if (rows == 0){
                    player.sendMessage("§cBidStatus konnte nicht geändert werden :/");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }


    public void countActiveBidsForPlayer(Player player, Consumer<int[]> callback){

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

            int top = 0;
            int notTop = 0;

            String sql = "SELECT a.auctionID, " +
                    "(SELECT bidOwner FROM Bids " +
                    "WHERE auctionID = a.auctionID AND bidCanceled = 0 " +
                    "ORDER BY bidAmount DESC LIMIT 1 ) AS highestBidOwner " +
                    "FROM Auctions a JOIN ItemStorage i ON i.itemID = a.itemID " +
                    "WHERE a.closed = 0 AND i.dateClaimed IS NULL AND EXISTS ( " +
                    "SELECT 1 FROM Bids pb " +
                    "WHERE pb.auctionID = a.auctionID AND pb.bidOwner = ? AND pb.bidCanceled != 2 )";

            try (PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setString(1, player.getUniqueId().toString());

                ResultSet rs = stmt.executeQuery();

                while(rs.next()){
                    String highestBidOwner = rs.getString("highestBidOwner");

                    if (highestBidOwner != null && highestBidOwner.equals(player.getUniqueId().toString())){
                        top++;
                    }else{
                        notTop++;
                    }

                }

            }catch (Exception e){
                e.printStackTrace();
            }

            int[] result = new int[]{top, notTop};
            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> callback.accept(result));

        });
    }

    public void getOpenReimbursements(UUID player, Consumer<List<HammaxPaymentLog>> callback){

        List<HammaxPaymentLog> logs= new ArrayList<>();

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

            String sql = "SELECT paymentID, playerUUID, auctionID, bidID, amount, type, paymentDate, paymentComplete, bookmark " +
                    "FROM Payments WHERE playerUUID = ? AND paymentComplete = 0";


            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setString(1, player.toString());

                ResultSet rs = stmt.executeQuery();
                while(rs.next()){
                    long paymentID = rs.getLong("paymentID");
                    String playerUUID = rs.getString("playerUUID");
                    long auctionID = rs.getLong("auctionID");
                    long bidID = rs.getLong("bidID");
                    double amount = rs.getDouble("amount");
                    String type = rs.getString("type");
                    long paymentDate = rs.getLong("paymentDate");
                    boolean paymentComplete = rs.getBoolean("paymentComplete");
                    boolean bookmark = rs.getBoolean("bookmark");

                    logs.add(new HammaxPaymentLog(paymentID, playerUUID,auctionID,bidID,amount,type,paymentDate,paymentComplete,bookmark));
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), ()-> callback.accept(logs));

        });

        /*

        "CREATE TABLE IF NOT EXISTS Payments (" +
                            "paymentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "playerUUID VARCHAR(36) NOT NULL, " +
                            "auctionID INTEGER, " +
                            "bidID INTEGER, " +
                            "amount REAL NOT NULL, " +
                            "type TEXT NOT NULL, " +            //Auction_Create / Bid_Create / Bid_Reimburse
                            "paymentDate BIGINT, " +
                            "paymentComplete BOOLEAN, " +          //keinen bock das auch noch ein zu fügen muss aber
                            "bookmark BOOLEAN DEFAULT 0 " +
                            ");"

         */
    }

    public void closeReimbursement(long paymentID){

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

            String sql = "UPDATE Payments SET paymentDate = ?, paymentComplete = 1 WHERE paymentID = ?";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){
                stmt.setLong(1, System.currentTimeMillis());
                stmt.setLong(2, paymentID);

                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    System.out.println("§cPayment Status konnte nicht geändert werden :/");
                    //player.sendMessage("§cBid Status konnte nicht geändert werden :/");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        });

    }

    public void createItemChangeLog(String playerUUID, long itemID, String type){

        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

            String sql = "INSERT INTO ItemChanges (playerUUID, itemID, type, date) VALUES (?, ?, ?, ?)";

            try(PreparedStatement stmt = SQLSetup.getConnection().prepareStatement(sql)){

                stmt.setString(1, playerUUID);
                stmt.setLong(2,itemID);
                stmt.setString(3,type);
                stmt.setLong(4,System.currentTimeMillis());

                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    System.out.println("§cItemChangeLog konnte nicht erstellt werden :/");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }



        });

         /*
    smt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ItemChanges (" +
                            "changeID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "playerUUID VARCHAR(36) NOT NULL, " +
                            "itemID INTEGER, " +
                            //"itemStack TEXT, " +          //unnötig
                            "type TEXT NOT NULL, " +        //Auction_Item_Add / Auction_Item_Remove / Item_Claim / Item_Add
                            "date BIGINT NOT NULL, " +
                            "bookmark BOOLEAN DEFAULT 0 " +
                            ");"
            );
     */

    }

    public void getPlayerAuctionLogsFromDB(String givenUUID, Consumer<HammaxPlayerLogs> callback){


        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()->{

            List<HammaxPaymentLog> payments = new ArrayList<>();
            List<HammaxItemChangeLog> itemChanges = new ArrayList<>();

            //Payments

            String paymentSql = "SELECT paymentID, playerUUID, auctionID, bidID, amount, type, paymentDate, paymentComplete, bookmark " +
                    "FROM Payments WHERE playerUUID = ? AND paymentComplete = 1 ORDER BY paymentDate DESC";

            try(PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentSql)){
                paymentStmt.setString(1, givenUUID);

                ResultSet rs = paymentStmt.executeQuery();

                while(rs.next()){
                    payments.add( new HammaxPaymentLog(
                            rs.getLong("paymentID"),
                            rs.getString("playerUUID"),
                            rs.getLong("auctionID"),
                            rs.getLong("bidID"),
                            rs.getDouble("amount"),
                            rs.getString("type"),
                            rs.getLong("paymentDate"),
                            rs.getBoolean("paymentComplete"),
                            rs.getBoolean("bookmark")
                    ));
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            //itemChanges

            String changeSql = "SELECT changeID, playerUUID, itemID, type, date, bookmark " +
                    "FROM ItemChanges WHERE playerUUID = ? ORDER BY date DESC";

            try(PreparedStatement changeStmt = SQLSetup.getConnection().prepareStatement(changeSql)){
                changeStmt.setString(1, givenUUID);

                ResultSet rs = changeStmt.executeQuery();

                while(rs.next()){
                    itemChanges.add(new HammaxItemChangeLog(
                            rs.getLong("changeID"),
                            rs.getString("playerUUID"),
                            rs.getLong("itemID"),
                            rs.getString("type"),
                            rs.getLong("date"),
                            rs.getBoolean("bookmark")
                    ));
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            HammaxPlayerLogs logs = new HammaxPlayerLogs(payments, itemChanges);

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), ()->{
                callback.accept(logs);
            });

        });
    }

    public void getPlayerpendingPayments(String givenUUID, Consumer<List<HammaxPaymentLog>> callback){
        Hammaxcustomauction.getInstance().getDBQueue().addToQueue(()-> {

            List<HammaxPaymentLog> payments = new ArrayList<>();

            String paymentSql = "SELECT paymentID, playerUUID, auctionID, bidID, amount, type, paymentDate, paymentComplete, bookmark " +
                    "FROM Payments WHERE playerUUID = ? AND paymentComplete = 0 ORDER BY paymentDate DESC";

            try (PreparedStatement paymentStmt = SQLSetup.getConnection().prepareStatement(paymentSql)) {
                paymentStmt.setString(1, givenUUID);

                ResultSet rs = paymentStmt.executeQuery();

                while (rs.next()) {
                    payments.add(new HammaxPaymentLog(
                            rs.getLong("paymentID"),
                            rs.getString("playerUUID"),
                            rs.getLong("auctionID"),
                            rs.getLong("bidID"),
                            rs.getDouble("amount"),
                            rs.getString("type"),
                            rs.getLong("paymentDate"),
                            rs.getBoolean("paymentComplete"),
                            rs.getBoolean("bookmark")
                    ));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(Hammaxcustomauction.getInstance(), () -> {
                callback.accept(payments);
            });

        });

    }




}
