package hdh.hammaxcustomauction;

public class HammaxPaymentLog {

    private long paymentID;
    private String playerUUID;
    private long auctionID;
    private long bidID;
    private double amount;
    private String type;                //Auction_Create / Bid_Create / Bid_Reimburse /Auction_Payout
    private long paymentDate;
    private boolean paymentComplete;
    private boolean bookmark;

    public HammaxPaymentLog(long paymentID, String playerUUID, long auctionID, long bidID, double amount, String type,
                            long paymentDate, boolean paymentComplete, boolean bookmark){
        this.paymentID = paymentID;
        this.playerUUID = playerUUID;
        this.auctionID = auctionID;
        this.bidID = bidID;
        this.amount = amount;
        this.type = type;
        this.paymentDate = paymentDate;
        this.paymentComplete = paymentComplete;
        this.bookmark = bookmark;
    }

    public long getPaymentID() {
        return paymentID;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public long getAuctionID() {
        return auctionID;
    }

    public long getBidID() {
        return bidID;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public long getPaymentDate() {
        return paymentDate;
    }

    public boolean isPaymentComplete() {
        return paymentComplete;
    }

    public boolean isBookmark() {
        return bookmark;
    }

    /*
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
    */
}
