package probe;

public interface StatusGetter {
    StatusType getStatus(String augUrl, int minVal, int maxVal);
}
