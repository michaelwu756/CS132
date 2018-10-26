package typecheck;

import syntaxtree.*;
import visitor.DepthFirstVisitor;

import java.util.*;

import static typecheck.Helper.*;

public class TypeCheckVisitor extends DepthFirstVisitor {
    private boolean typeChecks = true;

    public void visit(Goal n) {
        List<String> classNames = new ArrayList<>();
        Set<LinkSetPair> fullLinkSet = new HashSet<>();
        classNames.add(className(n.f0));
        for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof TypeDeclaration) {
                Node classDeclarationNode = ((TypeDeclaration) next).f0.choice;
                if (classDeclarationNode instanceof ClassDeclaration) {
                    ClassDeclaration classDeclaration = (ClassDeclaration) classDeclarationNode;
                    classNames.add(className(classDeclaration));
                    fullLinkSet.addAll(linkSet(classDeclaration));
                } else if (classDeclarationNode instanceof ClassExtendsDeclaration) {
                    ClassExtendsDeclaration classExtendsDeclaration = (ClassExtendsDeclaration) classDeclarationNode;
                    classNames.add(className(classExtendsDeclaration));
                    fullLinkSet.addAll(linkSet(classExtendsDeclaration));
                }
            }
        }
        if (!distinct(classNames) || !acyclic(fullLinkSet)) {
            typeChecks = false;
        } else {
            super.visit(n);
        }
    }

    public void visit(NodeToken n) {
        System.err.print("\"" + n.toString() + "\" ");
    }

    public boolean correctlyTypeChecks() {
        return typeChecks;
    }
}
