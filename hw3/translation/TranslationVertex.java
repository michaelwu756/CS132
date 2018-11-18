package translation;

import java.util.*;

public class TranslationVertex {
    private boolean visited = false;
    private Set<TranslationVertex> parentVertices = new HashSet<>();
    private List<String> fieldList = new ArrayList<>();
    private List<String> methodList = new ArrayList<>();
    private String className;

    public TranslationVertex(String className) {
        this.className = className;
    }

    public void addParentVertex(TranslationVertex v) {
        parentVertices.add(v);
    }

    public boolean unVisited() {
        return !visited;
    }

    public void addFieldList(List<String> list) {
        fieldList.addAll(list);
    }

    public void addMethodList(List<String> list) {
        methodList.addAll(list);
    }

    public List<String> getMethodList() {
        List<String> methods = new ArrayList<>();
        visited = true;
        if (parentVertices.isEmpty()) {
            for (String m : methodList) {
                methods.add(className + "." + m);
            }
        } else {
            for (TranslationVertex p : parentVertices) {
                if (p.unVisited()) {
                    methods = p.getMethodList();
                    Map<String, String> parentMethodClassMap = p.getMethodClassMap();
                    for (String m : methodList) {
                        String parentMethod = parentMethodClassMap.get(m);
                        if (parentMethod != null) {
                            for (int i = 0; i < methods.size(); i++) {
                                if (methods.get(i).equals(parentMethod)) {
                                    methods.remove(i);
                                    methods.add(i, className + "." + m);
                                    break;
                                }
                            }
                        } else {
                            methods.add(className + "." + m);
                        }
                    }
                }
            }
        }
        visited = false;
        return methods;
    }

    public Map<String, String> getMethodClassMap() {
        Map<String, String> methodClassMap = new HashMap<>();
        getMethodList().forEach(method -> methodClassMap.put(method.replaceAll("^.*\\.", ""), method));
        return methodClassMap;
    }

    public Map<String, Integer> getMethodOffsetMap() {
        Map<String, Integer> methodOffsetMap = new HashMap<>();
        List<String> methodList = getMethodList();
        for (int i = 0; i < methodList.size(); i++) {
            methodOffsetMap.put(methodList.get(i).replaceAll("^.*\\.", ""), i * 4);
        }
        return methodOffsetMap;
    }

    public List<String> getFieldList() {
        List<String> fields = new ArrayList<>();
        visited = true;
        for (TranslationVertex p : parentVertices) {
            if (p.unVisited()) {
                fields = p.getFieldList();
            }
        }
        fields.addAll(fieldList);
        visited = false;
        return fields;
    }

    public Map<String, Integer> getFieldOffsetMap() {
        Map<String, Integer> fieldOffsetMap = new HashMap<>();
        List<String> fieldList = getFieldList();
        for (int i = 0; i < fieldList.size(); i++) {
            fieldOffsetMap.put(fieldList.get(i), i * 4 + 4);
        }
        return fieldOffsetMap;
    }
}
