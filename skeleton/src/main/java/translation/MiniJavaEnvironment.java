package translation;

import java.util.Map;

public class MiniJavaEnvironment {
    private String indentation;
    private String currentClass;
    private Map<String, String> localVarMap;

    public MiniJavaEnvironment(String indentation, String currentClass, Map<String, String> localVarMap) {
        this.indentation = indentation;
        this.currentClass = currentClass;
        this.localVarMap = localVarMap;
    }

    public String getIndentation() {
        return indentation;
    }

    public String getCurrentClass() {
        return currentClass;
    }

    public Map<String, String> getLocalVarMap() {
        return localVarMap;
    }
}
