package ajou.com.skechip.Retrofit.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Kakao implements Parcelable{
    private Long userId;
    private String profileNickname;
    private String profileThumbnailImage;

    public Kakao(long userId, String profileNickname, String profileThumbnailImage) {
        this.userId = userId;
        this.profileNickname = profileNickname;
        this.profileThumbnailImage = profileThumbnailImage;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProfileNickname() {
        return profileNickname;
    }

    public void setProfileNickname(String profileNickname) {
        this.profileNickname = profileNickname;
    }

    public String getProfileThumbnailImage() {
        return profileThumbnailImage;
    }

    public void setProfileThumbnailImage(String profileThumbnailImage) {
        this.profileThumbnailImage = profileThumbnailImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (userId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(userId);
        }
        dest.writeString(profileNickname);
        dest.writeString(profileThumbnailImage);
    }

    protected Kakao(Parcel in) {
        if (in.readByte() == 0) {
            userId = null;
        } else {
            userId = in.readLong();
        }
        profileNickname = in.readString();
        profileThumbnailImage = in.readString();
    }

    public static final Creator<Kakao> CREATOR = new Creator<Kakao>() {
        @Override
        public Kakao createFromParcel(Parcel in) {
            return new Kakao(in);
        }

        @Override
        public Kakao[] newArray(int size) {
            return new Kakao[size];
        }
    };




}