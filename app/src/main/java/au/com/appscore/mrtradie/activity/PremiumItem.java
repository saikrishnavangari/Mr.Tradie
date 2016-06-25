package au.com.appscore.mrtradie.activity;

/**
 * Created by lijiazhou on 15/02/16.
 */
public class PremiumItem {
    String itemID;
    String itemPrice;

    public PremiumItem(String itemID, String itemPrice) {
        this.itemPrice = itemPrice;
        this.itemID = itemID;
    }

    public String getItemPrice() {

        return itemPrice;
    }

    public String getItemID() {

        return itemID;
    }
}
