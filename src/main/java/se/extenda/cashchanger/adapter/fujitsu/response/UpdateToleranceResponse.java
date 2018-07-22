package se.extenda.cashchanger.adapter.fujitsu.response;

public class UpdateToleranceResponse extends GeneralFujitsuResponse {

    private long status;

    public void setStatus(long status) {
        this.status = status;
    }

    public long getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return super.toString() + " UpdateToleranceResponse [status=" + status + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        final UpdateToleranceResponse other = (UpdateToleranceResponse) obj;
        return other != null && status == ((UpdateToleranceResponse) obj).status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (status ^ (status >>> 32));
        return result;
    }

}
