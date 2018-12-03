package registerAllocation;

import cs132.vapor.ast.*;

public class VaporVisitor extends VInstr.VisitorP<LivenessEnvironment, RuntimeException> {
    private StringBuilder translation = new StringBuilder();

    public VaporVisitor(VaporProgram program) {
        for (VDataSegment dataSegment : program.dataSegments) {
            if (!translation.toString().isEmpty()) {
                translation.append("\n");
            }
            if (dataSegment.mutable) {
                translation.append("var ");
            } else {
                translation.append("const ");
            }
            translation.append(dataSegment.ident).append("\n");
            for (VOperand.Static label : dataSegment.values) {
                translation.append("  ").append(label.toString()).append("\n");
            }
        }
        for (VFunction function : program.functions) {
            LivenessVisitor livenessVisitor = new LivenessVisitor(function);
            if (!translation.toString().isEmpty()) {
                translation.append("\n");
            }
            translation.append("func ").append(function.ident).append(" ").append(livenessVisitor.getStacks()).append("\n");
            for (int i = 0; i < livenessVisitor.getCalleeSavedCounter(); i++) {
                translation.append("  local[").append(livenessVisitor.getSpilledCounter() + i).append("] = $s").append(i).append("\n");
            }
            for (int i = 0; i < function.params.length; i++) {
                String assignment = livenessVisitor.getAssignment(function.params[i].ident);
                if (assignment != null) {
                    if (i < 4) {
                        translation.append("  ").append(assignment).append(" = $a").append(i).append("\n");
                    } else {
                        if (assignment.startsWith("local")) {
                            translation.append("  $v1 = in[").append(i - 4).append("]\n")
                                    .append("  ").append(assignment).append(" = $v1\n");
                        } else {
                            translation.append("  ").append(assignment).append(" = in[").append(i - 4).append("]\n");
                        }
                    }
                }
            }
            int i = 0;
            int j = 0;
            while (i < function.body.length || j < function.labels.length) {
                if (j >= function.labels.length || (i < function.body.length && function.body[i].sourcePos.line < function.labels[j].sourcePos.line)) {
                    function.body[i].accept(new LivenessEnvironment(i, livenessVisitor), this);
                    i++;
                } else {
                    translation.append(function.labels[j].ident).append(":\n");
                    j++;
                }
            }
            for (i = 0; i < livenessVisitor.getCalleeSavedCounter(); i++) {
                translation.append("  $s").append(i).append(" = local[").append(livenessVisitor.getSpilledCounter() + i).append("]\n");
            }
            translation.append("  ret\n");
        }
    }

