package se.extenda.sco.adapter.fujitsu.response;

public class InterruptResponse extends GeneralFujitsuResponse {
    private long status;

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "InterruptResponse{" +
                "status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        InterruptResponse that = (InterruptResponse) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (status ^ (status >>> 32));
        return result;
    }
}
