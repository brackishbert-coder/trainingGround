package Box.Scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import Box.Box.Box;
import Box.Interpreter.Bin;
import Box.Token.TTDynamic;
import Box.Token.Token;
import Box.Token.TokenType;
import Box.Token.TokenTypeEnum;

public class Scanner {

	private String source;
	private final ArrayList<Token> tokens = new ArrayList<>();
	public final ArrayList<String> identifiers = new ArrayList<>();
	private int column = 0;
	private int line = 1;
	private int start = 0;
	private int current = 0;
	private static Map<String, TokenType> keywords = new HashMap<>();

	public Scanner(String string) {
		keywords.put("#HATTAG", TokenType.HATTAG);
		keywords.put("#hattag", TokenType.HATTAG);
		keywords.put("#Hattag", TokenType.HATTAG);
		keywords.put("#HAttag", TokenType.HATTAG);
		keywords.put("#HATtag", TokenType.HATTAG);
		keywords.put("#HATTag", TokenType.HATTAG);
		keywords.put("#HATTAg", TokenType.HATTAG);
		keywords.put("#HATTaG", TokenType.HATTAG);
		keywords.put("#HATtAG", TokenType.HATTAG);
		keywords.put("#HAtTAG", TokenType.HATTAG);
		keywords.put("#HaTTAG", TokenType.HATTAG);
		keywords.put("#hATTAG", TokenType.HATTAG);
		keywords.put("#haTTAG", TokenType.HATTAG);
		keywords.put("#HatTAG", TokenType.HATTAG);
		keywords.put("#HAttAG", TokenType.HATTAG);
		keywords.put("#HATtaG", TokenType.HATTAG);
		keywords.put("#HATTag", TokenType.HATTAG);
		keywords.put("#GATTAH", TokenType.GATTAH);
		keywords.put("#gattah", TokenType.GATTAH);

		keywords.put("add", TokenType.ADD);
		keywords.put("remove", TokenType.REMOVE);
		keywords.put("clear", TokenType.CLEAR);
		keywords.put("size", TokenType.SIZE);
		keywords.put("empty", TokenType.EMPTY);
		keywords.put("push", TokenType.PUSH);
		keywords.put("pop", TokenType.POP);
		keywords.put("alive", TokenType.ALIVE);
		keywords.put("evila", TokenType.EVILA);
		keywords.put("setat", TokenType.SETAT);
		keywords.put("getat", TokenType.GETAT);
		keywords.put("sub", TokenType.SUB);
		keywords.put("ln", TokenType.LN);
		keywords.put("exp", TokenType.EXP);
		keywords.put("dda", TokenType.DDA);
		keywords.put("evomer", TokenType.EVOMER);
		keywords.put("raelc", TokenType.RAELC);
		keywords.put("ezis", TokenType.EZIS);
		keywords.put("ytpme", TokenType.YTPME);
		keywords.put("hsup", TokenType.HSUP);
		keywords.put("tates", TokenType.TATES);
		keywords.put("tateg", TokenType.TATEG);
		keywords.put("bus", TokenType.BUS);
		keywords.put("nl", TokenType.NL);
		keywords.put("pxe", TokenType.PXE);

		
		
		

		keywords.put("nur", TokenType.NUR);
		keywords.put("run", TokenType.RUN);
		keywords.put("true", TokenType.TRUE);
		keywords.put("false", TokenType.FALSE);
		keywords.put("eurt", TokenType.EURT);
		keywords.put("eslaf", TokenType.ESLAF);
		keywords.put("print", TokenType.PRINT);
		keywords.put("tnirp", TokenType.TNIRP);
		
		keywords.put("yroot", TokenType.YROOT);
		keywords.put("toory", TokenType.TOORY);
		keywords.put("sin", TokenType.SIN);
		keywords.put("nis", TokenType.NIS);
		keywords.put("cos", TokenType.COS);
		keywords.put("tan", TokenType.TAN);
		keywords.put("soc", TokenType.SOC);
		keywords.put("nat", TokenType.NAT);
		keywords.put("sinh", TokenType.SINH);
		keywords.put("cosh", TokenType.COSH);
		keywords.put("tanh", TokenType.TANH);
		keywords.put("hnis", TokenType.HNIS);
		keywords.put("hsoc", TokenType.HSOC);
		keywords.put("hnat", TokenType.HNAT);
		keywords.put("log", TokenType.LOG);
		keywords.put("gol", TokenType.GOL);
		keywords.put("abs",   TokenType.ABS);   keywords.put("sba",   TokenType.SBA);
		keywords.put("sqrt",  TokenType.SQRT);  keywords.put("trqs",  TokenType.TRQS);
		keywords.put("floor", TokenType.FLOOR); keywords.put("roolf", TokenType.ROOLF);
		keywords.put("ceil",  TokenType.CEIL);  keywords.put("liec",  TokenType.LIEC);
		keywords.put("round", TokenType.ROUND); keywords.put("dnuor", TokenType.DNUOR);
		keywords.put("sign",  TokenType.SIGN);  keywords.put("ngis",  TokenType.NGIS);
		keywords.put("asin",  TokenType.ASIN);  keywords.put("nisa",  TokenType.NISA);
		keywords.put("acos",  TokenType.ACOS);  keywords.put("soca",  TokenType.SOCA);
		keywords.put("atan",  TokenType.ATAN);  keywords.put("nata",  TokenType.NATA);
		keywords.put("asinh", TokenType.ASINH); keywords.put("hnisa", TokenType.HNISA);
		keywords.put("acosh", TokenType.ACOSH); keywords.put("hsoca", TokenType.HSOCA);
		keywords.put("atanh", TokenType.ATANH); keywords.put("hnata", TokenType.HNATA);
		keywords.put("min",   TokenType.MIN);   keywords.put("nim",   TokenType.NIM);
		keywords.put("max",   TokenType.MAX);   keywords.put("xam",   TokenType.XAM);
		keywords.put("clamp", TokenType.CLAMP); keywords.put("pmalc", TokenType.PMALC);
		keywords.put("len",   TokenType.LEN);   keywords.put("nel",   TokenType.NEL);
		keywords.put("upper", TokenType.UPPER); keywords.put("reppu", TokenType.REPPU);
		keywords.put("lower", TokenType.LOWER); keywords.put("rewol", TokenType.REWOL);
		keywords.put("rev",   TokenType.STRREV);keywords.put("ver",   TokenType.VERUTS);
		keywords.put("trim",  TokenType.TRIM);  keywords.put("mirt",  TokenType.MIRT);
		keywords.put("num",   TokenType.NUM);   keywords.put("mun",   TokenType.MUN);
		keywords.put("sum",     TokenType.SUM);     keywords.put("mus",     TokenType.MUS);
		keywords.put("product", TokenType.PRODUCT); keywords.put("tcudorp", TokenType.TCUDORP);
		keywords.put("mean",    TokenType.MEAN);    keywords.put("naem",    TokenType.NAEM);
		keywords.put("sort",    TokenType.SORT);    keywords.put("tros",    TokenType.TROS);
		keywords.put("minof",   TokenType.MINOF);   keywords.put("fonim",   TokenType.FONIM);
		keywords.put("maxof",   TokenType.MAXOF);   keywords.put("fomam",   TokenType.FOMAM);
		keywords.put("first",   TokenType.FIRST);   keywords.put("tsrif",   TokenType.TSRIF);
		keywords.put("last",    TokenType.LAST);    keywords.put("tsal",    TokenType.TSAL);
		keywords.put("flip",    TokenType.FLIP);    keywords.put("pilf",    TokenType.PILF);
		keywords.put("flat",    TokenType.FLAT);    keywords.put("talf",    TokenType.TALF);
		keywords.put("band",  TokenType.BAND);  keywords.put("dnab",  TokenType.DNAB);
		keywords.put("bor",   TokenType.BOR);   keywords.put("rob",   TokenType.ROB);
		keywords.put("bxor",  TokenType.BXOR);  keywords.put("roxb",  TokenType.ROXB);
		keywords.put("bnot",  TokenType.BNOT);  keywords.put("tonb",  TokenType.TONB);
		keywords.put("bleft", TokenType.BLEFT); keywords.put("tfelb", TokenType.TFELB);
		keywords.put("bright",TokenType.BRIGHT);keywords.put("thgirb",TokenType.THGIRB);
		keywords.put("contains", TokenType.CONTAINS);
		keywords.put("CONTAINS", TokenType.CONTAINS);
		keywords.put("sniatnoc", TokenType.SNIATNOC);
		keywords.put("SNIATNOC", TokenType.SNIATNOC);
		keywords.put("open", TokenType.OPEN);
		keywords.put("OPEN", TokenType.OPEN);
		keywords.put("nepo", TokenType.NEPO);
		keywords.put("NEPO", TokenType.NEPO);
		keywords.put("and", TokenType.AND);
		keywords.put("or", TokenType.OR);
		keywords.put("dna", TokenType.DNA);
		keywords.put("ro", TokenType.RO);
		keywords.put("fun", TokenType.FUN);
	keywords.put("nuf", TokenType.NUF);
	keywords.put("return", TokenType.RETURN);
	keywords.put("nruter", TokenType.NRUTER);
	keywords.put("assert", TokenType.ASSERT);
	keywords.put("tressa", TokenType.TRESSA);
	keywords.put("save", TokenType.SAVE);
	keywords.put("read", TokenType.READ);
	keywords.put("into", TokenType.INTO);
	keywords.put("evas", TokenType.EVAS);
	keywords.put("daer", TokenType.DAER);
	keywords.put("otni", TokenType.OTNI);
	keywords.put("rename", TokenType.RENAME);
	keywords.put("emaner", TokenType.EMANER);
	keywords.put("to", TokenType.TO);
	keywords.put("move", TokenType.MOVE);
	keywords.put("ot", TokenType.OT);
	keywords.put("evom", TokenType.EVOM);
	keywords.put("derive", TokenType.DERIVE);
	keywords.put("evired", TokenType.EVIRED);
	keywords.put("integrate", TokenType.INTEGRATE);
	keywords.put("etargetni", TokenType.ETARGETNI);
	keywords.put("fresnels", TokenType.FRESNELS);
	keywords.put("slensref", TokenType.SLENSREF);
	keywords.put("fresnelc", TokenType.FRESNELC);
	keywords.put("clenserf", TokenType.CLENSERF);
	keywords.put("norm",   TokenType.NORM);   keywords.put("mron",   TokenType.MRON);
	keywords.put("unit",   TokenType.UNIT);   keywords.put("tinu",   TokenType.TINU);
	keywords.put("trans",  TokenType.TRANS);  keywords.put("snart",  TokenType.SNART);
	keywords.put("vdet",   TokenType.VDET);   keywords.put("tedv",   TokenType.TEDV);
	keywords.put("vinv",   TokenType.VINV);   keywords.put("vniv",   TokenType.VNIV);
	keywords.put("trace",  TokenType.TRACE);  keywords.put("ecart",  TokenType.ECART);
	keywords.put("vdot",   TokenType.VDOT);   keywords.put("todv",   TokenType.TODV);
	keywords.put("cross",  TokenType.CROSS);  keywords.put("ssorc",  TokenType.SSORC);
	keywords.put("vadd",   TokenType.VADD);   keywords.put("ddav",   TokenType.DDAV);
	keywords.put("vsub",   TokenType.VSUB);   keywords.put("busv",   TokenType.BUSV);
	keywords.put("vscale", TokenType.VSCALE); keywords.put("elacsv", TokenType.ELACSV);
	keywords.put("by", TokenType.BY);
	keywords.put("yb", TokenType.YB);
	keywords.put("from", TokenType.FROM);
	keywords.put("morf", TokenType.MORF);
	keywords.put("at", TokenType.ATKW);
	keywords.put("ta", TokenType.TAKW);
	keywords.put("constant", TokenType.CONSTANT);
	keywords.put("tnatsnoc", TokenType.TNATSNOC);
	keywords.put("target", TokenType.TARGET);
	keywords.put("tegrat", TokenType.TEGRAT);
	keywords.put("null", TokenType.NULL);
	keywords.put("NULL", TokenType.NULL);
	keywords.put("llun", TokenType.LLUN);
	keywords.put("LLUN", TokenType.LLUN);

	keywords.put("NILL", TokenType.NILL);
	keywords.put("nill", TokenType.NILL);
	keywords.put("LLIN", TokenType.LLIN);
	keywords.put("llin", TokenType.LLIN);

	keywords.put("not", TokenType.NOT);
	keywords.put("ton", TokenType.TON);	
	keywords.put("DOUBLE", TokenType.DOUBLE);
	keywords.put("INT", TokenType.INT);
	keywords.put("BIN", TokenType.BIN);
	keywords.put("knt", TokenType.KNOT);
	keywords.put("cup", TokenType.CUP);
	keywords.put("pkt", TokenType.POCKET);
	keywords.put("box", TokenType.BOX);

	keywords.put("tnk", TokenType.TONK);
	keywords.put("puc", TokenType.PUC);
	keywords.put("tkp", TokenType.TEKCOP);
	keywords.put("xob", TokenType.XOB);
	keywords.put("type", TokenType.TYPE);
	keywords.put("epyt", TokenType.EPYT);
	keywords.put("cre", TokenType.FLCREATE);
	keywords.put("mov", TokenType.FLMOVE);
	keywords.put("des", TokenType.FLDESTROY);
		

		

		
		
		
		
	



		
		
		
		
		this.source = string;

	}

