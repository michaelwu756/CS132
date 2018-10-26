package typecheck;

import javafx.util.Pair;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.*;

import static typecheck.Helper.*;

public class TypeCheckVisitor extends GJDepthFirst<String, Pair<String, Map<String, String>>> {
    private boolean typeChecks = true;

    public String visit(Goal n, Pair<String, Map<String, String>> env) {
        List<String> classNames = new ArrayList<>();
        Set<Pair<String, String>> fullLinkSet = new HashSet<>();
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
            super.visit(n, env);
        }
        return null;
    }

    public String visit(MainClass n, Pair<String, Map<String, String>> env) {
        return super.visit(n, env);
    }

    public String visit(ClassDeclaration n, Pair<String, Map<String, String>> env) {
        return super.visit(n, env);
    }

    public String visit(ClassExtendsDeclaration n, Pair<String, Map<String, String>> env) {
        return super.visit(n, env);
    }

    public String visit(NodeToken n, Pair<String, Map<String, String>> env) {
        System.err.print("\"" + n.toString() + "\" ");
        return super.visit(n, env);
    }

    public String visit(Type n, Pair<String, Map<String, String>> env) {
        if (!isDefinedType(extractTypeString(n))) {
            typeChecks = false;
        }
        return super.visit(n, env);
    }

    public boolean correctlyTypeChecks() {
        return typeChecks;
    }
}
