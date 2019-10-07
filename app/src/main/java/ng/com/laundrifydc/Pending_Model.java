package ng.com.laundrifydc;

public class Pending_Model {

    String orderName, details, id, coTime, coAdd, note, number, collectionStamp, deliveryStamp;
    private boolean weeklyPickup;

    public Pending_Model(String orderName, String details, String id, String coTime, String coAdd, String note, String number,
                         String collectionStamp, String deliveryStamp, boolean weeklyPickup) {
        this.orderName = orderName;
        this.details = details;
        this.id = id;
        this.coTime = coTime;
        this.coAdd = coAdd;
        this.note = note;
        this.number = number;
        this.collectionStamp = collectionStamp;
        this.deliveryStamp = deliveryStamp;
        this.weeklyPickup = weeklyPickup;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCollectionStamp() {
        return collectionStamp;
    }

    public void setCollectionStamp(String collectionStamp) {
        this.collectionStamp = collectionStamp;
    }

    public String getDeliveryStamp() {
        return deliveryStamp;
    }

    public void setDeliveryStamp(String deliveryStamp) {
        this.deliveryStamp = deliveryStamp;
    }

    public boolean isWeeklyPickup() {
        return weeklyPickup;
    }

    public void setWeeklyPickup(boolean weeklyPickup) {
        this.weeklyPickup = weeklyPickup;
    }
}
