package typecheck;

import java.util.Collections;
import java.util.List;

public class MethodSignature {
    private final List<String> parameterTypes;
    private final String returnType;

    public MethodSignature(List<String> parameterTypes, String returnType) {
        this.parameterTypes = Collections.unmodifiableList(parameterTypes);
        this.returnType = returnType;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!MethodSignature.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final MethodSignature other = (MethodSignature) obj;
        return this.parameterTypes.equals(other.parameterTypes) && this.returnType.equals(other.returnType);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.parameterTypes.hashCode();
        hash = 53 * hash + this.returnType.hashCode();
        return hash;
    }
}
