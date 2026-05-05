package Box.math.Interpreter;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Hashtable;
import Box.Interpreter.RuntimeError;
import Box.math.BoxMathTokenType;
import Box.math.Syntax.Term;
import Box.math.Syntax.Term.Assignment;
import Box.math.Syntax.Term.Binary;
import Box.math.Syntax.Term.Derivitive;
import Box.math.Syntax.Term.E;
import Box.math.Syntax.Term.Factorial;
import Box.math.Syntax.Term.Function;
import Box.math.Syntax.Term.Integral;
import Box.math.Syntax.Term.Literal;
import Box.math.Syntax.Term.Ln;
import Box.math.Syntax.Term.Log;
import Box.math.Syntax.Term.Mono;
import Box.math.Syntax.Term.NotDefined;
import Box.math.Syntax.Term.PI;
import Box.math.Syntax.Term.ToDerive;
import Box.math.Syntax.Term.Unary;
import Box.math.Syntax.Term.Variable;
import Box.math.Token.MathToken;

public class MathDeriver implements Term.Visitor<Object> {
	private Hashtable<String, Term> values = new Hashtable<String, Term>();
	private Hashtable<String, Term> base = new Hashtable<String, Term>();
	private Variable replace = null;
	private Variable replaceWith = null;
	private boolean shouldReplace = false;

	public MathDeriver() {

	}

	public Object derive(Term term, boolean derive, boolean integrate, Term.Variable replace, Term.Variable replaceWith,
			boolean shouldReplace) {
		this.replace = replace;
		this.replaceWith = replaceWith;
		this.shouldReplace = shouldReplace;
		Object evaluate = null;
		try {

			evaluate = evaluate(term,  derive,  integrate);
			this.replace = null;
			this.replaceWith = null;
			this.shouldReplace = false;
		} catch (RuntimeError e) {
			System.out.println(e.getMessage());
		}
		return evaluate;

	}

	private Object evaluate(Term term, boolean derive, boolean integrate) {
		return term.accept(this,  derive,  integrate);
	}

	@Override
	public Object visitAssignmentTerm(Assignment term, boolean derive, boolean integrate) {
		if (term.name instanceof Term.Function) {
			Term functionBody = ((Term.Function) term.name).functionBody;
			values.put(((Term.Function) term.name).name.lexeme, term.value);
			base.put(((Term.Function) term.name).name.lexeme, ((Term.Function) term.name).functionBody);
		} else if (term.name instanceof Term.Variable) {
			values.put(((Term.Variable) term.name).name.lexeme, term.value);
		}
		return null;
	}

	private Object lookUp(Function function) {

		Term term = values.get(function.name.lexeme);
		if (term == null) {
			MathToken name = new MathToken(BoxMathTokenType.FUN, function.name.lexeme, null, -1, -1, -1, -1);
			return new Term.NotDefined(name);
		}

		return term;
	}

