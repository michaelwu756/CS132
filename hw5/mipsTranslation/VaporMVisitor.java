package mipsTranslation;

import cs132.vapor.ast.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntBinaryOperator;

public class VaporMVisitor extends VInstr.VisitorP<Integer, RuntimeException> {
    private StringBuilder translation = new StringBuilder();
    private Map<String, String> strMap = new LinkedHashMap<>();
    private int strCounter = 0;
    private boolean print = false;
    private boolean error = false;
    private boolean heapAlloc = false;

    public VaporMVisitor(VaporProgram program) {
        if (program.dataSegments.length > 0) {
            translation.append(".data\n");
        }
        for (VDataSegment dataSegment : program.dataSegments) {
            translation.append("\n")
                    .append(dataSegment.ident).append(":\n");
            for (VOperand.Static label : dataSegment.values) {
                if (label instanceof VLabelRef) {
                    translation.append("  ").append(((VLabelRef) label).ident).append("\n");
                }
            }
        }
        if (!translation.toString().isEmpty()) {
            translation.append("\n");
        }
        translation.append(".text\n")
                .append("\n")
                .append("  jal Main\n")
                .append("  li $v0 10\n")
                .append("  syscall\n");
        for (VFunction function : program.functions) {
            int stackSize = 8 + 4 * function.stack.local + 4 * function.stack.out;
            translation.append("\n")
                    .append(function.ident).append(":\n")
                    .append("  sw $fp -8($sp)\n")
                    .append("  move $fp $sp\n")
                    .append("  subiu $sp $sp ").append(stackSize).append("\n")
                    .append("  sw $ra -4($fp)\n");
            int i = 0;
            int j = 0;
            while (i < function.body.length || j < function.labels.length) {
                if (j >= function.labels.length || (i < function.body.length && function.body[i].sourcePos.line < function.labels[j].sourcePos.line)) {
                    function.body[i].accept(function.stack.local, this);
                    i++;
                } else {
                    translation.append(function.labels[j].ident).append(":\n");
                    j++;
                }
            }
            translation.append("  lw $ra -4($fp)\n")
                    .append("  lw $fp -8($fp)\n")
                    .append("  addiu $sp $sp ").append(stackSize).append("\n")
                    .append("  jr $ra\n");
        }
        if (print) {
            translation.append("\n")
                    .append("_print:\n")
                    .append("  li $v0 1   # syscall: print integer\n")
                    .append("  syscall\n")
                    .append("  la $a0 _newline\n")
                    .append("  li $v0 4   # syscall: print string\n")
                    .append("  syscall\n")
                    .append("  jr $ra\n");
        }
        if (error) {
            translation.append("\n")
                    .append("_error:\n")
                    .append("  li $v0 4   # syscall: print string\n")
                    .append("  syscall\n")
                    .append("  li $v0 10  # syscall: exit\n")
                    .append("  syscall\n");
        }
        if (heapAlloc) {
            translation.append("\n")
                    .append("_heapAlloc:\n")
                    .append("  li $v0 9   # syscall: sbrk\n")
                    .append("  syscall\n")
                    .append("  jr $ra\n");
        }
        if (print || !strMap.isEmpty()) {
            translation.append("\n")
                    .append(".data\n")
                    .append(".align 0\n");
            if (print) {
                translation.append("_newline: .asciiz \"\\n\"\n");
            }
            for (Map.Entry<String, String> entry : strMap.entrySet()) {
                translation.append(entry.getValue()).append(": .asciiz \"").append(entry.getKey()).append("\\n\"\n");
            }
        }
    }

    public void visit(Integer locals, VAssign var1) throws RuntimeException {
        if (var1.dest instanceof VVarRef.Register) {
            handleAssignment(((VVarRef.Register) var1.dest).ident, var1.source);
        }
    }

