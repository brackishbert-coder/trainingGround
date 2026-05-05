package Box.Interpreter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Flow {

    public enum Direction { FORWARD, BACKWARD }
    public enum BracketType { PAREN, BRACE, SQUARE }

    private Direction direction;
    private final BracketType bracketType;

    // Each entry is null (plain token) or a String (token that carries a cargo string).
    // When a non-null token is spent for bootstrapping its cargo string is removed from cargoBuffer.
    private final LinkedList<String> chainQueue = new LinkedList<>();

    // Permanent and token-linked cargo strings.  Never auto-cleared — index resets to 0 when
    // chainQueue empties so the next chain addition starts the rotation over.
    private final List<Object> cargoBuffer = new ArrayList<>();
    private int cargoIndex = 0;

    public Flow(Direction direction, BracketType bracketType, int chain) {
        this.direction   = direction;
        this.bracketType = bracketType;
        for (int i = 0; i < chain; i++) chainQueue.add(null);
    }

    public boolean canBootstrap() {
        return !chainQueue.isEmpty();
    }

    /**
     * Spend one bootstrap token (FIFO).
     * If the spent token carried a string, that string is removed from cargoBuffer
     * and cargoIndex is adjusted so it still points at the same element.
     * When the queue empties, cargoIndex resets to 0 (buffer is kept).
     */
    public boolean spendToken() {
        if (chainQueue.isEmpty()) return false;
        String entry = chainQueue.poll();          // FIFO — null = plain
        if (entry != null) {
            int removedIdx = cargoBuffer.indexOf(entry);
            if (removedIdx >= 0) {
                cargoBuffer.remove(removedIdx);
                if (!cargoBuffer.isEmpty()) {
                    if (removedIdx < cargoIndex) {
                        cargoIndex--;
                    } else if (cargoIndex >= cargoBuffer.size()) {
                        cargoIndex = cargoIndex % cargoBuffer.size();
                    }
                } else {
                    cargoIndex = 0;
                }
            }
        }
        if (chainQueue.isEmpty()) cargoIndex = 0;
        return true;
    }

    /** Add n plain (non-cargo) bootstrap tokens. */
    public void addChain(int n) {
        for (int i = 0; i < n; i++) chainQueue.add(null);
    }

    /**
     * Add a string-carrying token (first) plus n-1 plain tokens, and append s to cargoBuffer.
     * Used when a flow in the pocket body carries both chain tokens and a data string.
     * The first token spent for bootstrapping removes s from cargoBuffer; the remaining
     * n-1 are plain and do not affect cargoBuffer.
     */
    public void injectCargoChain(String s, int n) {
        chainQueue.add(s);                                 // string-carrying token first
        for (int i = 1; i < n; i++) chainQueue.add(null); // remaining plain tokens
        cargoBuffer.add(s);
    }

    /**
     * Add an item to cargoBuffer without adding a chain token.
     * Used when building a descriptor flow (extractFlowFromString) or injecting
     * permanent cargo that rotates for the lifetime of this flow's chain.
     */
    public void injectCargo(Object cargo) {
        cargoBuffer.add(cargo);
    }

    public void flipDirection() {
        direction = (direction == Direction.FORWARD) ? Direction.BACKWARD : Direction.FORWARD;
    }

    // ---- Cargo query -------------------------------------------------------

    public boolean hasCargo()          { return !cargoBuffer.isEmpty(); }

    public Object getCurrentCargo() {
        if (cargoBuffer.isEmpty()) return null;
        return cargoBuffer.get(cargoIndex);
    }

    /** Advance the cargo read-head by one, wrapping around. */
    public void advanceCargoIndex() {
        if (!cargoBuffer.isEmpty())
            cargoIndex = (cargoIndex + 1) % cargoBuffer.size();
    }

    /** Backward-compat alias for getCurrentCargo(). */
    public Object getCargo() { return getCurrentCargo(); }

    // ---- Accessors ---------------------------------------------------------

    public Direction   getDirection()   { return direction; }
    public BracketType getBracketType() { return bracketType; }
    public int         getChain()       { return chainQueue.size(); }
    public boolean     isForward()      { return direction == Direction.FORWARD; }
    public boolean     isBackward()     { return direction == Direction.BACKWARD; }

    @Override
    public String toString() {
        String arrow = direction == Direction.FORWARD ? "(." : ".)";
        return "Flow[" + arrow + " chain=" + chainQueue.size()
                + (cargoBuffer.isEmpty() ? "" : " cargo=" + cargoBuffer + "@" + cargoIndex) + "]";
    }
}
