package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;

public enum RunTimeTypes {
	Int, Double, Bin, Char, String, Boolean, Naeloob, Gnirts, Rahc, Nib, Elbuod, Tni, NULL, NILL, LLUN, LLIN, knt, cup,
	pkt, box, Any, CupOpenRight, CupOpenLeft, PocketOpenRight, PocketOpenLeft, BoxOpenRight, BoxOpenLeft,
	Type, Epyt, Function, tnk,tkp,xob, puc;

	public static RunTimeTypes getObjectType(Object obj, Object value, Interpreter interpreter) {
		if (obj instanceof Expr.Literal) {
			if (value instanceof Integer) {
				return RunTimeTypes.Int;
			} else if (value instanceof Double) {
				return RunTimeTypes.Double;
			} else if (value instanceof Bin) {
				return RunTimeTypes.Bin;
			} else if (value instanceof String) {
				return RunTimeTypes.String;
			} else if (value instanceof Boolean) {
				return RunTimeTypes.Boolean;
			} else if (value == null) {
				return RunTimeTypes.NULL;
			}
		}  else if (obj instanceof Expr.Cup) {
			return RunTimeTypes.cup;
		} else if (obj instanceof Expr.Pocket) {
			return RunTimeTypes.pkt;
		} else if (obj instanceof Expr.Box) {
			return RunTimeTypes.box;
		} else if (obj instanceof Expr.Knot) {
			return RunTimeTypes.knt;
		} else if (obj instanceof Expr.LiteralChar) {
			return RunTimeTypes.Char;
		}else if (obj instanceof Expr.Variable) {
			Object lookUpVariable = interpreter.lookUpVariable(((Expr.Variable)obj).name, (Expr.Variable)obj);
			if(lookUpVariable instanceof Instance) {
				if(((Instance)lookUpVariable).boxClass instanceof BoxClass) {
					if(((BoxClass)((Instance)lookUpVariable).boxClass).type == TokenType.BOXCONTAINER ) {
						return RunTimeTypes.box;
					}else if(((BoxClass)((Instance)lookUpVariable).boxClass).type == TokenType.CUPCONTAINER ) {
						return RunTimeTypes.cup;
					}else if(((BoxClass)((Instance)lookUpVariable).boxClass).type == TokenType.POCKETCONTAINER ) {
						return RunTimeTypes.pkt;
					}
				}
			}
			
		}else if (obj instanceof Integer) {
			return RunTimeTypes.Int;
		} else if (obj instanceof Double) {
			return RunTimeTypes.Double;
		} else if (obj instanceof Bin) {
			return RunTimeTypes.Bin;
		} else if (obj instanceof String) {
			return RunTimeTypes.String;
		} else if (obj instanceof Boolean) {
			return RunTimeTypes.Boolean;
		} else if (obj == null) {
			return RunTimeTypes.NULL;
		}

		return RunTimeTypes.Any;
	}

	static RunTimeTypes getTypeBasedOfToken(Token type2) {
		if (type2 != null) {
			if (type2.type == TokenType.BOXXX)
				return RunTimeTypes.box;
			else if (type2.type == TokenType.CUP)
				return RunTimeTypes.cup;
			else if (type2.type == TokenType.POCKET)
				return RunTimeTypes.pkt;
			else if (type2.type == TokenType.FUN)
				return RunTimeTypes.Function;
			else if (type2.type == TokenType.IDENTIFIER) {
				return RunTimeTypes.Any;
			}else if (type2.type == TokenType.KNOT) {
				return RunTimeTypes.knt;
			}else if (type2.type == TokenType.TONK) {
				return RunTimeTypes.tnk;
			}else if (type2.type == TokenType.PUC) {
				return RunTimeTypes.puc;
			}else if (type2.type == TokenType.XOB) {
				return RunTimeTypes.xob;
			}else if (type2.type == TokenType.TEKCOP) {
				return RunTimeTypes.tkp;
			}else if (type2.type == TokenType.BOX) {
				return RunTimeTypes.box;
			}
				
			
		}
		return RunTimeTypes.Any;
	}

	static RunTimeTypes getTypeBasedOfTokenType(TokenType type2) {
		
			if (type2 == TokenType.BOXXX)
				return RunTimeTypes.box;
			else if (type2 == TokenType.BOXCONTAINER)
				return RunTimeTypes.box;
			else if (type2 == TokenType.CUP)
				return RunTimeTypes.cup;
			else if (type2 == TokenType.CUPCONTAINER)
				return RunTimeTypes.cup;
			else if (type2 == TokenType.POCKET)
				return RunTimeTypes.pkt;
			else if (type2 == TokenType.POCKETCONTAINER)
				return RunTimeTypes.pkt;
			else if (type2 == TokenType.FUN)
				return RunTimeTypes.Function;
			return null;
			
		
	}



}
