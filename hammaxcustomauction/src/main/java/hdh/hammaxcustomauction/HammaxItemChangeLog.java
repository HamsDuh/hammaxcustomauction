package hdh.hammaxcustomauction;

public class HammaxItemChangeLog {

    private long changeID;
    private String playerUUID;
    private long itemID;
    private String type;                    //Auction_Item_Add / Auction_Item_Remove / Item_Claim / Item_Add
    private long date;
    private boolean bookmark;

    public HammaxItemChangeLog(long changeID, String playerUUID, long itemID, String type, long date, boolean bookmark){
        this.changeID = changeID;
        this.playerUUID = playerUUID;
        this.itemID = itemID;
        this.type = type;
        this.date = date;
        this.bookmark = bookmark;
    }

    public long getChangeID() {
        return changeID;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public long getItemID() {
        return itemID;
    }

    public String getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public boolean getBookmark() {
        return bookmark;
    }

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
