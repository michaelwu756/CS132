package translation;

import syntaxtree.Goal;

public class TranslationHelper {

    private static TranslationGraph graph = null;

    public static void init(Goal g) {
        TranslationVisitor translationVisitor = new TranslationVisitor();
        translationVisitor.visit(g);
        graph = translationVisitor.getGraph();
    }

    public static StringBuilder getVirtualMemoryTables() {
        StringBuilder result = new StringBuilder();
        for (String className : graph.getDefinedClasses()) {
            if (!result.toString().isEmpty()) {
                result.append("\n");
            }
            result.append("const vmt_").append(className).append("\n");
            for (String method : graph.getMethodList(className)) {
                result.append("  :").append(method).append("\n");
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
        for (String s : graph.getFieldList(className)) {
            if (s.equals(field)) {
                return true;
            }
        }
        return false;
    }
}
