package ajou.com.skechip.Retrofit.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AvailableMeetingTimesResponse {
    @SerializedName("message")
    private String msg;

    private boolean error;
    private Integer totalCount;
    private List<Integer> availableMeetingTimes;

    public AvailableMeetingTimesResponse(boolean error, String message, List<Integer> availableMeetingTimes, Integer totalCount) {
        this.error = error;
        this.msg = message;
        this.availableMeetingTimes = availableMeetingTimes;
        this.totalCount = totalCount;
    }

    public boolean isError() {
        return error;
    }

    public String getMsg() {
        return msg;
    }

    public List<Integer> getAvailableMeetingTimes() {
        return availableMeetingTimes;
    }

    public Integer getTotalCount() {
        return totalCount;
    }
}
