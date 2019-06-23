package ajou.com.skechip.Fragment.bean;

/**
 * Created by zhouchaoyuan on 2017/1/14.
 */

public class ColTitle {

    private String roomNumber;
    private String roomTypeName;

    public String getAlphabetName() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getTimeRangeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }
}
