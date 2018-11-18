package translation;

import syntaxtree.*;
import typecheck.Pair;
import typecheck.TypeCheckVisitor;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static translation.TranslationHelper.*;
import static typecheck.Helper.*;

public class MiniJavaToVaporVisitor extends GJDepthFirst<String, MiniJavaEnvironment> {
    private StringBuilder translation = new StringBuilder();
    private int nullCounter = 1;
    private int whileCounter = 1;
    private int ifCounter = 1;
    private int boundsCounter = 1;
    private int andCounter = 1;
    private int tempCounter = 0;
    private boolean arrayAllocated = false;

    public MiniJavaToVaporVisitor() {

    }

    public String visit(Goal n, MiniJavaEnvironment env) {
        translation.append(TranslationHelper.getVirtualMemoryTables());
        n.f0.accept(this, env);
        n.f1.accept(this, env);
        if (arrayAllocated) {
            translation.append(
                    "\n" +
                            "func AllocArray(size)\n" +
                            "  bytes = MulS(size 4)\n" +
                            "  bytes = Add(bytes 4)\n" +
                            "  v = HeapAllocZ(bytes)\n" +
                            "  [v] = size\n" +
                            "  ret v\n");
        }
        return null;
    }

    public String visit(MainClass n, MiniJavaEnvironment env) {
        if (!translation.toString().isEmpty()) {
            translation.append("\n");
        }
        translation.append("func Main()\n");
        MiniJavaEnvironment newEnv = new MiniJavaEnvironment("  ", null, extractVarMap(n.f14));
        n.f15.accept(this, newEnv);
        translation.append("  ret\n");
        return null;
    }

    public String visit(ClassDeclaration n, MiniJavaEnvironment env) {
        MiniJavaEnvironment newEnv = new MiniJavaEnvironment("", className(n), null);
        n.f4.accept(this, newEnv);
        return null;
    }

    public String visit(ClassExtendsDeclaration n, MiniJavaEnvironment env) {
        MiniJavaEnvironment newEnv = new MiniJavaEnvironment("", className(n), null);
        n.f6.accept(this, newEnv);
        return null;
    }

    public String visit(MethodDeclaration n, MiniJavaEnvironment env) {
        Map<String, String> varMap = extractFormalParameterListMap(n.f4);
        varMap.putAll(extractVarMap(n.f7));
        MiniJavaEnvironment newEnv = new MiniJavaEnvironment("  ", env.getCurrentClass(), varMap);
        translation.append("\n");
        translation.append("func ").append(env.getCurrentClass()).append(".").append(methodName(n)).append("(this");
        for (String parameter : extractFormalParameterListIds(n.f4)) {
            translation.append(" ").append(parameter);
        }
        translation.append(")\n");
        tempCounter = 0;
        n.f8.accept(this, newEnv);
        String retVal = convertToTemp(n.f10.accept(this, newEnv), newEnv);
        translation.append("  ret ").append(retVal).append("\n");
        return null;
    }

    public String visit(AssignmentStatement n, MiniJavaEnvironment env) {
        String id = n.f0.accept(this, env);
        String expression = n.f2.accept(this, env);
        if (id.contains("[")) {
            expression = convertToTemp(expression, env);
        }
        translation.append(env.getIndentation()).append(id).append(" = ").append(expression).append("\n");
        return null;
    }

