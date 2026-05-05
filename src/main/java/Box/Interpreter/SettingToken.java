package Box.Interpreter;

/**
 * Sentinel value placed in the global environment for each settings keyword.
 * When the interpreter evaluates a dot-access expression involving a SettingToken
 * and a known container name, it activates the corresponding setting flag rather
 * than performing a normal property lookup.
 *
 * Recognized patterns (both orderings accepted):
 *   CONTAINER.SETTING   e.g. TRASH.dumpPriority
 *   SETTING.CONTAINER   e.g. dumpPriority.TRASH
 */
public final class SettingToken {
    public enum Kind { DUMP_PRIORITY, THREAD, DYNAMIC_SAFTY }
    public final Kind kind;
    public SettingToken(Kind kind) { this.kind = kind; }
    @Override public String toString() { return "<setting:" + kind + ">"; }
}
