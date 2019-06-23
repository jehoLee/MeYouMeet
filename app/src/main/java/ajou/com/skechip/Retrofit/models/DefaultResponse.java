package ajou.com.skechip.Retrofit.models;

import com.google.gson.annotations.SerializedName;

public class DefaultResponse {

    @SerializedName("error")
    private boolean err;

    @SerializedName("message")
    private String msg;

    public DefaultResponse(boolean error, String message) {
        this.err = error;
        this.msg = message;
    }

    public boolean isErr() {
        return err;
    }

    public String getMsg() {
        return msg;
    }
}