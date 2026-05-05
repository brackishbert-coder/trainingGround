package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Interpreter.KnotRunner.Conditions;
import Box.Interpreter.KnotRunner.StmtEx;
import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;
import Parser.Expr.Tonk;
import Parser.Stmt;

public class KnotRunner {

	public class Conditions {
		private List<Condition> conds = new ArrayList<>();

		public void add(Condition conditions) {
			conds.add(conditions);

		}

		public boolean checkIfStartIncluded(int start) {
			for (Condition condition : conds) {
				if (condition.indexStart == start)
					return true;
			}
			return false;
		}

		public boolean checkIfEndIncluded(int end) {
			for (Condition condition : conds) {
				if (condition.indexTrue == end)
					return true;
			}
			return false;
		}

		public int size() {
			return conds.size();
		}

		public Condition get(int i) {
			return conds.get(i);
		}

		public boolean isIncluded(int i, Condition object) {

			if (conds.get(i).indexStart <= object.indexStart && conds.get(i).indexTrue >= object.indexTrue)
				return true;

			return false;
		}

		public boolean IsStart(int condsIndex, int start) {
			if (conds.get(condsIndex).indexStart == start)
				return true;
			return false;
		}

		@Override
		public String toString() {
			String str = "";
			for (Condition condition : conds) {
				str += condition.toString() + "\n";
			}
			return str;
		}

		public boolean notIncluded(Condition c) {
			for (Condition condition : conds) {
				if (condition.indexStart == c.indexStart && condition.indexTrue == c.indexTrue)
					return false;

			}
			return true;
		}

		public int getEnd(int start) {
			for (Condition condition : conds) {
				if (condition.indexStart == start)
					return condition.indexTrue;

			}
			return -1;
		}

		public int getStartForMatchingIdent(String lexeme) {
			for (Condition condition : conds) {
				if (condition.lexemeStart.equals(lexeme))
					return condition.indexStart;
			}
			return -1;
		}

		public int getTrueForMatchingIdent(String lexeme) {
			for (Condition condition : conds) {
				if (condition.lexemeStart.equals(lexeme))
					return condition.indexTrue;
			}
			return -1;
		}

		public boolean checkIfIncludedInCondition(int i) {
			for (Condition condition : conds) {
				if (condition.indexStart <= i && condition.indexTrue >= i)
					return true;
			}

			return false;
		}

		public int getFalseForMatching(String lexeme) {
			for (Condition condition : conds) {
				if (condition.lexemeStart.equals(lexeme))
					return condition.indexFalse;
			}
			return -1;
		}

	}

	public class Condition {
		int indexStart;
		int indexTrue;
		String lexemeStart;
		String lexemeTrue;
		String lexemeFalse;
		int indexFalse;

		public Condition(String lexemeStart, String lexemeTrue, String lexemeFalse, int indexStart, int indexTrue,
				int indexFalse) {
			this.lexemeStart = lexemeStart;
			this.lexemeTrue = lexemeTrue;
			this.lexemeFalse = lexemeFalse;
			this.indexStart = indexStart;
			this.indexTrue = indexTrue;
			this.indexFalse = indexFalse;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "lexemeStart " + lexemeStart + " lexemeTrue " + lexemeTrue + " lexemeFalse " + lexemeFalse
					+ " indexStart " + indexStart + " indexTrue " + indexTrue + " indexFalse " + indexFalse;
		}
	}

	public  class StmtEx {
		 List<Stmt> stmts = new ArrayList<Stmt>();
		 List<Boolean> toRun = new ArrayList<Boolean>();

		public StmtEx(List<Stmt> stmts) {
			this.stmts = stmts;
			for (int i = 0; i < stmts.size();i++) {
				toRun.add(true);
			} 
			
		}

		public Stmt get(int index) {
			return stmts.get(index);
		}

		public Boolean getToRanAt(int index) {
			return toRun.get(index);
		}

		public void setToRan(int index, boolean torun) {
			toRun.set(index, torun);
		}

		public int size() {
			return stmts.size();
		}

		public List<Stmt> reversed() {
			return stmts.reversed();
		}

		public int indexOf(Stmt stmt) {
			return stmts.indexOf(stmt);
		}

		public List<Stmt> getStmts() {
			return stmts;
		}
	}