	@Override
	public Object visitBinaryTerm(Binary term, boolean derive, boolean integrate) {
		Object left = null;
		Object right = null;
		Object leftDerivitive = null;
		Object rightDerivitive = null;
		Object total = null;
		MathToken operator = term.operator;

		switch (operator.type) {
		case PLUS:
			left = evaluate(term.left,  derive,  integrate);
			right = evaluate(term.right,  derive,  integrate);
			if (derive) {
				if (left instanceof Term.Function && right instanceof Term.Function) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, (Term) rightBodyEval);
				} else if (left instanceof Term.Function && right instanceof Double) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					return (Term) leftBodyEval;
				} else if (left instanceof Double && right instanceof Term.Function) {
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return (Term) rightBodyEval;
				} else if (left instanceof Double && right instanceof Double) {
					return new Term.Literal(0.0);
				} else if (left instanceof Term.Variable && right instanceof Double) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt))
						return new Term.Literal(1.0);
					else
						return new Term.Literal(0.0);
				} else if (left instanceof Double && right instanceof Term.Variable) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt))
						return new Term.Literal(1.0);
					else
						return new Term.Literal(0.0);
				} else if (left instanceof Term.Variable && right instanceof Term.Variable) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					double dL = ((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt) ? 1.0 : 0.0;
					double dR = ((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt) ? 1.0 : 0.0;
					return new Term.Literal(dL + dR);
				}
			} else if (integrate) {
				return new Term.Binary(buildIntegral(left), term.operator, buildIntegral(right));
			} else {
				if (left instanceof Term.Function && right instanceof Term.Function) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, (Term) rightBodyEval);
				} else if (left instanceof Term.Function && right instanceof Double) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, new Term.Literal((Double) right));
				} else if (left instanceof Double && right instanceof Term.Function) {
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary(new Term.Literal((Double) left), term.operator, (Term) rightBodyEval);
				} else if (left instanceof Double && right instanceof Double) {
					return new Term.Binary(new Term.Literal((Double) left), term.operator, new Term.Literal((Double) right));
				} else if (left instanceof Term.Variable && right instanceof Double) {
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(replace != null ? replace.name.lexeme : "x")) {
						if (shouldReplace) return new Term.Binary(replaceWith, term.operator, new Term.Literal((Double) right));
						else return new Term.Binary((Term) left, term.operator, new Term.Literal((Double) right));
					} else {
						return new Term.Binary((Term) left, term.operator, new Term.Literal((Double) right));
					}
				} else if (left instanceof Term.Variable && right instanceof Term.Variable) {
					return new Term.Binary((Term) left, term.operator, (Term) right);
				}
			}
			System.out.println("totalPLUS: " + total);
			break;
		case MINUS:
			left = evaluate(term.left,  derive,  integrate);
			right = evaluate(term.right,  derive,  integrate);
			if (derive) {
				if (left instanceof Term.Function && right instanceof Term.Function) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, (Term) rightBodyEval);
				} else if (left instanceof Term.Function && right instanceof Double) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					return (Term) leftBodyEval;
				} else if (left instanceof Double && right instanceof Term.Function) {
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return (Term) rightBodyEval;
				} else if (left instanceof Double && right instanceof Double) {
					return new Term.Literal(0.0);
				} else if (left instanceof Term.Variable && right instanceof Double) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt))
						return new Term.Literal(1.0);
					else
						return new Term.Literal(0.0);
				} else if (left instanceof Double && right instanceof Term.Variable) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					return new Term.Literal(((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt) ? -1.0 : 0.0);
				} else if (left instanceof Term.Variable && right instanceof Term.Variable) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					double dL = ((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt) ? 1.0 : 0.0;
					double dR = ((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt) ? 1.0 : 0.0;
					return new Term.Literal(dL - dR);
				}
			} else if (integrate) {
				return new Term.Binary(buildIntegral(left), term.operator, buildIntegral(right));
			} else {
				if (left instanceof Term.Function && right instanceof Term.Function) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, (Term) rightBodyEval);
				} else if (left instanceof Term.Function && right instanceof Double) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, new Term.Literal((Double) right));
				} else if (left instanceof Double && right instanceof Term.Function) {
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary(new Term.Literal((Double) left), term.operator, (Term) rightBodyEval);
				} else if (left instanceof Double && right instanceof Double) {
					return new Term.Binary(new Term.Literal((Double) left), term.operator, new Term.Literal((Double) right));
				} else if (left instanceof Term.Variable && right instanceof Double) {
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(replace != null ? replace.name.lexeme : "x")) {
						if (shouldReplace) return new Term.Binary(replaceWith, term.operator, new Term.Literal((Double) right));
						else return new Term.Binary((Term) left, term.operator, new Term.Literal((Double) right));
					} else {
						return new Term.Binary((Term) left, term.operator, new Term.Literal((Double) right));
					}
				} else if (left instanceof Term.Variable && right instanceof Term.Variable) {
					return new Term.Binary((Term) left, term.operator, (Term) right);
				}
			}
			System.out.println("totalMINUS: " + total);
			break;
		case POWER:
			left = evaluate(term.left,  derive,  integrate);
			right = evaluate(term.right,  derive,  integrate);
			if (derive) {
				if (left instanceof Double && right instanceof Double) {
					return new Term.Literal(0.0);
				} else if (left instanceof Term.Variable && right instanceof Double) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					Double n = (Double) right;
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					MathToken thepower = new MathToken(BoxMathTokenType.POWER, "^", null, -1, -1, -1, 1);
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt)) {
						Term base = shouldReplace ? replaceWith : (Term) left;
						Term xToNm1 = new Term.Binary(base, thepower, new Term.Literal(n - 1));
						return new Term.Binary(new Term.Literal(n), times, xToNm1);
					} else {
						return new Term.Binary((Term.Variable) left, term.operator, new Term.Literal(n));
					}
				} else if (left instanceof Term.Function && right instanceof Double) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					Double n = (Double) right;
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					MathToken thepower = new MathToken(BoxMathTokenType.POWER, "^", null, -1, -1, -1, 1);
					Term xToNm1 = new Term.Binary((Term) leftBodyEval, thepower, new Term.Literal(n - 1));
					return new Term.Binary(new Term.Literal(n), times, xToNm1);
				} else if (left instanceof Double && right instanceof Term.Variable) {
					// d/dx(c^x) = c^x * ln(c)
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt)) {
						MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
						MathToken lnTok = new MathToken(BoxMathTokenType.LN,    "ln", null, -1, -1, -1, 1);
						Term lnC    = new Term.Ln(lnTok, new Term.Literal((Double) left));
						Term cPowX  = new Term.Binary(new Term.Literal((Double) left), term.operator, (Term) right);
						return new Term.Binary(cPowX, times, lnC);
					}
					return new Term.Literal(0.0);
				} else if (left instanceof Term.Variable && right instanceof Term.Variable) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					boolean lIsX = ((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt);
					boolean rIsX = ((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt);
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					MathToken lnTok = new MathToken(BoxMathTokenType.LN,    "ln", null, -1, -1, -1, 1);
					if (lIsX && rIsX) {
						// d/dx(x^x) = x^x * (ln(x) + 1)
						MathToken plus_t = new MathToken(BoxMathTokenType.PLUS, "+", null, -1, -1, -1, 1);
						Term lnX      = new Term.Ln(lnTok, (Term) left);
						Term lnXplus1 = new Term.Binary(lnX, plus_t, new Term.Literal(1.0));
						Term xPowX    = new Term.Binary((Term) left, term.operator, (Term) right);
						return new Term.Binary(xPowX, times, lnXplus1);
					} else if (lIsX) {
						// d/dx(x^y) = y * x^(y-1)
						MathToken minusTok = new MathToken(BoxMathTokenType.MINUS, "-", null, -1, -1, -1, 1);
						MathToken powTok   = new MathToken(BoxMathTokenType.POWER, "^", null, -1, -1, -1, 1);
						Term nMinus1 = new Term.Binary((Term) right, minusTok, new Term.Literal(1.0));
						Term xToNm1  = new Term.Binary((Term) left, powTok, nMinus1);
						return new Term.Binary((Term) right, times, xToNm1);
					} else {
						// d/dx(y^x) = y^x * ln(y)
						Term lnY   = new Term.Ln(lnTok, (Term) left);
						Term yPowX = new Term.Binary((Term) left, term.operator, (Term) right);
						return new Term.Binary(yPowX, times, lnY);
					}
				}
			} else if (integrate) {
				if (left instanceof Term.Variable && right instanceof Double) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt)) {
						// ∫x^n dx = x^(n+1) / (n+1)
						double n = (Double) right;
						MathToken powTok = new MathToken(BoxMathTokenType.POWER,  "^", null, -1, -1, -1, 1);
						MathToken divTok = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, 1);
						Term xToNp1 = new Term.Binary((Term) left, powTok, new Term.Literal(n + 1));
						return new Term.Binary(xToNp1, divTok, new Term.Literal(n + 1));
					}
				} else if (left instanceof Double && right instanceof Term.Variable) {
					// ∫c^x dx = c^x / ln(c)
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt)) {
						MathToken lnTok  = new MathToken(BoxMathTokenType.LN,    "ln", null, -1, -1, -1, 1);
						MathToken divTok = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, 1);
						Term lnC   = new Term.Ln(lnTok, new Term.Literal((Double) left));
						Term cPowX = new Term.Binary(new Term.Literal((Double) left), term.operator, (Term) right);
						return new Term.Binary(cPowX, divTok, lnC);
					}
				}
			} else {
				if (left instanceof Double && right instanceof Double) {
					return new Term.Binary(new Term.Literal((Double) left), term.operator, new Term.Literal((Double) right));
				} else if (left instanceof Term.Variable && right instanceof Double) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt)) {
						if (shouldReplace) return new Term.Binary(replaceWith, term.operator, new Term.Literal((Double) right));
						else return new Term.Binary((Term) left, term.operator, new Term.Literal((Double) right));
					} else {
						return new Term.Binary((Term.Variable) left, term.operator, new Term.Literal((Double) right));
					}
				} else if (left instanceof Term.Function && right instanceof Double) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, new Term.Literal((Double) right));
				}
			}
			System.out.println("TotalPOWER: " + total);
			break;
		case DEVIDE:

			left = evaluate(term.left,  derive,  integrate);
			right = evaluate(term.right,  derive,  integrate);
			if (derive) {
				if (left instanceof Double && right instanceof Double) {
					return new Term.Literal(0.0);
				} else if (left instanceof Term.Variable && right instanceof Double) {
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(replace.name.lexeme)) {
						return new Term.Binary(new Term.Literal(1.0), term.operator, new Term.Literal((Double) right));
					} else {
						return new Term.Binary((Term) left, term.operator, new Term.Literal((Double) right));
					}
				} else if (left instanceof Term.Function && right instanceof Double) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, new Term.Literal((Double) right));
				} else if (left instanceof Double && right instanceof Term.Function) {
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary(new Term.Literal((Double) left), term.operator, (Term) rightBodyEval);
				} else if (left instanceof Term.Function && right instanceof Term.Function) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object rightevaluate = lookUp(((Term.Function) right));
					Object leftBodyEvalDerived = evaluate((Term) leftevaluate,  derive,  integrate);
					Object leftBodyEval = evaluate((Term) leftevaluate, false,false);
					Object rightBodyEvalDerived = evaluate((Term) rightevaluate,  derive,  integrate);
					Object rightBodyEval = evaluate((Term) rightevaluate, false,false);
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					Binary rightTimesLeftDerivitive = new Term.Binary((Term) rightBodyEval, times,
							(Term) leftBodyEvalDerived);
					Binary leftTimesRightDerivitive = new Term.Binary((Term) leftBodyEval, times,
							(Term) rightBodyEvalDerived);
					MathToken minus = new MathToken(BoxMathTokenType.MINUS, "-", null, -1, -1, -1, 1);
					Binary rightLeftDMinusrightleftD = new Term.Binary((Term) rightTimesLeftDerivitive, minus,
							(Term) leftTimesRightDerivitive);

					MathToken power = new MathToken(BoxMathTokenType.POWER, "^", null, -1, -1, -1, 1);
					Binary rightPowerTwo = new Term.Binary((Term) rightBodyEval, power, new Term.Literal(2.0));

					MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, 1);
					return new Term.Binary((Term) rightLeftDMinusrightleftD, devide, (Term) rightPowerTwo);

				}
			} else if (integrate) {
				if (right instanceof Double) {
					// ∫(f/c) dx = (1/c) * ∫f dx
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					return new Term.Binary(new Term.Literal(1.0 / (Double) right), times, buildIntegral(left));
				} else if (left instanceof Double && right instanceof Term.Variable) {
					// ∫(c/x) dx = c * ln(x)
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt)) {
						MathToken lnTok  = new MathToken(BoxMathTokenType.LN,    "ln", null, -1, -1, -1, 1);
						MathToken times  = new MathToken(BoxMathTokenType.TIMES, "*",  null, -1, -1, -1, 1);
						Term lnX = new Term.Ln(lnTok, (Term) right);
						return new Term.Binary(new Term.Literal((Double) left), times, lnX);
					}
				}
				// general fallthrough: return unevaluated binary
				if (left instanceof Term && right instanceof Term) {
					return new Term.Binary((Term) left, term.operator, (Term) right);
				}
			} else {
				if (left instanceof Double && right instanceof Double) {
					return new Term.Binary(new Term.Literal((Double) left), term.operator,
							new Term.Literal((Double) right));
				} else if (left instanceof Term.Variable && right instanceof Double) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt)) {
						if(shouldReplace)
						return new Term.Binary(replaceWith, term.operator, new Term.Literal((Double) right));
						else
							return new Term.Binary((Term)left, term.operator, new Term.Literal((Double) right));
					} else {
						return new Term.Binary((Term.Variable) left, term.operator, new Term.Literal((Double) right));
					}
				} else if (left instanceof Term.Function && right instanceof Double) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					return new Term.Binary((Term) leftBodyEval, term.operator, new Term.Literal((Double) right));
				} else if (left instanceof Double && right instanceof Term.Function) {
					Object rightevaluate = lookUp(((Term.Function) right));
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary(new Term.Literal((Double) left), term.operator, (Term) rightBodyEval);
				} else if (left instanceof Term.Function && right instanceof Term.Function) {
					Object leftevaluate = lookUp(((Term.Function) left));
					Object rightevaluate = lookUp(((Term.Function) right));
					Object leftBodyEval = evaluate((Term) leftevaluate,  derive,  integrate);
					Object rightBodyEval = evaluate((Term) rightevaluate,  derive,  integrate);

					return new Term.Binary((Term) leftBodyEval, term.operator, (Term) rightBodyEval);

				}
			}
			System.out.println("totalDEVIDE: " + total);
			break;
		case TIMES:
			left = evaluate(term.left,  derive,  integrate);
			right = evaluate(term.right,  derive,  integrate);
			if (derive) {
				if (left instanceof Term.Function && right instanceof Term.Function) {

					Object leftevaluate = lookUp(((Term.Function) left));
					Object rightevaluate = lookUp(((Term.Function) right));
					Object theleft = evaluate((Term) leftevaluate, false,false);
					Object theright = evaluate((Term) rightevaluate, false,false);
					leftDerivitive = evaluate((Term) leftevaluate,  derive,  integrate);
					rightDerivitive = evaluate((Term) rightevaluate,  derive,  integrate);
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					Binary leftTimes = new Term.Binary((Term) theleft, times, (Term) rightDerivitive);
					Binary rightTimes = new Term.Binary((Term) theright, times, (Term) leftDerivitive);
					MathToken plus = new MathToken(BoxMathTokenType.PLUS, "+", null, -1, -1, -1, 1);
					return new Term.Binary(leftTimes, plus, rightTimes);
				} else if (left instanceof Term.Function && right instanceof Double) {

					Object leftevaluate = lookUp(((Term.Function) left));

					leftDerivitive = evaluate((Term) leftevaluate,  derive,  integrate);
					return new Term.Binary(new Term.Literal((Double) right), term.operator, (Term) leftDerivitive);
				} else if (left instanceof Double && right instanceof Term.Function) {

					Object rightevaluate = lookUp(((Term.Function) right));

					rightDerivitive = evaluate((Term) rightevaluate,  derive,  integrate);
					return new Term.Binary(new Term.Literal((Double) left), term.operator, (Term) rightDerivitive);
				} else if (left instanceof Double && right instanceof Double) {
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					return new Term.Binary(new Term.Literal((Double) left), times, new Term.Literal((Double) right));
				} else if (left instanceof Term.Variable && right instanceof Double) {
					String tgt = replace != null ? replace.name.lexeme : "x";
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt)) {
						return new Term.Literal((Double) right);
					} else {
						MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
						return new Term.Binary((Term) left, times, new Term.Literal((Double) right));
					}
				} else if (left instanceof Term.Variable && right instanceof Term.Variable) {
					// product rule: f'g + fg'
					String tgt = replace != null ? replace.name.lexeme : "x";
					boolean lIsX = ((Term.Variable) left).name.lexeme.equalsIgnoreCase(tgt);
					boolean rIsX = ((Term.Variable) right).name.lexeme.equalsIgnoreCase(tgt);
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					MathToken plus_t = new MathToken(BoxMathTokenType.PLUS, "+", null, -1, -1, -1, 1);
					Term lD = new Term.Literal(lIsX ? 1.0 : 0.0);
					Term rD = new Term.Literal(rIsX ? 1.0 : 0.0);
					return new Term.Binary(
						new Term.Binary(lD, times, (Term) right),
						plus_t,
						new Term.Binary((Term) left, times, rD));
				}
			} else if (integrate) {
				MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
				if (left instanceof Double) {
					// ∫c * f dx = c * ∫f dx
					return new Term.Binary(new Term.Literal((Double) left), times, buildIntegral(right));
				} else if (right instanceof Double) {
					// ∫f * c dx = c * ∫f dx
					return new Term.Binary(new Term.Literal((Double) right), times, buildIntegral(left));
				}
			} else {
				if (left instanceof Term.Function && right instanceof Term.Function) {

					Object leftevaluate = lookUp(((Term.Function) left));
					Object rightevaluate = lookUp(((Term.Function) right));
					Object theleft = evaluate((Term) leftevaluate,  derive,  integrate);
					Object theright = evaluate((Term) rightevaluate,  derive,  integrate);
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					return new Term.Binary((Term) theleft, times, (Term) theright);
				} else if (left instanceof Term.Function && right instanceof Double) {

					Object leftevaluate = lookUp(((Term.Function) left));
					Object theleft = evaluate((Term) leftevaluate,  derive,  integrate);
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					return new Term.Binary(new Term.Literal((Double) right), times, (Term) theleft);
				} else if (left instanceof Double && right instanceof Term.Function) {

					Object rightevaluate = lookUp(((Term.Function) right));
					Object theright = evaluate((Term) rightevaluate,  derive,  integrate);
					MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
					return new Term.Binary(new Term.Literal((Double) left), times, (Term) theright);
				} else if (left instanceof Double && right instanceof Double) {
					return new Term.Binary(new Term.Literal((Double) left), term.operator,
							new Term.Literal((Double) right));
				} else if (left instanceof Term.Variable && right instanceof Double) {
					if (((Term.Variable) left).name.lexeme.equalsIgnoreCase(replace.name.lexeme)) {
						if(shouldReplace)
						return new Term.Binary(replaceWith, term.operator, new Term.Literal((Double) right));
						else
							return new Term.Binary((Term)left, term.operator, new Term.Literal((Double) right));
					} else {
						return new Term.Binary((Term.Variable) left, term.operator, new Term.Literal((Double) right));

					}
				}
			}
			System.out.println("totalTIMES: " + total);
			break;
		default:
			break;
		}
		return total;
	}

	@Override
	public Object visitMonoTerm(Mono term, boolean derive, boolean integrate) {
		MathToken operator = term.operator;

		Object total = null;
		switch (operator.type) {
		case SIN:
			if (derive && !integrate) {
				Object innerD = normalizeVarDeriv(evaluate(term.value, true, false));
				Object inner  = evaluate(term.value, false, false);
				MathToken cos = new MathToken(BoxMathTokenType.COS, "cos", null, -1, -1, -1, 1);
				Mono cosStmt = new Term.Mono((Term) inner, cos);
				if (innerD instanceof Double && (Double) innerD == 1.0) return cosStmt;
				MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
				return new Term.Binary((Term) innerD, times, cosStmt);
			} else if (!derive && !integrate) {
				Object inner = evaluate(term.value, false, false);
				MathToken sin = new MathToken(BoxMathTokenType.SIN, "sin", null, -1, -1, -1, 1);
				return new Term.Mono((Term) inner, sin);
			} else if (!derive && integrate) {
				if (!(term.value instanceof Variable)) return "dont know how to do this yet";
				MathToken cos = new MathToken(BoxMathTokenType.COS, "cos", null, -1, -1, -1, 1);
				MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
				Mono cosStmt = new Term.Mono((Term) term.value, cos);
				return new Term.Binary(new Term.Literal(-1.0), times, cosStmt);
			}
			break;

		case COS:
			if (derive && !integrate) {
				Object innerD = normalizeVarDeriv(evaluate(term.value, true, false));
				Object inner  = evaluate(term.value, false, false);
				MathToken sin = new MathToken(BoxMathTokenType.SIN, "sin", null, -1, -1, -1, 1);
				Mono sinStmt = new Term.Mono((Term) inner, sin);
				MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
				Term negSin = new Term.Binary(new Term.Literal(-1.0), times, sinStmt);
				if (innerD instanceof Double && (Double) innerD == 1.0) return negSin;
				return new Term.Binary((Term) innerD, times, negSin);
			} else if (!derive && !integrate) {
				Object inner = evaluate(term.value, false, false);
				MathToken cos = new MathToken(BoxMathTokenType.COS, "cos", null, -1, -1, -1, 1);
				return new Term.Mono((Term) inner, cos);
			} else if (!derive && integrate) {
				if (!(term.value instanceof Variable)) return "dont know how to do this yet";
				MathToken sin = new MathToken(BoxMathTokenType.SIN, "sin", null, -1, -1, -1, 1);
				return new Term.Mono((Term) term.value, sin);
			}
			break;

		case TAN:
			if (derive && !integrate) {
				Object innerD = normalizeVarDeriv(evaluate(term.value, true, false));
				Object inner  = evaluate(term.value, false, false);
				MathToken cos = new MathToken(BoxMathTokenType.COS, "cos", null, -1, -1, -1, 1);
				Mono cosStmt = new Term.Mono((Term) inner, cos);
				MathToken power = new MathToken(BoxMathTokenType.POWER, "^", null, -1, -1, -1, 1);
				Binary cos2 = new Term.Binary(cosStmt, power, new Term.Literal(2.0));
				MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, 1);
				Binary sec2 = new Term.Binary(new Term.Literal(1.0), devide, cos2);
				if (innerD instanceof Double && (Double) innerD == 1.0) return sec2;
				MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
				return new Term.Binary((Term) innerD, times, sec2);
			} else if (!derive && !integrate) {
				Object inner = evaluate(term.value, false, false);
				MathToken tan = new MathToken(BoxMathTokenType.TAN, "tan", null, -1, -1, -1, 1);
				return new Term.Mono((Term) inner, tan);
			} else if (!derive && integrate) {
				if (!(term.value instanceof Variable)) return "dont know how to do this yet";
				MathToken cos = new MathToken(BoxMathTokenType.COS, "cos", null, -1, -1, -1, 1);
				MathToken ln  = new MathToken(BoxMathTokenType.LN,  "ln",  null, -1, -1, -1, 1);
				MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
				Mono cosStmt = new Term.Mono((Term) term.value, cos);
				Ln lnCos = new Term.Ln(ln, cosStmt);
				return new Term.Binary(new Term.Literal(-1.0), times, lnCos);
			}
			break;

		case SINH:
			if (derive && !integrate) {
				Object innerD = normalizeVarDeriv(evaluate(term.value, true, false));
				Object inner  = evaluate(term.value, false, false);
				MathToken coshTok = new MathToken(BoxMathTokenType.COSH, "cosh", null, -1, -1, -1, 1);
				Mono coshStmt = new Term.Mono((Term) inner, coshTok);
				if (innerD instanceof Double && (Double) innerD == 1.0) return coshStmt;
				MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
				return new Term.Binary((Term) innerD, times, coshStmt);
			} else if (!derive && !integrate) {
				Object inner = evaluate(term.value, false, false);
				MathToken sinhTok = new MathToken(BoxMathTokenType.SINH, "sinh", null, -1, -1, -1, 1);
				return new Term.Mono((Term) inner, sinhTok);
			} else if (!derive && integrate) {
				if (!(term.value instanceof Variable)) return "dont know how to do this yet";
				MathToken coshTok = new MathToken(BoxMathTokenType.COSH, "cosh", null, -1, -1, -1, 1);
				return new Term.Mono((Term) term.value, coshTok);
			}
			break;

		case COSH:
			if (derive && !integrate) {
				Object innerD = normalizeVarDeriv(evaluate(term.value, true, false));
				Object inner  = evaluate(term.value, false, false);
				MathToken sinhTok = new MathToken(BoxMathTokenType.SINH, "sinh", null, -1, -1, -1, 1);
				Mono sinhStmt = new Term.Mono((Term) inner, sinhTok);
				if (innerD instanceof Double && (Double) innerD == 1.0) return sinhStmt;
				MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
				return new Term.Binary((Term) innerD, times, sinhStmt);
			} else if (!derive && !integrate) {
				Object inner = evaluate(term.value, false, false);
				MathToken coshTok = new MathToken(BoxMathTokenType.COSH, "cosh", null, -1, -1, -1, 1);
				return new Term.Mono((Term) inner, coshTok);
			} else if (!derive && integrate) {
				if (!(term.value instanceof Variable)) return "dont know how to do this yet";
				MathToken sinhTok = new MathToken(BoxMathTokenType.SINH, "sinh", null, -1, -1, -1, 1);
				return new Term.Mono((Term) term.value, sinhTok);
			}
			break;

		case TANH:
			if (derive && !integrate) {
				// d/dx(tanh(u)) = sech²(u) * u' = u' / cosh²(u)
				Object innerD = normalizeVarDeriv(evaluate(term.value, true, false));
				Object inner  = evaluate(term.value, false, false);
				MathToken coshTok = new MathToken(BoxMathTokenType.COSH, "cosh", null, -1, -1, -1, 1);
				MathToken powTok  = new MathToken(BoxMathTokenType.POWER,  "^",    null, -1, -1, -1, 1);
				MathToken divTok  = new MathToken(BoxMathTokenType.DEVIDE, "/",    null, -1, -1, -1, 1);
				MathToken times   = new MathToken(BoxMathTokenType.TIMES,  "*",    null, -1, -1, -1, 1);
				Mono coshU = new Term.Mono((Term) inner, coshTok);
				Binary cosh2U = new Term.Binary(coshU, powTok, new Term.Literal(2.0));
				Binary sech2  = new Term.Binary(new Term.Literal(1.0), divTok, cosh2U);
				if (innerD instanceof Double && (Double) innerD == 1.0) return sech2;
				return new Term.Binary((Term) innerD, times, sech2);
			} else if (!derive && !integrate) {
				Object inner = evaluate(term.value, false, false);
				MathToken tanhTok = new MathToken(BoxMathTokenType.TANH, "tanh", null, -1, -1, -1, 1);
				return new Term.Mono((Term) inner, tanhTok);
			} else if (!derive && integrate) {
				// ∫tanh(u) du = ln(cosh(u))
				if (!(term.value instanceof Variable)) return "dont know how to do this yet";
				MathToken coshTok = new MathToken(BoxMathTokenType.COSH, "cosh", null, -1, -1, -1, 1);
				MathToken lnTok   = new MathToken(BoxMathTokenType.LN,   "ln",   null, -1, -1, -1, 1);
				Mono coshU = new Term.Mono((Term) term.value, coshTok);
				return new Term.Ln(lnTok, coshU);
			}
			break;

		default:
			break;

		}

		return total;
	}

	@Override
	public Object visitLnTerm(Ln term, boolean derive, boolean integrate) {
		if (derive) {

			try {
				if (findX(term)) {
					if (term.value instanceof Binary) {

						if (((Binary) term.value).operator.type == BoxMathTokenType.POWER) {
							MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, -1);
							MathToken ln = new MathToken(BoxMathTokenType.LN, "ln", null, -1, -1, -1, -1);
							Ln lnOfleft = new Term.Ln(ln, ((Binary) term.value).left);
							return new Term.Binary(((Binary) term.value).right, times, lnOfleft);
							
						} else if (((Binary) term.value).operator.type == BoxMathTokenType.DEVIDE) {
							MathToken ln = new MathToken(BoxMathTokenType.LN, "ln", null, -1, -1, -1, -1);
							Ln lnOfleft = new Term.Ln(ln, ((Binary) term.value).left);
							Ln lnOfright = new Term.Ln(ln, ((Binary) term.value).right);
							MathToken minus = new MathToken(BoxMathTokenType.MINUS, "-", null, -1, -1, -1, -1);
							return new Term.Binary(lnOfleft, minus, lnOfright);
							
						} else if (((Binary) term.value).operator.type == BoxMathTokenType.TIMES) {
							MathToken ln = new MathToken(BoxMathTokenType.LN, "ln", null, -1, -1, -1, -1);
							Ln lnOfleft = new Term.Ln(ln, ((Binary) term.value).left);
							Ln lnOfright = new Term.Ln(ln, ((Binary) term.value).right);
							MathToken plus = new MathToken(BoxMathTokenType.PLUS, "+", null, -1, -1, -1, -1);
							return new Term.Binary(lnOfleft, plus, lnOfright);
						} else {
							Object evaluate = evaluate(term.value, false,false);
							Object evaluate2 = evaluate(term.value,  derive,  integrate);
							MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
							Binary oneDevidedByEvaluate = new Term.Binary(new Term.Literal(1.0), devide, (Term)evaluate);
							MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, -1);
							return new Term.Binary( oneDevidedByEvaluate, times, (Term)evaluate2);
							

						}

					} else if (term.value instanceof E) {
						return term.value;
					} else if (term.value instanceof Variable) {

						Term eval2Term = lookUp(((Variable) term.value).name);
						if (!(eval2Term instanceof NotDefined)) {

							Object evaluateB = evaluate(eval2Term,  derive,  integrate);
							
							MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
							return new Term.Binary(new Term.Literal(1.0), devide, (Term)evaluateB);
							
						} else if (eval2Term instanceof NotDefined) {
							MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
							return new Term.Binary(new Term.Literal(1.0), devide, new Term.Variable(((NotDefined) eval2Term).name));
							
						}

					} else if (term.value instanceof Log) {

						MathToken lnMathToken = new MathToken(BoxMathTokenType.LN, "ln", null, -1, -1, -1, -1);
						MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
						MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, -1);

						Ln lnTen = new Term.Ln(lnMathToken, new Term.Literal(10.0));
						Binary denominator = new Term.Binary((Term) term.value, times, lnTen);
						Binary dontTakeDerivitiveOf = new Term.Binary(new Term.Literal(1.0), devide, denominator);

						Object derivitiveOfLog = evaluate(term.value,  derive,  integrate);

						Object notDerived = evaluate(dontTakeDerivitiveOf, false,false);

						
						return new Term.Binary( (Term)derivitiveOfLog, times, (Term)notDerived);
						

					} else {
						Object evaluate2 = evaluate(term.value,  derive,  integrate);
						Object evaluate3 = evaluate(term.value, false,false);
						
						
						MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
						Binary oneDevidedByEvaluate = new Term.Binary(new Term.Literal(1.0), devide, (Term)evaluate3);
						MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, -1);
						return new Term.Binary( oneDevidedByEvaluate, times, (Term)evaluate2);
						

					}
				} else {
					Object evaluate = evaluate(term.value, false,false);
					MathToken ln = new MathToken(BoxMathTokenType.LN, "ln", null, -1, -1, -1, -1);
					return new Term.Mono((Term)evaluate, ln);
					
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {

				e.printStackTrace();
			}
			return "howdy";

		} else {
			Object evaluate = evaluate(term.value,  derive,  integrate);
			MathToken ln = new MathToken(BoxMathTokenType.LN, "ln", null, -1, -1, -1, -1);
			return new Term.Mono((Term)evaluate, ln);
		}
	}

	private Term lookUp(MathToken name) {
		if(shouldReplace) {
			if(name.lexeme.equalsIgnoreCase(replace.name.lexeme)) {
				return replaceWith;
			}
		}
		Term term = values.get(name.lexeme);
		if (term == null) {
			return new Term.NotDefined(name);
		}

		return term;
	}
	
	/** When visitVariableTerm returns the variable itself for derive=true, normalize tgt→1.0. */
	private Object normalizeVarDeriv(Object o) {
		if (o instanceof Variable) {
			String tgt = replace != null ? replace.name.lexeme : "x";
			if (((Variable) o).name.lexeme.equalsIgnoreCase(tgt)) return 1.0;
		}
		return o;
	}

	/** Wraps an already-evaluated atom into its antiderivative term wrt `replace`. */
	private Term buildIntegral(Object evaluated) {
		MathToken times = new MathToken(BoxMathTokenType.TIMES,  "*", null, -1, -1, -1, 1);
		MathToken power = new MathToken(BoxMathTokenType.POWER,  "^", null, -1, -1, -1, 1);
		MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, 1);
		Term xVar = (replace != null) ? replace
				: new Term.Variable(new MathToken(BoxMathTokenType.IDENTIFIER, "x", null, -1, -1, -1, 1));
		if (evaluated instanceof Double) {
			return new Term.Binary(new Term.Literal((Double) evaluated), times, xVar);
		}
		if (evaluated instanceof BigDecimal) {
			return new Term.Binary(new Term.Literal(((BigDecimal) evaluated).doubleValue()), times, xVar);
		}
		if (evaluated instanceof Variable) {
			String vName = ((Variable) evaluated).name.lexeme;
			String tName = (replace != null) ? replace.name.lexeme : "x";
			if (vName.equalsIgnoreCase(tName)) {
				Term xSq = new Term.Binary((Variable) evaluated, power, new Term.Literal(2.0));
				return new Term.Binary(xSq, devide, new Term.Literal(2.0));
			} else {
				return new Term.Binary((Variable) evaluated, times, xVar);
			}
		}
		if (evaluated instanceof Term) return (Term) evaluated;
		return new Term.Literal(0.0);
	}

	private boolean findX(Term value) throws IllegalArgumentException, IllegalAccessException {

		if (value instanceof Variable) {
			String target = (replace != null) ? replace.name.lexeme : "x";
			if (((Variable) value).name.lexeme.equalsIgnoreCase(target))
				return true;
		}

		Field[] declaredFields = value.getClass().getDeclaredFields();
		boolean found = false;
		for (Field field : declaredFields) {
			if (field.getType().equals(Term.class)) {
				found = findX((Term) field.get(value));
			}
			if (found)
				break;
		}

		return found;
	}
	@Override
	public Object visitLogTerm(Log term, boolean derive, boolean integrate) {
		if (derive) {

			try {
				if (findX(term)) {
					if (term.valueBase instanceof Binary) {

						if (((Binary) term.valueBase).operator.type == BoxMathTokenType.POWER) {
							MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, -1);
							MathToken log = new MathToken(BoxMathTokenType.LOG, "log", null, -1, -1, -1, -1);
							Log logOfleft = new Term.Log(log, ((Binary) term.valueBase).left,term.value);
							return new Term.Binary(((Binary) term.valueBase).right, times, logOfleft);
							
						} else if (((Binary) term.valueBase).operator.type == BoxMathTokenType.DEVIDE) {
							MathToken log = new MathToken(BoxMathTokenType.LOG, "log", null, -1, -1, -1, -1);
							Log logOfleft = new Term.Log(log, ((Binary) term.valueBase).left,term.value);
							Log logOfright = new Term.Log(log, ((Binary) term.valueBase).right,term.value);
							MathToken minus = new MathToken(BoxMathTokenType.MINUS, "-", null, -1, -1, -1, -1);
							return new Term.Binary(logOfleft, minus, logOfright);
							
						} else if (((Binary) term.valueBase).operator.type == BoxMathTokenType.TIMES) {
							MathToken log = new MathToken(BoxMathTokenType.LOG, "log", null, -1, -1, -1, -1);
							Log logOfleft = new Term.Log(log, ((Binary) term.valueBase).left,term.value);
							Log logOfright = new Term.Log(log, ((Binary) term.valueBase).right,term.value);
							MathToken plus = new MathToken(BoxMathTokenType.PLUS, "+", null, -1, -1, -1, -1);
							return new Term.Binary(logOfleft, plus, logOfright);
						} else {
							Object evaluate = evaluate(term.valueBase, false,false);
							Object evaluate2 = evaluate(term.valueBase,  derive,  integrate);
							MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
							Binary oneDevidedByEvaluate = new Term.Binary(new Term.Literal(1.0), devide, (Term)evaluate);
							MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, -1);
							return new Term.Binary( oneDevidedByEvaluate, times, (Term)evaluate2);
							

						}

					} else if (term.valueBase instanceof Variable) {

						Term eval2Term = lookUp(((Variable) term.valueBase).name);
						if (!(eval2Term instanceof NotDefined)) {

							Object evaluateB = evaluate(eval2Term,  derive,  integrate);
							
							MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
							return new Term.Binary(new Term.Literal(1.0), devide, (Term)evaluateB);
							
						} else if (eval2Term instanceof NotDefined) {
							MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
							return new Term.Binary(new Term.Literal(1.0), devide, new Term.Variable(((NotDefined) eval2Term).name));
							
						}

					} else if (term.valueBase instanceof Ln) {

						MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
						MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, -1);

						Object derivitiveOfLog = evaluate(term.valueBase,  derive,  integrate);

						Object notDerived = evaluate(term.valueBase, false,false);
						Binary dontTakeDerivitiveOf = new Term.Binary(new Term.Literal(1.0), devide,(Term) notDerived);
						
						return new Term.Binary( (Term)dontTakeDerivitiveOf, times, (Term)derivitiveOfLog);
						

					} else {
						
						MathToken lnMathToken = new MathToken(BoxMathTokenType.LN, "ln", null, -1, -1, -1, -1);
						MathToken devide = new MathToken(BoxMathTokenType.DEVIDE, "/", null, -1, -1, -1, -1);
						MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, -1);

						Ln lnTen = new Term.Ln(lnMathToken, new Term.Literal(10.0));
						Binary denominator = new Term.Binary((Term) term.value, times, lnTen);
						Binary dontTakeDerivitiveOf = new Term.Binary(new Term.Literal(1.0), devide, denominator);

						Object derivitiveOfLog = evaluate(term.value,  derive,  integrate);

						Object notDerived = evaluate(dontTakeDerivitiveOf, false,false);

						
						return new Term.Binary( (Term)derivitiveOfLog, times, (Term)notDerived);

					}
				} else {
					Object evaluate = evaluate(term.valueBase, false,false);
					MathToken log = new MathToken(BoxMathTokenType.LOG, "log", null, -1, -1, -1, -1);
					return new Term.Mono((Term)evaluate, log);
					
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {

				e.printStackTrace();
			}
			return "howdy";

		} else {
			Object evaluate = evaluate(term.valueBase,  derive,  integrate);
			MathToken log = new MathToken(BoxMathTokenType.LOG, "log", null, -1, -1, -1, -1);
			return new Term.Mono((Term)evaluate, log);
		}
	}

	@Override
	public Object visitFactorialTerm(Factorial term, boolean derive, boolean integrate) {
		return term;  // treat as atom; symbolic differentiation of factorial is not defined here
	}

	@Override
	public Object visitUnaryTerm(Unary term, boolean derive, boolean integrate) {
		MathToken times = new MathToken(BoxMathTokenType.TIMES, "*", null, -1, -1, -1, 1);
		if (integrate) {
			Object evalInner = evaluate(term.right, false, true);
			return new Term.Binary(new Term.Literal(-1.0), times, buildIntegral(evalInner));
		}
		Object inner = evaluate(term.right, derive, integrate);
		if (inner instanceof Double) return new Term.Literal(-((Double) inner));
		return new Term.Binary(new Term.Literal(-1.0), times, (Term) inner);
	}

	@Override
	public Object visitFunctionTerm(Function term, boolean derive, boolean integrate) {

		return term;
	}

	@Override
	public Object visitDerivitiveTerm(Derivitive term, boolean derive, boolean integrate) {
		return evaluate(term.function, true, false);
	}

	@Override
	public Object visitToDeriveTerm(ToDerive term, boolean derive, boolean integrate) {
		return evaluate(term.left, true,false);

	}

	@Override
	public Object visitVariableTerm(Variable term, boolean derive, boolean integrate) {
		
		if(shouldReplace) {
			if(term.name.lexeme.equalsIgnoreCase(replace.name.lexeme)){
				return replaceWith;
			}else
				return term;
		}else
		return term;
	}

	@Override
	public Object visitLiteralTerm(Literal term, boolean derive, boolean integrate) {

		return term.literal;
	}

	@Override
	public Object visitETerm(E term, boolean derive, boolean integrate) {

		return Math.E;
	}

	@Override
	public Object visitPITerm(PI term, boolean derive, boolean integrate) {

		return Math.PI;
	}

	@Override
	public Object visitNotDefinedTerm(NotDefined term, boolean derive, boolean integrate) {

		return term;
	}

	@Override
	public Object visitIntegralTerm(Integral term, boolean derive, boolean integrate) {
		Variable saved = this.replace;
		this.replace = new Term.Variable(term.toIntegrateOver);
		Object result = evaluate(term.function, false, true);
		this.replace = saved;
		return result;
	}

}
