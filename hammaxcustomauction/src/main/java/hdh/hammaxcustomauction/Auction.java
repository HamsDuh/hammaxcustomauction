package hdh.hammaxcustomauction;

public class Auction
{

    private long auctionID;
    private long itemID;
    private String startingOwner;
    private double startingPrice;
    private double bidIncrease;
    private long deadline;
    private boolean closed;

    private StorageItem auctionItem;
    private Bid bid;

    Auction(){
        this.auctionID = -1;
        this.itemID = -1;
        this.startingOwner = "";
        this.startingPrice = 0.0;
        this.bidIncrease = 0.0;
        this.deadline = 0;
        this.closed = false;
    }

    Auction(long givenAuctionID, long givenItemID, String givenOwner, double givenStartingPrice, double givenBidIncrease, long givenDeadline, boolean status){
        this.auctionID = givenAuctionID;
        this.itemID = givenItemID;
        this.startingOwner = givenOwner;
        this.startingPrice = givenStartingPrice;
        this.bidIncrease = givenBidIncrease;
        this.deadline = givenDeadline;
        this.closed = status;
    }

    public void setAuctionID(long auctionID) {
        this.auctionID = auctionID;
    }

    public void setItemID(long itemID) {
        this.itemID = itemID;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setBidIncrease(double bidIncrease) {
        this.bidIncrease = bidIncrease;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setStartingOwner(String startingOwner) {
        this.startingOwner = startingOwner;
    }

    public long getAuctionID() {
        return auctionID;
    }

    public long getItemID() {
        return itemID;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getBidIncrease() {
        return bidIncrease;
    }

    public long getDeadline() {
        return deadline;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setStorageItem(StorageItem given){
        this.auctionItem = given;
    }

    public StorageItem getStorageItem(){
        return this.auctionItem;
    }

    public String getStartingOwner() {
        return startingOwner;
    }

    public void setBid(Bid givenBid){
        this.bid = givenBid;
    }

    public Bid getBid(){
        return this.bid;
    }
}
