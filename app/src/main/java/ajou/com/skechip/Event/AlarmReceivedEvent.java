package ajou.com.skechip.Event;

import ajou.com.skechip.Fragment.bean.AlarmEntity;

public class AlarmReceivedEvent {
    private AlarmEntity alarmEntity;

    public AlarmReceivedEvent(AlarmEntity alarmEntity){
        this.alarmEntity = alarmEntity;
    }

    public AlarmEntity getAlarmEntity() {
        return alarmEntity;
    }

    public void setAlarmEntity(AlarmEntity alarmEntity) {
        this.alarmEntity = alarmEntity;
    }
}
