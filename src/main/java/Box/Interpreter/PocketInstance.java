package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.Expr.Lifetime;
import Parser.Expr.Literal;
import Parser.Stmt;
import Parser.Declaration.StmtDecl;
import Box.Interpreter.KnotRunner;

public class PocketInstance extends CupInstance implements IPkt {

	private final List<Flow> flows = new ArrayList<>();

	// Lifetime tracking
	private Lifetime lifetime;
	private int remainingTraversals;
	private boolean destroyed = false;
	private boolean stripping = false; // Stage 1: tkp-like — flows cleared, body readable, no modification

	public Lifetime getLifetime() { return lifetime; }

	public void setLifetime(Lifetime lt) {
		this.lifetime = lt;
		if (lt != null && lt.kind == Lifetime.Kind.TRAVERSAL) {
			this.remainingTraversals = lt.count;
		}
	}

	public boolean isAlive() { return !destroyed && !stripping; }
	public boolean isStripping() { return stripping && !destroyed; }

	// Stage 1: strip flows, stop tick. Body remains readable. Tick thread exits on next isAlive() check.
	public void beginDeath() {
		if (destroyed || stripping) return;
		stripping = true;
		flows.clear();
	}

	// Stage 2: body cleared, fully dead.
	public void destroy() {
		if (destroyed) return;
		destroyed = true;
		stripping = false;
		for (Object item : body)
			if (item instanceof PocketInstance) ((PocketInstance) item).beginDeath();
		body.clear();
		flows.clear();
	}

	// Proactive check: called after each statement, only enforces DEPENDENT/CONDITIONAL.
	// Does NOT decrement TRAVERSAL — that is onTraversal()'s job.
	public void probeLifetime() {
		if (destroyed || stripping || lifetime == null) return;
		switch (lifetime.kind) {
			case INDEFINITE:
			case TRAVERSAL:
				break;
			case DEPENDENT: {
				Token t = new Token(TokenType.IDENTIFIER, lifetime.dependsOn,
						null, null, null, -1, -1, -1, -1);
				Object dep = interpreter.environment.get(t, false);
				if (dep instanceof PocketInstance && !((PocketInstance) dep).isAlive()) seamCrossAndUpdate();
				else if (dep instanceof TkpInstance && !((TkpInstance) dep).isAlive()) seamCrossAndUpdate();
				else if (dep == null) seamCrossAndUpdate();
				break;
			}
			case CONDITIONAL: {
				Object result = interpreter.evaluate(lifetime.condition);
				if (Boolean.FALSE.equals(result) || result == null) seamCrossAndUpdate();
				break;
			}
		}
	}

	// Called at the start of every container operation.
	// Applies the lifetime policy and destroys the pocket if the policy is violated.
	// In stage 1 (stripping), transitions to stage 2 (destroy).
	private void onTraversal() {
		if (destroyed) return;
		if (stripping) { destroy(); return; }
		if (lifetime == null) return;
		switch (lifetime.kind) {
			case INDEFINITE:
				break;
			case TRAVERSAL:
				remainingTraversals--;
				if (remainingTraversals <= 0) seamCrossAndUpdate();
				break;
			case DEPENDENT: {
				Token t = new Token(TokenType.IDENTIFIER, lifetime.dependsOn,
						null, null, null, -1, -1, -1, -1);
				Object dep = interpreter.environment.get(t, false);
				if (dep instanceof PocketInstance && !((PocketInstance) dep).isAlive()) seamCrossAndUpdate();
				else if (dep instanceof TkpInstance && !((TkpInstance) dep).isAlive()) seamCrossAndUpdate();
				else if (dep == null) seamCrossAndUpdate();
				break;
			}
			case CONDITIONAL: {
				Object result = interpreter.evaluate(lifetime.condition);
				if (Boolean.FALSE.equals(result) || result == null) seamCrossAndUpdate();
				break;
			}
		}
	}

	public void injectFlow(Flow flow) {
		flows.add(flow);
	}

	public List<Flow> getFlows() {
		return flows;
	}

	// ---- Seam crossing (pkt → tkp on natural lifetime expiry) ----

	private Environment tickEnv;
	private Token tickName;

	private TkpInstance seamCrossToTkp() {
		TkpInstance tkp = new TkpInstance(boxClass, new ArrayList<>(body), expr, interpreter);
		if (lifetime != null && lifetime.kind == Lifetime.Kind.TRAVERSAL) {
			tkp.setLifetime(Lifetime.traversal(remainingTraversals));
		} else if (lifetime != null) {
			tkp.setLifetime(lifetime);
		}
		destroyed = true;
		stripping = false;
		body.clear();
		flows.clear();
		return tkp;
	}

