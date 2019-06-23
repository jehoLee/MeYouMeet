package ajou.com.skechip.Retrofit.models;

public class AlarmToken {
    private Integer id;
    private Long kakaoId;
    private String token;

    public AlarmToken(Integer id, Long kakaoId, String token) {
        this.id = id;
        this.kakaoId = kakaoId;
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getKakaoId() {
        return kakaoId;
    }

    public void setKakaoId(Long kakaoId) {
        this.kakaoId = kakaoId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}