package translation;

import syntaxtree.*;
import typecheck.MethodSignature;
import typecheck.Pair;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static typecheck.Helper.*;

public class TranslationHelper {

    private static TranslationGraph graph = null;

    public static void init(Goal g) {
        TranslationVisitor translationVisitor = new TranslationVisitor();
        translationVisitor.visit(g);
        graph = translationVisitor.getGraph();
    }

    public static List<Pair<String, String>> extractVarList(NodeListOptional n) {
        List<Pair<String, String>> result = new ArrayList<>();
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof VarDeclaration) {
                VarDeclaration var = (VarDeclaration) next;
                result.add(new Pair<>(var.f1.f0.toString(), extractTypeString(var.f0)));
            }
        }
        return result;
    }

    public static List<Pair<String, MethodSignature>> extractMethodSignatureList(NodeListOptional n) {
        List<Pair<String, MethodSignature>> result = new ArrayList<>();
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) next;
                result.add(new Pair<>(methodName(methodDeclaration), new MethodSignature(
                        extractFormalParameterListTypes(methodDeclaration.f4), extractTypeString(methodDeclaration.f1))));
            }
        }
        return result;
    }

    public static StringBuilder getVirtualMemoryTables() {
        StringBuilder result = new StringBuilder();
        for (String className : graph.getDefinedClasses()) {
            if (!result.toString().isEmpty()) {
                result.append("\n");
            }
            result.append("const vmt_").append(className).append("\n");
            for (Pair<String, MethodSignature> method : graph.getMethodList(className)) {
                result.append("  :").append(method.getKey()).append("\n");
            }
        }
        return result;
    }

    public static int getMethodOffset(String className, String method) {
        return graph.getMethodOffsetMap(className).get(method);
    }

    public static int getFieldOffset(String className, String field) {
        return graph.getFieldOffsetMap(className).get(field);
    }

    public static int getClassSize(String className) {
        return graph.getFieldList(className).size() * 4 + 4;
    }

    public static boolean fieldExists(String className, String field) {
        for (Pair<String, String> p : graph.getFieldList(className)) {
            if (p.getKey().equals(field))
                return true;
        }
        return false;
    }
}
