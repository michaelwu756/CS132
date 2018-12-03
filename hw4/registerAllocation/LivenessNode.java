package registerAllocation;

import java.util.HashSet;
import java.util.Set;

public class LivenessNode {
    private Set<LivenessNode> successors = new HashSet<>();
    private Set<String> def = new HashSet<>();
    private Set<String> use = new HashSet<>();
    private Set<String> in;
    private Set<String> out;

    public void addSuccessor(LivenessNode n) {
        successors.add(n);
    }

    public void addDef(String d) {
        def.add(d);
    }

    public void addUse(String u) {
        use.add(u);
    }

    public Set<LivenessNode> getSuccessors() {
        return successors;
    }

    public Set<String> getDef() {
        return def;
    }

    public Set<String> getUse() {
        return use;
    }

    public Set<String> getIn() {
        return in;
    }

    public void setIn(Set<String> i) {
        in = i;
    }

    public Set<String> getOut() {
        return out;
    }

    public void setOut(Set<String> o) {
        out = o;
    }
}