    public String visit(ArrayAssignmentStatement n, MiniJavaEnvironment env) {
        String id = convertToTemp(n.f0.accept(this, env), env);
        translation.append(env.getIndentation()).append("if ").append(id).append(" goto :null").append(String.valueOf(nullCounter)).append("\n")
                .append(env.getIndentation()).append("  Error(\"null pointer\")\n")
                .append(env.getIndentation()).append("null").append(String.valueOf(nullCounter)).append(":\n");
        nullCounter++;
        String index = convertToTemp(n.f2.accept(this, env), env);
        String temp = "t." + String.valueOf(tempCounter);
        tempCounter++;
        translation.append(env.getIndentation()).append(temp).append(" = [").append(id).append("]\n")
                .append(env.getIndentation()).append(temp).append(" = Lt(").append(index).append(" ").append(temp).append(")\n")
                .append(env.getIndentation()).append("if ").append(temp).append(" goto :bounds").append(String.valueOf(boundsCounter)).append("\n")
                .append(env.getIndentation()).append("  Error(\"array index out of bounds\")\n")
                .append(env.getIndentation()).append("bounds").append(String.valueOf(boundsCounter)).append(":\n")
                .append(env.getIndentation()).append(temp).append(" = MulS(").append(index).append(" 4)\n")
                .append(env.getIndentation()).append(temp).append(" = Add(").append(temp).append(" ").append(id).append(")\n");
        boundsCounter++;
        String expression = convertToTemp(n.f5.accept(this, env), env);
        translation.append(env.getIndentation()).append("[").append(temp).append("+4] = ").append(expression).append("\n");
        return null;
    }

    public String visit(IfStatement n, MiniJavaEnvironment env) {
        MiniJavaEnvironment newEnv = new MiniJavaEnvironment(env.getIndentation() + "  ", env.getCurrentClass(), env.getLocalVarMap());
        String ifNum = String.valueOf(ifCounter);
        ifCounter++;
        String condition = convertToTemp(n.f2.accept(this, env), env);
        translation.append(env.getIndentation()).append("if0 ").append(condition).append(" goto :if").append(ifNum).append("_else\n");
        n.f4.accept(this, newEnv);
        translation.append(env.getIndentation()).append("  goto :if").append(ifNum).append("_end\n")
                .append(env.getIndentation()).append("if").append(ifNum).append("_else:\n");
        n.f6.accept(this, newEnv);
        translation.append(env.getIndentation()).append("if").append(ifNum).append("_end:\n");
        return null;
    }

    public String visit(WhileStatement n, MiniJavaEnvironment env) {
        MiniJavaEnvironment newEnv = new MiniJavaEnvironment(env.getIndentation() + "  ", env.getCurrentClass(), env.getLocalVarMap());
        String whileNum = String.valueOf(whileCounter);
        whileCounter++;
        translation.append(env.getIndentation()).append("while").append(whileNum).append("_top:\n");
        String condition = convertToTemp(n.f2.accept(this, env), env);
        translation.append(env.getIndentation()).append("if0 ").append(condition).append(" goto :while").append(whileNum).append("_end\n");
        n.f4.accept(this, newEnv);
        translation.append(env.getIndentation()).append("  goto :while").append(whileNum).append("_top\n")
                .append(env.getIndentation()).append("while").append(whileNum).append("_end:\n");
        return null;
    }

    public String visit(PrintStatement n, MiniJavaEnvironment env) {
        String expression = convertToTemp(n.f2.accept(this, env), env);
        translation.append(env.getIndentation()).append("PrintIntS(").append(expression).append(")\n");
        return null;
    }

    public String visit(Expression n, MiniJavaEnvironment env) {
        return n.f0.accept(this, env);
    }

    public String visit(AndExpression n, MiniJavaEnvironment env) {
        MiniJavaEnvironment newEnv = new MiniJavaEnvironment(env.getIndentation() + "  ", env.getCurrentClass(), env.getLocalVarMap());
        String andNum = String.valueOf(andCounter);
        andCounter++;
        String temp = "t." + String.valueOf(tempCounter);
        tempCounter++;
        String first = convertToTemp(n.f0.accept(this, env), env);
        translation.append(env.getIndentation()).append("if0 ").append(first).append(" goto :ss").append(andNum).append("_else\n");
        String second = n.f2.accept(this, newEnv);
        translation.append(env.getIndentation()).append("  ").append(temp).append(" = ").append(second).append("\n")
                .append(env.getIndentation()).append("  goto :ss").append(andNum).append("_end\n")
                .append(env.getIndentation()).append("ss").append(andNum).append("_else:\n")
                .append(env.getIndentation()).append("  ").append(temp).append(" = 0\n")
                .append(env.getIndentation()).append("ss").append(andNum).append("_end:\n");
        return temp;
    }

