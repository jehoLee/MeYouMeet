package ajou.com.skechip.Fragment.bean;

import android.os.Parcel;
import android.os.Parcelable;


import java.util.ArrayList;
import java.util.List;

import ajou.com.skechip.Retrofit.models.Kakao;

public class GroupEntity implements Parcelable {
    private Integer groupID;
    private Long groupManager;
    private String groupTitle;
    private String groupTag;
    private int groupMemberNum;
    private List<Kakao> groupMembers;
    private List<MeetingEntity> meetingEntities = new ArrayList<>();

    //    private int groupImg;

    public GroupEntity(Integer groupID, String groupTitle, String groupTag, Long groupManager){
        this.groupID = groupID;
        this.groupTitle = groupTitle;
        this.groupTag = groupTag;
        this.groupManager = groupManager;
    }

    public GroupEntity(String groupTitle, String groupTag, int groupMemberNum, List<Kakao> groupMembers){
        this.groupTitle = groupTitle;
        this.groupTag = groupTag;
        this.groupMemberNum = groupMemberNum;
        this.groupMembers = groupMembers;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

//    public int getGroupImg() {
//        return groupImg;
//    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

//    public void setGroupImg(int groupImg) {
//        this.groupImg = groupImg;
//    }

    public void setMeetingEntities(List<MeetingEntity> meetingEntities) {
        this.meetingEntities = meetingEntities;
    }

    public void clearMeetingEntities() {
        this.meetingEntities.clear();
    }

    public Integer getGroupID() {
        return groupID;
    }

    public void setGroupID(Integer groupID) {
        this.groupID = groupID;
    }

    public Long getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(Long groupManager) {
        this.groupManager = groupManager;
    }

    public String getGroupTag() {
        return groupTag;
    }

    public void setGroupTag(String groupTag) {
        this.groupTag = groupTag;
    }

    public int getGroupMemberNum() {
        return groupMemberNum;
    }

    public void setGroupMemberNum(int groupMemberNum) {
        this.groupMemberNum = groupMemberNum;
    }

    public List<Kakao> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<Kakao> groupMembers) {
        this.groupMembers = groupMembers;
        this.groupMemberNum = groupMembers.size();
    }

    public List<MeetingEntity> getMeetingEntities() {
        return meetingEntities;
    }

//    public void setMeetingEntities(List<MeetingEntity> meetingEntities) {
//        this.meetingEntities = meetingEntities;
//    }

    public void addMeetingEntity(MeetingEntity meetingEntity){
        this.meetingEntities.add(meetingEntity);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (groupID == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(groupID);
        }
        if (groupManager == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(groupManager);
        }
        dest.writeString(groupTitle);
        dest.writeString(groupTag);
        dest.writeInt(groupMemberNum);
        dest.writeTypedList(groupMembers);
        dest.writeTypedList(meetingEntities);
    }

    protected GroupEntity(Parcel in) {
        if (in.readByte() == 0) {
            groupID = null;
        } else {
            groupID = in.readInt();
        }
        if (in.readByte() == 0) {
            groupManager = null;
        } else {
            groupManager = in.readLong();
        }
        groupTitle = in.readString();
        groupTag = in.readString();
        groupMemberNum = in.readInt();
        groupMembers = in.createTypedArrayList(Kakao.CREATOR);
        meetingEntities = in.createTypedArrayList(MeetingEntity.CREATOR);
    }

    public static final Creator<GroupEntity> CREATOR = new Creator<GroupEntity>() {
        @Override
        public GroupEntity createFromParcel(Parcel in) {
            return new GroupEntity(in);
        }

        @Override
        public GroupEntity[] newArray(int size) {
            return new GroupEntity[size];
        }
    };


}

//
//public class RoomContext implements Serializable {
//
//    public RoomContext (){
//
//    }
//
//    public RoomContext(String room_name, Vote vote){
//        this.room_name = room_name;
//        this.vote = vote;
//    }
//
//    @SerializedName("room_name")
//    private String room_name;
//
//    @SerializedName("vote")
//    private Vote vote;
//
//    @SerializedName("rtmp_url")
//    private String streaming_url;
//
//    @SerializedName("xmpp_host")
//    private String XMPPHost;
//
//    @SerializedName("xmpp_service_name")
//    private String XMPPService;
//
//    @SerializedName("xmpp_room_name")
//    private String XMPPRoomName;
//
//    @SerializedName("room_idx")
//    private String roomId;
//
//    //for sqlLite
//    private int id;
//
//    public String getRoomId() { return roomId; }
//
//    public String getXMPPHost() { return XMPPHost; }
//
//    public String getXMPPService() { return XMPPService; }
//
//    public String getXMPPRoomName() { return XMPPRoomName; }
//
//    public String getStreaming_url() {
//        return streaming_url;
//    }
//
//    public Vote getVote() {
//        return vote;
//    }
//
//    public void setVote(Vote vote) {
//        this.vote = vote;
//    }
//
//    public String getRoom_name() {
//        return room_name;
//    }
//
//    public void setRoom_name(String room_name) {
//        this.room_name = room_name;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public void setRoomId(String roomId) {
//        this.roomId = roomId;
//    }
//
//}