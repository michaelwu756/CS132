package typecheck;

import syntaxtree.*;
import visitor.DepthFirstVisitor;

import java.util.Enumeration;

public class ClassVisitor extends DepthFirstVisitor {
    private ClassGraph graph = new ClassGraph();

    public void visit(Goal n) {
        super.visit(n);
        graph.computeTransitiveClosure();
    }

    public void visit(ClassDeclaration n) {
        String id = n.f1.f0.toString();
        graph.addVertex(id);
        extractVars(id, n.f3);
        super.visit(n);
    }

    public void visit(ClassExtendsDeclaration n) {
        String id = n.f1.f0.toString();
        String parentId = n.f3.f0.toString();
        graph.addVertex(id);
        graph.addVertex(parentId);
        graph.addEdge(id, parentId);
        extractVars(id, n.f5);
        super.visit(n);
    }

    public ClassGraph getGraph() {
        return graph;
    }

    private void extractVars(String className, NodeListOptional n) {
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof VarDeclaration) {
                VarDeclaration varDeclaration = (VarDeclaration) next;
                String varId = varDeclaration.f1.f0.toString();
                String type;
                Node typeNode = varDeclaration.f0.f0.choice;
                if (typeNode instanceof ArrayType) {
                    ArrayType arrayNode = (ArrayType) typeNode;
                    type = arrayNode.f0.toString() + arrayNode.f1.toString() + arrayNode.f2.toString();
                } else if (typeNode instanceof BooleanType) {
                    type = ((BooleanType) typeNode).f0.toString();
                } else if (typeNode instanceof IntegerType) {
                    type = ((IntegerType) typeNode).f0.toString();
                } else if (typeNode instanceof Identifier) {
                    type = ((Identifier) typeNode).f0.toString();
                } else {
                    return;
                }
                graph.getVertex(className).addTypeAssociation(varId, type);
            }
        }
    }
}
