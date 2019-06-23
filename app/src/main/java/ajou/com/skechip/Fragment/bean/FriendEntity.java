package ajou.com.skechip.Fragment.bean;

//import com.google.gson.annotations.SerializedName;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FriendEntity implements Parcelable {
    private Long userID;
    private String userName;
    private String userImgPath;

    public FriendEntity(Long userID, String userName, String userImgPath) {
        this.userID = userID;
        this.userName = userName;
        this.userImgPath = userImgPath;
    }

    public Long getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImgPath() {
        return userImgPath;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (userID == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(userID);
        }
        dest.writeString(userName);
        dest.writeString(userImgPath);
    }

    protected FriendEntity(Parcel in) {
        if (in.readByte() == 0) {
            userID = null;
        } else {
            userID = in.readLong();
        }
        userName = in.readString();
        userImgPath = in.readString();
    }

    public static final Creator<FriendEntity> CREATOR = new Creator<FriendEntity>() {
        @Override
        public FriendEntity createFromParcel(Parcel in) {
            return new FriendEntity(in);
        }

        @Override
        public FriendEntity[] newArray(int size) {
            return new FriendEntity[size];
        }
    };
}
