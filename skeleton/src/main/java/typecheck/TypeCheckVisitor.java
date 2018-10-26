package typecheck;

import syntaxtree.NodeToken;
import visitor.DepthFirstVisitor;

public class TypeCheckVisitor extends DepthFirstVisitor {
    private boolean typeChecks = true;

    public void visit(NodeToken n) {
        System.out.print("\"" + n.toString() + "\" ");
    }

    public boolean correctlyTypeChecks() {
        return typeChecks;
    }
}
