package ajou.com.skechip.Fragment.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import ajou.com.skechip.Retrofit.models.Kakao;

public class MeetingEntity implements Parcelable {
    private String title;
    private String location;
    private Integer type;
    private List<Cell> meetingTimeCells;
    private List<Kakao> selectedMembers;
    private Integer meetingID;
    private Long meetingManager;

    public MeetingEntity(String title, String location, Integer type, Integer Id, Long managerId, List<Cell> meetingTimeCells) {
        this.title = title;
        this.location = location;
        this.type = type;
        this.meetingID = Id;
        this.meetingManager = managerId;
        this.meetingTimeCells = meetingTimeCells;
    }

    public MeetingEntity(String title, String location, Integer type, List<Cell> meetingTimeCells, List<Kakao> selectedMembers) {
        this.title = title;
        this.location = location;
        this.type = type;
        this.meetingTimeCells = meetingTimeCells;
        this.selectedMembers = selectedMembers;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<Cell> getMeetingTimeCells() {
        return meetingTimeCells;
    }

    public void setMeetingTimeCells(List<Cell> meetingTimeCells) {
        this.meetingTimeCells = meetingTimeCells;
    }

    public List<Kakao> getSelectedMembers() {
        return selectedMembers;
    }

    public void setSelectedMembers(List<Kakao> selectedMembers) {
        this.selectedMembers = selectedMembers;
    }

    public Integer getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(Integer meetingID) {
        this.meetingID = meetingID;
    }

    public Long getMeetingManager() {
        return meetingManager;
    }

    public void setMeetingManager(Long meetingManager) {
        this.meetingManager = meetingManager;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(location);
        if (type == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(type);
        }
        dest.writeTypedList(meetingTimeCells);
        dest.writeTypedList(selectedMembers);
        if (meetingID == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(meetingID);
        }
        if (meetingManager == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(meetingManager);
        }
    }

    protected MeetingEntity(Parcel in) {
        title = in.readString();
        location = in.readString();
        if (in.readByte() == 0) {
            type = null;
        } else {
            type = in.readInt();
        }
        meetingTimeCells = in.createTypedArrayList(Cell.CREATOR);
        selectedMembers = in.createTypedArrayList(Kakao.CREATOR);
        if (in.readByte() == 0) {
            meetingID = null;
        } else {
            meetingID = in.readInt();
        }
        if (in.readByte() == 0) {
            meetingManager = null;
        } else {
            meetingManager = in.readLong();
        }
    }

    public static final Creator<MeetingEntity> CREATOR = new Creator<MeetingEntity>() {
        @Override
        public MeetingEntity createFromParcel(Parcel in) {
            return new MeetingEntity(in);
        }

        @Override
        public MeetingEntity[] newArray(int size) {
            return new MeetingEntity[size];
        }
    };

}