	private void seamCrossAndUpdate() {
		if (tickEnv == null || tickName == null) { beginDeath(); return; }
		TkpInstance tkp = seamCrossToTkp();
		try { tickEnv.assign(tickName, (Box.Token.Token) null, tkp); } catch (RuntimeError ignored) {}
		tkp.startIndependent(tickEnv, tickName);
	}

	// ---- Independent tick loop ----

	private Thread tickThread;
	boolean pktForward;
	boolean pktInvertedMode;
	int windowSize = 1;
	int starvationThreshold = 3;
	String starvationPolicy = "STRICT";

	public boolean hasFlowsInBody() {
		for (Object item : body) {
			if (item instanceof Flow) return true;
			if (item instanceof String && extractFlowFromString((String) item) != null) return true;
		}
		return !flows.isEmpty();
	}

	public void startIndependent(Environment env, Token nameToken) {
		this.tickEnv = env;
		this.tickName = nameToken;
		this.pktForward = interpreter.isForward();
		this.pktInvertedMode = interpreter.isInverted();
		if (tickThread != null && tickThread.isAlive()) return;
		tickThread = new Thread(() -> {
			int tickCount = 0;
			int consecutiveFailures = 0;
			int skipRemaining = 0;
			boolean binaryHungry = false;
			int binaryFedCount = 0;
			int baseWindowSize = windowSize;

			while (isAlive() && !isHeatDeath()) {
				if (skipRemaining > 0) { skipRemaining--; tickCount++; continue; }

				boolean acquired;
				if ("STEAL".equals(starvationPolicy)) {
					interpreter.interpreterToken.set(false);
					acquired = true;
				} else if ("DRAIN".equals(starvationPolicy) && consecutiveFailures >= starvationThreshold) {
					interpreter.interpreterToken.set(false);
					acquired = true;
					consecutiveFailures = 0;
				} else {
					acquired = interpreter.interpreterToken.compareAndSet(true, false);
				}

				if (!acquired) {
					interpreter.bin.body.add(
						new TokenContentionError(nameToken.lexeme, tickCount, "interpreter").getMessage());
					tickCount++;
					consecutiveFailures++;
					switch (starvationPolicy) {
						case "IMMORTAL": break;
						case "FLOOD": consecutiveFailures = 0; break;
						case "INCREMENTAL": windowSize++; break;
						case "GREEDY":
							if (consecutiveFailures >= starvationThreshold) { windowSize++; consecutiveFailures = 0; }
							break;
						case "BACKOFF": skipRemaining = consecutiveFailures; break;
						case "BINARY":
							if (!binaryHungry && consecutiveFailures >= Math.max(1, starvationThreshold / 2)) {
								binaryHungry = true; windowSize = baseWindowSize * 2;
							}
							break;
						case "DRAIN": break;
						case "ROTATE": skipRemaining = starvationThreshold; break;
						default: // STRICT, EXPLICIT, EXPLICITGLOBAL, and anything unrecognized
							if (consecutiveFailures >= starvationThreshold) {
								TkpInstance tkp = new TkpInstance(boxClass, new ArrayList<>(body), expr, interpreter);
								destroyed = true; stripping = false; body.clear(); flows.clear();
								interpreter.bin.body.add("starvation: pkt->tkp pocket=" + nameToken.lexeme + " tick=" + tickCount);
								try { tickEnv.assign(tickName, (Box.Token.Token) null, tkp); } catch (RuntimeError ignored) {}
								return;
							}
					}
					continue;
				}

				consecutiveFailures = 0;
				if ("BINARY".equals(starvationPolicy) && binaryHungry) {
					binaryFedCount++;
					if (binaryFedCount >= starvationThreshold) {
						binaryHungry = false; windowSize = baseWindowSize; binaryFedCount = 0;
					}
				}
				if ("EXPLICITGLOBAL".equals(starvationPolicy) && interpreter.globalWindowSize > 0) {
					windowSize = interpreter.globalWindowSize;
				}
				try {
					interpreter.setForward(pktForward);
					interpreter.setInverted(pktInvertedMode);
					interpreter.currentCallerName = nameToken.lexeme;
					interpreter.currentCallerTick = tickCount;
					for (int w = 0; w < windowSize && isAlive() && !isHeatDeath(); w++) {
						interpreter.currentCallerTick = tickCount;
						try {
							tick();
						} catch (RuntimeError e) {
							interpreter.addToErrorSink(e.getMessage());
						} catch (TokenContentionError e) {
							interpreter.bin.body.add(e.getMessage());
						}
						tickCount++;
					}
				} finally {
					interpreter.currentCallerName = null;
					interpreter.currentCallerTick = 0;
					interpreter.interpreterToken.set(true);
				}
			}
		}, "pkt-tick-" + nameToken.lexeme);
		tickThread.setDaemon(true);
		tickThread.start();
	}

