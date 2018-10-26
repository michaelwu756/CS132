package typecheck;

import syntaxtree.*;
import visitor.DepthFirstVisitor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static typecheck.Helper.extractFormalParameterListTypes;
import static typecheck.Helper.extractTypeString;

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
        extractVars(id, n.f3);
        extractMethods(id, n.f4);
        super.visit(n);
    }

    public void visit(ClassExtendsDeclaration n) {
        String id = n.f1.f0.toString();
        String parentId = n.f3.f0.toString();
        graph.addVertex(id);
        graph.addDefinedClass(id);
        graph.addVertex(parentId);
        graph.addEdge(id, parentId);
        extractVars(id, n.f5);
        extractMethods(id, n.f6);
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
                String type = extractTypeString(varDeclaration.f0);
                if (type == null)
                    return;
                graph.getVertex(className).addTypeAssociation(varId, type);
            }
        }
    }

    private void extractMethods(String classname, NodeListOptional n) {
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) next;
                String returnType = extractTypeString(methodDeclaration.f1);
                String methodId = methodDeclaration.f2.f0.toString();
                List<String> parameterTypeList = new ArrayList<>();
                if (methodDeclaration.f4.present()) {
                    parameterTypeList = extractFormalParameterListTypes((FormalParameterList) methodDeclaration.f4.node);
                }
                graph.getVertex(classname).addMethodAssociation(methodId, new MethodSignature(parameterTypeList, returnType));
            }
        }
    }
}