	public ArrayList<Token> scanTokensFirstPass() {
		while (!isAtEnd()) {

			start = current;
			scanToken(tokens);

		}

		return tokens;
	}

	private void scanToken(ArrayList<Token> tokens) {
		char c = advance();

		switch (c) {
		case '#':
			if (current + 6 < source.length()) {
				CharSequence subSequence = source.subSequence(current - 1, current + 6);

				TokenTypeEnum tag = keywords.get(subSequence);

				if (tag != null && tag == TokenType.HATTAG && isAlpha(peekI(6))) {
					int count = 6;
					while (count != 0) {
						advance();
						count--;
					}
					readHATTAGS();
					TokenTypeEnum gat = checkIfKewordHattagorgattaH(tokens);
					while (gat != TokenType.GATTAH) {
						TTDynamic.getInstance();
						TTDynamic.addType(source.substring(start, current));
						readHATTAGS();
						gat = checkIfKewordHattagorgattaH(tokens);
					}
					start = current;
				} else {

					addToken(TokenType.HASH, tokens);
				}
			} else
				addToken(TokenType.HASH, tokens);

			break;
		case '(':
			addToken(TokenType.OPENPAREN, tokens);
			break;
		case ')':
			addToken(TokenType.CLOSEDPAREN, tokens);
			break;
		case '{':
			addToken(TokenType.OPENBRACE, tokens);

			break;
		case '}':
			addToken(TokenType.CLOSEDBRACE, tokens);
			break;
		case '[':
			addToken(TokenType.OPENSQUARE, tokens);

			break;
		case ']':
			addToken(TokenType.CLOSEDSQUARE, tokens);
			break;
		case '@':
			addToken(TokenType.AT, tokens);
			break;

		case ',':
			addToken(TokenType.COMMA, tokens);
			break;
		case '.':

			addToken(TokenType.DOT, tokens);

			break;
		case '?':
			addToken(TokenType.QMARK, tokens);

			break;
		case '-':
			if (match('=')) {
				addToken(TokenType.MINUSEQUALS, tokens);
			} else if (match('-')) {
				addToken(TokenType.MINUSMINUS, tokens);
			} else {
				addToken(TokenType.MINUS, tokens);
			}
			break;
		case '|':
			addToken(TokenType.TEMPLID, tokens);
			break;
		case '&':
			addToken(TokenType.SINGLEAND, tokens);
			break;
		case '+':
			if (match('=')) {
				addToken(TokenType.PLUSEQUALS, tokens);
			} else if (match('+')) {
				addToken(TokenType.PLUSPLUS, tokens);
			} else {
				addToken(TokenType.PLUS, tokens);
			}
			break;
		case '*':
			if (match('=')) {
				addToken(TokenType.TIMESEQUAL, tokens);
			} else
				addToken(TokenType.TIMES, tokens);
			break;

		case '^':
			if (match('=')) {
				addToken(TokenType.POWEREQUAL, tokens);
			} else
				addToken(TokenType.POWER, tokens);
			break;
		case '!':
			if (match('=')) {
				addToken(TokenType.NOTEQUALS, tokens);
			} else {
				addToken(TokenType.BANG, tokens);
			}
			break;
		case '=':
			if (match('=')) {
				addToken(TokenType.EQUALSEQUALS, tokens);

			} else if (match('!')) {
				addToken(TokenType.EQUALSNOT, tokens);
			} else if (match('+')) {
				addToken(TokenType.EQUALSPLUS, tokens);
			} else if (match('-')) {
				addToken(TokenType.EQUALSMINUS, tokens);
			} else if (match('>')) {
				addToken(TokenType.EQUALGREATERTHEN, tokens);
			} else if (match('<')) {
				addToken(TokenType.EQUALLESSTHEN, tokens);
			} else if (match('*')) {
				addToken(TokenType.EQUALTIMES, tokens);
			} else if (match('%')) {
				addToken(TokenType.EQUALMOD, tokens);
			} else if (match('/')) {
				addToken(TokenType.EQUALDIVIDEFORWARD, tokens);
			} else if (match('\\')) {
				addToken(TokenType.EQUALDIVIDEBACKWARD, tokens);
			} else if (match('^')) {
				addToken(TokenType.EQUALPOWER, tokens);
			} else {
				addToken(TokenType.ASIGNMENTEQUALS, tokens);

			}

			break;
		case '<':
			if (match('=')) {
				addToken(TokenType.LESSTHENEQUAL, tokens);
			} else if (match('<')) {
				if (match('<')) {
					addToken(TokenType.CONSUME, tokens);
				} else {
					Box.error(column, line, "Unexpected character " + c, true);
				}
			} else {
				addToken(TokenType.LESSTHEN, tokens);
			}
			break;
		case '>':
			if (match('=')) {
				addToken(TokenType.GREATERTHENEQUAL, tokens);
			} else if (match('>')) {
				if (match('>')) {
					addToken(TokenType.EXPELL, tokens);
				} else {
					Box.error(column, line, "Unexpected character " + c, true);
				}
			} else {
				addToken(TokenType.GREATERTHEN, tokens);
			}
			break;
		case '/':
			if (match('/')) {
				while (peek() != '\n' && !isAtEnd())
					advance();
			} else if (match('=')) {
				addToken(TokenType.EQUALDIVIDEFORWARD, tokens);
			} else {
				addToken(TokenType.FORWARDSLASH, tokens);
			}
			break;
		case '_':

			addToken(TokenType.UNDERSCORE, tokens);

			break;
		case '\\':
			if (match('=')) {
				addToken(TokenType.EQUALDIVIDEBACKWARD, tokens);
			} else
				addToken(TokenType.BACKSLASH, tokens);
			break;
		case '%':
			if (match('=')) {
				addToken(TokenType.MODEQUAL, tokens);
			} else

				addToken(TokenType.MOD, tokens);
			break;

		case ';':
			addToken(TokenType.SEMICOLON, tokens);
			break;
		case ':':
			addToken(TokenType.COLON, tokens);
			break;
		case '$':
			addToken(TokenType.DOLLAR, tokens);
			break;
		case ' ':
			addToken(TokenType.SPACE, tokens);
			break;
		case '\r':
			addToken(TokenType.SPACERETURN, tokens);
			break;
		case '\t':
			addToken(TokenType.TAB, tokens);
			break;
		case '\n':
			addToken(TokenType.NEWLINE, tokens);
			line++;
			column = 0;
			break;
		case '"':
			string(tokens);
			break;
		case '\'':
			character(tokens);
			break;
		default:
			if (isDigit(c))
				intNum_BinNum(tokens, c);
			else if (isAlpha(c))
				ident_BinNum_IntNum(tokens, c, false);
			else
				Box.error(column, line, "Unexpected character " + c, true);
		}

	}

