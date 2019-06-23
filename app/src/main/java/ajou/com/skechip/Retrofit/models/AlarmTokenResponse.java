package ajou.com.skechip.Retrofit.models;

public class AlarmTokenResponse {
    private Boolean error;
    private String alarmToken;

    public AlarmTokenResponse(Boolean error, String alarmToken) {
        this.error = error;
        this.alarmToken = alarmToken;
    }

    public Boolean getError() {
        return error;
    }

    public String getAlarmToken() {
        return alarmToken;
    }
}