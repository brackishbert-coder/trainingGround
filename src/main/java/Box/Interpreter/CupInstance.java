package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Declaration.StmtDecl;
import Parser.Expr;
import Parser.Expr.Literal;
import Parser.Stmt;

public class CupInstance extends BoxInstance implements ICup {

	protected List<Object> originalBody;

	public CupInstance(BoxCallable boxClass, List<Object> body, Expr expr, Interpreter interpreter) {
		super(boxClass, body, expr, interpreter, true);
		this.originalBody = new ArrayList<>(body);
		evaluateBody();
	}

	/** Skip-init: subclass manages its own body evaluation. originalBody is still captured. */
	protected CupInstance(BoxCallable boxClass, List<Object> body, Expr expr, Interpreter interpreter, boolean _skipInit) {
		super(boxClass, body, expr, interpreter, true);
		this.originalBody = new ArrayList<>(body);
	}

	// Re-execute from the original pre-evaluation statements.
	// Called when a flow bootstraps this cup inside a pkt/tkp body.
	public void execute() {
		ArrayList<Object> temp = new ArrayList<>();
		for (Object object : originalBody) {
			if (!isControl(object)) {
				if (isExpression(object)) {
					temp.add(Boxer.box(interpreter.execute((Declaration) object), interpreter));
				} else {
					temp.add(object);
				}
			} else {
				temp.add(object);
			}
		}
		body = temp;
	}

	private void evaluateBody() {
		lazyBindingCount = 0;
		eagerBindingCount = 0;
		ArrayList<Object> temp = new ArrayList<>();
		for (int i = 0; i < body.size(); i++) {
			Object object = body.get(i);
			if (!isControl(object)) {
				if (isExpression(object)) {
					if (interpreter.inContractContext()) {
						Expr inner = extractInnerExpr(object);
						if (inner instanceof Expr.Get) {
							String root = getChainRoot(inner);
							if (root != null && interpreter.globals.isSeedGlobal(root)) {
								lazyBindingCount++;
								temp.add(inner);
								continue;
							}
						}
					}
					eagerBindingCount++;
					temp.add(Boxer.box(interpreter.execute((Declaration) object), interpreter));
				} else
					temp.add(object);
			} else
				temp.add(object);
		}
		body = temp;
		if (interpreter != null && interpreter.inContractContext()) {
			int total = lazyBindingCount + eagerBindingCount;
			contractConfidence = total > 0 ? (double) lazyBindingCount / total : 0.0;
		}
	}

	private Expr extractInnerExpr(Object object) {
		if (object instanceof Stmt.Expression)
			return ((Stmt.Expression) object).expression;
		if (object instanceof StmtDecl) {
			Stmt state = ((StmtDecl) object).statement;
			if (state instanceof Stmt.Expression)
				return ((Stmt.Expression) state).expression;
		}
		return null;
	}

	protected boolean isExpression(Object object) {
		if (object instanceof Stmt.Expression) {
				return true;
		} else if (object instanceof StmtDecl) {
			Stmt state = ((StmtDecl) object).statement;
			if (state instanceof Stmt.Expression) {
					return true;
			}
		}
		return false;
	}

	protected boolean isControl(Object object) {
		if (object instanceof Stmt.Expression) {
			Expr expr2 = ((Stmt.Expression) object).expression;
			if (expr2 instanceof Expr.PocketOpen || expr2 instanceof Expr.PocketClosed || expr2 instanceof Expr.CupOpen
					|| expr2 instanceof Expr.CupClosed || expr2 instanceof Expr.BoxOpen
					|| expr2 instanceof Expr.BoxClosed) {

				return true;
			}
		} else if (object instanceof StmtDecl) {
			Stmt state = ((StmtDecl) object).statement;
			if (state instanceof Stmt.Expression) {
				Expr expr2 = ((Stmt.Expression) state).expression;
				if (expr2 instanceof Expr.PocketOpen || expr2 instanceof Expr.PocketClosed
						|| expr2 instanceof Expr.CupOpen || expr2 instanceof Expr.CupClosed
						|| expr2 instanceof Expr.BoxOpen || expr2 instanceof Expr.BoxClosed) {

					return true;
				}

			}
		}

		return false;
	}

	public void add(Token operator, Object toadd) {
		withTokenVoid(() -> {
			body.add(body.size() - 1, toadd);
			evaluateBody();
		});
	}

	@Override
	public String toString() {
		String str = "";
		for (Object object : body) {
			str += object.toString() + " ";
		}
		return str;
	}

