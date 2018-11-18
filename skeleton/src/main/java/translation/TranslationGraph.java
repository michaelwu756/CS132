package translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationGraph {
    private Map<String, TranslationVertex> vertexMap = new HashMap<>();
    private List<String> definedClasses = new ArrayList<>();

    public void addDefinedClass(String className) {
        definedClasses.add(className);
    }

    public void addVertex(String name) {
        if (!"".equals(name) && vertexMap.get(name) == null) {
            vertexMap.put(name, new TranslationVertex(name));
        }
    }

    public void addEdge(String child, String parent) {
        if (!"".equals(child) && !"".equals(parent) && vertexMap.get(parent) != null && vertexMap.get(child) != null) {
            TranslationVertex vc = vertexMap.get(child);
            TranslationVertex vp = vertexMap.get(parent);
            vc.addParentVertex(vp);
        }
    }

    public List<String> getDefinedClasses() {
        return definedClasses;
    }

    public TranslationVertex getVertex(String name) {
        return vertexMap.get(name);
    }

    public List<String> getMethodList(String name) {
        return vertexMap.get(name).getMethodList();
    }

    public Map<String, Integer> getMethodOffsetMap(String name) {
        return vertexMap.get(name).getMethodOffsetMap();
    }

    public List<String> getFieldList(String name) {
        return vertexMap.get(name).getFieldList();
    }

    public Map<String, Integer> getFieldOffsetMap(String name) {
        return vertexMap.get(name).getFieldOffsetMap();
    }
}
