package typecheck;

import javafx.util.Pair;
import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static typecheck.Helper.*;

public class TypeCheckVisitor extends GJDepthFirst<String, Pair<String, Map<String, String>>> {
    private boolean typeChecks = true;

    public String visit(Goal n, Pair<String, Map<String, String>> env) {
        if (!distinct(extractClassNames(n)) || !acyclic(extractFullLinkSet(n))) {
            typeChecks = false;
            return null;
        }
        return super.visit(n, env);
    }

    public String visit(MainClass n, Pair<String, Map<String, String>> env) {
        if (!distinct(extractVarIds(n.f14))) {
            typeChecks = false;
            return null;
        }
        return super.visit(n, new Pair<>(null, extractVarMap(n.f14)));
    }

    public String visit(ClassDeclaration n, Pair<String, Map<String, String>> env) {
        if (!distinct(extractVarIds(n.f3)) || !distinct(extractMethodNames(n.f4))) {
            typeChecks = false;
            return null;
        }
        return super.visit(n, new Pair<>(className(n), null));
    }

    public String visit(ClassExtendsDeclaration n, Pair<String, Map<String, String>> env) {
        List<String> methodNames = extractMethodNames(n.f6);
        if (!distinct(extractVarIds(n.f5)) || !distinct(methodNames) || !isDefinedClass(n.f3.f0.toString())) {
            typeChecks = false;
            return null;
        }
        for (String method : methodNames) {
            if (!noOverloading(className(n), n.f3.f0.toString(), method)) {
                typeChecks = false;
                return null;
            }
        }
        return super.visit(n, new Pair<>(className(n), null));
    }

    public String visit(MethodDeclaration n, Pair<String, Map<String, String>> env) {
        List<String> ids = extractFormalParameterListIds(n.f4);
        ids.addAll(extractVarIds(n.f7));
        Map<String, String> typeAssociations = fields(env.getKey());
        if (!distinct(ids) || typeAssociations == null) {
            typeChecks = false;
            return null;
        }
        typeAssociations.putAll(extractFormalParameterListMap(n.f4));
        typeAssociations.putAll(extractVarMap(n.f7));
        Pair<String, Map<String, String>> newEnv = new Pair<>(env.getKey(), typeAssociations);
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        n.f2.accept(this, env);
        n.f3.accept(this, env);
        n.f4.accept(this, env);
        n.f5.accept(this, env);
        n.f6.accept(this, env);
        n.f7.accept(this, env);
        n.f8.accept(this, newEnv);
        n.f9.accept(this, env);
        String returnType = n.f10.accept(this, newEnv);
        n.f11.accept(this, env);
        n.f12.accept(this, env);
        if (returnType == null || !returnType.equals(extractTypeString(n.f1))) {
            typeChecks = false;
        }
        return null;
    }

    public String visit(Type n, Pair<String, Map<String, String>> env) {
        if (!isDefinedType(extractTypeString(n))) {
            typeChecks = false;
        }
        return super.visit(n, env);
    }

    public String visit(Statement n, Pair<String, Map<String, String>> env) {
        return n.f0.accept(this, env);
    }

    public String visit(AssignmentStatement n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        String expressionType = n.f2.accept(this, env);
        n.f3.accept(this, env);
        String idType = env.getValue().get(n.f0.f0.toString());
        if (expressionType == null || idType == null || !subtype(expressionType, idType)) {
            typeChecks = false;
        }
        return null;
    }

    public String visit(ArrayAssignmentStatement n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        String indexType = n.f2.accept(this, env);
        n.f3.accept(this, env);
        n.f4.accept(this, env);
        String expressionType = n.f5.accept(this, env);
        n.f6.accept(this, env);
        String idType = env.getValue().get(n.f0.f0.toString());
        if (!"int".equals(expressionType) || !"int".equals(indexType) || !"int[]".equals(idType)) {
            typeChecks = false;
        }
        return null;
    }

    public String visit(IfStatement n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        String expressionType = n.f2.accept(this, env);
        n.f3.accept(this, env);
        n.f4.accept(this, env);
        n.f5.accept(this, env);
        n.f6.accept(this, env);
        if (!"boolean".equals(expressionType)) {
            typeChecks = false;
        }
        return null;
    }

    public String visit(WhileStatement n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        String expressionType = n.f2.accept(this, env);
        n.f3.accept(this, env);
        n.f4.accept(this, env);
        if (!"boolean".equals(expressionType)) {
            typeChecks = false;
        }
        return null;
    }

    public String visit(PrintStatement n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        String expressionType = n.f2.accept(this, env);
        n.f3.accept(this, env);
        n.f4.accept(this, env);
        if (!"int".equals(expressionType)) {
            typeChecks = false;
        }
        return null;
    }

    public String visit(Expression n, Pair<String, Map<String, String>> env) {
        return n.f0.accept(this, env);
    }

    public String visit(AndExpression n, Pair<String, Map<String, String>> env) {
        String firstType = n.f0.accept(this, env);
        n.f1.accept(this, env);
        String secondType = n.f2.accept(this, env);
        if (!"boolean".equals(firstType) || !"boolean".equals(secondType)) {
            typeChecks = false;
        }
        return "boolean";
    }

