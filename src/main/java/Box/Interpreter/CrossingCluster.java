package Box.Interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrossingCluster {

    private final List<Crossing> crossings = new ArrayList<>();

    public void add(Crossing crossing) {
        crossings.add(crossing);
    }

    public List<Crossing> getCrossings() {
        return Collections.unmodifiableList(crossings);
    }

    public int size() {
        return crossings.size();
    }

    public boolean isEmpty() {
        return crossings.isEmpty();
    }

    @Override
    public String toString() {
        return "CrossingCluster[" + crossings.size() + " crossings]";
    }
}
