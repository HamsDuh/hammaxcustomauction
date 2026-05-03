package hdh.hammaxcustomauction;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLSetup {

    private static Connection connection;
    private static Hammaxcustomauction plugin;

    public static void init(Hammaxcustomauction pl){
        plugin = pl;
        connect();
    }


    private static void connect(){
        try{
            File dbFile = new File(plugin.getDataFolder(), "hammaxauctions.db");

            if(!plugin.getDataFolder().exists()){
                plugin.getDataFolder().mkdirs();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setupSQL(){
        try(Statement smt = connection.createStatement()){

            smt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ItemStorage (" +
                            "itemID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "itemOwner VARCHAR(36) NOT NULL," +                    //varchar 36
                            "itemStack TEXT NOT NULL," +
                            "dateAdded BIGINT NOT NULL," +
                            "dateClaimed BIGINT" +
                            ");"
            );

            smt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Auctions (" +
                            "auctionID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "itemID INTEGER NOT NULL," +
                            "startingOwner VARCHAR(36) NOT NULL," +
                            "startingPrice REAL NOT NULL," +
                            "bidIncrease REAL NOT NULL," +
                            "deadline BIGINT NOT NULL," +
                            "closed BOOLEAN DEFAULT 0," +
                            "FOREIGN KEY(itemID) REFERENCES ItemStorage(itemID)" +
                            ");"
            );

            smt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Bids (" +
                            "bidID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "auctionID INTEGER NOT NULL," +
                            "bidOwner VARCHAR(36) NOT NULL," +
                            "bidAmount REAL NOT NULL," +
                            "bidDate BIGINT NOT NULL," +
                            "bidCanceled INTEGER DEFAULT 0," +
                            "FOREIGN KEY(auctionID) REFERENCES Auctions(auctionID)" +
                            ");"
            );


            smt.executeUpdate(
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
            );

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





/*
            smt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ErrorLogs (" +
                            "dupeID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "flagCount INTEGER DEFAULT 1 " +
                            "paymentID INTEGER, " +
                            "itemChangeID INTEGER, " +
                            "autoResolved BOOLEAN DEFAULT 0, " +
                            "manualResolved BOOLEAN DEFAULT 0 "
                            ");"


*/


            System.out.println("[HammaxAuction] Tabellen geladen");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Connection getConnection(){
        return connection;
    }
    /*

        ItemStorage
            itemID INTEGER
            itemOwner VARCHAR(36)
            itemStack TEXT
            dateAdded BIGINT
            dateClaimed BIGINT

        Auctions
            auctionID INTEGER
            itemID INTEGER
            startingPrice REAL
            bidIncrease REAL
            deadline BIGINT
            closed BOOLEAN DEFAULT 0

        Bids
            bidID INTEGER
            auctionID INTEGER
            bidOwner VARCHAR(36)
            bidDate BIGINT
            bidCanceled BOOLEAN

    */

}