	private StmtEx expression;
	Conditions condForward = new Conditions();
	Conditions condBackward = new Conditions();
	private Interpreter interp;

	public KnotRunner(Expr expr, List<Stmt> expression, Interpreter interp) {
		this.expression = new StmtEx(expression);
		this.interp = interp;
		findConditionsForward();
		findConditionsBackward();
		
	}



	public ArrayList<Object> runKnot() {
		int count = 0;
		if (!interp.isForward())
			count = expression.size() - 1;
		ArrayList<Object> notnull = new ArrayList<>();
		while (true) {

			if (interp.isForward()) {
				if (expression.get(count) instanceof Stmt.Expression) {
					if (((Stmt.Expression) expression.get(count)).expression instanceof Expr.CupOpen) {
						String lexeme = ((Expr.CupOpen) ((Stmt.Expression) expression
								.get(count)).expression).ctrl.identifierToken.lexeme;

						if (checkHasForwardConditions(lexeme)) {
							if (checkConditionsForward(count, lexeme))
								count = gotoConditionsForwardMet(lexeme) - 1;
							else
								count = gotoConditionsForwardNotMet(lexeme) - 1;
						}

					} else if (((Stmt.Expression) expression.get(count)).expression instanceof Expr.PocketOpen) {
						String lexeme = ((Expr.PocketOpen) ((Stmt.Expression) expression
								.get(count)).expression).ctrl.identifierToken.lexeme;

						if (checkHasForwardConditions(lexeme)) {
							if (checkConditionsForward(count, lexeme))
								count = gotoConditionsForwardMet(lexeme) - 1;
							else
								count = gotoConditionsForwardNotMet(lexeme) - 1;
						}

					} else if (((Stmt.Expression) expression.get(count)).expression instanceof Expr.CupClosed) {
						String lexeme = ((Expr.CupClosed) ((Stmt.Expression) expression
								.get(count)).expression).ctrl.reifitnediToken.lexeme;
						lexeme=reverse(lexeme);
						if (checkHasForwardConditions(lexeme)) {
							if (count == expression.size() - 1) {
								if (checkConditionsForward(count, lexeme)) {
									interp.setForward(!interp.isForward());
								} else {
									break;
								}
							} else {
								if (checkConditionsForward(count, lexeme)) {
									count = gotoConditionsForwardMet(lexeme) - 1;
								} else {
									count = gotoConditionsForwardNotMet(lexeme) - 1;
								}
							}
						}
					} else if (((Stmt.Expression) expression.get(count)).expression instanceof Expr.PocketClosed) {
						String lexeme = ((Expr.PocketClosed) ((Stmt.Expression) expression
								.get(count)).expression).ctrl.reifitnediToken.lexeme;

						lexeme=reverse(lexeme);
						if (checkHasForwardConditions(lexeme)) {
							if (count == expression.size() - 1) {
								if (checkConditionsForward(count, lexeme)) {
									interp.setForward(!interp.isForward());
								} else {
									break;
								}
							} else {
								if (checkConditionsForward(count, lexeme)) {
									count = gotoConditionsForwardMet(lexeme) - 1;
								} else {
									count = gotoConditionsForwardNotMet(lexeme) - 1;
								}
							}
						}

					} else {

						Stmt stmt = expression.get(count);
						System.err.println("1expr: "+expression.get(count)+" toRan: " +expression.getToRanAt(count));
						if (expression.getToRanAt(count)) {
							notnull.add(interp.execute(stmt));
						}
						if (stmt instanceof Stmt.Rav || stmt instanceof Stmt.Var) {
							expression.setToRan(count, false);
						}
					}
				} else {

					Stmt stmt = expression.get(count);
					System.err.println("2expr: "+expression.get(count)+" toRan: " +expression.getToRanAt(count));
					if (expression.getToRanAt(count)) {
						notnull.add(interp.execute(stmt));
					}
					if (stmt instanceof Stmt.Rav || stmt instanceof Stmt.Var) {
						expression.setToRan(count, false);
					}
				}
			} else {
				if (expression.get(count) instanceof Stmt.Expression) {
					if (((Stmt.Expression) expression.get(count)).expression instanceof Expr.CupOpen) {
						String lexeme = ((Expr.CupOpen) ((Stmt.Expression) expression
								.get(count)).expression).ctrl.identifierToken.lexeme;
						lexeme=reverse(lexeme);
						if (checkHasBackwardConditions(lexeme)) {
							if (count == 0) {
								if (checkConditionsBackward(count, lexeme)) {
									interp.setForward(!interp.isForward());
								} else {
									break;
								}
							} else {
								if (checkConditionsBackward(count, lexeme)) {
									count = gotoConditionsBackwardMet(lexeme)+1;
								} else {
									count = gotoConditionsBackwardNotMet(lexeme)+1;
								}
							}
						}

					} else if (((Stmt.Expression) expression.get(count)).expression instanceof Expr.PocketOpen) {
						String lexeme = ((Expr.PocketOpen) ((Stmt.Expression) expression
								.get(count)).expression).ctrl.identifierToken.lexeme;
						lexeme=reverse(lexeme);
						if (checkHasBackwardConditions(lexeme)) {
							if (count == 0) {
								if (checkConditionsBackward(count, lexeme)) {
									interp.setForward(!interp.isForward());
								} else {
									break;
								}
							} else {
								if (checkConditionsBackward(count, lexeme)) {
									count = gotoConditionsBackwardMet(lexeme)+1;
								} else {
									count = gotoConditionsBackwardNotMet(lexeme)+1;
								}
							}
						}

					} else if (((Stmt.Expression) expression.get(count)).expression instanceof Expr.CupClosed) {
						String lexeme = ((Expr.CupClosed) ((Stmt.Expression) expression
								.get(count)).expression).ctrl.reifitnediToken.lexeme;

						if (checkHasBackwardConditions(lexeme)) {
							if (checkConditionsBackward(count, lexeme)) {
								count = gotoConditionsBackwardMet(lexeme)+1;
							} else {
								count = gotoConditionsBackwardNotMet(lexeme)+1;
							}
						}

					} else if (((Stmt.Expression) expression.get(count)).expression instanceof Expr.PocketClosed) {
						String lexeme = ((Expr.PocketClosed) ((Stmt.Expression) expression
								.get(count)).expression).ctrl.reifitnediToken.lexeme;

						if (checkHasBackwardConditions(lexeme)) {
							if (checkConditionsBackward(count, lexeme)) {
								count = gotoConditionsBackwardMet(lexeme)+1;
							} else {
								count = gotoConditionsBackwardNotMet(lexeme)+1;
							}
						}

					} else {

						Stmt stmt = expression.get(count);
						System.err.println("3expr: "+expression.get(count)+" toRan: " +expression.getToRanAt(count));
						if (expression.getToRanAt(count)) {
							
							notnull.add(interp.execute(stmt));
						}
						if (stmt instanceof Stmt.Rav || stmt instanceof Stmt.Var) {
							expression.setToRan(count, false);
						}
					}
				} else {

					Stmt stmt = expression.get(count);
					System.err.println("4expr: "+expression.get(count)+" toRan: " +expression.getToRanAt(count));
					if (expression.getToRanAt(count)) {
						notnull.add(interp.execute(stmt));
					}
					if (stmt instanceof Stmt.Rav || stmt instanceof Stmt.Var) {
						expression.setToRan(count, false);
					}
				}
			}

			if (interp.isForward())
				count++;
			else
				count--;
			if (count >= expression.size())
				break;
			if (count < 0)
				break;
		}
		return notnull;
	}

