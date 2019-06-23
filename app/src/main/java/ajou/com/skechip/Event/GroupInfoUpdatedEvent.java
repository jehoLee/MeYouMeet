package ajou.com.skechip.Event;

import ajou.com.skechip.Fragment.bean.GroupEntity;

public class GroupInfoUpdatedEvent {
    private GroupEntity updatedGroupEntity;
    private Boolean isMeetingUpdated;

    public GroupInfoUpdatedEvent(GroupEntity updatedGroupEntity, Boolean isMeetingUpdated){
        this.updatedGroupEntity = updatedGroupEntity;
        this.isMeetingUpdated = isMeetingUpdated;
    }

    public GroupEntity getUpdatedGroupEntity() {
        return updatedGroupEntity;
    }

    public void setUpdatedGroupEntity(GroupEntity updatedGroupEntity) {
        this.updatedGroupEntity = updatedGroupEntity;
    }

    public Boolean isMeetingUpdated() {
        return isMeetingUpdated;
    }
}
