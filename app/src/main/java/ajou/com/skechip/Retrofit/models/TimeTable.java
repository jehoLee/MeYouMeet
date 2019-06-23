package ajou.com.skechip.Retrofit.models;

public class TimeTable {

    private Integer id, userId, cellPosition;
    private Character type;
    private String title, place;

    public TimeTable(Integer id, Integer userId, Character type, String title, String place, Integer cellPosition) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.place= place;
        this.cellPosition = cellPosition;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Character getType(){ return type; }

    public String getTitle() {
        return title;
    }

    public String getPlace() {
        return place;
    }

    public Integer getCellPosition() {
        return cellPosition;
    }
}