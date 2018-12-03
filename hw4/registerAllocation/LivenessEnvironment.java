package registerAllocation;

public class LivenessEnvironment {
    private int statementNum;
    private LivenessVisitor visitor;

    public LivenessEnvironment(int statementNum, LivenessVisitor visitor) {
        this.statementNum = statementNum;
        this.visitor = visitor;
    }

    public LivenessVisitor getLivenessVisitor() {
        return visitor;
    }

    public int getStatementNum() {
        return statementNum;
    }
}