    public void visit(Integer locals, VCall var1) throws RuntimeException {
        if (var1.addr instanceof VAddr.Label) {
            translation.append("  jal ").append(((VAddr.Label<VFunction>) var1.addr).label.ident).append("\n");
        } else if (var1.addr instanceof VAddr.Var && ((VAddr.Var<VFunction>) var1.addr).var instanceof VVarRef.Register) {
            translation.append("  jalr $").append(((VVarRef.Register) ((VAddr.Var<VFunction>) var1.addr).var).ident).append("\n");
        }

    }

    public void visit(Integer locals, VBuiltIn var1) throws RuntimeException {
        VVarRef.Register dest = null;
        if (var1.dest instanceof VVarRef.Register) {
            dest = (VVarRef.Register) var1.dest;
        }
        switch (var1.op.name) {
            case "Add":
                handleBuiltinBinaryOp(dest, var1.args, "addu", "addiu", true, (a, b) -> a + b);
                break;
            case "Sub":
                handleBuiltinBinaryOp(dest, var1.args, "subu", "subiu", false, (a, b) -> a - b);
                break;
            case "MulS":
                handleBuiltinBinaryOp(dest, var1.args, "mul", "mul", true, (a, b) -> a * b);
                break;
            case "Eq":
                handleBuiltinBinaryOp(dest, var1.args, "seq", "seq", true, (a, b) -> a == b ? 1 : 0);
                break;
            case "Lt":
                handleBuiltinBinaryOp(dest, var1.args, "sltu", "sltiu", false, (a, b) -> {
                    long first = a < 0 ? a + 0x100000000L : a;
                    long second = b < 0 ? b + 0x100000000L : b;
                    return first < second ? 1 : 0;
                });
                break;
            case "LtS":
                handleBuiltinBinaryOp(dest, var1.args, "slt", "slti", false, (a, b) -> a < b ? 1 : 0);
                break;
            case "Error":
                if (var1.args.length == 1 && var1.args[0] instanceof VLitStr) {
                    error = true;
                    translation.append("  la $a0 ").append(strMap.computeIfAbsent(((VLitStr) var1.args[0]).value, k -> "_str" + (strCounter++))).append("\n")
                            .append("  j _error\n");
                }
                break;
            case "PrintIntS":
                if (var1.args.length == 1) {
                    print = true;
                    handleAssignment("a0", var1.args[0]);
                    translation.append("  jal _print\n");
                }
                break;
            case "HeapAllocZ":
                if (dest != null && var1.args.length == 1) {
                    heapAlloc = true;
                    handleAssignment("a0", var1.args[0]);
                    translation.append("  jal _heapAlloc\n")
                            .append("  move $").append(dest.ident).append(" $v0\n");
                }
                break;
        }
    }

    public void visit(Integer locals, VMemWrite var1) throws RuntimeException {
        String source = getRegister(var1.source);
        String dest = handleMemRef(locals, var1.dest);
        translation.append("  sw $").append(source).append(" ").append(dest).append("\n");
    }

    public void visit(Integer locals, VMemRead var1) throws RuntimeException {
        String source = handleMemRef(locals, var1.source);
        if (var1.dest instanceof VVarRef.Register) {
            translation.append("  lw $").append(((VVarRef.Register) var1.dest).ident).append(" ").append(source).append("\n");
        }
    }

    public void visit(Integer locals, VBranch var1) throws RuntimeException {
        String src = getRegister(var1.value);
        String op = var1.positive ? "bnez" : "beqz";
        translation.append("  ").append(op).append(" $").append(src).append(" ").append(var1.target.ident).append("\n");
    }

    public void visit(Integer locals, VGoto var1) throws RuntimeException {
        if (var1.target instanceof VAddr.Var && ((VAddr.Var<VCodeLabel>) var1.target).var instanceof VVarRef.Register) {
            translation.append("  jr $").append(((VVarRef.Register) ((VAddr.Var<VCodeLabel>) var1.target).var).ident).append("\n");
        } else if (var1.target instanceof VAddr.Label) {
            translation.append("  j ").append(((VAddr.Label<VCodeLabel>) var1.target).label.ident).append("\n");
        }
    }

