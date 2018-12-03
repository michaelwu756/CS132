package registerAllocation;

import cs132.vapor.ast.*;

import java.util.*;

public class LivenessVisitor extends VInstr.VisitorP<Integer, RuntimeException> {
    private LivenessGraph graph;
    private Map<String, String> variableAssignments = new HashMap<>();
    private Map<String, LivenessInterval> variableLivenessIntervals = new HashMap<>();
    private int spilledCounter = 0;
    private int calleeSavedCounter;
    private int inCounter = 0;
    private int outCounter = 0;

    public LivenessVisitor(VFunction function) {
        graph = new LivenessGraph(function.body.length);
        for (int i = 0; i < function.body.length; i++) {
            function.body[i].accept(i, this);
        }
        graph.computeLiveIntervals();

        Set<String> calleeSavedVars = new HashSet<>();
        Set<String> callerSavedVars = new HashSet<>();
        for (int i = 0; i < function.body.length; i++) {
            if (function.body[i] instanceof VCall) {
                Set<String> toAdd = new HashSet<>(graph.getOut(i));
                toAdd.removeAll(graph.getDef(i));
                calleeSavedVars.addAll(toAdd);
            }
        }
        for (String var : function.vars) {
            if (!graph.getLiveInterval(var).isEmpty()) {
                List<Integer> liveIntervalList = graph.getLiveInterval(var);
                LivenessInterval interval = new LivenessInterval(var, liveIntervalList.get(0), liveIntervalList.get(liveIntervalList.size() - 1) + 1);
                variableLivenessIntervals.put(var, interval);
            }
            if (!calleeSavedVars.contains(var)) {
                callerSavedVars.add(var);
            }
        }
        linearScanRegisterAllocation(calleeSavedVars, "s", 8);
        linearScanRegisterAllocation(callerSavedVars, "t", 9);
        Set<String> calleeSaved = new HashSet<>();
        variableAssignments.values().stream().filter(v -> v.startsWith("$s")).forEach(calleeSaved::add);
        calleeSavedCounter = calleeSaved.size();
        if (function.params.length - 4 > inCounter) {
            inCounter = function.params.length - 4;
        }
        /*System.err.println("Liveness of Function " + function.ident);
        for (String var : function.vars) {
            if (variableLivenessIntervals.get(var) != null) {
                System.err.println(var + ": " + variableLivenessIntervals.get(var).getStart() + "-" + variableLivenessIntervals.get(var).getEnd());
            } else {
                System.err.println(var + ":");
            }
        }
        System.err.println("assignments");
        for (String var : function.vars) {
            System.err.println(var + ": " + variableAssignments.get(var));
        }*/
    }

    public void visit(Integer instructionNumber, VAssign var1) throws RuntimeException {
        graph.addEdge(instructionNumber, instructionNumber + 1);
        if (var1.source instanceof VVarRef.Local) {
            graph.addUse(instructionNumber, ((VVarRef.Local) var1.source).ident);
        }
        if (var1.dest instanceof VVarRef.Local) {
            graph.addDef(instructionNumber, ((VVarRef.Local) var1.dest).ident);
        }
    }

    public void visit(Integer instructionNumber, VCall var1) throws RuntimeException {
        graph.addEdge(instructionNumber, instructionNumber + 1);
        if (var1.addr instanceof VAddr.Var && ((VAddr.Var<VFunction>) var1.addr).var instanceof VVarRef.Local) {
            graph.addUse(instructionNumber, ((VVarRef.Local) ((VAddr.Var<VFunction>) var1.addr).var).ident);
        }
        for (VOperand arg : var1.args) {
            if (arg instanceof VVarRef.Local) {
                graph.addUse(instructionNumber, ((VVarRef.Local) arg).ident);
            }
        }
        graph.addDef(instructionNumber, var1.dest.ident);
        if (var1.args.length - 4 > outCounter) {
            outCounter = var1.args.length - 4;
        }
    }

    public void visit(Integer instructionNumber, VBuiltIn var1) throws RuntimeException {
        graph.addEdge(instructionNumber, instructionNumber + 1);
        for (VOperand arg : var1.args) {
            if (arg instanceof VVarRef.Local) {
                graph.addUse(instructionNumber, ((VVarRef.Local) arg).ident);
            }
        }
        if (var1.dest instanceof VVarRef.Local) {
            graph.addDef(instructionNumber, ((VVarRef.Local) var1.dest).ident);
        }
        if (var1.args.length - 4 > outCounter) {
            outCounter = var1.args.length - 4;
        }
    }

