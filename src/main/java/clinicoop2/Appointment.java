package clinicoop2;

import java.sql.Timestamp;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private String appointmentType;
    private Timestamp appointmentDate;
    private boolean checkedIn;
    private boolean paymentStatus;
    private String diagnosis;
    private boolean vaccinationStatus;


    public Appointment(int appointmentId, int patientId, int doctorId, String appointmentType, Timestamp appointmentDate,
                       boolean checkedIn, boolean paymentStatus, String diagnosis, boolean vaccinationStatus) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentType = appointmentType;
        this.appointmentDate = appointmentDate;
        this.checkedIn = checkedIn;
        this.paymentStatus = paymentStatus;
        this.diagnosis = diagnosis;
        this.vaccinationStatus = vaccinationStatus;
    }


}
