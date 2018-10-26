package typecheck;

import syntaxtree.*;

import java.util.*;

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

    public static String extractTypeString(Type n) {
        Node typeNode = n.f0.choice;
        if (typeNode instanceof ArrayType) {
            ArrayType arrayNode = (ArrayType) typeNode;
            return arrayNode.f0.toString() + arrayNode.f1.toString() + arrayNode.f2.toString();
        } else if (typeNode instanceof BooleanType) {
            return ((BooleanType) typeNode).f0.toString();
        } else if (typeNode instanceof IntegerType) {
            return ((IntegerType) typeNode).f0.toString();
        } else if (typeNode instanceof Identifier) {
            return ((Identifier) typeNode).f0.toString();
        }
        return null;
    }

    public static List<String> extractFormalParameterListTypes(FormalParameterList n) {
        List<String> result = new ArrayList<>();
        result.add(extractTypeString(n.f0.f0));
        for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof FormalParameterRest) {
                result.add(extractTypeString(((FormalParameterRest) next).f1.f0));
            }
        }
        return result;
    }

    public static boolean subtype(String child, String parent) {
        return graph.hasSubtypeRelation(child, parent);
    }

    public static boolean distinct(List<String> l) {
        return new HashSet<String>(l).size() == l.size();
    }

    public static boolean acyclic(Set<LinkSetPair> s) {
        ClassGraph graph = new ClassGraph();
        for (LinkSetPair pair : s) {
            graph.addVertex(pair.getId());
            graph.addVertex(pair.getParentId());
            graph.addEdge(pair.getId(), pair.getParentId());
        }
        return graph.hasNoCycles();
    }

    public static Map<String, String> fields(String className) {
        return graph.getFields(className);
    }

    public static MethodSignature methodType(String className, String methodName) {
        return graph.getMethods(className).get(methodName);
    }

    public static boolean noOverrides(String child, String parent, String methodName) {
        return methodType(parent, methodName) == null || methodType(parent, methodName).equals(methodType(child, methodName));
    }
}
