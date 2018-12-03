package registerAllocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LivenessGraph {
    private LivenessNode[] nodes;

    public LivenessGraph(int n) {
        nodes = new LivenessNode[n];
        for (int i = 0; i < n; i++) {
            nodes[i] = new LivenessNode();
        }
    }

    public void addEdge(int current, int successor) {
        nodes[current].addSuccessor(nodes[successor]);
    }

    public void addDef(int current, String def) {
        nodes[current].addDef(def);
    }

    public void addUse(int current, String use) {
        nodes[current].addUse(use);
    }

    public Set<String> getOut(int current) {
        return nodes[current].getOut();
    }

    public Set<String> getDef(int current) {
        return nodes[current].getDef();
    }

    public void computeLiveIntervals() {
        for (LivenessNode n : nodes) {
            n.setIn(new HashSet<>(n.getUse()));
        }
        boolean updated = true;
        while (updated) {
            updated = false;
            for (int i = nodes.length - 1; i >= 0; i--) {
                Set<String> out = new HashSet<>();
                for (LivenessNode s : nodes[i].getSuccessors()) {
                    out.addAll(s.getIn());
                }
                Set<String> in = new HashSet<>(nodes[i].getUse());
                Set<String> outMinusDef = new HashSet<>(out);
                outMinusDef.removeAll(nodes[i].getDef());
                in.addAll(outMinusDef);
                if (!in.equals(nodes[i].getIn()) || !out.equals(nodes[i].getOut())) {
                    updated = true;
                    nodes[i].setIn(in);
                    nodes[i].setOut(out);
                }
            }
        }
    }

    public List<Integer> getLiveInterval(String var) {
        List<Integer> ret = new ArrayList<>();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getIn().contains(var)) {
                ret.add(i);
            }
        }
        return ret;
    }
}