    public void visit(Integer locals, VReturn var1) throws RuntimeException {
    }

    public String outputTranslation() {
        return translation.toString();
    }

    private void handleBuiltinBinaryOp(VVarRef.Register dest, VOperand[] args, String opreg, String opimm, boolean commutative, IntBinaryOperator f) {
        if (dest != null && args.length == 2) {
            VOperand first = args[0];
            VOperand second = args[1];
            if (first instanceof VVarRef.Register && second instanceof VVarRef.Register) {
                translation.append("  ").append(opreg).append(" $").append(dest.ident)
                        .append(" $").append(((VVarRef.Register) first).ident)
                        .append(" $").append(((VVarRef.Register) second).ident).append("\n");
            } else if (first instanceof VVarRef.Register && second instanceof VLitInt) {
                translation.append("  ").append(opimm).append(" $").append(dest.ident)
                        .append(" $").append(((VVarRef.Register) first).ident)
                        .append(" ").append(((VLitInt) second).value).append("\n");
            } else if (first instanceof VLitInt && second instanceof VVarRef.Register) {
                if (commutative) {
                    translation.append("  ").append(opimm).append(" $").append(dest.ident)
                            .append(" $").append(((VVarRef.Register) second).ident)
                            .append(" ").append(((VLitInt) first).value).append("\n");
                } else {
                    translation.append("  li $t9 ").append(((VLitInt) first).value).append("\n");
                    translation.append("  ").append(opreg).append(" $").append(dest.ident).append(" $t9 $").append(((VVarRef.Register) second).ident).append("\n");
                }
            } else if (first instanceof VLitInt && second instanceof VLitInt) {
                translation.append("  li $").append(dest.ident).append(" ").append(f.applyAsInt(((VLitInt) first).value, ((VLitInt) second).value)).append("\n");
            }
        }
    }

    private String getRegister(VOperand src) {
        if (src instanceof VVarRef.Register) {
            return ((VVarRef.Register) src).ident;
        } else if (src instanceof VLitInt && ((VLitInt) src).value == 0) {
            return "0";
        }
        handleAssignment("t9", src);
        return "t9";
    }

    private void handleAssignment(String dest, VOperand src) {
        if (src instanceof VVarRef.Register) {
            translation.append("  move $").append(dest).append(" $").append(((VVarRef.Register) src).ident).append("\n");
        } else if (src instanceof VLitInt) {
            translation.append("  li $").append(dest).append(" ").append(((VLitInt) src).value).append("\n");
        } else if (src instanceof VLabelRef) {
            translation.append("  la $").append(dest).append(" ").append(((VLabelRef) src).ident).append("\n");
        }
    }

    private String handleMemRef(Integer locals, VMemRef ref) {
        if (ref instanceof VMemRef.Stack) {
            if (((VMemRef.Stack) ref).region == VMemRef.Stack.Region.In) {
                return ((VMemRef.Stack) ref).index * 4 + "($fp)";
            } else if (((VMemRef.Stack) ref).region == VMemRef.Stack.Region.Out) {
                return (locals + ((VMemRef.Stack) ref).index) * 4 + "($sp)";
            } else if (((VMemRef.Stack) ref).region == VMemRef.Stack.Region.Local) {
                return ((VMemRef.Stack) ref).index * 4 + "($sp)";
            }
        } else if (ref instanceof VMemRef.Global) {
            VMemRef.Global global = (VMemRef.Global) ref;
            if (global.base instanceof VAddr.Var && ((VAddr.Var<VDataSegment>) global.base).var instanceof VVarRef.Register) {
                return global.byteOffset + "($" + ((VVarRef.Register) ((VAddr.Var<VDataSegment>) global.base).var).ident + ")";
            } else if (global.base instanceof VAddr.Label) {
                return global.byteOffset + "(" + ((VAddr.Label<VDataSegment>) global.base).label.ident;
            }
        }
        return null;
    }
}