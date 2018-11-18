package translation;

import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import visitor.DepthFirstVisitor;

import static typecheck.Helper.*;

public class TranslationVisitor extends DepthFirstVisitor {
    private TranslationGraph graph = new TranslationGraph();

    public void visit(ClassDeclaration n) {
        String id = className(n);
        graph.addVertex(id);
        graph.addDefinedClass(id);
        graph.getVertex(id).addFieldList(extractVarIds(n.f3));
        graph.getVertex(id).addMethodList(extractMethodNames(n.f4));
        super.visit(n);
    }

    public void visit(ClassExtendsDeclaration n) {
        String id = className(n);
        String parentId = n.f3.f0.toString();
        graph.addVertex(id);
        graph.addDefinedClass(id);
        graph.addVertex(parentId);
        graph.addEdge(id, parentId);
        graph.getVertex(id).addFieldList(extractVarIds(n.f5));
        graph.getVertex(id).addMethodList(extractMethodNames(n.f6));
        super.visit(n);
    }

    public TranslationGraph getGraph() {
        return graph;
    }
}
