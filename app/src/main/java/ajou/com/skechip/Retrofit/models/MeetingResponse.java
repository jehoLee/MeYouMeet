package ajou.com.skechip.Retrofit.models;

import java.util.List;

public class MeetingResponse {
    private boolean error;

    private List<Integer> idList;
    private List<Integer> typeList;
    private List<Long> managerList;
    private List<String> titleList;
    private List<String> placeList;
    private List<Integer> cellPositionList;
    private  Integer totalCount;
    private Integer groupId;

    public MeetingResponse(boolean error, List<Integer> idList, List<Integer> typeList, List<Long> managerList, List<String> titleList, List<String> placeList, List<Integer> cellPositionList, Integer totalCount, Integer groupId) {
        this.error = error;
        this.idList = idList;
        this.typeList = typeList;
        this.managerList = managerList;
        this.titleList = titleList;
        this.placeList = placeList;
        this.cellPositionList = cellPositionList;
        this.totalCount = totalCount;
        this.groupId = groupId;
    }

    public boolean isError() {
        return error;
    }

    public List<Integer> getIdList() {
        return idList;
    }

    public List<Integer> getTypeList() {
        return typeList;
    }

    public List<Long> getManagerList() {
        return managerList;
    }

    public List<String> getTitleList() {
        return titleList;
    }

    public List<String> getPlaceList() {
        return placeList;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public List<Integer> getCellPositionList() {
        return cellPositionList;
    }
}