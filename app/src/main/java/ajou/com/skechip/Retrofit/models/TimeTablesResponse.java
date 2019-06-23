package ajou.com.skechip.Retrofit.models;

import java.util.List;

public class TimeTablesResponse {

    private boolean error;
    private List<TimeTable> timeTables;

    public TimeTablesResponse(boolean error, List<TimeTable> timeTables) {
        this.error = error;
        this.timeTables = timeTables;
    }

    public boolean isError() {
        return error;
    }

    public List<TimeTable> getTimeTables() {
        return timeTables;
    }
}