    public String visit(CompareExpression n, MiniJavaEnvironment env) {
        String first = convertToTemp(n.f0.accept(this, env), env);
        String second = convertToTemp(n.f2.accept(this, env), env);
        return "LtS(" + first + " " + second + ")";
    }

    public String visit(PlusExpression n, MiniJavaEnvironment env) {
        String first = convertToTemp(n.f0.accept(this, env), env);
        String second = convertToTemp(n.f2.accept(this, env), env);
        return "Add(" + first + " " + second + ")";
    }

    public String visit(MinusExpression n, MiniJavaEnvironment env) {
        String first = convertToTemp(n.f0.accept(this, env), env);
        String second = convertToTemp(n.f2.accept(this, env), env);
        return "Sub(" + first + " " + second + ")";
    }

    public String visit(TimesExpression n, MiniJavaEnvironment env) {
        String first = convertToTemp(n.f0.accept(this, env), env);
        String second = convertToTemp(n.f2.accept(this, env), env);
        return "MulS(" + first + " " + second + ")";
    }

    public String visit(ArrayLookup n, MiniJavaEnvironment env) {
        String id = convertToTemp(n.f0.accept(this, env), env);
        translation.append(env.getIndentation()).append("if ").append(id).append(" goto :null").append(String.valueOf(nullCounter)).append("\n")
                .append(env.getIndentation()).append("  Error(\"null pointer\")\n")
                .append(env.getIndentation()).append("null").append(String.valueOf(nullCounter)).append(":\n");
        nullCounter++;
        String index = convertToTemp(n.f2.accept(this, env), env);
        String temp = "t." + String.valueOf(tempCounter);
        tempCounter++;
        translation.append(env.getIndentation()).append(temp).append(" = [").append(id).append("]\n")
                .append(env.getIndentation()).append(temp).append(" = Lt(").append(index).append(" ").append(temp).append(")\n")
                .append(env.getIndentation()).append("if ").append(temp).append(" goto :bounds").append(String.valueOf(boundsCounter)).append("\n")
                .append(env.getIndentation()).append("  Error(\"array index out of bounds\")\n")
                .append(env.getIndentation()).append("bounds").append(String.valueOf(boundsCounter)).append(":\n")
                .append(env.getIndentation()).append(temp).append(" = MulS(").append(index).append(" 4)\n")
                .append(env.getIndentation()).append(temp).append(" = Add(").append(temp).append(" ").append(id).append(")\n");
        boundsCounter++;
        return "[" + temp + "+4]";
    }

    public String visit(ArrayLength n, MiniJavaEnvironment env) {
        String id = convertToTemp(n.f0.accept(this, env), env);
        translation.append(env.getIndentation()).append("if ").append(id).append(" goto :null").append(String.valueOf(nullCounter)).append("\n")
                .append(env.getIndentation()).append("  Error(\"null pointer\")\n")
                .append(env.getIndentation()).append("null").append(String.valueOf(nullCounter)).append(":\n");
        nullCounter++;
        return "[" + id + "]";
    }

    public String visit(MessageSend n, MiniJavaEnvironment env) {
        String id = convertToTemp(n.f0.accept(this, env), env);
        String temp1 = "t." + String.valueOf(tempCounter);
        tempCounter++;
        if (!id.equals("this")) {
            translation.append(env.getIndentation()).append("if ").append(id).append(" goto :null").append(String.valueOf(nullCounter)).append("\n")
                    .append(env.getIndentation()).append("  Error(\"null pointer\")\n")
                    .append(env.getIndentation()).append("null").append(String.valueOf(nullCounter)).append(":\n");
            nullCounter++;
        }
        translation.append(env.getIndentation()).append(temp1).append(" = [").append(id).append("]\n");
        Map<String, String> typeMap = fields(env.getCurrentClass());
        if (typeMap != null) {
            typeMap.putAll(env.getLocalVarMap());
        } else {
            typeMap = env.getLocalVarMap();
        }
        Pair<String, Map<String, String>> typeCheckEnvironment = new Pair<>(env.getCurrentClass(), typeMap);
        String idType = n.f0.accept(new TypeCheckVisitor(), typeCheckEnvironment);
        String offset = String.valueOf(getMethodOffset(idType, n.f2.f0.toString()));
        translation.append(env.getIndentation()).append(temp1).append(" = [").append(temp1).append("+").append(offset).append("]\n");
        List<String> expressionListIds = getExpressionListIds(n.f4, env);
        StringBuilder ret = new StringBuilder();
        ret.append("call ").append(temp1).append("(").append(id);
        expressionListIds.forEach(expression -> ret.append(" ").append(expression));
        ret.append(")");
        return ret.toString();
    }