	public Object remove(Token operator, Object value) {
		return withToken(() -> {
			if (value instanceof Integer) {
				Integer index = ((Integer) value) + 1;
				if (index >= 1 && index <= bodySizeExclude()) {
					return body.remove((int) index);
				} else
					throw new RuntimeError(operator, "index out of bounds");
			} else
				throw new RuntimeError(operator, "must pass an Integer to remove");
		});
	}

	protected int bodySizeExclude() {
		int count = 0;
		for (Object object : body) {
			if (object instanceof Stmt.Expression) {
				Expr expr = ((Stmt.Expression) object).expression;
				if (!(expr instanceof Expr.PocketOpen) && !(expr instanceof Expr.PocketClosed)
						&& !(expr instanceof Expr.CupOpen) && !(expr instanceof Expr.CupClosed)) {
					count++;
				}
			} else
				count++;
		}
		return count;
	}

	public Object getat(Token operator, Object value) {
		return withToken(() -> {
			if (value instanceof Integer) {
				Integer index = ((Integer) value) + 1;
				if (index >= 1 && index <= bodySizeExclude()) {
					return body.get((int) index);
				} else
					throw new RuntimeError(operator, "index out of bounds");
			} else
				throw new RuntimeError(operator, "must pass an Integer to remove");
		});
	}

	public Object size(Token operator) {
		return withToken(() -> bodySizeExclude());
	}

	public Object clear(Token operator) {
		return withToken(() -> {
			List<Object> cleared = new ArrayList<>();
			cleared.add(body.get(0));
			cleared.add(body.get(body.size() - 1));
			body = cleared;
			return null;
		});
	}

	public Object empty(Token operator) {
		return withToken(() -> bodySizeExclude() == 0 ? true : false);
	}

	protected boolean isIndexControl(int index) {
		Object object = body.get(index);
		if (object instanceof Stmt.Expression) {
			Expr expr = ((Stmt.Expression) object).expression;
			if (!(expr instanceof Expr.PocketOpen) && !(expr instanceof Expr.PocketClosed)
					&& !(expr instanceof Expr.CupOpen) && !(expr instanceof Expr.CupClosed)) {
				return false;
			} else
				return true;
		} else {
			return false;
		}
	}

	public Object pop(Token operator) {
		return withToken(() -> {
			int index = 1;
			while (isIndexControl(index)) {
				index++;
			}
			if (index >= bodySizeExclude())
				return null;

			return body.remove(index);
		});
	}

	public void push(Token operator, Expr toadd) {
		withTokenVoid(() -> {
			body.add(1, toadd);
			evaluateBody();
		});
	}

	public void setat(Literal index, Expr toset) {
		withTokenVoid(() -> {
			if (index.value instanceof Integer) {
				Integer i = ((Integer) (index.value)) + 1;
				if (i >= 1 && i <= bodySizeExclude()) {
					body.add((int) i, toset);
					body.remove(i + 1);
					evaluateBody();
				} else
					throw new RuntimeError(new Token(TokenType.SETAT, "", null, null, null, -1, -1, -1, -1),
							"index out of bounds");
			} else
				throw new RuntimeError(new Token(TokenType.SETAT, "", null, null, null, -1, -1, -1, -1),
						"invalid parameters to setat");
		});
	}

	public Object sub(Literal start, Literal end) {
		return withToken(() -> {
			if (start.value instanceof Integer && end.value instanceof Integer) {
				Integer i = ((Integer) (start.value)) + 1;
				Integer j = ((Integer) (end.value)) + 1;
				if (i >= 1 && i <= bodySizeExclude()) {
					if (j >= i && j <= bodySizeExclude()) {
						return body.subList(i, j);
					} else
						throw new RuntimeError(new Token(TokenType.SUB, "", null, null, null, -1, -1, -1, -1),
								"cross bounds mismatch in the call to sub ");
				} else
					throw new RuntimeError(new Token(TokenType.SUB, "", null, null, null, -1, -1, -1, -1),
							"index out of bounds in call to sub");
			} else
				throw new RuntimeError(new Token(TokenType.SUB, "", null, null, null, -1, -1, -1, -1),
						"invalid parameters to sub");
		});
	}

	public boolean contains(Declaration contents) {
		return (boolean)(Boolean) withToken(() -> {
			if(contents instanceof Stmt.Expression) {
				Object value = ((Stmt.Expression)contents).expression;
				if(value instanceof Literal) {
					return body.contains(((Literal)value).value);
				}else {
					Object execute = interpreter.execute(contents);
					return body.contains(execute);
				}
			}else {
				for (Object obj : body) {

						if(contents.equals(obj))return true;

				}
				return false;
			}
		});
	}

}
