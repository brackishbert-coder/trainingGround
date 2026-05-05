package Box.Interpreter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MatchTable {

    private final Map<ControlNode, ControlNode>  matches = new LinkedHashMap<>();
    private final Map<ControlNode, MatchResult>  results = new LinkedHashMap<>();
    private final Map<ControlNode, MatchPair>    pairs   = new LinkedHashMap<>();
    private final List<MatchPair>                allPairs = new ArrayList<>();

    public void add(ControlNode open, ControlNode close, String pairId) {
        MatchPair pair = new MatchPair(pairId, open, close);
        matches.put(open,  close);
        matches.put(close, open);
        results.put(open,  MatchResult.MATCHED);
        results.put(close, MatchResult.MATCHED);
        pairs.put(open,  pair);
        pairs.put(close, pair);
        allPairs.add(pair);
        open.matchResult    = MatchResult.MATCHED;
        open.matchedPartner = close;
        close.matchResult    = MatchResult.MATCHED;
        close.matchedPartner = open;
    }

    public void setResult(ControlNode node, MatchResult result) {
        results.put(node, result);
        node.matchResult = result;
    }

    public ControlNode getMatch(ControlNode node) {
        return matches.get(node);
    }

    public MatchResult getResult(ControlNode node) {
        MatchResult r = results.get(node);
        return r != null ? r : MatchResult.UNMATCHED_OPEN;
    }

    public MatchPair getPairForNode(ControlNode node) {
        return pairs.get(node);
    }

    public List<MatchPair> getAllPairs() {
        return allPairs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MatchTable[\n");
        for (MatchPair p : allPairs) sb.append("  ").append(p).append("\n");
        sb.append("]");
        return sb.toString();
    }
}
