package typecheck;

import syntaxtree.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Helper {

    private static ClassGraph graph = null;

    public static void init(Goal g) {
        ClassVisitor classVisitor = new ClassVisitor();
        classVisitor.visit(g);
        graph = classVisitor.getGraph();
    }

    public static String className(MainClass n) {
        return n.f1.f0.toString();
    }

    public static String className(ClassDeclaration n) {
        return n.f1.f0.toString();
    }

    public static String className(ClassExtendsDeclaration n) {
        return n.f1.f0.toString();
    }

    public static Set<LinkSetPair> linkSet(MainClass n) {
        return new HashSet<>();
    }

    public static Set<LinkSetPair> linkSet(ClassDeclaration n) {
        return new HashSet<>();
    }

    public static Set<LinkSetPair> linkSet(ClassExtendsDeclaration n) {
        Set<LinkSetPair> result = new HashSet<>();
        result.add(new LinkSetPair(n.f1.f0.toString(), n.f3.f0.toString()));
        return result;
    }

    public static String methodName(MethodDeclaration n) {
        return n.f2.f0.toString();
    }

    public static boolean distinct(List<String> l) {
        return new HashSet<String>(l).size() == l.size();
    }

    public static boolean acyclic(Set<LinkSetPair> s) {
        ClassGraph graph = new ClassGraph();
        for (LinkSetPair pair: s) {
            graph.addVertex(pair.getId());
            graph.addVertex(pair.getParentId());
            graph.addEdge(pair.getId(), pair.getParentId());
        }
        return graph.hasNoCycles();
    }

    public static Map<String, String> fields(String id) {
        return graph.getFields(id);
    }

    public static boolean subtype(String child, String parent) {
        return graph.hasSubtypeRelation(child, parent);
    }
}
