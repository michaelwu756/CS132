package typecheck;

public class LinkSetPair {
    private String id;
    private String parentId;

    public LinkSetPair(String id, String parentId) {
        this.id = id;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!LinkSetPair.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final LinkSetPair other = (LinkSetPair) obj;
        return this.id.equals(other.id) && this.parentId.equals(other.parentId);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.id.hashCode();
        hash = 53 * hash + this.parentId.hashCode();
        return hash;
    }
}
