package ajou.com.skechip.Retrofit.models;

import com.google.gson.annotations.SerializedName;

public class CreateMeetingResponse {

    @SerializedName("error")
    private boolean err;

    @SerializedName("message")
    private String msg;

    private Integer meetingId;

    public CreateMeetingResponse(boolean error, String message, Integer meetingId) {
        this.err = error;
        this.msg = message;
        this.meetingId = meetingId;
    }

    public boolean isErr() {
        return err;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getMeetingId() {
        return meetingId;
    }
}