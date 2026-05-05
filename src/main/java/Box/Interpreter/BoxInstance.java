package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.Expr.Literal;
import Parser.Stmt;
import Parser.Declaration.StmtDecl;

public class BoxInstance extends Instance implements IBox {

	protected List<Object> body;
	protected Interpreter interpreter;
	public int lazyBindingCount = 0;
	public int eagerBindingCount = 0;
	public double contractConfidence = -1.0;

	// ---- Container token -------------------------------------------------------
	final java.util.concurrent.atomic.AtomicBoolean containerToken =
			new java.util.concurrent.atomic.AtomicBoolean(true);

	void acquireContainerToken(String pocketName, int tick, String containerName) {
		if (!containerToken.compareAndSet(true, false))
			throw new TokenContentionError(pocketName, tick, containerName);
	}

	void releaseContainerToken() {
		containerToken.set(true);
	}

	protected Object withToken(java.util.function.Supplier<Object> op) {
		String caller = (interpreter != null && interpreter.currentCallerName != null)
				? interpreter.currentCallerName : "main";
		int tick = interpreter != null ? interpreter.currentCallerTick : 0;
		try {
			acquireContainerToken(caller, tick, getClass().getSimpleName());
		} catch (TokenContentionError e) {
			if (interpreter != null) {
				interpreter.bin.body.add(e.getMessage());
				interpreter.notifyContainerModified(interpreter.bin);
			}
			return null;
		}
		try { return op.get(); } finally { releaseContainerToken(); }
	}

	protected void withTokenVoid(Runnable op) {
		String caller = (interpreter != null && interpreter.currentCallerName != null)
				? interpreter.currentCallerName : "main";
		int tick = interpreter != null ? interpreter.currentCallerTick : 0;
		try {
			acquireContainerToken(caller, tick, getClass().getSimpleName());
		} catch (TokenContentionError e) {
			if (interpreter != null) {
				interpreter.bin.body.add(e.getMessage());
				interpreter.notifyContainerModified(interpreter.bin);
			}
			return;
		}
		try { op.run(); } finally { releaseContainerToken(); }
	}

	@Override
	public List<Object> getBody() { return body; }

	public BoxInstance(BoxCallable boxClass, List<Object> body, Expr expr, Interpreter interpreter) {
		super(boxClass, expr);
		this.body = body;
		this.interpreter = interpreter;
		evaluateBody();
	}

	/** Skip-init constructor for subclasses that manage their own body initialization. */
	protected BoxInstance(BoxCallable boxClass, List<Object> body, Expr expr, Interpreter interpreter, boolean _skipInit) {
		super(boxClass, expr);
		this.body = body;
		this.interpreter = interpreter;
	}

	private void evaluateBody() {
		lazyBindingCount = 0;
		eagerBindingCount = 0;
		ArrayList<Object> temp = new ArrayList<>();
		for (int i = 0; i < body.size(); i++) {
			Object object = body.get(i);
			if (!isControl(object)) {
				if (isExpression(object)) {
					Expr expr = (Expr) object;
					if (expr instanceof Expr.Get && interpreter.inContractContext()) {
						String root = getChainRoot(expr);
						if (root != null && interpreter.globals.isSeedGlobal(root)) {
							lazyBindingCount++;
							temp.add(expr);
							continue;
						}
					}
					eagerBindingCount++;
					temp.add(Boxer.box(interpreter.execute(expr), interpreter));
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

	protected static String getChainRoot(Expr expr) {
		while (expr instanceof Expr.Get) expr = ((Expr.Get) expr).object;
		return (expr instanceof Expr.Variable) ? ((Expr.Variable) expr).name.lexeme : null;
	}

	private boolean isExpression(Object object) {
		if (object instanceof Expr)
			return true;
		return false;
	}

	private boolean isControl(Object object) {
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

	@Override
	public String toString() {
		String str = "[";
		for (int i = 0; i < body.size(); i++) {
			if(i==body.size()-1) {
				str+=body.get(i).toString();
			}else {
				str+=body.get(i).toString()+",";
			}
		}
		return str+"]";
	}

	public void add(Token operator, Object toadd) {
		withTokenVoid(() -> {
			if (toadd instanceof Expr.Literal || toadd instanceof Expr.Cup || toadd instanceof Expr.Pocket
					|| toadd instanceof Expr.Box || toadd instanceof Expr.Knot || toadd instanceof Expr.Tonk
					|| toadd instanceof Expr.Variable || toadd instanceof Expr.LiteralChar) {
				body.add(toadd);
			} else
				throw new RuntimeError(operator, "not of acceptable type");
		});
	}

	public Object remove(Token operator, Object value) {
		return withToken(() -> {
			if (value instanceof Integer) {
				Integer index = ((Integer) value);
				if (index >= 0 && index <= bodySizeExclude()) {
					return body.remove((int) index);
				} else
					throw new RuntimeError(operator, "index out of bounds");
			} else
				throw new RuntimeError(operator, "must pass an Integer to remove");
		});
	}

	private int bodySizeExclude() {
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
				Integer index = ((Integer) value);
				if (index >= 0 && index <= bodySizeExclude() - 1) {
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
			body = new ArrayList<>();
			return null;
		});
	}

	public Object empty(Token operator) {
		return withToken(() -> bodySizeExclude() == 0);
	}

	private boolean isIndexControl(int index) {
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
			int index = 0;
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
			body.add(0, toadd);
			evaluateBody();
		});
	}

	public void setat(Literal index, Expr toset) {
		withTokenVoid(() -> {
			if (index.value instanceof Integer) {
				Integer i = ((Integer) (index.value));
				if (i >= 0 && i <= bodySizeExclude() - 1) {
					body.add((int) i, toset);
					body.remove(i.intValue() + 1);
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
				Integer i = ((Integer) (start.value));
				Integer j = ((Integer) (end.value));
				if (i >= 0 && i <= bodySizeExclude()) {
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
			if (contents instanceof Stmt.Expression) {
				Object value = ((Stmt.Expression) contents).expression;
				if (value instanceof Literal) {
					Object value2 = ((Literal) value).value;
					for (Object object : body) {
						if (value2.equals(object))
							return true;
					}
					return false;
				} else {
					Object execute = interpreter.execute(contents);
					return body.contains(execute);
				}
			} else {
				for (Object obj : body) {
					if (contents.equals(obj))
						return true;
				}
				return false;
			}
		});
	}

}
