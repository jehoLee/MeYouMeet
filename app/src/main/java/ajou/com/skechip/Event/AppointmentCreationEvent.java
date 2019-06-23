package ajou.com.skechip.Event;

import java.util.List;

import ajou.com.skechip.Fragment.bean.Cell;

public class AppointmentCreationEvent {

    private List<Cell> appointmentTimeCells;

    public Long getFriendID() {
        return friendID;
    }

    private Long friendID;

    public AppointmentCreationEvent(List<Cell> appointmentTimeCells, Long friendID) {
        this.appointmentTimeCells = appointmentTimeCells;
        this.friendID = friendID;
    }

    public List<Cell> getAppointmentTimeCells() {
        return appointmentTimeCells;
    }

    public void setAppointmentTimeCells(List<Cell> appointmentTimeCells) {
        this.appointmentTimeCells = appointmentTimeCells;
    }
}
