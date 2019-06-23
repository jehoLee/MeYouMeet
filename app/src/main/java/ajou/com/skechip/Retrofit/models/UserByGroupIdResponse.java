package ajou.com.skechip.Retrofit.models;

import java.util.List;

public class UserByGroupIdResponse {
    private boolean error;
    private List<Kakao> kakaoList;
    private Integer groupId;

    public UserByGroupIdResponse(boolean error, List<Kakao> kakaoList, Integer groupId) {
        this.error = error;
        this.kakaoList = kakaoList;
    }

    public boolean isError() {
        return error;
    }

    public Integer getGroupId() { return groupId;}

    public List<Kakao> getKakaoList() {
        return kakaoList;
    }

}
