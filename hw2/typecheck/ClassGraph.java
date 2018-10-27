package typecheck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassGraph {
    private Vertex root = new Vertex();
    private Map<String, Vertex> vertexMap = new HashMap<>();
    private Map<String, Map<String, Boolean>> transitiveClosure = null;
    private List<String> definedClasses = new ArrayList<>();

    public ClassGraph() {
        vertexMap.put("", root);
    }

    public void addDefinedClass(String className) {
        definedClasses.add(className);
    }

    public void addVertex(String name) {
        if (!"".equals(name) && vertexMap.get(name) == null) {
            vertexMap.put(name, new Vertex());
            root.addChildVertex(vertexMap.get(name));
        }
    }

    public void addEdge(String child, String parent) {
        if (!"".equals(child) && !"".equals(parent) && vertexMap.get(parent) != null && vertexMap.get(child) != null) {
            Vertex vc = vertexMap.get(child);
            Vertex vp = vertexMap.get(parent);
            vp.addChildVertex(vc);
            vc.addParentVertex(vp);
        }
    }

    public Vertex getVertex(String name) {
        return vertexMap.get(name);
    }

    public boolean hasNoCycles() {
        return root.doesNotLeadToCycle();
    }

    public void computeTransitiveClosure() {
        transitiveClosure = new HashMap<>();
        vertexMap.keySet().forEach(start -> {
            Map<String, Boolean> reachableNodes = new HashMap<>();
            vertexMap.keySet().forEach(finish -> {
                if (vertexMap.get(start).canReach(vertexMap.get(finish))) {
                    reachableNodes.put(finish, true);
                } else {
                    reachableNodes.put(finish, false);
                }
            });
            transitiveClosure.put(start, reachableNodes);
        });
    }

    public boolean hasSubtypeRelation(String child, String parent) {
        return transitiveClosure != null ? transitiveClosure.get(parent).get(child) : false;
    }

    public boolean hasDefinedClass(String className) {
        return definedClasses.contains(className);
    }

    public Map<String, String> getFields(String name) {
        return vertexMap.get(name).getTypeAssociations();
    }

    public Map<String, MethodSignature> getMethods(String name) {
        return vertexMap.get(name).getMethodAssociations();
    }
}
