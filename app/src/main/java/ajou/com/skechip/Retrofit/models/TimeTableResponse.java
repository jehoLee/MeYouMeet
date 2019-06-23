package ajou.com.skechip.Retrofit.models;

public class TimeTableResponse {

    private boolean error;

    private String message;
    private TimeTable timeTable;

    public TimeTableResponse(boolean error, String message, TimeTable timeTable) {
        this.error = error;
        this.message = message;
        this.timeTable = timeTable;
    }

    public boolean isError() {
        return error;
    }

    public String getMsg() {
        return message;
    }

    public TimeTable getTimeTable() {
        return timeTable;
    }
}