    public void visit(Integer instructionNumber, VMemWrite var1) throws RuntimeException {
        graph.addEdge(instructionNumber, instructionNumber + 1);
        if (var1.source instanceof VVarRef.Local) {
            graph.addUse(instructionNumber, ((VVarRef.Local) var1.source).ident);
        }
        if (var1.dest instanceof VMemRef.Global &&
                ((VMemRef.Global) var1.dest).base instanceof VAddr.Var &&
                ((VAddr.Var<VDataSegment>) ((VMemRef.Global) var1.dest).base).var instanceof VVarRef.Local) {
            graph.addUse(instructionNumber, ((VVarRef.Local) ((VAddr.Var<VDataSegment>) ((VMemRef.Global) var1.dest).base).var).ident);
        }
    }

    public void visit(Integer instructionNumber, VMemRead var1) throws RuntimeException {
        graph.addEdge(instructionNumber, instructionNumber + 1);
        if (var1.source instanceof VMemRef.Global &&
                ((VMemRef.Global) var1.source).base instanceof VAddr.Var &&
                ((VAddr.Var<VDataSegment>) ((VMemRef.Global) var1.source).base).var instanceof VVarRef.Local) {
            graph.addUse(instructionNumber, ((VVarRef.Local) ((VAddr.Var<VDataSegment>) ((VMemRef.Global) var1.source).base).var).ident);
        }
        if (var1.dest instanceof VVarRef.Local) {
            graph.addDef(instructionNumber, ((VVarRef.Local) var1.dest).ident);
        }
    }

    public void visit(Integer instructionNumber, VBranch var1) throws RuntimeException {
        graph.addEdge(instructionNumber, instructionNumber + 1);
        graph.addEdge(instructionNumber, var1.target.getTarget().instrIndex);
        if (var1.value instanceof VVarRef.Local) {
            graph.addUse(instructionNumber, ((VVarRef.Local) var1.value).ident);
        }
    }

    public void visit(Integer instructionNumber, VGoto var1) throws RuntimeException {
        graph.addEdge(instructionNumber, ((VAddr.Label<VCodeLabel>) var1.target).label.getTarget().instrIndex);
    }

    public void visit(Integer instructionNumber, VReturn var1) throws RuntimeException {
        if (var1.value instanceof VVarRef.Local) {
            graph.addUse(instructionNumber, ((VVarRef.Local) var1.value).ident);
        }
    }

    public String getStacks() {
        return "[in " + getInCounter() + ", out " + getOutCounter() + ", local " + (getSpilledCounter() + getCalleeSavedCounter()) + "]";
    }

    public String getAssignment(String var) {
        return variableAssignments.get(var);
    }

    public int getInCounter() {
        return inCounter;
    }

    public int getOutCounter() {
        return outCounter;
    }

    public int getSpilledCounter() {
        return spilledCounter;
    }

    public int getCalleeSavedCounter() {
        return calleeSavedCounter;
    }

    public LivenessInterval getLivenessInterval(String var) {
        return variableLivenessIntervals.get(var);
    }

    private void linearScanRegisterAllocation(Set<String> vars, String regPrefix, int numRegs) {
        SortedSet<String> freeRegisters = new TreeSet<>();
        for (int i = 0; i < numRegs; i++) {
            freeRegisters.add("$" + regPrefix + i);
        }
        SortedSet<LivenessInterval> startOrderedIntervals = new TreeSet<>(Comparator.comparingInt(LivenessInterval::getStart)
                .thenComparingInt(LivenessInterval::getEnd)
                .thenComparing(LivenessInterval::getName));
        for (String s : vars) {
            if (variableLivenessIntervals.get(s) != null) {
                startOrderedIntervals.add(variableLivenessIntervals.get(s));
            }
        }
        SortedSet<LivenessInterval> active = new TreeSet<>(Comparator.comparingInt(LivenessInterval::getEnd)
                .thenComparingInt(LivenessInterval::getEnd)
                .thenComparing(LivenessInterval::getName));
        for (LivenessInterval interval : startOrderedIntervals) {
            active.stream().filter(activeInterval -> !activeInterval.overlaps(interval)).forEach(activeInterval -> freeRegisters.add(variableAssignments.get(activeInterval.getName())));
            active.removeIf(activeInterval -> !activeInterval.overlaps(interval));
            if (active.size() == numRegs) {
                LivenessInterval toSpill = active.last();
                if (toSpill.getEnd() > interval.getEnd()) {
                    variableAssignments.put(interval.getName(), variableAssignments.get(toSpill.getName()));
                    variableAssignments.put(toSpill.getName(), "local[" + spilledCounter + "]");
                    active.remove(toSpill);
                    active.add(interval);
                } else {
                    variableAssignments.put(interval.getName(), "local[" + spilledCounter + "]");
                }
                spilledCounter++;
            } else {
                variableAssignments.put(interval.getName(), freeRegisters.first());
                freeRegisters.remove(freeRegisters.first());
                active.add(interval);
            }
        }
    }
}
