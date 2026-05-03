package hdh.hammaxcustomauction;

public class AuctionFilter {

    private String sortStyle;       //Time oder Price
    private boolean ascending;
    private String nameFilter;
    private int page;
    private String[] possibleSortStyles = {"Zeit", "Preis"};

    public AuctionFilter(){
        this.sortStyle = "Zeit";
        this.ascending = true;
        this.nameFilter = null;
        this.page = 0;
    }

    public AuctionFilter(String style, boolean asc, String filter){
        this.sortStyle = style;
        this.ascending = asc;
        this.nameFilter = filter;
    }

    public void setSortStyle(String sortStyle) {
        this.sortStyle = sortStyle;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public void setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
    }
    public void setPage(int given){
        this.page = given;
    }

    public String getSortStyle() {
        return sortStyle;
    }

    public boolean getAscending(){
        return ascending;
    }

    public String getNameFilter() {
        return nameFilter;
    }

    public int getPage() {
        return page;
    }
}