	private int gotoConditionsForwardNotMet(String lexeme) {
		return condForward.getFalseForMatching(reverse(lexeme));
	}

	private int gotoConditionsBackwardNotMet(String lexeme) {
		return condBackward.getFalseForMatching(reverse(lexeme));
	}

	private boolean checkHasForwardConditions(String lexeme) {
		return condForward.getStartForMatchingIdent(lexeme) != -1;
	}

	private boolean checkHasBackwardConditions(String lexeme) {
		return condBackward.getStartForMatchingIdent(lexeme) != -1;
	}

	private int gotoConditionsForwardMet(String lexeme) {

		return condForward.getTrueForMatchingIdent(lexeme);
	}

	private int gotoConditionsBackwardMet(String lexeme) {

		return condBackward.getTrueForMatchingIdent(lexeme);
	}

	private int gotoendIfConditionsNotMet(int count, String lexeme) {
		for (Stmt stmt : expression.getStmts()) {
			if (stmt instanceof Stmt.Expression) {
				if (((Stmt.Expression) stmt).expression instanceof Expr.PocketClosed) {
					Expr.PocketClosed poc = ((Expr.PocketClosed) ((Stmt.Expression) stmt).expression);
					if (poc.ctrl.reifitnediToken.lexeme.equals(lexeme)) {
						count = expression.indexOf(stmt);
						break;
					}
				} else if (((Stmt.Expression) stmt).expression instanceof Expr.CupClosed) {
					Expr.CupClosed poc = ((Expr.CupClosed) ((Stmt.Expression) stmt).expression);
					if (poc.ctrl.reifitnediToken.lexeme.equals(lexeme)) {
						count = expression.indexOf(stmt);
						break;
					}
				}
			}
		}
		return count;
	}

