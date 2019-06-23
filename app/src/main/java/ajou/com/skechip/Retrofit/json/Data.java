package ajou.com.skechip.Retrofit.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("alarmType")
    @Expose
    private String alarmType;

    @SerializedName("toToken")
    @Expose
    private String toToken;

    @SerializedName("fromToken")
    @Expose
    private String fromToken;

    public Data() {
    }

    public Data(String alarmType, String toToken, String fromToken) {
        this.alarmType = alarmType;
        this.toToken = toToken;
        this.fromToken = fromToken;
    }

    public String getalarmType() {
        return alarmType;
    }

    public void setalarmType(String alarmType) {
        this.alarmType = alarmType;
    }

}
