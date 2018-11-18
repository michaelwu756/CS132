package translation;

import syntaxtree.ClassDeclaration;
import syntaxtree.ClassExtendsDeclaration;
import visitor.DepthFirstVisitor;

import static translation.TranslationHelper.extractMethodSignatureList;
import static translation.TranslationHelper.extractVarList;
import static typecheck.Helper.className;

public class TranslationVisitor extends DepthFirstVisitor {
    private TranslationGraph graph = new TranslationGraph();

    public void visit(ClassDeclaration n) {
        String id = className(n);
        graph.addVertex(id);
        graph.addDefinedClass(id);
        graph.getVertex(id).addFieldList(extractVarList(n.f3));
        graph.getVertex(id).addMethodList(extractMethodSignatureList(n.f4));
        super.visit(n);
    }

    public void visit(ClassExtendsDeclaration n) {
        String id = className(n);
        String parentId = n.f3.f0.toString();
        graph.addVertex(id);
        graph.addDefinedClass(id);
        graph.addVertex(parentId);
        graph.addEdge(id, parentId);
        graph.getVertex(id).addFieldList(extractVarList(n.f5));
        graph.getVertex(id).addMethodList(extractMethodSignatureList(n.f6));
        super.visit(n);
    }

    public TranslationGraph getGraph() {
        return graph;
    }
}
