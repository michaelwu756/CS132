package typecheck;

import java.util.HashMap;
import java.util.Map;

public class ClassGraph {
    private Vertex root = new Vertex();
    private Map<String, Vertex> vertexMap = new HashMap<>();
    private Map<String, Map<String, Boolean>> transitiveClosure = null;

    public ClassGraph() {
        vertexMap.put("", root);
    }

    public void addVertex(String name) {
        if(!name.equals("") && vertexMap.get(name)==null) {
            vertexMap.put(name, new Vertex());
            root.addChildVertex(vertexMap.get(name));
        }
    }

    public void addEdge(String child, String parent) {
        if(!child.equals("") && !parent.equals("") && vertexMap.get(parent)!=null && vertexMap.get(child)!=null) {
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
                if(vertexMap.get(start).canReach(vertexMap.get(finish))) {
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

    public Map<String, String> getFields(String name) {
        return vertexMap.get(name).getTypeAssociations();
    }
}
