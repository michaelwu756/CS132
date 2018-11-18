package translation;

import typecheck.MethodSignature;
import typecheck.Pair;

import java.util.*;

public class TranslationVertex {
    private boolean visited = false;
    private Set<TranslationVertex> parentVertices = new HashSet<>();
    private List<Pair<String, String>> fieldList = new ArrayList<>();
    private List<Pair<String, MethodSignature>> methodList = new ArrayList<>();
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

    public void addFieldList(List<Pair<String, String>> list) {
        fieldList.addAll(list);
    }

    public void addMethodList(List<Pair<String, MethodSignature>> list) {
        methodList.addAll(list);
    }

    public List<Pair<String, MethodSignature>> getMethodList() {
        List<Pair<String, MethodSignature>> methods = new ArrayList<>();
        visited = true;
        if (parentVertices.isEmpty()) {
            for (Pair<String, MethodSignature> m : methodList) {
                methods.add(new Pair<>(className + "." + m.getKey(), m.getValue()));
            }
        } else {
            for (TranslationVertex p : parentVertices) {
                if (p.unVisited()) {
                    methods = p.getMethodList();
                    Map<String, Pair<String, MethodSignature>> parentMethodClassMap = p.getMethodClassMap();
                    for (Pair<String, MethodSignature> m : methodList) {
                        Pair<String, MethodSignature> parentMethod = parentMethodClassMap.get(m.getKey());
                        if (parentMethod != null) {
                            for (int i = 0; i < methods.size(); i++) {
                                if (methods.get(i).equals(parentMethod)) {
                                    methods.remove(i);
                                    methods.add(i, new Pair<>(className + "." + m.getKey(), m.getValue()));
                                    break;
                                }
                            }
                        } else {
                            methods.add(new Pair<>(className + "." + m.getKey(), m.getValue()));
                        }
                    }
                }
            }
        }
        visited = false;
        return methods;
    }

    public Map<String, Pair<String, MethodSignature>> getMethodClassMap() {
        Map<String, Pair<String, MethodSignature>> methodClassMap = new HashMap<>();
        getMethodList().forEach(pair -> methodClassMap.put(pair.getKey().replaceAll("^.*\\.", ""), pair));
        return methodClassMap;
    }

    public Map<String, Integer> getMethodOffsetMap() {
        Map<String, Integer> methodOffsetMap = new HashMap<>();
        List<Pair<String, MethodSignature>> methodList = getMethodList();
        for (int i = 0; i < methodList.size(); i++) {
            methodOffsetMap.put(methodList.get(i).getKey().replaceAll("^.*\\.", ""), i * 4);
        }
        return methodOffsetMap;
    }

    public List<Pair<String, String>> getFieldList() {
        List<Pair<String, String>> fields = new ArrayList<>();
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
        List<Pair<String, String>> fieldList = getFieldList();
        for (int i = 0; i < fieldList.size(); i++) {
            fieldOffsetMap.put(fieldList.get(i).getKey(), i * 4 + 4);
        }
        return fieldOffsetMap;
    }
}