    public void visit(LivenessEnvironment env, VAssign var1) throws RuntimeException {
        String sourceName;
        if (var1.source instanceof VVarRef.Local) {
            sourceName = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.source).ident);
        } else {
            sourceName = var1.source.toString();
        }
        if (var1.dest instanceof VVarRef.Local) {
            String varName = ((VVarRef.Local) var1.dest).ident;
            String destName = env.getLivenessVisitor().getAssignment(varName);
            if (destName != null && env.getLivenessVisitor().getLivenessInterval(varName).assignableAt(env.getStatementNum())) {
                if (sourceName.startsWith("local") && destName.startsWith("local")) {
                    translation.append("  $v1 = ").append(sourceName).append("\n")
                            .append("  ").append(destName).append(" = $v1\n");
                } else {
                    translation.append("  ").append(destName).append(" = ").append(sourceName).append("\n");
                }
            }
        }
    }

    public void visit(LivenessEnvironment env, VCall var1) throws RuntimeException {
        for (int i = 0; i < var1.args.length; i++) {
            String assignment;
            if (var1.args[i] instanceof VVarRef.Local) {
                assignment = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.args[i]).ident);
            } else {
                assignment = var1.args[i].toString();
            }
            if (i < 4) {
                translation.append("  $a").append(i).append(" = ").append(assignment).append("\n");
            } else {
                if (assignment.startsWith("local")) {
                    translation.append("  $v1 = ").append(assignment).append("\n")
                            .append("  out[").append(i - 4).append("] = $v1\n");
                } else {
                    translation.append("  out[").append(i - 4).append("] = ").append(assignment).append("\n");
                }
            }
        }
        String calledLocation;
        if (var1.addr instanceof VAddr.Var && ((VAddr.Var<VFunction>) var1.addr).var instanceof VVarRef.Local) {
            calledLocation = env.getLivenessVisitor().getAssignment(((VVarRef.Local) ((VAddr.Var<VFunction>) var1.addr).var).ident);
        } else {
            calledLocation = var1.addr.toString();
        }
        translation.append("  call ").append(calledLocation).append("\n");
        if (var1.dest != null) {
            String destName = env.getLivenessVisitor().getAssignment(var1.dest.ident);
            if (destName != null && env.getLivenessVisitor().getLivenessInterval(var1.dest.ident).assignableAt(env.getStatementNum())) {
                translation.append("  ").append(destName).append(" = $v0\n");
            }
        }
    }

    public void visit(LivenessEnvironment env, VBuiltIn var1) throws RuntimeException {
        if (var1.dest instanceof VVarRef.Local && var1.op.numParams == 2) {
            String destName = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.dest).ident);
            if (destName != null && env.getLivenessVisitor().getLivenessInterval(((VVarRef.Local) var1.dest).ident).assignableAt(env.getStatementNum())) {
                String arg1;
                String arg2;
                if (var1.args[0] instanceof VVarRef.Local) {
                    arg1 = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.args[0]).ident);
                    if (arg1.startsWith("local")) {
                        translation.append("  $v0 = ").append(arg1).append("\n");
                        arg1 = "$v0";
                    }
                } else {
                    arg1 = var1.args[0].toString();
                }
                if (var1.args[1] instanceof VVarRef.Local) {
                    arg2 = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.args[1]).ident);
                    if (arg2.startsWith("local")) {
                        translation.append("  $v1 = ").append(arg2).append("\n");
                        arg2 = "$v1";
                    }
                } else {
                    arg2 = var1.args[1].toString();
                }
                if (destName.startsWith("local")) {
                    translation.append("  $v0 = ");
                } else {
                    translation.append("  ").append(destName).append(" = ");
                }
                translation.append(var1.op.name).append("(").append(arg1).append(" ").append(arg2).append(")\n");
                if (destName.startsWith("local")) {
                    translation.append("  ").append(destName).append(" = $v0\n");
                }
            }

        } else if (var1.op.numParams == 1) {
            String assignment;
            if (var1.args[0] instanceof VVarRef.Local) {
                assignment = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.args[0]).ident);
                if (assignment.startsWith("local")) {
                    translation.append("  $v1 = ").append(assignment).append("\n");
                    assignment = "$v1";
                }
            } else {
                assignment = var1.args[0].toString();
            }
            if (var1.dest != null) {
                if (var1.dest instanceof VVarRef.Local && env.getLivenessVisitor().getLivenessInterval(((VVarRef.Local) var1.dest).ident).assignableAt(env.getStatementNum())) {
                    String destName = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.dest).ident);
                    translation.append("  ").append(destName).append(" = ").append(var1.op.name).append("(").append(assignment).append(")\n");
                }
            } else {
                translation.append("  ").append(var1.op.name).append("(").append(assignment).append(")\n");
            }
        }

    }

    public void visit(LivenessEnvironment env, VMemWrite var1) throws RuntimeException {
        VMemRef.Global dest = (VMemRef.Global) var1.dest;
        String ident;
        if (dest.base instanceof VAddr.Var && ((VAddr.Var<VDataSegment>) dest.base).var instanceof VVarRef.Local) {
            ident = env.getLivenessVisitor().getAssignment(((VVarRef.Local) ((VAddr.Var<VDataSegment>) dest.base).var).ident);
            if (ident.startsWith("local")) {
                translation.append("  $v0 = ").append(ident).append("\n");
                ident = "$v0";
            }
        } else {
            ident = dest.base.toString();
        }
        String source;
        if (var1.source instanceof VVarRef.Local) {
            source = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.source).ident);
            if (source.startsWith("local")) {
                translation.append("  $v1 = ").append(source).append("\n");
                source = "$v1";
            }
        } else {
            source = var1.source.toString();
        }
        translation.append("  [").append(ident);
        if (dest.byteOffset != 0) {
            translation.append("+").append(dest.byteOffset);
        }
        translation.append("] = ").append(source).append("\n");
    }

    public void visit(LivenessEnvironment env, VMemRead var1) throws RuntimeException {
        VMemRef.Global source = (VMemRef.Global) var1.source;
        String dest;
        if (var1.dest instanceof VVarRef.Local) {
            dest = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.dest).ident);
            if (dest != null && env.getLivenessVisitor().getLivenessInterval(((VVarRef.Local) var1.dest).ident).assignableAt(env.getStatementNum())) {
                if (dest.startsWith("local")) {
                    translation.append("  $v0 = ").append(dest).append("\n");
                    dest = "$v0";
                }
                String src;
                if (source.base instanceof VAddr.Var && ((VAddr.Var<VDataSegment>) source.base).var instanceof VVarRef.Local) {
                    src = env.getLivenessVisitor().getAssignment(((VVarRef.Local) ((VAddr.Var<VDataSegment>) source.base).var).ident);
                    if (src.startsWith("local")) {
                        translation.append("  $v1 = ").append(src).append("\n");
                        src = "$v1";
                    }
                } else {
                    src = source.base.toString();
                }
                translation.append("  ").append(dest).append(" = [").append(src);
                if (source.byteOffset != 0) {
                    translation.append("+").append(source.byteOffset);
                }
                translation.append("]\n");
            }
        }

    }

    public void visit(LivenessEnvironment env, VBranch var1) throws RuntimeException {
        String val;
        if (var1.value instanceof VVarRef.Local) {
            val = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.value).ident);
            if (val.startsWith("local")) {
                translation.append("  $v1 = ").append(val).append("\n");
                val = "$v1";
            }
        } else {
            val = var1.value.toString();
        }
        translation.append("  if");
        if (!var1.positive) {
            translation.append("0");
        }
        translation.append(" ").append(val).append(" goto :").append(var1.target.ident).append("\n");
    }

    public void visit(LivenessEnvironment env, VGoto var1) throws RuntimeException {
        translation.append("  goto ").append(var1.target.toString()).append("\n");
    }

    public void visit(LivenessEnvironment env, VReturn var1) throws RuntimeException {
        String val;
        if (var1.value != null) {
            if (var1.value instanceof VVarRef.Local) {
                val = env.getLivenessVisitor().getAssignment(((VVarRef.Local) var1.value).ident);
            } else {
                val = var1.value.toString();
            }
            translation.append("  $v0 = ").append(val).append("\n");
        }
    }

    public String outputTranslation() {
        return translation.toString();
    }
}