    public String visit(PrimaryExpression n, MiniJavaEnvironment env) {
        return n.f0.accept(this, env);
    }

    public String visit(IntegerLiteral n, MiniJavaEnvironment env) {
        return n.f0.toString();
    }

    public String visit(TrueLiteral n, MiniJavaEnvironment env) {
        return "1";
    }

    public String visit(FalseLiteral n, MiniJavaEnvironment env) {
        return "0";
    }

    public String visit(Identifier n, MiniJavaEnvironment env) {
        String variable = n.f0.toString();
        if (env == null || env.getLocalVarMap() == null || env.getCurrentClass() == null || env.getLocalVarMap().containsKey(variable))
            return variable;
        if (fieldExists(env.getCurrentClass(), variable)) {
            String offset = String.valueOf(getFieldOffset(env.getCurrentClass(), variable));
            return "[this+" + offset + "]";
        }
        return null;
    }

    public String visit(ThisExpression n, MiniJavaEnvironment env) {
        return "this";
    }

    public String visit(ArrayAllocationExpression n, MiniJavaEnvironment env) {
        String expression = convertToTemp(n.f3.accept(this, env), env);
        arrayAllocated = true;
        return "call :AllocArray(" + expression + ")";
    }

    public String visit(AllocationExpression n, MiniJavaEnvironment env) {
        String className = n.f1.f0.toString();
        String temp = "t." + String.valueOf(tempCounter);
        tempCounter++;
        translation.append(env.getIndentation()).append(temp).append(" = HeapAllocZ(").append(String.valueOf(getClassSize(className))).append(")\n")
                .append(env.getIndentation()).append("[").append(temp).append("] = :vmt_").append(className).append("\n");
        return temp;
    }

    public String visit(NotExpression n, MiniJavaEnvironment env) {
        String expression = convertToTemp(n.f1.accept(this, env), env);
        return "Sub(1 " + expression + ")";
    }

    public String visit(BracketExpression n, MiniJavaEnvironment env) {
        return n.f1.accept(this, env);
    }

    public String outputTranslation() {
        return translation.toString();
    }

    private List<String> getExpressionListIds(NodeOptional n, MiniJavaEnvironment env) {
        List<String> result = new ArrayList<>();
        if (n.present() && n.node instanceof ExpressionList) {
            ExpressionList expressionList = (ExpressionList) n.node;
            result.add(convertToTemp(expressionList.f0.accept(this, env), env));
            for (Enumeration<Node> e = expressionList.f1.elements(); e.hasMoreElements(); ) {
                Node next = e.nextElement();
                if (next instanceof ExpressionRest) {
                    result.add(convertToTemp(((ExpressionRest) next).f1.accept(this, env), env));
                }
            }
        }
        return result;
    }

    private String convertToTemp(String id, MiniJavaEnvironment env) {
        if (id.contains("[") || id.contains("Sub") || id.contains("Add") || id.contains("MulS") || id.contains("LtS") || id.contains("call")) {
            String temp = "t." + String.valueOf(tempCounter);
            tempCounter++;
            translation.append(env.getIndentation()).append(temp).append(" = ").append(id).append("\n");
            return temp;
        }
        return id;
    }
}
