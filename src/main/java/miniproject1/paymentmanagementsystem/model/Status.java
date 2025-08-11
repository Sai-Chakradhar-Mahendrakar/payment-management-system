package miniproject1.paymentmanagementsystem.model;

public class Status {
    private int statusId;
    private String statusName;

    public Status() {}

    public Status(String statusName) {
        this.statusName = statusName;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    @Override
    public String toString() {
        return "Status{" +
                "statusId=" + statusId +
                ", statusName='" + statusName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Status status = (Status) obj;
        return statusId == status.statusId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(statusId);
    }
}
