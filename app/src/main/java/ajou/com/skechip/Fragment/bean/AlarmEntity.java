package ajou.com.skechip.Fragment.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmEntity implements Parcelable {
    private int alarmNum;
    private String alarmTitle;
    private char alarmType;
    private String from;
    private String alarmContent;
    private String alarmTime;
    public AlarmEntity(int alarmNum, char alarmType, String from, String alarmTime) {
        this.alarmNum = alarmNum;
        this.alarmType = alarmType;
        this.from = from;
        this.alarmTime = alarmTime;
        switch(alarmType){
            case 'm':   //meeting
                this.alarmTitle="모임 추가";
                this.alarmContent= from + "님이 모임을 추가하였습니다.";
                break;
            case 's':   //schedule
                this.alarmTitle="모임 일정 추가";
                this.alarmContent=from +"님이 모임일정을 추가하였습니다.";
                break;
            case 'p':   //promise
                this.alarmTitle="1:1 만남 일정 추가";
                this.alarmContent=from+"님이 만남일정을 추가하였습니다.";
                break;
            case 'r':   //request
                this.alarmTitle="친구요청";
                this.alarmContent=from+"님이 친구요청을 하였습니다.";
        }
    }
    public int getAlarmNum(){ return alarmNum;}

    public String getAlarmTitle() {
        return alarmTitle;
    }

    public String getAlarmContent() {
        return alarmContent;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public char getAlarmType() {
        return alarmType;
    }


    protected AlarmEntity(Parcel in) {
        alarmNum = in.readInt();
        alarmTitle = in.readString();
        alarmType = (char) in.readInt();
        from = in.readString();
        alarmContent = in.readString();
        alarmTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt((int)alarmNum);
        dest.writeString(alarmTitle);
        dest.writeInt((int) alarmType);
        dest.writeString(from);
        dest.writeString(alarmContent);
        dest.writeString(alarmTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlarmEntity> CREATOR = new Creator<AlarmEntity>() {
        @Override
        public AlarmEntity createFromParcel(Parcel in) {
            return new AlarmEntity(in);
        }

        @Override
        public AlarmEntity[] newArray(int size) {
            return new AlarmEntity[size];
        }
    };
}
