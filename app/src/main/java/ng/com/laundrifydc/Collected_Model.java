package ng.com.laundrifydc;

import java.util.ArrayList;

class Collected_Model {
    String day, total, dataDay;
    ArrayList<String> keys;
    public Collected_Model(String day, String total, ArrayList<String> keys, String dataDay) {
        this.day = day;
        this.total = total;
        this.keys = keys;
        this.dataDay = dataDay;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }

    public String getDataDay() {
        return dataDay;
    }

    public void setDataDay(String dataDay) {
        this.dataDay = dataDay;
    }
}
