package typecheck;

import javafx.util.Pair;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static typecheck.Helper.*;

public class TypeCheckVisitor extends GJDepthFirst<String, Pair<String, Map<String, String>>> {
    private boolean typeChecks = true;

    public String visit(Goal n, Pair<String, Map<String, String>> env) {
        List<String> classNames = extractClassNames(n);
        Set<Pair<String, String>> fullLinkSet = extractFullLinkSet(n);
        if (!distinct(classNames) || !acyclic(fullLinkSet)) {
            typeChecks = false;
            return null;
        }
        return super.visit(n, env);
    }

    public String visit(MainClass n, Pair<String, Map<String, String>> env) {
        List<String> varNames = extractVarIds(n.f14);
        Map<String, String> varMap = extractVarMap(n.f14);
        if (!distinct(varNames)) {
            typeChecks = false;
            return null;
        }
        return super.visit(n, new Pair<>(null, varMap));
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