	public boolean isHeatDeath() {
		for (Flow flow : flows) {
			if (flow.canBootstrap()) return false;
		}
		for (Object item : body) {
			if (item instanceof Flow) return false;
			if (item instanceof String && extractFlowFromString((String) item) != null) return false;
		}
		for (Object item : body) {
			if (item instanceof CupInstance) return false;
		}
		return true;
	}

	public boolean tick() {
		if (flows.isEmpty()) {
			for (int i = 0; i < body.size(); i++) {
				Object item = body.get(i);
				if (item instanceof Flow) {
					flows.add((Flow) body.remove(i));
					break;
				}
				if (item instanceof String) {
					Flow sf = extractFlowFromString((String) item);
					if (sf != null) {
						flows.add(sf);
						body.remove(i);
						break;
					}
				}
			}
		}
		if (flows.isEmpty()) return false;

		// Track indices to remove (not items) so equals-based removeAll can't over-delete
		// duplicate-valued items (e.g. two "." strings).
		List<Integer> removeQueue = new ArrayList<>();
		boolean worked = false;

		// Snapshot flows to avoid ConcurrentModificationException when new flows
		// are added to the list while iterating (bracket signals, knotted pockets).
		List<Flow> currentFlows = new ArrayList<>(flows);
		for (Flow flow : currentFlows) {
			// Exclude structural brackets at body[0] (PocketOpen) and body[last] (PocketClose).
			int start = flow.isForward() ? 1 : body.size() - 2;
			int end   = flow.isForward() ? body.size() - 1 : 0;
			int step  = flow.isForward() ? 1 : -1;
			boolean flowWorked = false;

			for (int i = start; i != end; i += step) {
				Object item = body.get(i);

				if (item instanceof Flow) {
					flow.addChain(((Flow) item).getChain());
					removeQueue.add(i);
					worked = true;
					flowWorked = true;
					break;
				}

				if (item instanceof String) {
					// Single period: flip this flow's direction.
					// Standalone DOT parses as Expr.Literal(".") via primative() in ParserTest.
					// evaluateBody() reduces it to String(".") before the tick loop sees it.
					if (item.equals(".")) {
						flow.flipDirection();
						removeQueue.add(i);
						worked = true;
						flowWorked = true;
						break;
					}
					// Bare bracket string: synthesize period, promote to new independent flow.
					Flow.Direction bracketDir = bracketStringDirection((String) item);
					if (bracketDir != null) {
						Flow.BracketType bt = bracketStringType((String) item);
						flows.add(new Flow(bracketDir, bt, 1));
						removeQueue.add(i);
						worked = true;
						flowWorked = true;
						break;
					}
					Flow sf = extractFlowFromString((String) item);
					if (sf != null) {
						flow.injectCargoChain((String) item, sf.getChain());
						removeQueue.add(i);
						worked = true;
						flowWorked = true;
						break;
					}
				}

				// Bracket control expression: synthesize period, promote to new independent flow.
				if (item instanceof Stmt.Expression) {
					Expr expr2 = ((Stmt.Expression) item).expression;
					Flow newFlow = controlExprToFlow(expr2);
					if (newFlow != null) {
						flows.add(newFlow);
						removeQueue.add(i);
						worked = true;
						flowWorked = true;
						break;
					}
				}

				// knt/tnk bootstrap: Flow starts the knot running. Routing is the knot's own responsibility.
				if (flow.canBootstrap() && (item instanceof KnotInstance || item instanceof TonkInstance)) {
					boolean forKnot = item instanceof KnotInstance;
					List<Object> rawBody = forKnot ? ((KnotInstance) item).body : ((TonkInstance) item).body;
					List<Stmt> stmts = new ArrayList<>();
					for (Object o : rawBody) {
						if (o instanceof Stmt) stmts.add((Stmt) o);
					}
					new KnotRunner(null, stmts, interpreter).runWithRouting(forKnot);
					flow.spendToken();
					worked = true;
					flowWorked = true;
					break;
				}

				// Cup bootstrap: flow kicks off cup execution, cup is consumed.
				if (flow.canBootstrap() && item instanceof CupInstance) {
					((CupInstance) item).execute();
					removeQueue.add(i);
					flow.spendToken();
					worked = true;
					flowWorked = true;
					break;
				}

				// Function bootstrap: call BoxFunction, deposit return value in-place.
				if (flow.canBootstrap() && item instanceof BoxFunction) {
					BoxFunction fn = (BoxFunction) item;
					int fnIndex = i;

					// Capture cargo before spendToken — spending the last token resets cargoIndex.
					Object pendingCargo = flow.hasCargo() ? flow.getCurrentCargo() : null;
					flow.spendToken();

					// Direction check BEFORE arg evaluation — no side effects on mismatch.
					if (fn.isForward != interpreter.isForward()) {
						worked    = true;
						flowWorked = true;
						break;
					}

					List<Object> args;
					if (pendingCargo != null) {
						// Option 4 — package current cargo as a BoxInstance argument.
						List<Object> contents = new ArrayList<>();
						contents.add(pendingCargo);
						args = new ArrayList<>();
						args.add(new BoxInstance(null, contents, null, interpreter));
						flow.advanceCargoIndex();
					} else {
						// Option 3 — consume preceding body items to satisfy arity.
						int arity = fn.arity();
						args = new ArrayList<>();
						if (arity == 0) {
							args.add(new BoxInstance(null, new ArrayList<>(), null, interpreter));
						} else {
							int j = i - 1;
							while (j >= 1 && args.size() < arity) {
								if (!isControl(body.get(j))) {
									args.add(0, body.remove(j));
									fnIndex--;
								}
								j--;
							}
							while (args.size() < arity)
								args.add(new BoxInstance(null, new ArrayList<>(), null, interpreter));
						}
					}

					try {
						Object result = fn.call(interpreter, args);
						body.add(fnIndex + 1, Boxer.box(result, interpreter));
					} catch (RuntimeError mismatch) {
						interpreter.addToErrorSink(mismatch.getMessage());
					}
					removeQueue.add(fnIndex);
					worked    = true;
					flowWorked = true;
					break;
				}

			}

			if (!flowWorked && flow.canBootstrap()) {
				flow.spendToken();
			}
		}

		// Remove in descending index order so earlier indices stay valid.
		removeQueue.sort((a, b) -> b - a);
		for (int idx : removeQueue) body.remove(idx);
		return worked;
	}

