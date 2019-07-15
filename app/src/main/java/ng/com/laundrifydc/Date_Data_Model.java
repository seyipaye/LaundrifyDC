package ng.com.laundrifydc;

import com.google.firebase.database.DataSnapshot;

import java.util.Date;

public class Date_Data_Model {
    Date date;
    DataSnapshot datasnapshot;

    public Date_Data_Model (Date date, DataSnapshot datasnapshot) {
        this.date = date;
        this.datasnapshot = datasnapshot;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DataSnapshot getDatasnapshot() {
        return datasnapshot;
    }

    public void setDatasnapshot(DataSnapshot datasnapshot) {
        this.datasnapshot = datasnapshot;
    }
}
