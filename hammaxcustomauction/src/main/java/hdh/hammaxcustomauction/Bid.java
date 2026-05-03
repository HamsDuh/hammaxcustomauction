package hdh.hammaxcustomauction;

import org.bukkit.entity.Player;

public class Bid {

    private long bidID;
    private long auctionID;
    private String bidOwner;
    private double bidAmount;
    private long bidDate;
    private int bidCanceled;

    public Bid(){
        this.bidID = -1;
        this.auctionID = -1;
        this.bidOwner = "";
        this.bidAmount = 0;
        this.bidDate = 0;
        this.bidCanceled = 0;
    }

    public Bid(long givenID,long givenAuctionID, String givenOwner, double givenAnmount, long givenDate, int givenStatus){
        this.bidID = givenID;
        this.auctionID = givenAuctionID;
        this.bidOwner = givenOwner;
        this.bidAmount = givenAnmount;
        this.bidDate = givenDate;
        this.bidCanceled = givenStatus;
    }

    public void setBidID(long bidID) {
        this.bidID = bidID;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public void setBidOwner(String bidOwner) {
        this.bidOwner = bidOwner;
    }

    public void setBidDate(long bidDate) {
        this.bidDate = bidDate;
    }

    public void setBidCanceled(int bidCanceled) {
        this.bidCanceled = bidCanceled;
    }

    public void setAuctionID(long givenAuctionID){
        this.auctionID = givenAuctionID;
    }


    public long getBidID() {
        return bidID;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public String getBidOwner() {
        return bidOwner;
    }

    public long getBidDate() {
        return bidDate;
    }

    public int getBidCanceled(){
        return bidCanceled;
    }

    public long getAuctionID() {
        return auctionID;
    }
}
