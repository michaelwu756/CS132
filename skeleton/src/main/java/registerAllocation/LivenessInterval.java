package registerAllocation;

public class LivenessInterval {
    private String name;
    private int start;
    private int end;

    public LivenessInterval(String n, int s, int e) {
        name = n;
        start = s;
        end = e;
    }

    public boolean overlaps(LivenessInterval other) {
        return other.getStart() <= start && start < other.getEnd() || start <= other.getStart() && other.getStart() < end;
    }

    public boolean assignableAt(int i) {
        return start <= i + 1 && i + 1 < end;
    }

    public String getName() {
        return name;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