    public String visit(CompareExpression n, Pair<String, Map<String, String>> env) {
        String firstType = n.f0.accept(this, env);
        n.f1.accept(this, env);
        String secondType = n.f2.accept(this, env);
        if (!"int".equals(firstType) || !"int".equals(secondType)) {
            typeChecks = false;
        }
        return "boolean";
    }

    public String visit(PlusExpression n, Pair<String, Map<String, String>> env) {
        String firstType = n.f0.accept(this, env);
        n.f1.accept(this, env);
        String secondType = n.f2.accept(this, env);
        if (!"int".equals(firstType) || !"int".equals(secondType)) {
            typeChecks = false;
        }
        return "int";
    }

    public String visit(MinusExpression n, Pair<String, Map<String, String>> env) {
        String firstType = n.f0.accept(this, env);
        n.f1.accept(this, env);
        String secondType = n.f2.accept(this, env);
        if (!"int".equals(firstType) || !"int".equals(secondType)) {
            typeChecks = false;
        }
        return "int";
    }

    public String visit(TimesExpression n, Pair<String, Map<String, String>> env) {
        String firstType = n.f0.accept(this, env);
        n.f1.accept(this, env);
        String secondType = n.f2.accept(this, env);
        if (!"int".equals(firstType) || !"int".equals(secondType)) {
            typeChecks = false;
        }
        return "int";
    }

    public String visit(ArrayLookup n, Pair<String, Map<String, String>> env) {
        String arrType = n.f0.accept(this, env);
        n.f1.accept(this, env);
        String indexType = n.f2.accept(this, env);
        n.f3.accept(this, env);
        if (!"int[]".equals(arrType) || !"int".equals(indexType)) {
            typeChecks = false;
        }
        return "int";
    }

    public String visit(ArrayLength n, Pair<String, Map<String, String>> env) {
        String arrType = n.f0.accept(this, env);
        n.f1.accept(this, env);
        n.f2.accept(this, env);
        if (!"int[]".equals(arrType)) {
            typeChecks = false;
        }
        return "int";
    }

    public String visit(MessageSend n, Pair<String, Map<String, String>> env) {
        String callerType = n.f0.accept(this, env);
        n.f1.accept(this, env);
        n.f2.accept(this, env);
        n.f3.accept(this, env);
        List<String> expressionListTypes = getExpressionListTypes(n.f4, env);
        n.f5.accept(this, env);
        MethodSignature methodSignature = methodType(callerType, n.f2.f0.toString());
        if (callerType == null || methodSignature == null || expressionListTypes.size() != methodSignature.getParameterTypes().size()) {
            typeChecks = false;
            return null;
        }
        for (int i = 0; i < expressionListTypes.size(); i++) {
            if (!subtype(expressionListTypes.get(i), methodSignature.getParameterTypes().get(i))) {
                typeChecks = false;
                return null;
            }
        }
        return methodSignature.getReturnType();
    }

    public String visit(PrimaryExpression n, Pair<String, Map<String, String>> env) {
        return n.f0.accept(this, env);
    }

    public String visit(IntegerLiteral n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        return "int";
    }

    public String visit(TrueLiteral n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        return "boolean";
    }

    public String visit(FalseLiteral n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        return "boolean";
    }

    public String visit(Identifier n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        if (env.getValue() != null) {
            return env.getValue().get(n.f0.toString());
        }
        return null;
    }

    public String visit(ThisExpression n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        if (env.getKey() == null) {
            typeChecks = false;
            return null;
        }
        return env.getKey();
    }

    public String visit(ArrayAllocationExpression n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        n.f2.accept(this, env);
        String indexType = n.f3.accept(this, env);
        n.f4.accept(this, env);
        if (!"int".equals(indexType)) {
            typeChecks = false;
        }
        return "int[]";
    }

    public String visit(AllocationExpression n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        n.f2.accept(this, env);
        n.f3.accept(this, env);
        if (!isDefinedClass(n.f1.f0.toString())) {
            typeChecks = false;
            return null;
        }
        return n.f1.f0.toString();
    }

    public String visit(NotExpression n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        String expressionType = n.f1.accept(this, env);
        if (!"boolean".equals(expressionType)) {
            typeChecks = false;
        }
        return "boolean";
    }

    public String visit(BracketExpression n, Pair<String, Map<String, String>> env) {
        n.f0.accept(this, env);
        String expressionType = n.f1.accept(this, env);
        n.f2.accept(this, env);
        return expressionType;
    }

    private List<String> getExpressionListTypes(NodeOptional n, Pair<String, Map<String, String>> env) {
        List<String> result = new ArrayList<>();
        if (n.present() && n.node instanceof ExpressionList) {
            ExpressionList expressionList = (ExpressionList) n.node;
            result.add(expressionList.f0.accept(this, env));
            for (Enumeration<Node> e = expressionList.f1.elements(); e.hasMoreElements(); ) {
                Node next = e.nextElement();
                if (next instanceof ExpressionRest) {
                    result.add(((ExpressionRest) next).f1.accept(this, env));
                }
            }
        }
        return result;
    }

    public boolean correctlyTypeChecks() {
        return typeChecks;
    }
}
