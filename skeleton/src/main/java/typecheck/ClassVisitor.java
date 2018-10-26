package typecheck;

import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import syntaxtree.Goal;
import visitor.DepthFirstVisitor;

import static typecheck.Helper.extractMethodSignatureMap;
import static typecheck.Helper.extractVarMap;

public class ClassVisitor extends DepthFirstVisitor {
    private ClassGraph graph = new ClassGraph();

    public void visit(Goal n) {
        super.visit(n);
        graph.computeTransitiveClosure();
    }

    public void visit(ClassDeclaration n) {
        String id = n.f1.f0.toString();
        graph.addVertex(id);
        graph.addDefinedClass(id);
        graph.getVertex(id).addTypeAssociation(extractVarMap(n.f3));
        graph.getVertex(id).addMethodAssociation(extractMethodSignatureMap(n.f4));
        super.visit(n);
    }

    public void visit(ClassExtendsDeclaration n) {
        String id = n.f1.f0.toString();
        String parentId = n.f3.f0.toString();
        graph.addVertex(id);
        graph.addDefinedClass(id);
        graph.addVertex(parentId);
        graph.addEdge(id, parentId);
        graph.getVertex(id).addTypeAssociation(extractVarMap(n.f5));
        graph.getVertex(id).addMethodAssociation(extractMethodSignatureMap(n.f6));
        super.visit(n);
    }

    public ClassGraph getGraph() {
        return graph;
    }
}
