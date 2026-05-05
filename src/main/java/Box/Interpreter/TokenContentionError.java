package Box.Interpreter;

public class TokenContentionError extends RuntimeException {

    public final String pocketName;
    public final int tickNumber;
    public final String tokenLevel; // "interpreter" or the container's name

    public TokenContentionError(String pocketName, int tickNumber, String tokenLevel) {
        super("Token contention [" + tokenLevel + "] pocket=" + pocketName + " tick=" + tickNumber);
        this.pocketName = pocketName;
        this.tickNumber = tickNumber;
        this.tokenLevel = tokenLevel;
    }
}
