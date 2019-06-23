package ajou.com.skechip.Retrofit.models;

import java.util.List;

public class AlarmResponse {
    private Boolean error;
    private List<Alarm> alarmList;
    private Alarm alarm;

    public AlarmResponse(boolean error, List<Alarm> alarmList, Alarm alarm) {
        this.error = error;
        this.alarmList = alarmList;
        this.alarm = alarm;
    }

    public boolean isError() {
        return error;
    }

    public List<Alarm> getAlarmList() {
        return alarmList;
    }

    public Alarm getAlarm() {
        return alarm;
    }
}