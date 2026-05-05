package Box.Interpreter;

public class ControlRegion {

    public final ControlNode leftControl;
    public final ControlNode rightControl;
    public final int         startIndex;
    public final int         endIndex;

    public RegionKind       regionKind       = RegionKind.AMBIGUOUS;
    public ReachabilityKind reachabilityKind = ReachabilityKind.DEFAULT_REACHABLE;
    public ExecutionPolicy  executionPolicy  = ExecutionPolicy.DEFAULT;

    public ControlRegion(ControlNode leftControl, ControlNode rightControl,
                         int startIndex, int endIndex) {
        this.leftControl  = leftControl;
        this.rightControl = rightControl;
        this.startIndex   = startIndex;
        this.endIndex     = endIndex;
    }

    public boolean isEmpty() {
        return endIndex - startIndex <= 1;
    }

    @Override
    public String toString() {
        return "Region[" + startIndex + ".." + endIndex
                + " " + regionKind + " " + reachabilityKind + "]";
    }
}