	private void readHATTAGS() {
		start = current;
		advance();
		while (peek() != '#' && isAlphaNumeric(peek())) {
			advance();
		}
	}

	private TokenTypeEnum checkIfKewordHattagorgattaH(ArrayList<Token> tokens2) {
		String text = source.substring(start, current);
		TokenTypeEnum type = keywords.get(text);
		return type;
	}

	private void ident_BinNum_IntNum(ArrayList<Token> tokens, char c, boolean hasAlphafromPass) {

		if (isb(c) && isBinary(peek())) {
			boolean endb = false;
			Bin binNum = null;
			while (isBinary(peek())) {
				advance();
				if (isb(peek())) {
					advance();
					endb = true;
					break;
				}
			}

			boolean advancedFurther = false;
			while (isAlphaNumeric(peek())) {
				advancedFurther = true;
				advance();
			}

			if (!endb && !advancedFurther && !hasAlphafromPass) {
				binNum = Bin.valueOf(source.substring(start + 1, current));
				addToken(TokenType.BINNUM, binNum, tokens);
			} else if (endb && !advancedFurther && !hasAlphafromPass) {
				binNum = Bin.valueOf(source.substring(start + 1, current - 1));
				addToken(TokenType.BINNUM, binNum, tokens);
			} else if (!endb && !advancedFurther && hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			} else if (!endb && advancedFurther && !hasAlphafromPass) {
				binNum = Bin.valueOf(source.substring(start + 1, current));
				addToken(TokenType.BINNUM, binNum, tokens);
			} else if (!endb && advancedFurther && hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			} else if (endb && !advancedFurther && hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			} else if (endb && advancedFurther && !hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			} else if (endb && advancedFurther && hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			}

		} else if (isBinary(c)) {
			boolean endb = false;
			Bin binNum = null;
			while (isBinary(peek())) {
				advance();
				if (isb(peek())) {
					advance();
					endb = true;
					break;
				}
			}
			boolean advancedFurther = false;
			boolean hasAlpha = false;
			while (isAlphaNumeric(peek())) {
				if (isAlpha(peek()))
					hasAlpha = true;
				advancedFurther = true;
				advance();
			}

			if (endb && !advancedFurther && !hasAlphafromPass) {
				binNum = Bin.valueOf(source.substring(start, current - 1));
				addToken(TokenType.BINNUM, binNum, tokens);
			} else if (!endb && !advancedFurther && !hasAlphafromPass) {
				addToken(TokenType.INTNUM, Integer.valueOf(source.substring(start, current)), tokens);
			} else if (!endb && advancedFurther && hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			} else if (!endb && advancedFurther && !hasAlphafromPass) {
				addToken(TokenType.INTNUM, Integer.valueOf(source.substring(start, current)), tokens);
			} else if (!endb && !advancedFurther && hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			} else if (endb && advancedFurther && hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			} else if (endb && advancedFurther && !hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			} else if (endb && !advancedFurther && hasAlphafromPass) {
				checkIfKewordOrIdentifierAndAdd(tokens);
			}

		} else if (isAlphaNumeric(c)) {
			boolean hasAlpha = isAlpha(c);
			while (isAlphaNumeric(peek())) {
				if (isAlpha(peek()))
					hasAlpha = true;
				advance();
			}

			if (hasAlpha || hasAlphafromPass)
				if (checkIfKeword(tokens) && peek() == '.') {
					checkIfKewordOrIdentifierAndAdd(tokens);
				} else if (checkIfKeword(tokens) && peek() != '.') {
					checkIfKewordOrIdentifierAndAdd(tokens);
				} else if (!checkIfKeword(tokens) && peek() == '.') {
					addToken(TokenType.IDENTIFIER, source.substring(start, current), tokens);
				} else {
					addToken(TokenType.IDENTIFIER, source.substring(start, current), tokens);

				}
			else {
				addToken(TokenType.INTNUM, Integer.valueOf(source.substring(start, current)), tokens);
			}

		}

	}

