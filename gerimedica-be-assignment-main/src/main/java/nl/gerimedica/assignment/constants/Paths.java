package nl.gerimedica.assignment.constants;

public final class Paths {

    private Paths() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ROUTE_PATH = "/api";
    public static final String PAGED_APPOINTMENTS = "/appointments/paged";
    public static final String  BULK_APPOINTMENTS = "/bulk-appointments";
    public static final String APPOINTMENT_BY_REASON = "/appointments-by-reason";
    public static final String GET_ALL_APPOINTMENTS = "/getAllAppointments";
    public static final String DELETE_APPOINTMENTS = "/delete-appointments";
    public static final String LATEST_APPOINTMENTS = "/appointments/latest";

}