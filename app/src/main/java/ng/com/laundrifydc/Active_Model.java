package ng.com.laundrifydc;

public class Active_Model {
    String orderName, details, orderID, userID, coTime, coAdd, note, pNumber, deTime, deAdd;
    boolean isWeeklyPickup, isPayed;
    int progress, price;

    public Active_Model(String orderID, String userID, String orderName, boolean isWeeklyPickup, String details, String coTime,
                        String coAdd, String deTime, String deAdd, String note, int price,
                        boolean isPayed, String pNumber, int progress) {

        this.orderName = orderName;
        this.details = details;
        this.orderID = orderID;
        this.userID = userID;
        this.coTime = coTime;
        this.coAdd = coAdd;
        this.note = note;
        this.pNumber = pNumber;
        this.isWeeklyPickup = isWeeklyPickup;
        this.deTime = deTime;
        this.deAdd = deAdd;
        this.price = price;
        this.isPayed = isPayed;
        this.progress = progress;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCoTime() {
        return coTime;
    }

    public void setCoTime(String coTime) {
        this.coTime = coTime;
    }

    public String getCoAdd() {
        return coAdd;
    }

    public void setCoAdd(String coAdd) {
        this.coAdd = coAdd;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDeTime() {
        return deTime;
    }

    public void setDeTime(String deTime) {
        this.deTime = deTime;
    }

    public String getDeAdd() {
        return deAdd;
    }

    public void setDeAdd(String deAdd) {
        this.deAdd = deAdd;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public void setPayed(boolean payed) {
        isPayed = payed;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getpNumber() {
        return pNumber;
    }

    public void setpNumber(String pNumber) {
        this.pNumber = pNumber;
    }

    public boolean isWeeklyPickup() {
        return isWeeklyPickup;
    }

    public void setWeeklyPickup(boolean weeklyPickup) {
        isWeeklyPickup = weeklyPickup;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
