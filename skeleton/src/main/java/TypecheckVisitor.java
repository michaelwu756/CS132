import syntaxtree.NodeToken;
import visitor.DepthFirstVisitor;

public class TypecheckVisitor extends DepthFirstVisitor {
    public void visit(NodeToken n) {
        System.out.print("\""+n.toString()+"\" ");
    }
}