	private Flow.Direction bracketStringDirection(String s) {
		if (s.equals("(") || s.equals("{") || s.equals("[")) return Flow.Direction.FORWARD;
		if (s.equals(")") || s.equals("}") || s.equals("]")) return Flow.Direction.BACKWARD;
		return null;
	}

	private Flow.BracketType bracketStringType(String s) {
		if (s.equals("(") || s.equals(")")) return Flow.BracketType.PAREN;
		if (s.equals("{") || s.equals("}")) return Flow.BracketType.BRACE;
		return Flow.BracketType.SQUARE;
	}

	private Flow controlExprToFlow(Expr expr) {
		if (expr instanceof Expr.PocketOpen || expr instanceof Expr.CupOpen)
			return new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
		if (expr instanceof Expr.PocketClosed || expr instanceof Expr.CupClosed)
			return new Flow(Flow.Direction.BACKWARD, Flow.BracketType.PAREN, 1);
		return null;
	}

	private Flow extractFlowFromString(String s) {
		String[] fwd = { "(.", "{.", "[.", "(,.", "{,.", "[,." };
		String[] bwd = { ".)", ".}", ".]", ".,)", ".,}", ".,]" };

		Flow.Direction dir = null;
		Flow.BracketType bt = Flow.BracketType.PAREN;
		int chain = 0;

		for (int i = 0; i < s.length(); i++) {
			for (int fi = 0; fi < fwd.length; fi++) {
				if (s.startsWith(fwd[fi], i)) {
					if (dir == null) {
						dir = Flow.Direction.FORWARD;
						bt = fi < 2 ? Flow.BracketType.PAREN
						   : fi < 4 ? Flow.BracketType.BRACE
						   :          Flow.BracketType.SQUARE;
					}
					chain++;
					i += fwd[fi].length() - 1;
					break;
				}
			}
			for (int bi = 0; bi < bwd.length; bi++) {
				if (s.startsWith(bwd[bi], i)) {
					if (dir == null) {
						dir = Flow.Direction.BACKWARD;
						bt = bi < 2 ? Flow.BracketType.PAREN
						   : bi < 4 ? Flow.BracketType.BRACE
						   :          Flow.BracketType.SQUARE;
					}
					chain++;
					i += bwd[bi].length() - 1;
					break;
				}
			}
		}

		if (chain == 0) return null;
		Flow f = new Flow(dir, bt, chain);
		f.injectCargo(s);
		return f;
	}

