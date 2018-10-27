package typecheck;

import javafx.util.Pair;
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

    public static Set<Pair<String, String>> linkSet(ClassExtendsDeclaration n) {
        Set<Pair<String, String>> result = new HashSet<>();
        result.add(new Pair<>(n.f1.f0.toString(), n.f3.f0.toString()));
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

    public static List<String> extractMethodNames(NodeListOptional n) {
        List<String> result = new ArrayList<>();
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof MethodDeclaration) {
                result.add(methodName((MethodDeclaration) next));
            }
        }
        return result;
    }

    public static Map<String, MethodSignature> extractMethodSignatureMap(NodeListOptional n) {
        Map<String, MethodSignature> result = new HashMap<>();
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) next;
                result.put(methodName(methodDeclaration), new MethodSignature(
                        extractFormalParameterListTypes(methodDeclaration.f4), extractTypeString(methodDeclaration.f1)));
            }
        }
        return result;
    }

    public static List<String> extractFormalParameterListTypes(NodeOptional n) {
        List<String> result = new ArrayList<>();
        if (n.present() && n.node instanceof FormalParameterList) {
            FormalParameterList parameterList = (FormalParameterList) n.node;
            result.add(extractTypeString(parameterList.f0.f0));
            for (Enumeration<Node> e = parameterList.f1.elements(); e.hasMoreElements(); ) {
                Node next = e.nextElement();
                if (next instanceof FormalParameterRest) {
                    result.add(extractTypeString(((FormalParameterRest) next).f1.f0));
                }
            }
        }
        return result;
    }

    public static Map<String, String> extractFormalParameterListMap(NodeOptional n) {
        Map<String, String> result = new HashMap<>();
        if (n.present() && n.node instanceof FormalParameterList) {
            FormalParameterList parameterList = (FormalParameterList) n.node;
            result.put(parameterList.f0.f1.f0.toString(), extractTypeString(parameterList.f0.f0));
            for (Enumeration<Node> e = parameterList.f1.elements(); e.hasMoreElements(); ) {
                Node next = e.nextElement();
                if (next instanceof FormalParameterRest) {
                    FormalParameterRest rest = (FormalParameterRest) next;
                    result.put(rest.f1.f1.f0.toString(), extractTypeString((rest.f1.f0)));
                }
            }
        }
        return result;
    }

    public static List<String> extractFormalParameterListIds(NodeOptional n) {
        List<String> result = new ArrayList<>();
        if (n.present() && n.node instanceof FormalParameterList) {
            FormalParameterList parameterList = (FormalParameterList) n.node;
            result.add(parameterList.f0.f1.f0.toString());
            for (Enumeration<Node> e = parameterList.f1.elements(); e.hasMoreElements(); ) {
                Node next = e.nextElement();
                if (next instanceof FormalParameterRest) {
                    result.add(((FormalParameterRest) next).f1.f1.f0.toString());
                }
            }
        }
        return result;
    }

    public static List<String> extractClassNames(Goal n) {
        List<String> result = new ArrayList<>();
        result.add(className(n.f0));
        for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof TypeDeclaration) {
                Node classDeclarationNode = ((TypeDeclaration) next).f0.choice;
                if (classDeclarationNode instanceof ClassDeclaration) {
                    result.add(className((ClassDeclaration) classDeclarationNode));
                } else if (classDeclarationNode instanceof ClassExtendsDeclaration) {
                    result.add(className((ClassExtendsDeclaration) classDeclarationNode));
                }
            }
        }
        return result;
    }

    public static Set<Pair<String, String>> extractFullLinkSet(Goal n) {
        Set<Pair<String, String>> result = new HashSet<>();
        for (Enumeration<Node> e = n.f1.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof TypeDeclaration) {
                Node classDeclarationNode = ((TypeDeclaration) next).f0.choice;
                if (classDeclarationNode instanceof ClassExtendsDeclaration) {
                    result.addAll(linkSet((ClassExtendsDeclaration) classDeclarationNode));
                }
            }
        }
        return result;
    }

    public static List<String> extractVarIds(NodeListOptional n) {
        List<String> result = new ArrayList<>();
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof VarDeclaration) {
                result.add(((VarDeclaration) next).f1.f0.toString());
            }
        }
        return result;
    }

    public static Map<String, String> extractVarMap(NodeListOptional n) {
        Map<String, String> result = new HashMap<>();
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Node next = e.nextElement();
            if (next instanceof VarDeclaration) {
                VarDeclaration var = (VarDeclaration) next;
                result.put(var.f1.f0.toString(), extractTypeString(var.f0));
            }
        }
        return result;
    }

    public static boolean subtype(String child, String parent) {
        return isPrimitiveType(child) && isPrimitiveType(parent) && child.equals(parent) ||
                isDefinedClass(child) && isDefinedClass(parent) && graph.hasSubtypeRelation(child, parent);
    }

    public static boolean distinct(List<String> l) {
        return new HashSet<String>(l).size() == l.size();
    }

    public static boolean acyclic(Set<Pair<String, String>> s) {
        ClassGraph graph = new ClassGraph();
        for (Pair<String, String> pair : s) {
            graph.addVertex(pair.getKey());
            graph.addVertex(pair.getValue());
            graph.addEdge(pair.getKey(), pair.getValue());
        }
        return graph.hasNoCycles();
    }

    public static Map<String, String> fields(String className) {
        return isDefinedClass(className) ? graph.getFields(className) : null;
    }

    public static MethodSignature methodType(String className, String methodName) {
        return isDefinedClass(className) ? graph.getMethods(className).get(methodName) : null;
    }

    public static boolean noOverloading(String child, String parent, String methodName) {
        return !isDefinedClass(child) ||
                !isDefinedClass(parent) ||
                !subtype(child, parent) ||
                methodType(parent, methodName) == null ||
                methodType(parent, methodName).equals(methodType(child, methodName));
    }

    public static boolean isDefinedClass(String className) {
        return className != null && graph.hasDefinedClass(className);
    }

    public static boolean isPrimitiveType(String type) {
        return "int".equals(type) || "int[]".equals(type) || "boolean".equals(type);
    }

    public static boolean isDefinedType(String type) {
        return isDefinedClass(type) || isPrimitiveType(type);
    }
}
