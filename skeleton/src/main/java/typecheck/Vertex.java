package typecheck;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Vertex {
    private boolean visited = false;
    private Set<Vertex> childVertices = new HashSet<>();
    private Set<Vertex> parentVertices = new HashSet<>();
    private Map<String, String> typeAssociations = new HashMap<>();
    private Map<String, MethodSignature> methodAssociations = new HashMap<>();

    public boolean doesNotLeadToCycle() {
        visited = true;
        for (Vertex vertex : childVertices) {
            if (vertex.isVisited() || !vertex.doesNotLeadToCycle()) {
                return false;
            }
        }
        visited = false;
        return true;
    }

    public boolean canReach(Vertex v) {
        if (this == v) {
            return true;
        }
        visited = true;
        for (Vertex vertex : childVertices) {
            if (!vertex.isVisited() && vertex.canReach(v)) {
                return true;
            }
        }
        visited = false;
        return false;
    }

    public void addChildVertex(Vertex v) {
        childVertices.add(v);
    }

    public void addParentVertex(Vertex v) {
        parentVertices.add(v);
    }

    public boolean isVisited() {
        return visited;
    }

    public void addTypeAssociation(String id, String type) {
        typeAssociations.put(id, type);
    }

    public void addMethodAssociation(String id, MethodSignature signature) {
        methodAssociations.put(id, signature);
    }

    public Map<String, MethodSignature> getMethodAssociations() {
        Map<String, MethodSignature> methods = new HashMap<>();
        visited = true;
        for (Vertex p : parentVertices) {
            if (!p.isVisited()) {
                methods.putAll(p.getMethodAssociations());
            }
        }
        visited = false;
        methods.putAll(methodAssociations);
        return methods;
    }

    public Map<String, String> getTypeAssociations() {
        Map<String, String> types = new HashMap<>();
        visited = true;
        for (Vertex p : parentVertices) {
            if (!p.isVisited()) {
                types.putAll(p.getTypeAssociations());
            }
        }
        visited = false;
        types.putAll(typeAssociations);
        return types;
    }
}