	private int gotobeginIfConditionsNotMet(int count, String lexeme) {

		List<Stmt> reversed = expression.reversed();

		for (Stmt stmt : reversed) {
			if (stmt instanceof Stmt.Expression) {
				if (((Stmt.Expression) stmt).expression instanceof Expr.PocketOpen) {
					Expr.PocketOpen poc = ((Expr.PocketOpen) ((Stmt.Expression) stmt).expression);
					if (poc.ctrl.identifierToken.lexeme.equals(lexeme)) {
						count = expression.indexOf(stmt);
						break;
					}
				} else if (((Stmt.Expression) stmt).expression instanceof Expr.CupOpen) {
					Expr.CupOpen poc = ((Expr.CupOpen) ((Stmt.Expression) stmt).expression);
					if (poc.ctrl.identifierToken.lexeme.equals(lexeme)) {
						count = expression.indexOf(stmt);
						break;
					}
				}
			}
		}
		return count;
	}

	private void findandRunSetup() {

		Conditions setup = new Conditions();
		findSetupForward(setup);
		findSetupBackward(setup);

		for (int i = 0; i < setup.size(); i++) {
			for (int j = setup.get(i).indexStart + 1; j < setup.get(i).indexTrue; j++) {
				interp.execute(expression.get(j));
			}
		}

	}

	private void findSetupBackward(Conditions setup) {

	}

	private void findSetupForward(Conditions setup) {

	}

	private Boolean checkConditionsForward(int count, String lexeme) {
		Boolean evaluate = true;

		int start = condForward.getStartForMatchingIdent(lexeme);
		int end = condForward.getTrueForMatchingIdent(lexeme);

		if (start != -1 && end != -1) {
			for (int i = start + 1; i < end; i++) {
				Boolean temp = ((Boolean) interp.evaluate(expression.get(i)));
				if(temp==null)temp=false;
				evaluate &= temp;
			}
			return evaluate;

		} else
			return false;

	}

	private Boolean checkConditionsBackward(int count, String lexeme) {
		Boolean evaluate = true;

		int start = condBackward.getStartForMatchingIdent(lexeme);
		int end = condBackward.getTrueForMatchingIdent(lexeme);

		if (start != -1 && end != -1) {
			for (int i = start - 1; i > end; i--) {
				Boolean temp = ((Boolean) interp.evaluate(expression.get(i)));
				if(temp==null)temp=false;
				evaluate &= temp;
			}
			return evaluate;

		} else
			return false;

	}

	private int moveConditions(int count) {
		for (int i = count + 1; i < condForward.getEnd(count); i++) {
			count = i;
		}
		return count;
	}

