package ajou.com.skechip.Event;

import ajou.com.skechip.Fragment.bean.GroupEntity;

public class GroupCreationEvent {

    private GroupEntity newGroup;

    public GroupCreationEvent(GroupEntity groupEntity){
        this.newGroup = groupEntity;
    }

    public GroupEntity getNewGroup() {
        return newGroup;
    }

    public void setNewGroup(GroupEntity newGroup) {
        this.newGroup = newGroup;
    }
}
