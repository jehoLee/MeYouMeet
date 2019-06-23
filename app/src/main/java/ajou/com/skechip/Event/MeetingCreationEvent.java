package ajou.com.skechip.Event;

import ajou.com.skechip.Fragment.bean.GroupEntity;
import ajou.com.skechip.Fragment.bean.MeetingEntity;

public class MeetingCreationEvent {

    private GroupEntity groupEntity;

    public MeetingCreationEvent(GroupEntity groupEntity){
        this.groupEntity = groupEntity;
    }

    public GroupEntity getGroupEntityWithNewMeeting() {
        return groupEntity;
    }
}