	private String reverse(String str) {
		String nstr = "";
		char ch;
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i); // extracts each character
			nstr = ch + nstr; // adds each character in front of the existing string
		}
		return nstr;
	}

	private void findConditionsForward() {
		for (int i = 0; i < expression.size(); i++) {
			if (expression.get(i) instanceof Stmt.Expression) {
				Stmt.Expression stmt = ((Stmt.Expression) expression.get(i));
				Expr expr = stmt.expression;
				if (expr instanceof Expr.PocketOpen) {
					int startIndex = i;
					Expr.PocketOpen startExpr = ((Expr.PocketOpen) expr);
					String lexemeStart = startExpr.ctrl.identifierToken.lexeme;
					boolean falseFound = false;
					for (int j = i + 1; j < expression.size(); j++) {

						if (expression.get(j) instanceof Stmt.Expression) {
							Stmt.Expression stmt2 = ((Stmt.Expression) expression.get(j));
							Expr expr2 = stmt2.expression;
							if (expr2 instanceof Expr.CupOpen) {
								break;
							} else if (expr2 instanceof Expr.PocketOpen) {
								break;
							} else if (expr2 instanceof Expr.CupClosed) {
								int indexTrue = j;
								Expr.CupClosed exprTrue = ((Expr.CupClosed) expr2);
								String lexemeTrue = exprTrue.ctrl.reifitnediToken.lexeme;
								String reverse = reverse(lexemeStart);

								int count = j + 1;
								while (count < expression.size()) {
									if (expression.get(count) instanceof Stmt.Expression) {
										Stmt.Expression stmt3 = ((Stmt.Expression) expression.get(count));
										Expr expr3 = stmt3.expression;
										if (expr3 instanceof Expr.PocketClosed) {
											Expr.PocketClosed expr4 = ((Expr.PocketClosed) expr3);
											String lexeme = expr4.ctrl.reifitnediToken.lexeme;
											if (reverse.equals(lexeme)) {
												condForward.add(new Condition(lexemeStart, lexemeTrue, lexeme,
														startIndex, indexTrue, count));
												falseFound = true;
												break;
											}
										}
									}
									count++;
								}
							}
						}
						if (falseFound)
							break;
					}
				} else if (expr instanceof Expr.CupOpen) {
					int startIndex = i;
					Expr.CupOpen startExpr = ((Expr.CupOpen) expr);
					String lexemeStart = startExpr.ctrl.identifierToken.lexeme;
					boolean falseFound = false;
					for (int j = i + 1; j < expression.size(); j++) {

						if (expression.get(j) instanceof Stmt.Expression) {
							Stmt.Expression stmt2 = ((Stmt.Expression) expression.get(j));
							Expr expr2 = stmt2.expression;
							if (expr2 instanceof Expr.CupOpen) {
								break;
							} else if (expr2 instanceof Expr.PocketOpen) {
								break;
							} else if (expr2 instanceof Expr.PocketClosed) {
								int indexTrue = j;
								Expr.PocketClosed exprTrue = ((Expr.PocketClosed) expr2);
								String lexemeTrue = exprTrue.ctrl.reifitnediToken.lexeme;
								String reverse = reverse(lexemeStart);

								int count = j + 1;
								while (count < expression.size()) {
									if (expression.get(count) instanceof Stmt.Expression) {
										Stmt.Expression stmt3 = ((Stmt.Expression) expression.get(count));
										Expr expr3 = stmt3.expression;
										if (expr3 instanceof Expr.CupClosed) {
											Expr.CupClosed expr4 = ((Expr.CupClosed) expr3);
											String lexeme = expr4.ctrl.reifitnediToken.lexeme;
											if (reverse.equals(lexeme)) {
												condForward.add(new Condition(lexemeStart, lexemeTrue, lexeme,
														startIndex, indexTrue, count));
												falseFound = true;
												break;
											}
										}
									}
									count++;
								}
							}

						}
						if (falseFound)
							break;

					}
				}
			}
		}

	}

	private void findConditionsBackward() {
		for (int i = expression.size() - 1; i >= 0; i--) {
			if (expression.get(i) instanceof Stmt.Expression) {
				Stmt.Expression stmt = ((Stmt.Expression) expression.get(i));
				Expr expr = stmt.expression;
				if (expr instanceof Expr.PocketClosed) {
					int startIndex = i;
					Expr.PocketClosed startExpr = ((Expr.PocketClosed) expr);
					String lexemeStart = startExpr.ctrl.reifitnediToken.lexeme;
					boolean falseFound = false;
					for (int j = i - 1; j >= 0; j--) {

						if (expression.get(j) instanceof Stmt.Expression) {
							Stmt.Expression stmt2 = ((Stmt.Expression) expression.get(j));
							Expr expr2 = stmt2.expression;
							if (expr2 instanceof Expr.CupClosed) {
								break;
							} else if (expr2 instanceof Expr.PocketOpen) {
								break;
							} else if (expr2 instanceof Expr.CupOpen) {
								int indexTrue = j;
								Expr.CupOpen exprTrue = ((Expr.CupOpen) expr2);
								String lexemeTrue = exprTrue.ctrl.identifierToken.lexeme;
								String reverse = reverse(lexemeStart);

								int count = j - 1;
								while (count >= 0) {
									if (expression.get(count) instanceof Stmt.Expression) {
										Stmt.Expression stmt3 = ((Stmt.Expression) expression.get(count));
										Expr expr3 = stmt3.expression;
										if (expr3 instanceof Expr.PocketOpen) {
											Expr.PocketOpen expr4 = ((Expr.PocketOpen) expr3);
											String lexeme = expr4.ctrl.identifierToken.lexeme;
											if (reverse.equals(lexeme)) {
												condBackward.add(new Condition(lexemeStart, lexemeTrue, lexeme,
														startIndex, indexTrue, count));
												falseFound = true;
												break;
											}
										}
									}
									count--;
								}
							}
						}
						if (falseFound)
							break;
					}
				} else if (expr instanceof Expr.CupClosed) {
					int startIndex = i;
					Expr.CupClosed startExpr = ((Expr.CupClosed) expr);
					String lexemeStart = startExpr.ctrl.reifitnediToken.lexeme;
					boolean falseFound = false;
					for (int j = i - 1; j >= 0; j--) {

						if (expression.get(j) instanceof Stmt.Expression) {
							Stmt.Expression stmt2 = ((Stmt.Expression) expression.get(j));
							Expr expr2 = stmt2.expression;
							if (expr2 instanceof Expr.CupOpen) {
								break;
							} else if (expr2 instanceof Expr.PocketClosed) {
								break;
							} else if (expr2 instanceof Expr.PocketOpen) {
								int indexTrue = j;
								Expr.PocketOpen exprTrue = ((Expr.PocketOpen) expr2);
								String lexemeTrue = exprTrue.ctrl.identifierToken.lexeme;
								String reverse = reverse(lexemeStart);

								int count = j - 1;
								while (count >= 0) {
									if (expression.get(count) instanceof Stmt.Expression) {
										Stmt.Expression stmt3 = ((Stmt.Expression) expression.get(count));
										Expr expr3 = stmt3.expression;
										if (expr3 instanceof Expr.CupOpen) {
											Expr.CupOpen expr4 = ((Expr.CupOpen) expr3);
											String lexeme = expr4.ctrl.identifierToken.lexeme;
											if (reverse.equals(lexeme)) {
												condBackward.add(new Condition(lexemeStart, lexemeTrue, lexeme,
														startIndex, indexTrue, count));
												falseFound = true;
												break;
											}
										}
									}
									count--;
								}
							}

						}
						if (falseFound)
							break;

					}
				}
			}
		}

	}

	private int backTrackToLastClosedPocket(int stop, int count) {
		for (int i = count; i > stop; i--) {
			if (expression.get(i) instanceof Stmt.Expression) {
				if (((Stmt.Expression) expression.get(i)).expression instanceof Expr.PocketClosed)
					return i;
			}
		}
		return count;
	}

	private int goTofirstOpenPocket(int count) {
		for (int i = count; i >= 0; i--) {
			if (expression.get(i) instanceof Stmt.Expression) {
				if (((Stmt.Expression) expression.get(i)).expression instanceof Expr.PocketOpen)
					return i;
			}
		}
		return count;
	}

	private int findNextPocketOpen(int count) {
		while (count >= 0 && !pocketOpen(count)) {
			count--;
		}
		return count;
	}

	private int findNextCupOpen(int count) {
		while (count < expression.size() && !pocketClosed(count)) {
			count++;
		}
		return count;
	}

	private boolean pocketClosed(int count) {
		if (expression.get(count) instanceof Stmt.Expression) {
			return ((Stmt.Expression) expression.get(count)).expression instanceof Expr.CupOpen
					|| ((Stmt.Expression) expression.get(count)).expression instanceof Expr.PocketClosed;
		}
		return false;
	}

	private boolean pocketOpen(int count) {
		if (expression.get(count) instanceof Stmt.Expression) {
			return ((Stmt.Expression) expression.get(count)).expression instanceof Expr.PocketOpen
					|| ((Stmt.Expression) expression.get(count)).expression instanceof Expr.CupClosed;
		}
		return false;
	}

	public void runTonk() {
		// TODO Auto-generated method stub
		
	}

}
