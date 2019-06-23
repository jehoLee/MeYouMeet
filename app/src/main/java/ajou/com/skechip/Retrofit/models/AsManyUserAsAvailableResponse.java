package ajou.com.skechip.Retrofit.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AsManyUserAsAvailableResponse {
    @SerializedName("message")
    private String msg;

    private boolean error;
    private Integer totalCount;
    private List<Integer> asManyUserAsAvailableList;

    public AsManyUserAsAvailableResponse(boolean error, String message, List<Integer> asManyUserAsAvailableList, Integer totalCount) {
        this.error = error;
        this.msg = message;
        this.asManyUserAsAvailableList = asManyUserAsAvailableList;
        this.totalCount = totalCount;
    }

    public boolean isError() {
        return error;
    }

    public String getMsg() {
        return msg;
    }

    public List<Integer> getAsManyUserAsAvailableList() {
        return asManyUserAsAvailableList;
    }

    public Integer getTotalCount() {
        return totalCount;
    }
}