	private void checkIfKewordOrIdentifierAndAdd(ArrayList<Token> tokens) {
		String text = source.substring(start, current);
		TokenType type = keywords.get(text);
		if (type == null) {
			type = TokenType.IDENTIFIER;
			addToken(type, text, tokens);
		} else {
			addToken(type, tokens);
		}
	}

	private boolean checkIfKeword(ArrayList<Token> tokens) {
		String text = source.substring(start, current);
		TokenTypeEnum type = keywords.get(text);
		if (type != null) {
			return true;
		}
		return false;
	}

	private boolean isAlphaNumeric(char c) {

		return isAlpha(c) || isDigit(c);
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	private boolean isb(char c) {
		return (c == 'b');
	}

	private void intNum_BinNum(ArrayList<Token> tokens, char c) {
		while (isBinary(peek()))
			advance();
		boolean isB = false;
		if (isb(peek())) {
			advance();
			isB = true;
		}
		if (isB && isNewline_Space_Tab_Return_Eof(peek())) {
			determinIdentifier_BinNum_IntNum(tokens, isB);
		} else if ((isB && !isNewline_Space_Tab_Return_Eof(peek()))) {
			determinIdentifier_BinNum_IntNum(tokens, isB);
		} else if ((!isB && isNewline_Space_Tab_Return_Eof(peek()))) {
			isItIdent_IntNum_BinNum(tokens);
		} else if ((!isB && !isNewline_Space_Tab_Return_Eof(peek()))) {
			isItIdent_IntNum_BinNum(tokens);
		}
	}

	private void isItIdent_IntNum_BinNum(ArrayList<Token> tokens) {
		boolean hasAlpha = false;
		while (isAlphaNumeric(peek())) {
			if (isAlpha(peek()))
				hasAlpha = true;
			advance();
		}
		if (peek() == '.') {
			advance();
			while (isDigit(peek())) {
				advance();
			}
			addToken(TokenType.DOUBLENUM, Double.valueOf(source.substring(start, current)), tokens);

		} else {
			if (hasAlpha) {
				addToken(TokenType.IDENTIFIER, source.substring(start, current), tokens);
			} else {
				addToken(TokenType.INTNUM, Integer.valueOf(source.substring(start, current)), tokens);

			}
		}
	}

	private void determinIdentifier_BinNum_IntNum(ArrayList<Token> tokens, boolean isB) {
		if (!isAlphaNumeric(peek()) && isB) {
			addToken(TokenType.BINNUM, Bin.valueOf(source.substring(start, current - 1)), tokens);
		} else if (!isAlphaNumeric(peek()) && !isB) {
			while (isDigit(peek())) {
				advance();
			}
			if (isAlpha(peek())) {
				ident_BinNum_IntNum(tokens, peek(), true);
			} else {
				addToken(TokenType.INTNUM, Integer.valueOf(source.substring(start, current)), tokens);
			}
		} else if (isAlphaNumeric(peek()) && !isB) {
			ident_BinNum_IntNum(tokens, peek(), true);
		} else if (isAlphaNumeric(peek()) && isB) {
			ident_BinNum_IntNum(tokens, peek(), true);
		}
	}

	private boolean isNewline_Space_Tab_Return_Eof(char c) {
		boolean istrue = false;
		switch (c) {
		case '\n':
			istrue = true;
			break;
		case ' ':
			istrue = true;
			break;
		case '\t':
			istrue = true;

			break;
		case '\r':
			istrue = true;
			break;
		case '\0':
			istrue = true;
			break;

		}
		return istrue;
	}

	private boolean anythingOtherThenDigit(char c) {
		boolean istrue = false;
		switch (c) {
		case '(':
			istrue = true;
			break;
		case ')':
			istrue = true;
			break;
		case '{':
			istrue = true;

			break;
		case '}':
			istrue = true;
			break;
		case '[':
			istrue = true;

			break;
		case ']':
			istrue = true;
			break;
		case ',':
			istrue = true;
			break;
		case '.':
			istrue = true;
			break;
		case '-':
			istrue = true;
			break;
		case '|':
			istrue = true;
			break;
		case '&':
			istrue = true;
			break;
		case '+':
			istrue = true;
			break;
		case '*':
			istrue = true;
			break;
		case '^':
			istrue = true;
			break;
		case '!':
			istrue = true;
			break;
		case '=':
			istrue = true;
			break;
		case '<':
			istrue = true;
			break;
		case '>':
			istrue = true;
			break;
		case '/':
			istrue = true;
			break;
		case '\\':
			istrue = true;
			break;
		case ';':
			istrue = true;
			break;
		case '"':
			istrue = true;
			break;
		case '\'':
			istrue = true;
			break;
		}
		return istrue;
	}

	private char previous() {
		if (start == 0)
			return '\0';
		return source.charAt(start - 1);

	}

	private char previousCurrent() {
		if (start == 0)
			return '\0';
		return source.charAt(current - 1);

	}

	private boolean isBinary(String text) {
		boolean isBinary = true;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '0' || text.charAt(i) == '1') {

			} else {
				isBinary = false;
			}
		}
		return isBinary;
	}

	private char peekNext() {
		if (current + 1 >= source.length())
			return '\0';
		return source.charAt(current + 1);
	}

	private boolean isDigit(char c) {

		return c >= '0' && c <= '9';
	}

	private boolean isBinary(char c) {

		return c == '0' || c == '1';
	}

	private void string(ArrayList<Token> tokens) {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') {
				line++;
				column = 0;
			}
			advance();
		}

		if (isAtEnd()) {
			Box.error(column, line, "Unterminated String", true);
			return;
		}

		advance();
		String value = source.substring(start + 1, current - 1);
		addToken(TokenType.STRING, value, tokens);
	}

	private void character(ArrayList<Token> tokens) {
		if (peek() != '\'') {
			if (peek() == '\n') {
				line++;
				column = 0;
			}
			advance();
		}

		if (isAtEnd()) {
			Box.error(column, line, "Unterminated char", true);
			return;
		}

		advance();
		String value = source.substring(start + 1, current - 1);
		addToken(TokenType.CHAR, value, tokens);
	}

	private char peek() {
		if (isAtEnd())
			return '\0';
		return source.charAt(current);
	}

	private char peekI(int index) {

		if (isAtEnd())
			return '\0';
		return source.charAt(current + index);

	}

	private boolean match(char c) {
		if (isAtEnd())
			return false;
		if (source.charAt(current) != c)
			return false;
		current++;
		column++;
		return true;
	}

	private void addToken(TokenType type, ArrayList<Token> tokens) {
		addToken(type, null, tokens);

	}

	private void addToken(TokenType type, Object literal, ArrayList<Token> tokens) {
		String text = source.substring(start, current);

		column = column - (current - start);
		tokens.add(new Token(type, text, literal, null, null, column, line, start, current));
	}

	private void addToken(TokenType type, Object literal, int theStart, int theCurrent, ArrayList<Token> tokens) {
		String text = source.substring(theStart, theCurrent);

		column = column - (theCurrent - theStart);
		tokens.add(new Token(type, text, literal, null, null, column, line, theStart, theCurrent));
	}

	private char advance() {

		current++;
		column++;
		return source.charAt(current - 1);
	}

	private char regress() {
		current--;
		column--;
		return source.charAt(current - 1);
	}

	private boolean isAtEnd() {

		return current >= source.length();
	}

	private boolean peekNextisAtEnd() {
		if (current + 1 >= source.length())
			return true;
		return source.charAt(current + 1) == '\0';
	}

}
