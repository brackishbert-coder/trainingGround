package Box.Grouper;

import java.util.ArrayList;
import Box.Token.Token;
import Box.Token.TokenType;

public class FlowPreprocessor {

    public ArrayList<Token> process(ArrayList<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);

            // Forward flow: OPEN_BRACKET [COMMA] DOT
            if (isOpenBracket(t)) {
                if (i + 1 < tokens.size() && tokens.get(i + 1).type == TokenType.DOT) {
                    t.isFlow = true;
                    tokens.get(i + 1).isFlow = true;
                } else if (i + 2 < tokens.size()
                        && tokens.get(i + 1).type == TokenType.COMMA
                        && tokens.get(i + 2).type == TokenType.DOT) {
                    t.isFlow = true;
                    tokens.get(i + 1).isFlow = true;
                    tokens.get(i + 2).isFlow = true;
                }
            }

            // Backward flow: DOT [COMMA] CLOSED_BRACKET
            if (t.type == TokenType.DOT) {
                if (i + 1 < tokens.size() && isClosedBracket(tokens.get(i + 1))) {
                    t.isFlow = true;
                    tokens.get(i + 1).isFlow = true;
                } else if (i + 2 < tokens.size()
                        && tokens.get(i + 1).type == TokenType.COMMA
                        && isClosedBracket(tokens.get(i + 2))) {
                    t.isFlow = true;
                    tokens.get(i + 1).isFlow = true;
                    tokens.get(i + 2).isFlow = true;
                }
            }
        }
        return tokens;
    }

    private boolean isOpenBracket(Token t) {
        return t.type == TokenType.OPENPAREN
            || t.type == TokenType.OPENBRACE
            || t.type == TokenType.OPENSQUARE;
    }

    private boolean isClosedBracket(Token t) {
        return t.type == TokenType.CLOSEDPAREN
            || t.type == TokenType.CLOSEDBRACE
            || t.type == TokenType.CLOSEDSQUARE;
    }
}