	public PocketInstance(BoxCallable boxClass, List<Object> body, Expr expr, Interpreter interpreter) {
		super(boxClass, body, expr, interpreter, true);
		evaluateBody();
	}

	private void evaluateBody() {
		ArrayList<Object> temp = new ArrayList<>();
		for (int i = 0; i < body.size(); i++) {
			Object object = body.get(i);
			if (!isControl(object)) {
				if (isExpression(object)) {
					temp.add(Boxer.box(interpreter.execute((Stmt) object), interpreter));
				} else
					temp.add(object);
			} else
				temp.add(object);
		}
		body = temp;
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
			onTraversal();
			if (destroyed || stripping) return;
			if (toadd instanceof Stmt || toadd instanceof Expr) {
				body.add(body.size() - 1, toadd);
				evaluateBody();
			} else
				throw new RuntimeError(operator, "not of acceptable type");
		});
	}

	@Override
	public String toString() {
		String str = "(";
		int count = 0;
		for (Object object : body) {
			count++;
			if (object instanceof Stmt.Expression ) {
				if (((Stmt.Expression)object).expression instanceof Expr.PocketOpen|| ((Stmt.Expression)object).expression instanceof Expr.PocketClosed) {
					
				}else {
					if(count==body.size()-1) {
						str += object.toString();
					break;
					}else
					str += object.toString() + ",";
				}
			} else {
				if(count==body.size()-1) {
					str += object.toString();
				break;
				}else
				str += object.toString() + ",";
			}
			
		}
		return str + ")";
	}

	public Object remove(Token operator, Object value) {
		return withToken(() -> {
			onTraversal();
			if (destroyed || stripping) return null;
			if (value instanceof Integer) {
				Integer index = ((Integer) value) + 1;
				if (index >= 1 && index <= bodySizeExclude() - 1) {
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
			if (destroyed) return null;
			if (value instanceof Integer) {
				Integer index = ((Integer) value) + 1;
				if (index >= 1 && index <= bodySizeExclude() - 1) {
					return body.get((int) index);
				} else
					throw new RuntimeError(operator, "index out of bounds");
			} else
				throw new RuntimeError(operator, "must pass an Integer to remove");
		});
	}

	public Object size(Token operator) {
		return withToken(() -> {
			if (destroyed) return 0;
			return bodySizeExclude();
		});
	}

	public Object clear(Token operator) {
		return withToken(() -> {
			onTraversal();
			if (destroyed || stripping) return null;
			List<Object> cleared = new ArrayList<>();
			cleared.add(body.get(0));
			cleared.add(body.get(body.size() - 1));
			body = cleared;
			return null;
		});
	}

	public Object empty(Token operator) {
		return withToken(() -> {
			if (destroyed) return true;
			return bodySizeExclude() == 0 ? true : false;
		});
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
			onTraversal();
			if (destroyed || stripping) return null;
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
			onTraversal();
			if (destroyed || stripping) return;
			body.add(1, toadd);
			evaluateBody();
		});
	}

	public void setat(Literal index, Expr toset) {
		withTokenVoid(() -> {
			onTraversal();
			if (destroyed || stripping) return;
			if (index.value instanceof Integer) {
				Integer i = ((Integer) (index.value)) + 1;
				if (i >= 1 && i <= bodySizeExclude() - 1) {
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
			if (destroyed) return null;
			if (start.value instanceof Integer && end.value instanceof Integer) {
				Integer i = ((Integer) (start.value)) + 1;
				Integer j = ((Integer) (end.value)) + 1;
				if (i >= 1 && i <= bodySizeExclude() - 1) {
					if (j >= i && j <= bodySizeExclude() - 1) {
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
			if (destroyed) return false;
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
