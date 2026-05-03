package hdh.hammaxcustomauction;

import java.util.ArrayList;
import java.util.List;

public class HammaxPlayerLogs {

    private List<HammaxPaymentLog> payments;
    private List<HammaxItemChangeLog> itemChanges;

    public HammaxPlayerLogs(List<HammaxPaymentLog> payments, List<HammaxItemChangeLog> itemChanges){
        this.payments = payments;
        this.itemChanges = itemChanges;
    }

    public List<HammaxPaymentLog> getPayments() {
        return payments;
    }

    public List<HammaxItemChangeLog> getItemChanges() {
        return itemChanges;
    }

    public List<Integer[]> getSortList(){
        List<Integer[]> erg = new ArrayList<>();
        int countP = 0;
        int countC = 0;
        for (int i = 0; i <payments.size() + itemChanges.size(); i++){
            if (payments.size() > countP && itemChanges.size() > countC){
                if (payments.get(countP).getPaymentDate() > itemChanges.get(countC).getDate()){
                    erg.add(new Integer[]{0, countP});
                    countP++;
                } else {
                    erg.add(new Integer[]{1, countC});
                    countC++;
                }
            }else if (payments.size() > countP){
                erg.add(new Integer[]{0, countP});
                countP ++;
            } else if (itemChanges.size() > countC) {
                erg.add(new Integer[]{1, countC});
                countC ++;
            }
        }

        return erg;
    }



}
