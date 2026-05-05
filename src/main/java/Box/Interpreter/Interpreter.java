package Box.Interpreter;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import Box.math.Interpreter.MathInterpreter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Box.Box.Box;
import Box.Grouper.Grouper;
import Box.Scanner.Scanner;
import Box.Token.Token;
import Box.Token.TokenType;
import FlatLand.Physics.TypeOfEntity;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLandFacebook;
import FlatLander.FlatLander;
import Parser.Declaration;
import Parser.Expr;
import Parser.ParserTest;
import Parser.Stmt;
import Parser.Declaration.FunDecl;
import Parser.Declaration.StmtDecl;
import Parser.Expr.Additive;
import Parser.Expr.Addittidda;
import Parser.Expr.Assignment;
import Parser.Expr.Assignmenttnemgissa;
import Parser.Expr.Binary;
import Parser.Expr.Binaryyranib;
import Parser.Expr.BoxClosed;
import Parser.Expr.BoxOpen;
import Parser.Expr.Bus;
import Parser.Expr.Call;
import Parser.Expr.Callllac;
import Parser.Expr.Contains;
import Parser.Expr.Containssniatnoc;
import Parser.Expr.Cup;
import Parser.Expr.CupClosed;
import Parser.Expr.CupOpen;
import Parser.Expr.EOF;
import Parser.Expr.Epyt;
import Parser.Expr.Evitidda;
import Parser.Expr.Expressiontmts;
import Parser.Expr.Factorial;
import Parser.Expr.Get;
import Parser.Expr.Gol;
import Parser.Expr.Knot;
import Parser.Expr.Lairotcaf;
import Parser.Expr.Link;
import Parser.Expr.Literal;
import Parser.Expr.LiteralBool;
import Parser.Expr.LiteralChar;
import Parser.Expr.LiteralLoob;
import Parser.Expr.Llac;
import Parser.Expr.Log;
import Parser.Expr.Loggol;
import Parser.Expr.Mono;
import Parser.Expr.Monoonom;
import Parser.Expr.NoPaCoOOoCaPoN;
import Parser.Expr.NonParamContOp;
import Parser.Expr.Onom;
import Parser.Expr.ParCoOppOoCraP;
import Parser.Expr.ParamContOp;
import Parser.Expr.PoTnocMarap;
import Parser.Expr.PoTnocMarapNon;
import Parser.Expr.Pocket;
import Parser.Expr.PocketClosed;
import Parser.Expr.PocketOpen;
import Parser.Expr.Set;
import Parser.Expr.Setat;
import Parser.Expr.Setattates;
import Parser.Expr.Sniatnoc;
import Parser.Expr.Sub;
import Parser.Expr.Subbus;
import Parser.Expr.Swap;
import Parser.Expr.Tates;
import Parser.Expr.Teg;
import Parser.Expr.Template;
import Parser.Expr.Tes;
import Parser.Expr.Tnemngissa;
import Parser.Expr.Tonk;
import Parser.Expr.Type;
import Parser.Expr.Unary;
import Parser.Expr.Variable;
import Parser.Expr.Yranib;
import Parser.Expr.Yranu;
import Parser.Fun.Function;
import Parser.Fun.FunctionLink;
import Parser.Stmt.Assert;
import Parser.Stmt.Consume;
import Parser.Stmt.Daer;
import Parser.Stmt.Emaner;
import Parser.Stmt.Evas;
import Parser.Stmt.Evom;
import Parser.Stmt.Expel;
import Parser.Stmt.FLCreate;
import Parser.Stmt.FLDestroy;
import Parser.Stmt.FLECreate;
import Parser.Stmt.FLEDestroy;
import Parser.Stmt.FLMove;
import Parser.Stmt.FLsetValue;
import Parser.Stmt.Fi;
import Parser.Stmt.If;
import Parser.Stmt.Ifi;
import Parser.Stmt.Move;
import Parser.Stmt.Moveevom;
import Parser.Stmt.Nruter;
import Parser.Stmt.Nur;
import Parser.Stmt.Print;
import Parser.Stmt.Rav;
import Parser.Stmt.Read;
import Parser.Stmt.Readdaer;
import Parser.Stmt.Rename;
import Parser.Stmt.Renameemaner;
import Parser.Stmt.Run;
import Parser.Stmt.Save;
import Parser.Stmt.Saveevas;
import Parser.Stmt.StmttmtS;
import Parser.Stmt.TemplatVar;
import Parser.Stmt.Tnirp;
import Parser.Stmt.Var;
import XMLLEVELLOADER.FlatLanderWrper;
import audiolizer.InterpreterAudio;
import flatLand.trainingGround.EventHandler;
import flatLand.trainingGround.GameEvent;
import flatLand.trainingGround.Sprites.SkeletonTwo;
import resolver.Resolver;

public class Interpreter extends Thread implements Declaration.Visitor<Object> {

	private InterpreterAudio audio = InterpreterAudio.NONE;

	public void setAudio(InterpreterAudio audio) {
		this.audio = (audio == null ? InterpreterAudio.NONE : audio);
	}

	public class KnotMap<refrence, expression> {
		ArrayList<refrence> ref = new ArrayList<refrence>();
		ArrayList<expression> expr = new ArrayList<expression>();

		public void put(refrence lexeme, expression expression) {
			ref.add(lexeme);
			expr.add(expression);
		}

		public refrence getRef(int i) {

			return ref.get(i);
		}

		public expression getExpr(int i) {

			return expr.get(i);
		}

	}

	public Environment globals = new Environment();
	Environment environment = globals;
	private Map<Expr, Integer> locals = new WesMap<>();
	private boolean fromCall = false;
	private boolean forward;
	private boolean invertedMode = false;
	HashMap<String, String> nameMap = new HashMap<>();
	HashMap<String, String> classMapToSuperClass = new HashMap<>();
	private ArrayList<Object> identifiers = new ArrayList<>();
	private ArrayList<String> classes = new ArrayList<>();
	ArrayList<String> templates = new ArrayList<>();
	ArrayList<String> links = new ArrayList<>();
	java.util.Stack<Boolean> contractContextStack = new java.util.Stack<>();

	// ---- User-defined type registry (declaration order) -----------------------
	public static class UserTypeEntry {
		public final String typeName;
		public final java.util.List<String> linkNames;
		public final String templateName;
		public final java.util.List<Expr.SlotDescriptor> slots;
		public BoxClass resolvedTemplate; // null until a matching template is declared
		public UserTypeEntry(String typeName, java.util.List<String> linkNames,
				String templateName, java.util.List<Expr.SlotDescriptor> slots) {
			this.typeName = typeName;
			this.linkNames = linkNames != null ? linkNames : new java.util.ArrayList<>();
			this.templateName = templateName;
			this.slots = slots != null ? slots : new java.util.ArrayList<>();
		}
	}
	public final java.util.List<UserTypeEntry> userTypeRegistry = new java.util.ArrayList<>();
	public final java.util.Deque<UserTypeEntry> typeContextStack = new java.util.ArrayDeque<>();
	public boolean inContractContext() { return !contractContextStack.isEmpty(); }
	private ViewableFlatLand viewableFlatLand;
	private FlatLandFacebook flatLanderFaceBook;
	private EventHandler events;

	public final BoxInstance     bin   = new BoxInstance(null, new java.util.ArrayList<>(), null, this);
	public final XobInstance     gap   = new XobInstance(null, new java.util.ArrayList<>(), null, this);
	public final CupInstance     non   = new CupInstance(null, new java.util.ArrayList<>(), null, this);
	public final PocketInstance  trash = new PocketInstance(null, new java.util.ArrayList<>(), null, this);
	public final TkpInstance     voidC = new TkpInstance(null, new java.util.ArrayList<>(), null, this);
	public final PucInstance     limbo = new PucInstance(null, new java.util.ArrayList<>(), null, this);

	// ---- Interpreter token (pocket thread access) ------------------------------
	public final java.util.concurrent.atomic.AtomicBoolean interpreterToken =
			new java.util.concurrent.atomic.AtomicBoolean(true);
	public int globalWindowSize = 0; // 0 = not set; used by EXPLICITGLOBAL policy

	// ---- Container token caller context (set by pocket/tkp threads, null = main) --
	public String currentCallerName = null;
	public int currentCallerTick = 0;

	// ---- Settings flags --------------------------------------------------------
	public boolean dynamicPolicy     = false;
	public boolean trashThreaded     = false;
	public boolean voidThreaded      = false;
	public final java.util.Set<String> activeDumpPriority = new java.util.HashSet<>();

	// Sentinel instances registered in globals for settings keywords.
	private final SettingToken dumpPriorityToken  = new SettingToken(SettingToken.Kind.DUMP_PRIORITY);
	private final SettingToken threadToken        = new SettingToken(SettingToken.Kind.THREAD);
	private final SettingToken dynamicSaftyToken  = new SettingToken(SettingToken.Kind.DYNAMIC_SAFTY);

	// ---- Active I/O workers (lazy, one per threadable container) ---------------

	private java.util.concurrent.ExecutorService trashWorker = null;
	private java.util.concurrent.ExecutorService voidWorker  = null;

	private java.util.concurrent.ExecutorService trashWorker() {
		if (trashWorker == null)
			trashWorker = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
				Thread t = new Thread(r, "pcb-trash-worker"); t.setDaemon(true); return t;
			});
		return trashWorker;
	}

	private java.util.concurrent.ExecutorService voidWorker() {
		if (voidWorker == null)
			voidWorker = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
				Thread t = new Thread(r, "pcb-void-worker"); t.setDaemon(true); return t;
			});
		return voidWorker;
	}

	/**
	 * Called after any body mutation on a system container.
	 * If dumpPriority is active for that container, flushes it to disk immediately.
	 * If the THREAD flag is also active, the flush is submitted to the container's worker thread.
	 */
	public void notifyContainerModified(Object container) {
		String canonical = canonicalContainer(container);
		if (canonical == null || !activeDumpPriority.contains(canonical)) return;
		if (canonical.equals("TRASH") && trashThreaded) {
			java.util.List<Object> snapshot = new java.util.ArrayList<>(trash.body);
			trashWorker().submit(() -> ContainerPersistence.dumpSnapshot(snapshot, "TRASH"));
		} else if (canonical.equals("VOID") && voidThreaded) {
			java.util.List<Object> snapshot = new java.util.ArrayList<>(voidC.body);
			voidWorker().submit(() -> ContainerPersistence.dumpSnapshot(snapshot, "VOID"));
		} else {
			ContainerPersistence.dumpOne(this, canonical);
		}
	}

	/** Blocks until all pending worker-thread file I/O tasks complete. For testing and shutdown. */
	public void awaitActiveWorkers() {
		try {
			if (trashWorker != null && !trashWorker.isShutdown())
				trashWorker.submit(() -> {}).get();
			if (voidWorker  != null && !voidWorker.isShutdown())
				voidWorker.submit(() ->  {}).get();
		} catch (Exception ignored) {}
	}

	/** Dispatch error value to BIN (static) or TRASH (dynamic) per active policy. */
	public void addToErrorSink(Object value) {
		if (dynamicPolicy) { trash.body.add(value); notifyContainerModified(trash); }
		else               { bin.body.add(value);   notifyContainerModified(bin);   }
	}

	/** Dispatch unclaimed value to GAP (static) or VOID (dynamic) per active policy. */
	public void addToUnclaimed(Object value) {
		if (dynamicPolicy) { voidC.body.add(value); notifyContainerModified(voidC); }
		else               { gap.body.add(value);   notifyContainerModified(gap);   }
	}

	public BoxInstance getActiveErrorSink() { return dynamicPolicy ? trash : bin; }

	public Interpreter() {
		globals.define("BIN",    (Token) null, bin);
		globals.define("NIB",    (Token) null, bin);
		globals.define("GAP",    (Token) null, gap);
		globals.define("PAG",    (Token) null, gap);
		globals.define("NON",    (Token) null, non);
		globals.define("TRASH",  (Token) null, trash);
		globals.define("HSART",  (Token) null, trash);
		globals.define("VOID",   (Token) null, voidC);
		globals.define("DIOV",   (Token) null, voidC);
		globals.define("LIMBO",  (Token) null, limbo);
		globals.define("OBMIL",  (Token) null, limbo);

		globals.define("dumpPriority",  (Token) null, dumpPriorityToken);
		globals.define("ytirorpPmud",   (Token) null, dumpPriorityToken);
		globals.define("THREAD",        (Token) null, threadToken);
		globals.define("DAERHT",        (Token) null, threadToken);
		globals.define("DYNAMICSAFTY",  (Token) null, dynamicSaftyToken);
		globals.define("YTFASCIMANYD",  (Token) null, dynamicSaftyToken);

		ContainerPersistence.loadAll(this);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (trashWorker != null) { trashWorker.shutdown();
				try { trashWorker.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException ignored) {} }
			if (voidWorker  != null) { voidWorker.shutdown();
				try { voidWorker.awaitTermination(5,  java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException ignored) {} }
			ContainerPersistence.dumpAll(this);
		}));

		globals.define("clock", null, new BoxCallable() {

			@Override
			public int arity() {

				return 0;
			}

			@Override
			public String toString() {
				return "<native fn>";
			}

			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				return (double) System.currentTimeMillis() / 1000.0;
			}

			@Override
			public BoxFunction findMethod(String lexeme) {
				// TODO Auto-generated method stub
				return null;
			}

		});

	}

	public boolean isForward() {
		return forward;
	}

	public void interpret(List<Declaration> statements2) {
		try {

			for (int i = forward ? 0 : statements2.size() - 1; i >= 0 && i < statements2.size();) {
				execute(statements2.get(i));
				probeLifetimes();
				if (forward)
					i++;
				else
					i--;
			}

		} catch (RuntimeError e) {
			Box.runtimeError(e);
			addToErrorSink(e.getMessage());
		}

	}

	Object execute(Stmt stmt) {
		return stmt.accept(this);
	}

	Object execute(Declaration stmt) {
		return stmt.accept(this);

	}

	Object evaluate(Expr expression) {
		return expression.accept(this);
	}

	Object evaluate(Declaration expression) {
		return expression.accept(this);
	}
	public Object evaluate(Stmt stmt) {

		return stmt.accept(this);

	}

	private String stringify(Object object) {
		if (object == null)
			return "null";

		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}
		if (object instanceof Bin) {
			String text = ((Bin) object).toString();
			return text;
		}
		if (object instanceof Literal) {
			String text = ((Literal) object).value.toString();
			return text;
		}
		if (object instanceof ArrayList) {
			String total = "";
			for (Object entry : (ArrayList<?>) object) {

				if (entry instanceof ArrayList) {
					for (Object subEntry : (ArrayList<?>) entry) {
						total += stringify(subEntry);
					}
				} else if (!(entry instanceof ArrayList) && entry != null) {

					total += entry.toString();
				}
			}
			return total;
		}
		if (object instanceof String) {
			return ((String) object).replaceAll("\\\\n", "\n");

		}

		return object.toString();
	}
	private String stringify(Object object, Expr expression) {
		if (object == null)
			return "null";

		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length() - 2);
			}
			return text;
		}
		if (object instanceof Bin) {
			String text = ((Bin) object).toString();
			return text;
		}
		if (object instanceof Literal) {
			String text = ((Literal) object).value.toString();
			return text;
		}
		if (object instanceof ArrayList) {
			Object lookUpVariableTypeByName = lookUpVariableTypeByName((Expr.Variable)expression);
			String total = "";
			for (Object entry : (ArrayList<?>) object) {

				if (entry instanceof ArrayList) {
					for (Object subEntry : (ArrayList<?>) entry) {
						total += stringify(subEntry);
					}
				} else if (!(entry instanceof ArrayList) && entry != null) {

					total += entry.toString();
				}
			}
			return total;
		}
		
		if (object instanceof String) {
			return ((String) object).replaceAll("\\\\n", "\n");

		}
		if (object instanceof BoxInstance) {
			return ((BoxInstance)object).toString();

		}

		return object.toString();
	}

	@Override
	public Object visitExpressionStmt(Stmt.Expression stmt) {
		Expr activeExpr = forward ? stmt.expression : stmt.noisserpxe;
		if (activeExpr == null) return null;
		Object result = evaluate(activeExpr);
		if (activeExpr instanceof Expr.Variable) {
			Token nameToken = ((Expr.Variable) activeExpr).name;
			if (result instanceof PocketInstance) {
				PocketInstance pkt = (PocketInstance) result;
				if (pkt.isAlive()) pkt.startIndependent(environment, nameToken);
			} else if (result instanceof TkpInstance) {
				TkpInstance tkp = (TkpInstance) result;
				if (tkp.isAlive()) tkp.startIndependent(environment, nameToken);
			}
		}
		return result;
	}

	@Override
	public Object visitAssignmentExpr(Assignment expr) {
		if (forward) {
			Object value = evaluate(expr.value);
			if (value == null) {
				Object current = lookUpVariable(expr.name, expr);
				if (current instanceof PocketInstance) ((PocketInstance) current).beginDeath();
			}
			Object boxed = Boxer.box(value, this);
			Integer distance = locals.get(expr);
			if (distance != null)
				environment.assignAt(distance, expr.name, boxed, boxed, this);
			else
				globals.assign(expr.name, boxed, boxed, this);
			return boxed;
		}
		return null;
	}

	@Override
	public Object visitVariableExpr(Variable expr) {

		return lookUpVariable(expr.name, expr);
	}

	Object lookUpVariable(Token name, Expr expr) {

		Integer distance = null;
		String tokenName = null;
		boolean inmap = false;
		if (nameMap.keySet().contains(name.lexeme)) {
			java.util.Set<Expr> keySet = locals.keySet();
			for (Expr expr2 : keySet) {
				tokenName = getTokenName(expr2);
				if (nameMap.get(name.lexeme).equals(tokenName)) {
					distance = locals.get(expr2);
					inmap = true;
					break;
				}
			}
		} else {
			distance = locals.get(expr);

		}
Object returnObject=null;
		if (distance != null) {
			if (inmap)
				returnObject= environment.getAt(distance, tokenName);
			else
				returnObject= environment.getAt(distance, name.lexeme);
		} else {
			if (fromCall) {
				fromCall = false;
				returnObject= globals.get(name, true);
			} else {
				returnObject= globals.get(name, false);
			}
		}
		
		
	
			return returnObject;
		
		
	}

	private String getTokenName(Expr evaluate) {
		if (evaluate instanceof Expr.Box) {
			return ((Expr.Box) evaluate).identifier.lexeme;
		} else if (evaluate instanceof Expr.Pocket) {
			return ((Expr.Pocket) evaluate).identifier.lexeme;
		} else if (evaluate instanceof Expr.Cup) {
			return ((Expr.Cup) evaluate).identifier.lexeme;
		} else if (evaluate instanceof Expr.Knot) {
			return ((Expr.Knot) evaluate).identifier.lexeme;
		} else if (evaluate instanceof Expr.Tonk) {
			return ((Tonk) evaluate).identifier.lexeme;
		} else if (evaluate instanceof Expr.Variable) {
			return ((Variable) evaluate).name.lexeme;
		}
		return null;
	}

	@Override
	public Object visitPrintStmt(Print stmt) {
		Object value = "";
		if (forward) {
			value = evaluate(stmt.expression);
			audio.onEvent(new InterpreterAudio.AudioEvent(TokenType.PRINT, stringify(value), 0.0, System.nanoTime()));
			System.out.print(stringify(value));

		}
		return value;
	}

	@Override
	public Object visitBinaryExpr(Binary expr) {
		Object left = null;
		Object right = null;

		if (expr.left instanceof Pocket || expr.left instanceof Cup) {
			left = evaluate(expr.left);
			right = evaluate(expr.right);
		} else if (expr.right instanceof Pocket || expr.right instanceof Cup) {
			right = evaluate(expr.right);
			left = evaluate(expr.left);
		} else {
			left = evaluate(expr.left);
			right = evaluate(expr.right);
		}

		Object rawLeft = left, rawRight = right;
		if (!isVecBinaryOp(expr.operator.type)) {
			left = parseBinData(left);
			right = parseBinData(right);
		}

		switch (expr.operator.type) {
		case NOTEQUALS:
			return !isEqual(left, right);
		case EQUALSNOT:
			return !isEqual(left, right);
		case EQUALSEQUALS:
			return isEqual(left, right);

		case GREATERTHEN:
			return invertedMode ? lessthen(left, right) : greaterthen(left, right);
		case GREATERTHENEQUAL:
			return invertedMode ? lessequalthen(left, right) : greaterequalthen(left, right);
		case LESSTHEN:
			return invertedMode ? greaterthen(left, right) : lessthen(left, right);
		case LESSTHENEQUAL:
			return invertedMode ? greaterequalthen(left, right) : lessequalthen(left, right);
		case EQUALLESSTHEN:
			return greaterequalthen(left, right);
		case EQUALGREATERTHEN:
			return lessequalthen(left, right);

		case MINUS:
			if (invertedMode) return add(left, right);
			audio.onEvent(new InterpreterAudio.AudioEvent(TokenType.MINUS, left.toString() + "-" + right.toString(),
					0.0, System.nanoTime()));
			return sub(left, right);
		case EQUALSMINUS:
			return sub(left, right, expr.left, expr.operator);
		case MINUSEQUALS:
			return sub(left, right, expr.right, expr.operator);

		case EQUALSPLUS:
			return add(left, right, expr.left, expr.operator);
		case PLUSEQUALS:
			return add(left, right, expr.right, expr.operator);
		case PLUS:
			if (invertedMode) return sub(left, right);
			audio.onEvent(new InterpreterAudio.AudioEvent(TokenType.PLUS, left.toString() + "+" + right.toString(), 0.0,
					System.nanoTime()));
			return add(left, right, expr.left, expr.right);
		case MOD:
			return mod(left, right);
		case MODEQUAL:
			return mod(left, right, expr.right, expr.operator);
		case EQUALMOD:
			return mod(left, right, expr.left, expr.operator);

		case FORWARDSLASH:
			if (invertedMode) return times(left, right);
			audio.onEvent(new InterpreterAudio.AudioEvent(TokenType.FORWARDSLASH,
					left.toString() + "/" + right.toString(), 0.0, System.nanoTime()));
			return div(left, right);
		case BACKSLASH:
			if (invertedMode) return times(right, left);
			Object div = div(right, left);
			return div;
		case EQUALDIVIDEFORWARD:
			return div(right, left, expr.left, expr.operator);
		case EQUALDIVIDEBACKWARD:
			return div(right, left, expr.right, expr.operator);

		case TIMES:
			if (invertedMode) return div(left, right);
			audio.onEvent(new InterpreterAudio.AudioEvent(TokenType.PLUS, left.toString() + "*" + right.toString(), 0.0,
					System.nanoTime()));
			return times(left, right);
		case TIMESEQUAL:
			return times(left, right, expr.right, expr.operator);
		case EQUALTIMES:
			return times(left, right, expr.left, expr.operator);

		case POWER:
			return power(left, right);
		case EQUALPOWER:
			return power(left, right, expr.left, expr.operator);
		case POWEREQUAL:
			return power(left, right, expr.right, expr.operator);
		case YROOT:
			return toory(expr.right,expr.left);
		case TOORY:
			return toory(expr.left,expr.right);

		case MIN: return invertedMode ? bitwiseMax(left, right) : bitwiseMin(left, right);
		case MAX: return invertedMode ? bitwiseMin(left, right) : bitwiseMax(left, right);
		case BAND: return invertedMode ? bitwiseOr(left, right)  : bitwiseAnd(left, right);
		case BOR:  return invertedMode ? bitwiseAnd(left, right) : bitwiseOr(left, right);
		case BXOR: return bitwiseXor(left, right);
		case BLEFT: return invertedMode ? bitwiseRshift(left, right) : bitwiseLshift(left, right);
		case BRIGHT:return invertedMode ? bitwiseLshift(left, right) : bitwiseRshift(left, right);

		case VDOT: case CROSS: case VADD: case VSUB: case VSCALE:
			return VecMatHelper.dispatchVecBinary(expr.operator.type, rawLeft, rawRight,
			                                      expr.operator, invertedMode, this);

		case DNA:
			if (!forward)
				return invertedMode ? or(left, right) : and(left, right);
			else
				return null;
		case AND:
			if (forward)
				return invertedMode ? or(left, right) : and(left, right);
			else
				return null;
		case RO:
			if (!forward)
				return invertedMode ? and(left, right) : or(left, right);
			else
				return false;
		case OR:
			if (forward)
				return invertedMode ? and(left, right) : or(left, right);
			else
				return false;
		default:
			return null;
		}

	}

	private Object power(Object left, Object right, Expr left2, Token operator) {
		Object toreturn = null;
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				toreturn = powerIntegerInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = powerIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = powerIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = powerArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				toreturn = timesDoubleInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = timesDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = timesDoubleBin(left, right);
			} else if (right instanceof String) {
				toreturn = timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = powerArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				toreturn = timesBinInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = timesBinDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = timesBinBin(left, right);
			} else if (right instanceof String) {
				toreturn = timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = powerArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				toreturn = timesObjectString(right, left);
			} else if (right instanceof Double) {
				toreturn = timesObjectString(right, left);
			} else if (right instanceof Bin) {
				toreturn = timesObjectString(right, left);
			} else if (right instanceof ArrayList<?>) {
				toreturn = addStringArray(left, right);
			}

		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				toreturn = powerArrayListArrayList(left, right, 1);
			else
				toreturn = powerBinaryArrayList(left, right, 1);
		}
		if (left2 instanceof Variable)
			setGlobalOrCurrentEnvironmentVariable(toreturn, ((Variable) left2));
		else
			throw new RuntimeError(operator, "either left or right must be a variable.");

		return toreturn;
	}

	private Object mod(Object left, Object right, Expr right2, Token operator) {
		Object toreturn = null;
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				toreturn = modIntegerInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = modIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = modIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = modArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				toreturn = modDoubleInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = modDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = modDoubleBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = modArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				toreturn = modBinInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = modBinDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = modBinBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = modArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				toreturn = modArrayListArrayList(left, right, 1);
			else
				toreturn = modBinaryArrayList(left, right, 1);
		}

		if (right2 instanceof Variable)
			setGlobalOrCurrentEnvironmentVariable(toreturn, ((Variable) right2));
		else
			throw new RuntimeError(operator, "either left or right must be a variable.");

		return toreturn;
	}

	private Object mod(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return modIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return modIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return modIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return modArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return modDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return modDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return modDoubleBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return modArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return modBinInteger(left, right);
			} else if (right instanceof Double) {
				return modBinDouble(left, right);
			} else if (right instanceof Bin) {
				return modBinBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return modArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return modArrayListArrayList(left, right, 1);
			else
				return modBinaryArrayList(left, right, 1);
		}
		return null;
	}

	private Object times(Object left, Object right, Expr right2, Token operator) {
		Object toreturn = null;
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				toreturn = timesIntegerInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = timesIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = timesIntegerBin(left, right);
			} else if (right instanceof String) {
				toreturn = timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = timesArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				toreturn = timesDoubleInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = timesDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = timesDoubleBin(left, right);
			} else if (right instanceof String) {
				toreturn = timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = timesArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				toreturn = timesBinInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = timesBinDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = timesBinBin(left, right);
			} else if (right instanceof String) {
				toreturn = timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = timesArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				toreturn = timesObjectString(right, left);
			} else if (right instanceof Double) {
				toreturn = timesObjectString(right, left);
			} else if (right instanceof Bin) {
				toreturn = timesObjectString(right, left);
			} else if (right instanceof ArrayList<?>) {
				toreturn = addStringArray(left, right);
			}

		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				toreturn = timesArrayListArrayList(left, right, 1);
			else
				toreturn = timesBinaryArrayList(left, right, 1);
		}
		if (right2 instanceof Variable)
			setGlobalOrCurrentEnvironmentVariable(toreturn, ((Variable) right2));
		else
			throw new RuntimeError(operator, "either left or right must be a variable.");

		return toreturn;
	}

	private Object div(Object right, Object left, Object lef, Token operator) {
		Object toreturn = null;
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				toreturn = divIntegerInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = divIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = divIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = divArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				toreturn = divDoubleInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = divDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = divDoubleBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = divArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				toreturn = divBinInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = divBinDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = divBinBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = divArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				toreturn = divArrayListArrayList(left, right, 1);
			else
				toreturn = divBinaryArrayList(left, right, 1);
		}

		if (lef instanceof Variable)
			setGlobalOrCurrentEnvironmentVariable(toreturn, ((Variable) lef));
		else
			throw new RuntimeError(operator, "either left or right must be a variable.");

		return toreturn;
	}

	private Object add(Object left, Object right, Expr left2, Token operator) {
		Object toreturn = null;
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				toreturn = addIntegerInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = addIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = addIntegerBin(left, right);
			} else if (right instanceof String) {
				toreturn = addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				toreturn = addDoubleInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = addDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = addDoubleBin(left, right);
			} else if (right instanceof String) {
				toreturn = addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				toreturn = addBinInteger(left, right);
			} else if (right instanceof Double) {
				toreturn = addBinDouble(left, right);
			} else if (right instanceof Bin) {
				toreturn = addBinBin(left, right);
			} else if (right instanceof String) {
				toreturn = addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {

				toreturn = addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				toreturn = addObjectString(left, right);
			} else if (right instanceof Double) {
				toreturn = addObjectString(left, right);
			} else if (right instanceof Bin) {
				toreturn = addObjectString(left, right);
			} else if (right instanceof String) {
				toreturn = addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				toreturn = addStringArray(left, right);
			}

		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				toreturn = addArrayListArrayList(left, right, 1);
			else
				toreturn = addBinaryArrayList(left, right, 1);
		}

		if (left2 instanceof Variable)
			setGlobalOrCurrentEnvironmentVariable(toreturn, ((Variable) left2));
		else
			throw new RuntimeError(operator, "either left or right must be a variable.");

		return toreturn;
	}

	private Object sub(Object left, Object right, Object left2, Token operator) {
		Object toreturn = null;
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				toreturn = addIntegerInteger(left, -1 * (Integer) right);
			} else if (right instanceof Double) {
				toreturn = addIntegerDouble(left, -1 * (Double) right);
			} else if (right instanceof Bin) {
				toreturn = addIntegerBin(left, Bin.times(new Bin(-1), (Bin) right));
			} else if (right instanceof ArrayList<?>) {
				toreturn = addArrayBinaryList(left, right, -1);

			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				toreturn = addDoubleInteger(left, -1 * (Integer) right);
			} else if (right instanceof Double) {
				toreturn = addDoubleDouble(left, -1 * (Double) right);
			} else if (right instanceof Bin) {
				toreturn = addDoubleBin(left, Bin.times(new Bin(-1), (Bin) right));
			} else if (right instanceof ArrayList<?>) {

				toreturn = addArrayBinaryList(left, right, -1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				toreturn = addBinInteger(left, -1 * (Integer) right);
			} else if (right instanceof Double) {
				toreturn = addBinDouble(left, -1 * (Double) right);
			} else if (right instanceof Bin) {
				toreturn = addBinBin(left, Bin.times(new Bin(-1), (Bin) right));
			} else if (right instanceof ArrayList<?>) {
				toreturn = addArrayBinaryList(left, right, -1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				toreturn = addArrayListArrayList(left, right, -1);
			else
				toreturn = addBinaryArrayList(left, right, -1);
		}

		if (left2 instanceof Variable)
			setGlobalOrCurrentEnvironmentVariable(toreturn, ((Variable) left2));
		else
			throw new RuntimeError(operator, "either left or right must be a variable.");

		return toreturn;
	}

	private Object and(Object left, Object right) {
		ArrayList<Boolean> bolArr = new ArrayList<>();
		if (left instanceof Boolean && right instanceof Boolean) {
			return (Boolean) left && (Boolean) right;
		} else if (left == null && right == null) {
			return false;
		} else if (left instanceof Boolean && right == null) {
			return (Boolean) left;
		} else if (left == null && right instanceof Boolean) {
			return (Boolean) right;
		} else if (left instanceof ArrayList<?> && right == null) {
			return left;
		} else if (left == null && right instanceof ArrayList<?>) {
			return right;
		} else if (left instanceof ArrayList<?> && right instanceof Boolean) {
			ArrayList<?> arr = ((ArrayList<?>) left);
			for (Object object : arr) {
				if (object instanceof Boolean) {
					bolArr.add((Boolean) object && (Boolean) right);
				} else if (object instanceof ArrayList<?>) {
					bolArr.add((Boolean) and(object, right) && (Boolean) right);
				}
			}
			return bolArr;
		} else if (left instanceof Boolean && right instanceof ArrayList<?>) {
			ArrayList<?> arr = ((ArrayList<?>) right);
			for (Object object : arr) {
				if (object instanceof Boolean) {
					bolArr.add((Boolean) left && (Boolean) object);
				} else if (object instanceof ArrayList<?>) {
					bolArr.add((Boolean) and(left, object) && (Boolean) left);
				}
			}
			return bolArr;
		} else if (left instanceof ArrayList<?> && right instanceof ArrayList<?>) {
			ArrayList<?> arr = ((ArrayList<?>) left);
			ArrayList<?> arr1 = ((ArrayList<?>) right);
			for (Object object : arr) {
				for (Object object1 : arr1) {
					if (object instanceof Boolean && object1 instanceof Boolean) {
						bolArr.add((Boolean) object && (Boolean) object1);
					} else if (object instanceof Boolean && object1 instanceof ArrayList<?>) {
						bolArr.add((Boolean) object && (Boolean) and(object, object1));
					} else if (object instanceof ArrayList<?> && object1 instanceof Boolean) {
						bolArr.add((Boolean) and(object, object1) && (Boolean) object1);
					} else if (object instanceof ArrayList<?> && object1 instanceof ArrayList<?>) {
						bolArr.add((Boolean) and(object, object1) && (Boolean) and(object, object1));
					}
				}
			}
			return bolArr;

		}
		return null;
	}

	private Object or(Object left, Object right) {
		ArrayList<Boolean> bolArr = new ArrayList<>();
		if (left instanceof Boolean && right instanceof Boolean) {
			return (Boolean) left && (Boolean) right;
		} else if (left == null && right == null) {
			return false;
		} else if (left instanceof Boolean && right == null) {
			return (Boolean) left;
		} else if (left == null && right instanceof Boolean) {
			return (Boolean) right;
		} else if (left instanceof ArrayList<?> && right == null) {
			return left;
		} else if (left == null && right instanceof ArrayList<?>) {
			return right;
		} else if (left instanceof ArrayList<?> && right instanceof Boolean) {
			ArrayList<?> arr = ((ArrayList<?>) left);
			for (Object object : arr) {
				if (object instanceof Boolean) {
					bolArr.add((Boolean) object || (Boolean) right);
				} else if (object instanceof ArrayList<?>) {
					bolArr.add((Boolean) and(object, right) || (Boolean) right);
				}
			}
			return bolArr;
		} else if (left instanceof Boolean && right instanceof ArrayList<?>) {
			ArrayList<?> arr = ((ArrayList<?>) right);
			for (Object object : arr) {
				if (object instanceof Boolean) {
					bolArr.add((Boolean) left || (Boolean) object);
				} else if (object instanceof ArrayList<?>) {
					bolArr.add((Boolean) and(left, object) || (Boolean) left);
				}
			}
			return bolArr;
		} else if (left instanceof ArrayList<?> && right instanceof ArrayList<?>) {
			ArrayList<?> arr = ((ArrayList<?>) left);
			ArrayList<?> arr1 = ((ArrayList<?>) right);
			for (Object object : arr) {
				for (Object object1 : arr1) {
					if (object instanceof Boolean && object1 instanceof Boolean) {
						bolArr.add((Boolean) object || (Boolean) object1);
					} else if (object instanceof Boolean && object1 instanceof ArrayList<?>) {
						bolArr.add((Boolean) object || (Boolean) and(object, object1));
					} else if (object instanceof ArrayList<?> && object1 instanceof Boolean) {
						bolArr.add((Boolean) and(object, object1) || (Boolean) object1);
					} else if (object instanceof ArrayList<?> && object1 instanceof ArrayList<?>) {
						bolArr.add((Boolean) and(object, object1) || (Boolean) and(object, object1));
					}
				}
			}
			return bolArr;

		}
		return null;
	}

	private Object times(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return timesIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return timesIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return timesIntegerBin(left, right);
			} else if (right instanceof String) {
				return timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return timesArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return timesDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return timesDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return timesDoubleBin(left, right);
			} else if (right instanceof String) {
				return timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return timesArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return timesBinInteger(left, right);
			} else if (right instanceof Double) {
				return timesBinDouble(left, right);
			} else if (right instanceof Bin) {
				return timesBinBin(left, right);
			} else if (right instanceof String) {
				return timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return timesArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				return timesObjectString(right, left);
			} else if (right instanceof Double) {
				return timesObjectString(right, left);
			} else if (right instanceof Bin) {
				return timesObjectString(right, left);
			} else if (right instanceof ArrayList<?>) {
				return addStringArray(left, right);
			}

		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return timesArrayListArrayList(left, right, 1);
			else
				return timesBinaryArrayList(left, right, 1);
		}else if(left instanceof BoxInstance) {
			List<?> bodyLeft = ((BoxInstance)left).body;
			
			
			if (right instanceof Integer) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add(((Integer)right));
				List<?> timesArrayListArrayList = timesArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
			} else if (right instanceof Double) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add(((Double)right));
				List<?> timesArrayListArrayList = timesArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof Bin) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add((Bin)right);
				List<?> timesArrayListArrayList = timesArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof String) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add((String)right);
				List<?> timesArrayListArrayList = timesArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof ArrayList<?>) {
				
				return timesArrayListArrayList(bodyLeft, right, 1);
			}
		}
		return null;
	}

	private Object power(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return powerIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return powerIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return powerIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return powerArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return timesDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return timesDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return timesDoubleBin(left, right);
			} else if (right instanceof String) {
				return timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return powerArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return timesBinInteger(left, right);
			} else if (right instanceof Double) {
				return timesBinDouble(left, right);
			} else if (right instanceof Bin) {
				return timesBinBin(left, right);
			} else if (right instanceof String) {
				return timesObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return powerArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				return timesObjectString(right, left);
			} else if (right instanceof Double) {
				return timesObjectString(right, left);
			} else if (right instanceof Bin) {
				return timesObjectString(right, left);
			} else if (right instanceof ArrayList<?>) {
				return addStringArray(left, right);
			}

		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return powerArrayListArrayList(left, right, 1);
			else
				return powerBinaryArrayList(left, right, 1);
		}
		return null;
	}

	private Object sub(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return addIntegerInteger(left, -1 * (Integer) right);
			} else if (right instanceof Double) {
				return addIntegerDouble(left, -1 * (Double) right);
			} else if (right instanceof Bin) {
				return addIntegerBin(left, Bin.times(new Bin(-1), (Bin) right));
			} else if (right instanceof ArrayList<?>) {
				return addArrayBinaryList(left, right, -1);

			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return addDoubleInteger(left, -1 * (Integer) right);
			} else if (right instanceof Double) {
				return addDoubleDouble(left, -1 * (Double) right);
			} else if (right instanceof Bin) {
				return addDoubleBin(left, Bin.times(new Bin(-1), (Bin) right));
			} else if (right instanceof ArrayList<?>) {

				return addArrayBinaryList(left, right, -1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return addBinInteger(left, -1 * (Integer) right);
			} else if (right instanceof Double) {
				return addBinDouble(left, -1 * (Double) right);
			} else if (right instanceof Bin) {
				return addBinBin(left, Bin.times(new Bin(-1), (Bin) right));
			} else if (right instanceof ArrayList<?>) {
				return addArrayBinaryList(left, right, -1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return addArrayListArrayList(left, right, -1);
			else
				return addBinaryArrayList(left, right, -1);
		}else if(left instanceof BoxInstance) {
			List<?> bodyLeft = ((BoxInstance)left).body;
			
			
			if (right instanceof Integer) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add(((Integer)right));
				List<?> timesArrayListArrayList = addArrayListArrayList(bodyLeft, bodyRight, -1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
			} else if (right instanceof Double) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add(((Double)right));
				List<?> timesArrayListArrayList = addArrayListArrayList(bodyLeft, bodyRight, -1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof Bin) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add((Bin)right);
				List<?> timesArrayListArrayList = addArrayListArrayList(bodyLeft, bodyRight, -1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof String) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add((String)right);
				List<?> timesArrayListArrayList = addArrayListArrayList(bodyLeft, bodyRight, -1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof ArrayList<?>) {
				
				return addArrayListArrayList(bodyLeft, right, -1);
			}
		}

		return null;
	}

	private Object add(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return addIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return addIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return addIntegerBin(left, right);
			} else if (right instanceof String) {
				return addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return addDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return addDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return addDoubleBin(left, right);
			} else if (right instanceof String) {
				return addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return addBinInteger(left, right);
			} else if (right instanceof Double) {
				return addBinDouble(left, right);
			} else if (right instanceof Bin) {
				return addBinBin(left, right);
			} else if (right instanceof String) {
				return addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {

				return addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				return addObjectString(left, right);
			} else if (right instanceof Double) {
				return addObjectString(left, right);
			} else if (right instanceof Bin) {
				return addObjectString(left, right);
			} else if (right instanceof String) {
				return addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return addStringArray(left, right);
			} else {

				if (right != null)
					return left.toString() + right.toString();
				else
					return left.toString() + "null";
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return addArrayListArrayList(left, right, 1);
			else
				return addBinaryArrayList(left, right, 1);
		}
		return null;
	}

	private Object add(Object left, Object right, Expr left2, Expr right2) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return addIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return addIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return addIntegerBin(left, right);
			} else if (right instanceof String) {
				return addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return addDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return addDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return addDoubleBin(left, right);
			} else if (right instanceof String) {
				return addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return addBinInteger(left, right);
			} else if (right instanceof Double) {
				return addBinDouble(left, right);
			} else if (right instanceof Bin) {
				return addBinBin(left, right);
			} else if (right instanceof String) {
				return addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {

				return addArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof String) {
			if (right instanceof Integer) {
				return addObjectString(left, right);
			} else if (right instanceof Double) {
				return addObjectString(left, right);
			} else if (right instanceof Bin) {
				return addObjectString(left, right);
			} else if (right instanceof String) {
				return addObjectString(left, right);
			} else if (right instanceof ArrayList<?>) {
				return addStringArray(left, right);
			} else {

				if (right instanceof Boolean)
					return left.toString() + right2.toString();
				else
					return left.toString() + right.toString();
			}
		} else if (left instanceof Boolean) {

			if (right instanceof String)
				return left2.toString() + right.toString();
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return addArrayListArrayList(left, right, 1);
			else
				return addBinaryArrayList(left, right, 1);
		}else if(left instanceof BoxInstance) {
			List<?> bodyLeft = ((BoxInstance)left).body;
			
			
			if (right instanceof Integer) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add(((Integer)right));
				List<?> timesArrayListArrayList = addArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
			} else if (right instanceof Double) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add(((Double)right));
				List<?> timesArrayListArrayList = addArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof Bin) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add((Bin)right);
				List<?> timesArrayListArrayList = addArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof String) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add((String)right);
				List<?> timesArrayListArrayList = addArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof ArrayList<?>) {
				
				return addArrayListArrayList(bodyLeft, right, 1);
			}
		}
		return null;

	}

	private Object greaterthen(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return gtIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return gtIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return gtIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return gtArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return gtDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return gtDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return gtDoubleBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return gtArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return gtBinInteger(left, right);
			} else if (right instanceof Double) {
				return gtBinDouble(left, right);
			} else if (right instanceof Bin) {
				return gtBinBin(left, right);
			} else if (right instanceof ArrayList<?>) {

				return gtArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return gtArrayListArrayList(left, right, 1);
			else
				return gtBinaryArrayList(left, right, 1);
		}
		return null;
	}

	private Object greaterequalthen(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return gteIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return gteIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return gteIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return gteArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return gteDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return gteDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return gteDoubleBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return gteArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return gteBinInteger(left, right);
			} else if (right instanceof Double) {
				return gteBinDouble(left, right);
			} else if (right instanceof Bin) {
				return gteBinBin(left, right);
			} else if (right instanceof ArrayList<?>) {

				return gteArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return gteArrayListArrayList(left, right, 1);
			else
				return gteBinaryArrayList(left, right, 1);
		}
		return null;
	}

	private Object lessequalthen(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return lteIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return lteIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return lteIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return lteArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return lteDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return lteDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return lteDoubleBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return lteArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return lteBinInteger(left, right);
			} else if (right instanceof Double) {
				return lteBinDouble(left, right);
			} else if (right instanceof Bin) {
				return lteBinBin(left, right);
			} else if (right instanceof ArrayList<?>) {

				return lteArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return lteArrayListArrayList(left, right, 1);
			else
				return lteBinaryArrayList(left, right, 1);
		}
		return null;
	}

	private Object lessthen(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return ltIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return ltIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return ltIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return ltArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return ltDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return ltDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return ltDoubleBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return ltArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return ltBinInteger(left, right);
			} else if (right instanceof Double) {
				return ltBinDouble(left, right);
			} else if (right instanceof Bin) {
				return ltBinBin(left, right);
			} else if (right instanceof ArrayList<?>) {

				return ltArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return ltArrayListArrayList(left, right, 1);
			else
				return ltBinaryArrayList(left, right, 1);
		}
		return null;
	}

	private Object div(Object left, Object right) {
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				return divIntegerInteger(left, right);
			} else if (right instanceof Double) {
				return divIntegerDouble(left, right);
			} else if (right instanceof Bin) {
				return divIntegerBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return divArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				return divDoubleInteger(left, right);
			} else if (right instanceof Double) {
				return divDoubleDouble(left, right);
			} else if (right instanceof Bin) {
				return divDoubleBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return divArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof Bin) {
			if (right instanceof Integer) {
				return divBinInteger(left, right);
			} else if (right instanceof Double) {
				return divBinDouble(left, right);
			} else if (right instanceof Bin) {
				return divBinBin(left, right);
			} else if (right instanceof ArrayList<?>) {
				return divArrayBinaryList(left, right, 1);
			}
		} else if (left instanceof ArrayList<?>) {
			if (right instanceof ArrayList<?>)
				return divArrayListArrayList(left, right, 1);
			else
				return divBinaryArrayList(left, right, 1);
		}else if(left instanceof BoxInstance) {
			List<?> bodyLeft = ((BoxInstance)left).body;
			
			
			if (right instanceof Integer) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add(((Integer)right));
				List<?> timesArrayListArrayList = divArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
			} else if (right instanceof Double) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add(((Double)right));
				List<?> timesArrayListArrayList = divArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof Bin) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add((Bin)right);
				List<?> timesArrayListArrayList = divArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof String) {
				List<Object> bodyRight = new ArrayList<>();
				bodyRight.add((String)right);
				List<?> timesArrayListArrayList = divArrayListArrayList(bodyLeft, bodyRight, 1);
				((BoxInstance)left).body =(List<Object>) timesArrayListArrayList;
				return left;
				} else if (right instanceof ArrayList<?>) {
				
				return divArrayListArrayList(bodyLeft, right, 1);
			}
		}
		return null;
	}

	private ArrayList<?> addArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) left + (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Integer) left + (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin((Integer) left),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left + (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left + (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin(((Double) left).intValue()),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Bin.add(((Bin) left), new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof Double) {
					arr2.add(Bin.add(((Bin) left),
							new Bin(Bin.times(new Bin(i), new Bin(((Double) object).intValue())))));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(((Bin) left), new Bin(Bin.times(new Bin(i), (Bin) object))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addArrayBinaryList(left, object, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> ltArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) left < (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Integer) left < (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add((Integer) left < ((Bin) object).toInteger());

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left < (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left < (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(((Double) left) < ((Bin) object).toInteger());

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) left).toInteger() < Bin.times(new Bin(i), new Bin((Integer) object)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) left).toInteger() < Bin.times(new Bin(i), new Bin(((Double) object).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) left).toInteger() < Bin.times(new Bin(i), (Bin) object));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltArrayBinaryList(left, object, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> gtArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) left > (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Integer) left > (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add((Integer) left > ((Bin) object).toInteger());

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left > (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left > (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(((Double) left) > ((Bin) object).toInteger());

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) left).toInteger() > Bin.times(new Bin(i), new Bin((Integer) object)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) left).toInteger() > Bin.times(new Bin(i), new Bin(((Double) object).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) left).toInteger() > Bin.times(new Bin(i), (Bin) object));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtArrayBinaryList(left, object, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> gteArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) left >= (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Integer) left >= (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add((Integer) left >= ((Bin) object).toInteger());

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left >= (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left >= (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(((Double) left) >= ((Bin) object).toInteger());

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) left).toInteger() >= Bin.times(new Bin(i), new Bin((Integer) object)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) left).toInteger() >= Bin.times(new Bin(i), new Bin(((Double) object).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) left).toInteger() >= Bin.times(new Bin(i), (Bin) object));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtArrayBinaryList(left, object, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> lteArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) left <= (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Integer) left <= (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add((Integer) left <= ((Bin) object).toInteger());

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left <= (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left <= (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(((Double) left) <= ((Bin) object).toInteger());

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) left).toInteger() <= Bin.times(new Bin(i), new Bin((Integer) object)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) left).toInteger() <= Bin.times(new Bin(i), new Bin(((Double) object).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) left).toInteger() <= Bin.times(new Bin(i), (Bin) object));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltArrayBinaryList(left, object, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> powerArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(
							Math.pow(((Integer) left).doubleValue(), ((Integer) (i * (Integer) object)).doubleValue()));
				} else if (object instanceof Double) {
					arr2.add(Math.pow(((Integer) left).doubleValue(), (Double) (i * (Double) object)));

				} else if (object instanceof Bin) {
					arr2.add(Math.pow((new Bin((Integer) left)).toDouble(),
							(new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))).toDouble()));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(powerArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left + (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left + (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin(((Double) left).intValue()),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(powerArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Bin.add(((Bin) left), new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof Double) {
					arr2.add(Bin.add(((Bin) left),
							new Bin(Bin.times(new Bin(i), new Bin(((Double) object).intValue())))));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(((Bin) left), new Bin(Bin.times(new Bin(i), (Bin) object))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(powerArrayBinaryList(left, object, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> timesArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) left * (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Integer) left * (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin((Integer) left),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addArrayBinaryList(left, object, i));

				} else if (object instanceof String) {
					String str = "";

					for (int j = 0; j < ((Integer) left); j++) {
						str += object.toString();
					}
					arr2.add(str);
				}
			}
		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left * (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left * (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin(((Double) left).intValue()),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addArrayBinaryList(left, object, i));

				} else if (object instanceof String) {
					String str = "";

					for (int j = 0; j < ((Double) left).intValue(); j++) {
						str += object.toString();
					}
					arr2.add(str);
				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Bin.times(((Bin) left), new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof Double) {
					arr2.add(Bin.times(((Bin) left),
							new Bin(Bin.times(new Bin(i), new Bin(((Double) object).intValue())))));

				} else if (object instanceof Bin) {
					arr2.add(Bin.times(((Bin) left), new Bin(Bin.times(new Bin(i), (Bin) object))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addArrayBinaryList(left, object, i));

				} else if (object instanceof String) {
					String str = "";

					for (int j = 0; j < ((Bin) left).toInteger(); j++) {
						str += object.toString();
					}
					arr2.add(str);
				}
			}

		}
		return arr2;

	}

	private ArrayList<?> divArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) left / (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Integer) left / (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(Bin.divide(new Bin((Integer) left),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(divArrayBinaryList(left, object, i));

				}
			}
		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left / (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left / (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(Bin.divide(new Bin(((Double) left).intValue()),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(divArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Bin.divide(((Bin) left), new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

				} else if (object instanceof Double) {
					arr2.add(Bin.divide(((Bin) left),
							new Bin(Bin.times(new Bin(i), new Bin(((Double) object).intValue())))));

				} else if (object instanceof Bin) {
					arr2.add(Bin.divide(((Bin) left), new Bin(Bin.times(new Bin(i), (Bin) object))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(divArrayBinaryList(left, object, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> modArrayBinaryList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (left instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) left % (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Integer) left % (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add((Integer) left % Bin.times(new Bin(i), new Bin((Integer) object)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(modArrayBinaryList(left, object, i));

				}
			}
		} else if (left instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) left % (i * (Integer) object));
				} else if (object instanceof Double) {
					arr2.add((Double) left % (i * (Double) object));

				} else if (object instanceof Bin) {
					arr2.add(((Double) left).intValue() % Bin.times(new Bin(i), new Bin((Integer) object)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(modArrayBinaryList(left, object, i));

				}
			}

		} else if (left instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) left).toInteger() % Bin.times(new Bin(i), new Bin((Integer) object)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) left).toInteger() % Bin.times(new Bin(i), new Bin(((Double) object).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) left).toInteger() % Bin.times(new Bin(i), (Bin) object));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(modArrayBinaryList(left, object, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> addBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) object + (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Integer) object + (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin((Integer) object),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) object + (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object + (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin(((Double) object).intValue()),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Bin.add(((Bin) object), new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof Double) {
					arr2.add(Bin.add(((Bin) object),
							new Bin(Bin.times(new Bin(i), new Bin(((Double) right).intValue())))));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(((Bin) object), new Bin(Bin.times(new Bin(i), (Bin) right))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(addBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof String) {
			String str = "";
			str += "[";
			for (int j = 0; j < arr.size(); j++) {
				if (arr.get(j) instanceof Integer) {
					if (j == arr.size() - 1)
						str += (Integer) arr.get(j);
					else
						str += (Integer) arr.get(j) + ", ";

				} else if (arr.get(j) instanceof Double) {
					if (j == arr.size() - 1)
						str += (Double) arr.get(j);
					else
						str += (Double) arr.get(j) + ", ";
				} else if (arr.get(j) instanceof Boolean) {
					if (j == arr.size() - 1)
						str += (Boolean) arr.get(j);
					else
						str += (Boolean) arr.get(j) + ", ";
				} else if (arr.get(j) instanceof Bin) {
					if (j == arr.size() - 1)
						str += ((Bin) arr.get(j)).toString();
					else
						str += ((Bin) arr.get(j)).toString() + ", ";
				} else if (arr.get(j) instanceof ArrayList<?>) {
					if (j == arr.size() - 1)
						str += arr.get(j).toString();
					else
						str += arr.get(j).toString() + ", ";
				}

			}
			str += "]";
			str = str + right;
			arr2.add(str);
		}
		return arr2;

	}

	private ArrayList<?> gtBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) object > (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Integer) object > (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add((Integer) object > Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) object > (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object > (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(((Double) object) > Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) object).toInteger() > Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) object).toInteger() > Bin.times(new Bin(i), new Bin(((Double) right).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) object).toInteger() > Bin.times(new Bin(i), (Bin) right));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gtBinaryArrayList(object, right, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> gteBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) object >= (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Integer) object >= (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add((Integer) object >= Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gteBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) object >= (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object >= (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(((Double) object) >= Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gteBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) object).toInteger() >= Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) object).toInteger() >= Bin.times(new Bin(i), new Bin(((Double) right).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) object).toInteger() >= Bin.times(new Bin(i), (Bin) right));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(gteBinaryArrayList(object, right, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> lteBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) object <= (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Integer) object <= (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add((Integer) object <= Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(lteBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) object <= (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object <= (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(((Double) object) <= Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(lteBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) object).toInteger() <= Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) object).toInteger() <= Bin.times(new Bin(i), new Bin(((Double) right).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) object).toInteger() <= Bin.times(new Bin(i), (Bin) right));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(lteBinaryArrayList(object, right, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> ltBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) object < (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Integer) object < (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add((Integer) object < Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) object < (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object < (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(((Double) object) < Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) object).toInteger() < Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) object).toInteger() < Bin.times(new Bin(i), new Bin(((Double) right).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) object).toInteger() < Bin.times(new Bin(i), (Bin) right));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(ltBinaryArrayList(object, right, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> timesBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) object * (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Integer) object * (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin((Integer) object),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(timesBinaryArrayList(object, right, i));

				} else if (object instanceof String) {
					String str = "";

					for (int j = 0; j < ((Integer) right); j++) {
						str += object.toString();
					}
					arr2.add(str);
				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) object * (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object * (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(Bin.add(new Bin(((Double) object).intValue()),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(timesBinaryArrayList(object, right, i));

				} else if (object instanceof String) {
					String str = "";

					for (int j = 0; j < ((Double) right).intValue(); j++) {
						str += object.toString();
					}
					arr2.add(str);
				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Bin.times(((Bin) object), new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof Double) {
					arr2.add(Bin.times(((Bin) object),
							new Bin(Bin.times(new Bin(i), new Bin(((Double) right).intValue())))));

				} else if (object instanceof Bin) {
					arr2.add(Bin.times(((Bin) object), new Bin(Bin.times(new Bin(i), (Bin) right))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(timesBinaryArrayList(object, right, i));

				} else if (object instanceof String) {
					String str = "";

					for (int j = 0; j < ((Bin) right).toInteger(); j++) {
						str += object.toString();
					}
					arr2.add(str);
				}
			}

		} else if (right instanceof String) {
			for (Object object : arr) {
				if (object instanceof Integer) {
					String str = "";

					for (int j = 0; j < ((Integer) object); j++) {
						str += right.toString();
					}
					arr2.add(str);

				} else if (object instanceof Double) {
					String str = "";

					for (int j = 0; j < ((Double) object).intValue(); j++) {
						str += right.toString();
					}
					arr2.add(str);

				} else if (object instanceof Bin) {
					String str = "";

					for (int j = 0; j < ((Bin) object).toInteger(); j++) {
						str += right.toString();
					}
					arr2.add(str);

				} else if (object instanceof ArrayList<?>) {
					arr2.add(timesBinaryArrayList(object, right, i));

				} else if (object instanceof String) {
					String str = "";

					str += object.toString() + right.toString();

					arr2.add(str);
				}
			}

		}
		return arr2;

	}

	private ArrayList<?> powerBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Math.pow(((Integer) object).doubleValue(),
							((Integer) (i * (Integer) right)).doubleValue()));
				} else if (object instanceof Double) {
					arr2.add(Math.pow((Double) object, (Integer) (i * (Integer) right)));

				} else if (object instanceof Bin) {
					arr2.add(Math.pow((Double) object,
							(new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))).toDouble()));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(powerBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Math.pow((Double) object, ((Integer) (i * (Integer) right)).doubleValue()));
				} else if (object instanceof Double) {
					arr2.add(Math.pow((Double) object, (i * (Double) right)));

				} else if (object instanceof Bin) {
					arr2.add(Math.pow(((Bin) object).toDouble(), ((Double) (i * (Double) right))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(powerBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Math.pow(((Bin) object).toDouble(),
							(new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))).toDouble()));

				} else if (object instanceof Double) {
					arr2.add(Math.pow(((Bin) object).toDouble(),
							(new Bin(Bin.times(new Bin(i), new Bin(((Double) right).intValue())))).toDouble()));

				} else if (object instanceof Bin) {
					arr2.add(Math.pow(((Bin) object).toDouble(),
							(new Bin(Bin.times(new Bin(i), (Bin) right))).toDouble()));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(powerBinaryArrayList(object, right, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> modBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) object % (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object % (i * (Integer) right));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) object).toInteger() % Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(modBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) object % (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object % (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(((Double) object) % Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(modBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(((Bin) object).toInteger() % Bin.times(new Bin(i), new Bin((Integer) right)));

				} else if (object instanceof Double) {
					arr2.add(((Bin) object).toInteger() % Bin.times(new Bin(i), new Bin(((Double) right).intValue())));

				} else if (object instanceof Bin) {
					arr2.add(((Bin) object).toInteger() % Bin.times(new Bin(i), (Bin) right));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(modBinaryArrayList(object, right, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> divBinaryArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();

		if (right instanceof Integer) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Integer) object / (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Integer) object / (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(Bin.divide(new Bin((Integer) object),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(divBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Double) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add((Double) object / (i * (Integer) right));
				} else if (object instanceof Double) {
					arr2.add((Double) object / (i * (Double) right));

				} else if (object instanceof Bin) {
					arr2.add(Bin.divide(new Bin(((Double) object).intValue()),
							new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(divBinaryArrayList(object, right, i));

				}
			}

		} else if (right instanceof Bin) {
			for (Object object : arr) {
				if (object instanceof Integer) {

					arr2.add(Bin.divide(((Bin) object), new Bin(Bin.times(new Bin(i), new Bin((Integer) right)))));

				} else if (object instanceof Double) {
					arr2.add(Bin.divide(((Bin) object),
							new Bin(Bin.times(new Bin(i), new Bin(((Double) right).intValue())))));

				} else if (object instanceof Bin) {
					arr2.add(Bin.divide(((Bin) object), new Bin(Bin.times(new Bin(i), (Bin) right))));

				} else if (object instanceof ArrayList<?>) {
					arr2.add(divBinaryArrayList(object, right, i));

				}
			}

		}
		return arr2;

	}

	private ArrayList<?> addArrayListArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<?> arr1 = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Integer) object1 + (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Integer) object1 + (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(Bin.add(new Bin((Integer) object1),
								new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(addBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 + (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 + (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(Bin.add(new Bin(((Double) object1).intValue()),
								new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(addBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(Bin.add(((Bin) object1), new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof Double) {
						arr2.add(Bin.add(((Bin) object1),
								new Bin(Bin.times(new Bin(i), new Bin(((Double) object).intValue())))));

					} else if (object instanceof Bin) {
						arr2.add(Bin.add(((Bin) object1), new Bin(Bin.times(new Bin(i), (Bin) object))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(addBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 + (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 + (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(Bin.add(new Bin(((Double) object1).intValue()),
								new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {

								arr2.add(addBinaryArrayList(object2, object3, i));
							}
						}

					}
				}

			}
		}
		return arr2;

	}

	private ArrayList<?> gtArrayListArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<?> arr1 = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Integer) object1 > (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Integer) object1 > (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add((Integer) object1 > Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(gtBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 > (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 > (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1) > Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(gtBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(((Bin) object1).toInteger() > Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof Double) {
						arr2.add(((Bin) object1).toInteger() > Bin.times(new Bin(i),
								new Bin(((Double) object).intValue())));

					} else if (object instanceof Bin) {
						arr2.add(((Bin) object1).toInteger() > Bin.times(new Bin(i), (Bin) object));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(gtBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 > (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 > (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1).intValue() > Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {

								arr2.add(gtBinaryArrayList(object2, object3, i));
							}
						}

					}
				}

			}
		}
		return arr2;

	}

	private ArrayList<?> gteArrayListArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<?> arr1 = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Integer) object1 >= (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Integer) object1 >= (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add((Integer) object1 >= Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(gteBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 >= (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 >= (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1) >= Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(gteBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(((Bin) object1).toInteger() >= Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof Double) {
						arr2.add(((Bin) object1).toInteger() >= Bin.times(new Bin(i),
								new Bin(((Double) object).intValue())));

					} else if (object instanceof Bin) {
						arr2.add(((Bin) object1).toInteger() >= Bin.times(new Bin(i), (Bin) object));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(gteBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 >= (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 >= (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1).intValue() >= Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {

								arr2.add(gteBinaryArrayList(object2, object3, i));
							}
						}

					}
				}

			}
		}
		return arr2;

	}

	private ArrayList<?> lteArrayListArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<?> arr1 = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Integer) object1 <= (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Integer) object1 <= (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add((Integer) object1 <= Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(lteBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 <= (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 <= (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1) <= Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(lteBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(((Bin) object1).toInteger() <= Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof Double) {
						arr2.add(((Bin) object1).toInteger() <= Bin.times(new Bin(i),
								new Bin(((Double) object).intValue())));

					} else if (object instanceof Bin) {
						arr2.add(((Bin) object1).toInteger() <= Bin.times(new Bin(i), (Bin) object));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(lteBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 <= (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 <= (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1).intValue() <= Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {

								arr2.add(lteBinaryArrayList(object2, object3, i));
							}
						}

					}
				}

			}
		}
		return arr2;

	}

	private ArrayList<?> ltArrayListArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<?> arr1 = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Integer) object1 < (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Integer) object1 < (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add((Integer) object1 < Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(ltBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 < (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 < (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1) < Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(ltBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(((Bin) object1).toInteger() < Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof Double) {
						arr2.add(((Bin) object1).toInteger() < Bin.times(new Bin(i),
								new Bin(((Double) object).intValue())));

					} else if (object instanceof Bin) {
						arr2.add(((Bin) object1).toInteger() < Bin.times(new Bin(i), (Bin) object));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(ltBinaryArrayList(object1, object, i));

					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 < (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 < (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1) < Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {

								arr2.add(ltBinaryArrayList(object2, object3, i));
							}
						}

					}
				}

			}
		}
		return arr2;

	}

	private List<?> timesArrayListArrayList(Object left, Object right, int i) {
		List<?> arr = (List<?>) right;
		List<?> arr1 = (List<?>) left;
		List<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Integer) object1 * (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Integer) object1 * (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(Bin.times(new Bin((Integer) object1),
								new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(timesArrayBinaryList(object1, object, i));

					} else if (object instanceof String) {
						String str = "";

						for (int j = 0; j < ((Integer) object1); j++) {
							str += object.toString();
						}
						arr2.add(str);
					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 * (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 * (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(Bin.times(new Bin(((Double) object1).intValue()),
								new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(timesArrayBinaryList(object1, object, i));

					} else if (object instanceof String) {
						String str = "";

						for (int j = 0; j < ((Double) object1).intValue(); j++) {
							str += object.toString();
						}
						arr2.add(str);
					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(Bin.times(((Bin) object1), new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof Double) {
						arr2.add(Bin.times(((Bin) object1),
								new Bin(Bin.times(new Bin(i), new Bin(((Double) object).intValue())))));

					} else if (object instanceof Bin) {
						arr2.add(Bin.times(((Bin) object1), new Bin(Bin.times(new Bin(i), (Bin) object))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(timesArrayBinaryList(object1, object, i));

					} else if (object instanceof String) {
						String str = "";

						for (int j = 0; j < ((Bin) object1).toInteger(); j++) {
							str += object.toString();
						}
						arr2.add(str);
					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(timesBinaryArrayList(object1, object, i));
					} else if (object instanceof Double) {
						arr2.add(timesBinaryArrayList(object1, object, i));

					} else if (object instanceof Bin) {
						arr2.add(timesBinaryArrayList(object1, object, i));
					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {
								if (object2 instanceof ArrayList<?> && !(object3 instanceof ArrayList<?>))
									arr2.add(timesBinaryArrayList(object2, object3, i));
								else if (object3 instanceof ArrayList<?> && !(object2 instanceof ArrayList<?>))
									arr2.add(timesArrayBinaryList(object2, object3, i));
								else if (object3 instanceof ArrayList<?> && (object2 instanceof ArrayList<?>))
									arr2.add(timesArrayListArrayList(object2, object3, i));
								else
									arr2.add(times(object2, object3));

							}
						}

					} else if (object instanceof String) {
						arr2.add(timesBinaryArrayList(object1, object, i));
					}
				}

			}
		}
		return arr2;

	}

	private ArrayList<?> powerArrayListArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<?> arr1 = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(Math.pow(((Integer) object1).doubleValue(),
								((Integer) (i * (Integer) object)).doubleValue()));
					} else if (object instanceof Double) {
						arr2.add(Math.pow(((Integer) object1).doubleValue(), (Double) (i * (Double) object)));

					} else if (object instanceof Bin) {
						arr2.add(Math.pow((new Bin((Integer) object1)).toDouble(),
								(new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))).toDouble()));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(powerArrayBinaryList(object1, object, i));

					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(Math.pow(((Double) object1), ((Integer) (i * (Integer) object)).doubleValue()));
					} else if (object instanceof Double) {
						arr2.add(Math.pow(((Double) object1), (Double) (i * (Double) object)));

					} else if (object instanceof Bin) {
						arr2.add(Math.pow(((Double) object1),
								(new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))).toDouble()));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(powerArrayBinaryList(object1, object, i));

					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(Math.pow(((Bin) left).toDouble(),
								(new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))).toDouble()));

					} else if (object instanceof Double) {
						arr2.add(Math.pow(((Bin) left).toDouble(),
								(new Bin(Bin.times(new Bin(i), new Bin(((Double) object).intValue())))).toDouble()));

					} else if (object instanceof Bin) {
						arr2.add(Math.pow(((Bin) left).toDouble(),
								(new Bin(Bin.times(new Bin(i), (Bin) object))).toDouble()));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(powerArrayBinaryList(left, object, i));

					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(timesBinaryArrayList(object1, object, i));
					} else if (object instanceof Double) {
						arr2.add(timesBinaryArrayList(object1, object, i));

					} else if (object instanceof Bin) {
						arr2.add(timesBinaryArrayList(object1, object, i));
					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {
								if (object2 instanceof ArrayList<?> && !(object3 instanceof ArrayList<?>))
									arr2.add(powerBinaryArrayList(object2, object3, i));
								else if (object3 instanceof ArrayList<?> && !(object2 instanceof ArrayList<?>))
									arr2.add(powerArrayBinaryList(object2, object3, i));
								else if (object3 instanceof ArrayList<?> && (object2 instanceof ArrayList<?>))
									arr2.add(powerArrayListArrayList(object2, object3, i));
								else
									arr2.add(power(object2, object3));

							}
						}

					}
				}

			}
		}
		return arr2;

	}

	private ArrayList<?> modArrayListArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<?> arr1 = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Integer) object1 % (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Integer) object1 % (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Integer) object1) % Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(modArrayBinaryList(object1, object, i));

					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 % (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 % (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(((Double) object1) % Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(modArrayBinaryList(object1, object, i));

					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(((Bin) object1).toInteger() % Bin.times(new Bin(i), new Bin((Integer) object)));

					} else if (object instanceof Double) {
						arr2.add(((Bin) object1).toInteger()
								% Bin.times(new Bin(i), new Bin(((Double) object).intValue())));

					} else if (object instanceof Bin) {
						arr2.add(((Bin) object1).toInteger() % Bin.times(new Bin(i), (Bin) object));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(modArrayBinaryList(object1, object, i));

					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(modBinaryArrayList(object1, object, i));
					} else if (object instanceof Double) {
						arr2.add(modBinaryArrayList(object1, object, i));

					} else if (object instanceof Bin) {
						arr2.add(modBinaryArrayList(object1, object, i));
					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {

								if (object2 instanceof ArrayList<?> && !(object3 instanceof ArrayList<?>))
									arr2.add(modBinaryArrayList(object2, object3, i));
								else if (object3 instanceof ArrayList<?> && !(object2 instanceof ArrayList<?>))
									arr2.add(modBinaryArrayList(object2, object3, i));
								else if (object3 instanceof ArrayList<?> && (object2 instanceof ArrayList<?>))
									arr2.add(modBinaryArrayList(object2, object3, i));
								else
									arr2.add(mod(object2, object3));

							}
						}

					}
				}

			}
		}
		return arr2;

	}

	private ArrayList<?> divArrayListArrayList(Object left, Object right, int i) {
		ArrayList<?> arr = (ArrayList<?>) right;
		ArrayList<?> arr1 = (ArrayList<?>) left;
		ArrayList<Object> arr2 = new ArrayList<>();
		for (Object object1 : arr1) {
			if (object1 instanceof Integer) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Integer) object1 / (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Integer) object1 / (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(Bin.divide(new Bin((Integer) object1),
								new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(divArrayBinaryList(object1, object, i));

					}
				}

			} else if (object1 instanceof Double) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add((Double) object1 / (i * (Integer) object));
					} else if (object instanceof Double) {
						arr2.add((Double) object1 / (i * (Double) object));

					} else if (object instanceof Bin) {
						arr2.add(Bin.divide(new Bin(((Double) object1).intValue()),
								new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(divArrayBinaryList(object1, object, i));

					}
				}

			} else if (object1 instanceof Bin) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(
								Bin.divide(((Bin) object1), new Bin(Bin.times(new Bin(i), new Bin((Integer) object)))));

					} else if (object instanceof Double) {
						arr2.add(Bin.divide(((Bin) object1),
								new Bin(Bin.times(new Bin(i), new Bin(((Double) object).intValue())))));

					} else if (object instanceof Bin) {
						arr2.add(Bin.divide(((Bin) object1), new Bin(Bin.times(new Bin(i), (Bin) object))));

					} else if (object instanceof ArrayList<?>) {
						arr2.add(divArrayBinaryList(object1, object, i));

					}
				}

			} else if (object1 instanceof ArrayList<?>) {
				for (Object object : arr) {
					if (object instanceof Integer) {

						arr2.add(divBinaryArrayList(object1, object, i));
					} else if (object instanceof Double) {
						arr2.add(divBinaryArrayList(object1, object, i));

					} else if (object instanceof Bin) {
						arr2.add(divBinaryArrayList(object1, object, i));
					} else if (object instanceof ArrayList<?>) {

						ArrayList<?> temparr0 = ((ArrayList<?>) object1);
						ArrayList<?> temparr1 = ((ArrayList<?>) object);

						for (Object object2 : temparr0) {
							for (Object object3 : temparr1) {

								if (object2 instanceof ArrayList<?> && !(object3 instanceof ArrayList<?>))
									arr2.add(divBinaryArrayList(object2, object3, i));
								else if (object3 instanceof ArrayList<?> && !(object2 instanceof ArrayList<?>))
									arr2.add(divArrayBinaryList(object2, object3, i));
								else if (object3 instanceof ArrayList<?> && (object2 instanceof ArrayList<?>))
									arr2.add(divArrayListArrayList(object2, object3, i));
								else
									arr2.add(div(object2, object3));

							}
						}

					}
				}

			}
		}
		return arr2;

	}

	private Object addStringArray(Object left, Object right) {
		ArrayList<?> arr = ((ArrayList<?>) right);
		String str = "";

		str += "[";
		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(i) instanceof Integer) {
				if (i == arr.size() - 1)
					str += ((Integer) arr.get(i));
				else
					str += ((Integer) arr.get(i)) + ", ";
			} else if (arr.get(i) instanceof Double) {
				if (i == arr.size() - 1)
					str += ((Double) arr.get(i));
				else
					str += ((Double) arr.get(i)) + ", ";

			} else if (arr.get(i) instanceof Bin) {
				if (i == arr.size() - 1)
					str += ((Bin) arr.get(i));
				else
					str += ((Bin) arr.get(i)) + ", ";
			} else if (arr.get(i) instanceof String) {
				if (i == arr.size() - 1)
					str += ((String) arr.get(i));
				else
					str += ((String) arr.get(i)) + ", ";
			} else if (arr.get(i) instanceof Boolean) {
				if (i == arr.size() - 1)
					str += ((Boolean) arr.get(i));
				else
					str += ((Boolean) arr.get(i)) + ", ";
			} else if (arr.get(i) instanceof ArrayList<?>) {
				if (i == arr.size() - 1) {
					str += "[";
					str += printArray("", (ArrayList<?>) arr.get(i));
					str += "]";
				} else {

					str += "[";
					str += printArray("", (ArrayList<?>) arr.get(i));
					str += "], ";
				}
			}
		}
		str += "]";
		str = (String) left + str;
		return str;
	}

	private String printArray(String str, ArrayList<?> object) {

		for (int i = 0; i < object.size(); i++) {
			if (object.get(i) instanceof ArrayList<?>) {
				if (i == object.size() - 1) {
					str += "[";
					str += printArray("", (ArrayList<?>) object.get(i));
					str += "]";
				} else {

					str += "[";
					str += printArray("", (ArrayList<?>) object.get(i));
					str += "], ";
				}
			} else {
				if (i == object.size() - 1)
					str += object.get(i).toString();
				else
					str += object.get(i).toString() + ", ";
			}
		}

		return str;
	}

	private Object addIntegerInteger(Object left, Object right) {
		return ((Integer) left) + ((Integer) right);
	}

	private Object gtIntegerInteger(Object left, Object right) {
		return ((Integer) left) > ((Integer) right);
	}

	private Object gteIntegerInteger(Object left, Object right) {
		return ((Integer) left) >= ((Integer) right);
	}

	private Object lteIntegerInteger(Object left, Object right) {
		return ((Integer) left) <= ((Integer) right);
	}

	private Object ltIntegerInteger(Object left, Object right) {
		return ((Integer) left) < ((Integer) right);
	}

	private Object timesIntegerInteger(Object left, Object right) {
		return ((Integer) left) * ((Integer) right);
	}

	private Object powerIntegerInteger(Object left, Object right) {
		return Math.pow(((Integer) left).doubleValue(), ((Integer) right).doubleValue());
	}

	private Object divIntegerInteger(Object left, Object right) {
		return ((Integer) left) / ((Integer) right);
	}

	private Object modIntegerInteger(Object left, Object right) {
		return ((Integer) left) % ((Integer) right);
	}

	private Object addDoubleInteger(Object left, Object right) {
		return ((Double) left) + ((Integer) right);
	}

	private Object gtDoubleInteger(Object left, Object right) {
		return ((Double) left) > ((Integer) right);
	}

	private Object gteDoubleInteger(Object left, Object right) {
		return ((Double) left) >= ((Integer) right);
	}

	private Object lteDoubleInteger(Object left, Object right) {
		return ((Double) left) <= ((Integer) right);
	}

	private Object ltDoubleInteger(Object left, Object right) {
		return ((Double) left) < ((Integer) right);
	}

	private Object timesDoubleInteger(Object left, Object right) {
		return ((Double) left) * ((Integer) right);
	}

	private Object divDoubleInteger(Object left, Object right) {
		return ((Double) left) / ((Integer) right);
	}

	private Object modDoubleInteger(Object left, Object right) {
		return ((Double) left) % ((Integer) right);
	}

	private Object addDoubleDouble(Object left, Object right) {
		return ((Double) left) + ((Double) right);
	}

	private Object gtDoubleDouble(Object left, Object right) {
		return ((Double) left) > ((Double) right);
	}

	private Object gteDoubleDouble(Object left, Object right) {
		return ((Double) left) >= ((Double) right);
	}

	private Object lteDoubleDouble(Object left, Object right) {
		return ((Double) left) <= ((Double) right);
	}

	private Object ltDoubleDouble(Object left, Object right) {
		return ((Double) left) < ((Double) right);
	}

	private Object timesDoubleDouble(Object left, Object right) {
		return ((Double) left) * ((Double) right);
	}

	private Object divDoubleDouble(Object left, Object right) {
		return ((Double) left) / ((Double) right);
	}

	private Object modDoubleDouble(Object left, Object right) {
		return ((Double) left) % ((Double) right);
	}

	private Object addIntegerDouble(Object left, Object right) {
		return ((Integer) left) + ((Double) right);
	}

	private Object gtIntegerDouble(Object left, Object right) {
		return ((Integer) left) > ((Double) right);
	}

	private Object gteIntegerDouble(Object left, Object right) {
		return ((Integer) left) >= ((Double) right);
	}

	private Object lteIntegerDouble(Object left, Object right) {
		return ((Integer) left) <= ((Double) right);
	}

	private Object ltIntegerDouble(Object left, Object right) {
		return ((Integer) left) < ((Double) right);
	}

	private Object timesIntegerDouble(Object left, Object right) {
		return ((Integer) left) * ((Double) right);
	}

	private Object powerIntegerDouble(Object left, Object right) {
		return Math.pow(((Integer) left).doubleValue(), ((Double) right));
	}

	private Object divIntegerDouble(Object left, Object right) {
		return ((Integer) left) / ((Double) right);
	}

	private Object modIntegerDouble(Object left, Object right) {
		return ((Integer) left) % ((Double) right);
	}

	private Object addDoubleBin(Object left, Object right) {
		return Bin.add(new Bin(((Double) left).intValue()), ((Bin) right));
	}

	private Object gtDoubleBin(Object left, Object right) {
		return ((Double) left).intValue() > ((Bin) right).toInteger();
	}

	private Object gteDoubleBin(Object left, Object right) {
		return ((Double) left).intValue() >= ((Bin) right).toInteger();
	}

	private Object lteDoubleBin(Object left, Object right) {
		return ((Double) left).intValue() <= ((Bin) right).toInteger();
	}

	private Object ltDoubleBin(Object left, Object right) {
		return ((Double) left).intValue() < ((Bin) right).toInteger();
	}

	private Object timesDoubleBin(Object left, Object right) {
		return Bin.times(new Bin(((Double) left).intValue()), ((Bin) right));
	}

	private Object divDoubleBin(Object left, Object right) {
		return Bin.divide(new Bin(((Double) left).intValue()), ((Bin) right));
	}

	private Object modDoubleBin(Object left, Object right) {
		return ((Double) left) % ((Bin) right).toDouble();
	}

	private Object addIntegerBin(Object left, Object right) {
		return Bin.add(new Bin((Integer) left), ((Bin) right));
	}

	private Object gtIntegerBin(Object left, Object right) {
		return ((Integer) left) > ((Bin) right).toInteger();
	}

	private Object gteIntegerBin(Object left, Object right) {
		return ((Integer) left) >= ((Bin) right).toInteger();
	}

	private Object lteIntegerBin(Object left, Object right) {
		return ((Integer) left) <= ((Bin) right).toInteger();
	}

	private Object ltIntegerBin(Object left, Object right) {
		return ((Integer) left) < ((Bin) right).toInteger();
	}

	private Object timesIntegerBin(Object left, Object right) {
		return Bin.add(new Bin((Integer) left), ((Bin) right));
	}

	private Object powerIntegerBin(Object left, Object right) {
		return Math.pow(((Integer) left).doubleValue(), ((Bin) right).toDouble());
	}

	private Object divIntegerBin(Object left, Object right) {
		return Bin.divide((new Bin((Integer) left)), ((Bin) right));
	}

	private Object modIntegerBin(Object left, Object right) {
		return ((Integer) left) % ((Bin) right).toInteger();
	}

	private Object addBinInteger(Object left, Object right) {
		return Bin.add((Bin) left, new Bin((Integer) right));
	}

	private Object gtBinInteger(Object left, Object right) {
		return ((Bin) left).toInteger() > (Integer) right;
	}

	private Object gteBinInteger(Object left, Object right) {
		return ((Bin) left).toInteger() >= (Integer) right;
	}

	private Object lteBinInteger(Object left, Object right) {
		return ((Bin) left).toInteger() <= (Integer) right;
	}

	private Object ltBinInteger(Object left, Object right) {
		return ((Bin) left).toInteger() < (Integer) right;
	}

	private Object timesBinInteger(Object left, Object right) {
		return Bin.times((Bin) left, new Bin((Integer) right));
	}

	private Object divBinInteger(Object left, Object right) {
		return Bin.divide((Bin) left, new Bin((Integer) right));
	}

	private Object modBinInteger(Object left, Object right) {
		return ((Bin) left).toInteger() % ((Integer) right);
	}

	private Object addBinDouble(Object left, Object right) {
		return Bin.add((Bin) left, new Bin(((Double) right).intValue()));
	}

	private Object gtBinDouble(Object left, Object right) {
		return ((Bin) left).toInteger() > ((Double) right);
	}

	private Object gteBinDouble(Object left, Object right) {
		return ((Bin) left).toInteger() >= ((Double) right);
	}

	private Object lteBinDouble(Object left, Object right) {
		return ((Bin) left).toInteger() <= ((Double) right);
	}

	private Object ltBinDouble(Object left, Object right) {
		return ((Bin) left).toInteger() < ((Double) right);
	}

	private Object timesBinDouble(Object left, Object right) {
		return Bin.times((Bin) left, new Bin(((Double) right).intValue()));
	}

	private Object divBinDouble(Object left, Object right) {
		return Bin.divide((Bin) left, new Bin(((Double) right).intValue()));
	}

	private Object modBinDouble(Object left, Object right) {
		return ((Bin) left).toInteger() % ((Double) right);
	}

	private Object addBinBin(Object left, Object right) {
		return Bin.add((Bin) left, (Bin) right);
	}

	private Object gtBinBin(Object left, Object right) {
		return ((Bin) left).toInteger() > ((Bin) right).toInteger();
	}

	private Object gteBinBin(Object left, Object right) {
		return ((Bin) left).toInteger() >= ((Bin) right).toInteger();
	}

	private Object lteBinBin(Object left, Object right) {
		return ((Bin) left).toInteger() <= ((Bin) right).toInteger();
	}

	private Object ltBinBin(Object left, Object right) {
		return ((Bin) left).toInteger() < ((Bin) right).toInteger();
	}

	private Object timesBinBin(Object left, Object right) {
		return Bin.times((Bin) left, (Bin) right);
	}

	private Object divBinBin(Object left, Object right) {
		return Bin.divide((Bin) left, (Bin) right);
	}

	private Object modBinBin(Object left, Object right) {
		return ((Bin) left).toInteger() % ((Bin) right).toInteger();
	}

	private Object addObjectString(Object left, Object right) {
		return left.toString() + right.toString();
	}

	private Object timesObjectString(Object left, Object right) {
		String str = "";
		if (left instanceof Integer) {

			for (int i = 0; i < ((Integer) left); i++) {
				str += right.toString();
			}

		} else if (left instanceof Double) {
			for (int i = 0; i < ((Double) left).intValue(); i++) {
				str += right.toString();
			}

		} else if (left instanceof Bin) {
			for (int i = 0; i < ((Bin) left).toInteger(); i++) {
				str += right.toString();
			}

		}
		return str;
	}

	private Object parse(Object left) {
		if (left instanceof Stmt.Expression) {
			Expr expression = ((Stmt.Expression) left).expression;
			if (expression instanceof Expr.Literal) {
				left = ((Expr.Literal) expression).value;
			}
		}
		return left;
	}

	private Object parseBinData(Object left) {
		if (left instanceof Stmt.Expression) {
			Expr expression = ((Stmt.Expression) left).expression;
			if (expression instanceof Expr.Literal) {
				left = ((Expr.Literal) expression).value;
			}
		} else if (left instanceof ArrayList<?>) {
			left = reformatArrayListDataForJustValues((ArrayList<?>) left);
		} else if (left instanceof BoxInstance) {
			left = Boxer.unbox(left);
		}
		return left;
	}

	private ArrayList<?> reformatArrayListDataForJustValues(ArrayList<?> left) {
		ArrayList<?> arr = left;
		ArrayList<Object> arr1 = new ArrayList<>();
		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(i) instanceof Stmt.Expression) {
				Expr expr = ((Stmt.Expression) arr.get(i)).expression;
				if (expr instanceof Literal) {
					arr1.add(((Literal) expr).value);
				} else {
					arr1.add(expr);
				}
			} else if (arr.get(i) instanceof ArrayList<?>) {
				arr1.add(reformatArrayListDataForJustValues((ArrayList<?>) arr.get(i)));
			} else {
				arr1.add(arr.get(i));
			}
		}

		return arr1;

	}

	private Double findNthRootOfRemainder(Double theRight, Double remainder) {
		int i = 0;
		int ii = 0;
		boolean shouldbreak = false;
		for (i = 0; i <= remainder.intValue() * 1000; i++) {

			if (fPrime(theRight, i, remainder) < 0) {
				for (ii = i; ii < 1000; ii++) {
					if (fPrime(theRight, ii, remainder) > 0) {
						shouldbreak = true;
						break;
					}
				}
			}
			if (shouldbreak)
				break;

		}

		Double xsubzero = (double) (((double) i + (double) ii) / 2);
		Double fprime = fPrime(theRight, xsubzero, remainder);
		Double fprimeFirstDirivitive = firstDerivitive(theRight, xsubzero);
		Double fprimeSecondDirivitive = secondDerivitive(theRight, xsubzero);
		Double xsubone = xsubzero - ((2 * fprime * fprimeFirstDirivitive)
				/ (2 * Math.pow(fprimeFirstDirivitive, 2) - fprime * fprimeSecondDirivitive));

		for (int j = 0; j < 10; j++) {
			fprime = fPrime(theRight, xsubone, remainder);
			fprimeFirstDirivitive = firstDerivitive(theRight, xsubone);
			fprimeSecondDirivitive = secondDerivitive(theRight, xsubone);
			xsubone = xsubone - ((2 * fprime * fprimeFirstDirivitive)
					/ (2 * Math.pow(fprimeFirstDirivitive, 2) - fprime * fprimeSecondDirivitive));

		}
		return xsubone;
	}

	private double secondDerivitive(Double theRight, double x) {
		Double doubleValue = theRight;
		Double d = doubleValue - 2;
		Double e = doubleValue * (doubleValue - 1);
		return e * Math.pow(x, d);
	}

	private double firstDerivitive(Double theRight, double x) {
		Double doubleValue = theRight;
		Double d = doubleValue - 1;
		return doubleValue * Math.pow(x, d);
	}

	private double fPrime(Double theRight, double x, Double remainder) {
		return Math.pow(x, theRight) - remainder;
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {

		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {

		Object right = evaluate(expr.right);

		right = parse(right);
		switch (expr.operator.type) {
		case QMARK:
			return invertedMode ? isTruthy(right) : !isTruthy(right);
		case MINUS:
			if (right instanceof Double)
				return -(double) right;
			if (right instanceof Integer)
				return -(int) right;

			if (right instanceof Bin)
				return ((Bin) right).negate();
			int valueToAssign = -1;
			if (right instanceof CupInstance) {
				Environment previous = environment;
				try {
					environment = new Environment(environment);
					for (int i = 0; i < ((CupInstance) right).body.size(); i++) {
						Object at = ((CupInstance) right).body.get(i);
						multiplyByValueAndAssign(at, valueToAssign);
					}
				} finally {
					this.environment = previous;
				}
				return right;

			}
			if (right instanceof PocketInstance) {
				Environment previous = environment;
				try {
					environment = new Environment(environment);
					for (int i = 0; i < ((PocketInstance) right).body.size(); i++) {
						Object at = ((PocketInstance) right).body.get(i);
						multiplyByValueAndAssign(at, valueToAssign);
					}
				} finally {
					this.environment = previous;
				}
				return right;

			}
			if (right instanceof BoxInstance) {
				Environment previous = environment;
				try {
					environment = new Environment(environment);
					for (int i = 0; i < ((BoxInstance) right).body.size(); i++) {
						Object at = ((BoxInstance) right).body.get(i);
						multiplyByValueAndAssign(at, valueToAssign);
					}
				} finally {
					this.environment = previous;
				}
				return right;

			}
			if (right instanceof KnotInstance) {
				Environment previous = environment;
				try {
					environment = new Environment(environment);
					for (int i = 0; i < ((KnotInstance) right).body.size(); i++) {
						Object at = ((KnotInstance) right).body.get(i);
						multiplyByValueAndAssign(at, valueToAssign);
					}
				} finally {
					this.environment = previous;
				}
				return right;

			}
			if (right instanceof TonkInstance) {
				Environment previous = environment;
				try {
					environment = new Environment(environment);
					for (int i = 0; i < ((TonkInstance) right).body.size(); i++) {
						Object at = ((TonkInstance) right).body.get(i);
						multiplyByValueAndAssign(at, valueToAssign);
					}
				} finally {
					this.environment = previous;
				}
				return right;

			}

			throw new RuntimeError(expr.operator, "Operand must be a number.");

		case PLUSPLUS:
			if (forward) {
				if (right instanceof Double) {

					double value = (double) right + 1.0;
					assignValue(expr, value);

					return value;
				}
				if (right instanceof Integer) {

					int value = (int) right + 1;
					assignValue(expr, value);

					return value;
				}
				if (right instanceof Bin) {

					Bin value = Bin.add((Bin) right, new Bin("1"));
					assignValue(expr, value);

					return value;
				}
				int valueToAssignpp = 1;

				Object pocketInstance = isPocketInstance(right, valueToAssignpp);
				Object cupInstance = isCupInstance(right, valueToAssignpp);
				Object knotInstance = isKnotInstance(right, valueToAssignpp);
				Object tonkInstance = isTonkInstance(right, valueToAssignpp);
				Object boxInstance = isBoxInstance(right, valueToAssignpp);

				if (pocketInstance != null)
					return pocketInstance;
				else if (cupInstance != null)
					return cupInstance;
				else if (boxInstance != null)
					return boxInstance;
				else if (knotInstance != null)
					return knotInstance;
				else if (tonkInstance != null)
					return tonkInstance;

			}
			return right;
		case MINUSMINUS:
			if (forward) {
				if (right instanceof Double) {

					double value = (double) right - 1.0;
					assignValue(expr, value);

					return value;
				}
				if (right instanceof Integer) {

					int value = (int) right - 1;
					assignValue(expr, value);

					return value;
				}
				if (right instanceof Bin) {

					Bin value = Bin.subtract((Bin) right, new Bin("1"));
					assignValue(expr, value);

					return value;
				}

				int valueToAssignmm = -1;

				Object pocketInstance = isPocketInstance(right, valueToAssignmm);
				Object cupInstance = isCupInstance(right, valueToAssignmm);
				Object knotInstance = isKnotInstance(right, valueToAssignmm);
				Object tonkInstance = isTonkInstance(right, valueToAssignmm);
				Object boxInstance = isBoxInstance(right, valueToAssignmm);

				if (pocketInstance != null)
					return pocketInstance;
				else if (cupInstance != null)
					return cupInstance;
				else if (boxInstance != null)
					return boxInstance;
				else if (knotInstance != null)
					return knotInstance;
				else if (tonkInstance != null)
					return tonkInstance;
			}
			return right;
		default:
			return null;
		}

	}

	private Object isBoxInstance(Object right, int valueToAssign) {
		if (right instanceof BoxInstance) {
			Environment previous = environment;
			ArrayList<Object> arr = new ArrayList<>();
			try {
				environment = new Environment(environment);
				Object evaluate = null;
				for (int i = 0; i < ((BoxInstance) right).body.size(); i++) {
					Object at = ((BoxInstance) right).body.get(i);
					addOrSubtractAndAssign(at, valueToAssign);
					if (at instanceof Stmt.Expression) {
						evaluate = evaluate(((Stmt.Expression) at));
						if (evaluate != null)
							lookUpVariableAddToArray(arr, evaluate);
					}
				}
			} finally {
				this.environment = previous;
			}
			return right;
		} else {
			return null;
		}
	}

	private Object isCupInstance(Object right, int valueToAssign) {
		if (right instanceof CupInstance) {
			Environment previous = environment;
			ArrayList<Object> arr = new ArrayList<>();
			try {
				environment = new Environment(environment);
				Object evaluate = null;
				for (int i = 0; i < ((CupInstance) right).body.size(); i++) {
					Object at = ((CupInstance) right).body.get(i);
					addOrSubtractAndAssign(at, valueToAssign);
					if (at instanceof StmtDecl) {
						evaluate = evaluate(((StmtDecl) at));
						if (evaluate != null)
							lookUpVariableAddToArray(arr, evaluate);
					}
				}
			} finally {
				this.environment = previous;
			}

			Expr.Cup expr2 = (Cup) ((CupInstance) right).expr;
//			Token identifier = new Token(TokenType.IDENTIFIER, expr2.identifier.lexeme + "varravargssgra", null, null,
//					null, expr2.identifier.column, expr2.identifier.line, expr2.identifier.start,
//					expr2.identifier.finish);
			Token identifier = new Token(TokenType.IDENTIFIER, expr2.identifier.lexeme, null, null, null,
					expr2.identifier.column, expr2.identifier.line, expr2.identifier.start, expr2.identifier.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			setGlobalOrCurrentEnvironmentVariable(arr, variable);

			return arr;
		} else {
			return null;
		}
	}

	private void lookUpVariableAddToArray(ArrayList<Object> arr, Object evaluate) {
		if (evaluate instanceof CupInstance) {
			Cup cup = (Expr.Cup) ((CupInstance) evaluate).expr;
//			Token identifier = new Token(TokenType.IDENTIFIER, cup.identifier.lexeme + "varravargssgra", null, null,
//					null, cup.identifier.column, cup.identifier.line, cup.identifier.start, cup.identifier.finish);
			Token identifier = new Token(TokenType.IDENTIFIER, cup.identifier.lexeme, null, null, null,
					cup.identifier.column, cup.identifier.line, cup.identifier.start, cup.identifier.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			Object lookUpVariableByName = lookUpVariableByName(variable);
			arr.add(lookUpVariableByName);
		} else if (evaluate instanceof PocketInstance) {
			Pocket pocket = (Expr.Pocket) ((PocketInstance) evaluate).expr;
			Token identifier = new Token(TokenType.IDENTIFIER,

					pocket.identifier.lexeme, null, null, null, pocket.identifier.column, pocket.identifier.line,
					pocket.identifier.start, pocket.identifier.finish);
//			Token identifier = new Token(TokenType.IDENTIFIER,
//					
//					pocket.identifier.lexeme + "varravargssgra", null, null, null, pocket.identifier.column,
//					pocket.identifier.line, pocket.identifier.start, pocket.identifier.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			Object lookUpVariableByName = lookUpVariableByName(variable);
			arr.add(lookUpVariableByName);
		} else if (evaluate instanceof BoxInstance) {
			Parser.Expr.Box box = (Expr.Box) ((BoxInstance) evaluate).expr;
			Token identifier = new Token(TokenType.IDENTIFIER, box.identifier.lexeme, null, null, null,
					box.identifier.column, box.identifier.line, box.identifier.start, box.identifier.finish);
//			Token identifier = new Token(TokenType.IDENTIFIER, box.identifier.lexeme + "varravargssgra", null, null,
//					null, box.identifier.column, box.identifier.line, box.identifier.start, box.identifier.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			Object lookUpVariableByName = lookUpVariableByName(variable);
			arr.add(lookUpVariableByName);
		} else {
			arr.add(evaluate);

		}
	}

	private Object isPocketInstance(Object right, int valueToAssign) {
		if (right instanceof PocketInstance) {
			Environment previous = environment;
			ArrayList<Object> arr = new ArrayList<>();
			try {
				environment = new Environment(environment);
				Object evaluate = null;
				for (int i = 0; i < ((PocketInstance) right).body.size(); i++) {
					Object at = ((PocketInstance) right).body.get(i);
					addOrSubtractAndAssign(at, valueToAssign);
					if (at instanceof Stmt.Expression) {
						evaluate = evaluate(((Stmt.Expression) at));
						if (evaluate != null)
							lookUpVariableAddToArray(arr, evaluate);
					}
				}
			} finally {
				this.environment = previous;
			}
			return right;
		} else {
			return null;
		}
	}

	private Object isKnotInstance(Object right, int valueToAssign) {
		if (right instanceof PocketInstance) {
			Environment previous = environment;
			ArrayList<Object> arr = new ArrayList<>();
			try {
				environment = new Environment(environment);
				Object evaluate = null;
				for (int i = 0; i < ((KnotInstance) right).body.size(); i++) {
					Object at = ((KnotInstance) right).body.get(i);
					addOrSubtractAndAssign(at, valueToAssign);
					if (at instanceof Stmt.Expression) {
						evaluate = evaluate(((Stmt.Expression) at));
						if (evaluate != null)
							lookUpVariableAddToArray(arr, evaluate);
					}
				}
			} finally {
				this.environment = previous;
			}

			return right;
		} else {
			return null;
		}
	}

	private Object isTonkInstance(Object right, int valueToAssign) {
		if (right instanceof PocketInstance) {
			Environment previous = environment;
			ArrayList<Object> arr = new ArrayList<>();
			try {
				environment = new Environment(environment);
				Object evaluate = null;
				for (int i = 0; i < ((TonkInstance) right).body.size(); i++) {
					Object at = ((TonkInstance) right).body.get(i);
					addOrSubtractAndAssign(at, valueToAssign);
					if (at instanceof Stmt.Expression) {
						evaluate = evaluate(((Stmt.Expression) at));
						if (evaluate != null)
							lookUpVariableAddToArray(arr, evaluate);
					}
				}
			} finally {
				this.environment = previous;
			}
			return right;
		} else {
			return null;
		}
	}

	private Object evaluate(StmtDecl stmtDecl) {
		return stmtDecl.statement.accept(this);

	}

	private void addOrSubtractAndAssign(Object at, int valueToAssign) {
		if (at instanceof StmtDecl) {
			assignExpression(((StmtDecl) at).statement, valueToAssign);
		} else if (at instanceof Stmt.Expression) {
			assignExpression(at, valueToAssign);

		} else if (at instanceof Expr.Cup) {
			assignCupAdd(valueToAssign, (Expr) at);
		} else if (at instanceof Expr.Pocket) {
			assignPocketAdd(valueToAssign, (Expr) at);
		} else if (at instanceof Expr.Box) {
			assignBoxAdd(valueToAssign, (Expr) at);
		} else if (at instanceof Expr.Knot) {
			assignKnotAdd(valueToAssign, (Expr) at);
		} else if (at instanceof Expr.Tonk) {
			assignTonkAdd(valueToAssign, (Expr) at);
		} else if (at instanceof Expr.Literal) {
			if (((Literal) at).value instanceof Integer) {
				((Literal) at).value = ((Integer) ((Literal) at).value) + valueToAssign;
			} else if (((Literal) at).value instanceof Double) {
				((Literal) at).value = ((Double) ((Literal) at).value) + valueToAssign;
			} else if (((Literal) at).value instanceof Bin) {
				((Literal) at).value = Bin.add(((Bin) ((Literal) at).value), new Bin(valueToAssign));
			}
		}
	}

	private void multiplyByValueAndAssign(Object at, int valueToAssign) {
		if (at instanceof StmtDecl) {
			multByValueAndAssignExpression(((StmtDecl) at).statement, valueToAssign);
		} else if (at instanceof Stmt.Expression) {
			multByValueAndAssignExpression(at, valueToAssign);

		}
	}

	private void multByValueAndAssignExpression(Object at, int valueToAssign) {

		if (at instanceof Stmt.Expression) {
			Expr expression = ((Stmt.Expression) at).expression;
			if (expression instanceof Expr.Unary) {
				Expr right2 = ((Expr.Unary) expression).right;
				if (right2 instanceof Expr.Variable) {
					Variable variable = (Expr.Variable) right2;
					assignVarableMult(variable, valueToAssign);
				} else if (right2 instanceof Expr.Cup) {
					assignCupMultiply(valueToAssign, right2);
				} else if (right2 instanceof Expr.Pocket) {
					assignPocketMultiply(valueToAssign, right2);
				} else if (expression instanceof Expr.Binary) {
					assignBinaryMultiply(valueToAssign, expression);
				}
			}
		}
	}

	private void assignExpression(Object at, int valueToAssign) {

		if (at instanceof Stmt.Expression) {
			Expr expression = ((Stmt.Expression) at).expression;
			if (expression instanceof Expr.Unary) {
				Expr right2 = ((Expr.Unary) expression).right;
				if (right2 instanceof Expr.Variable) {
					Variable variable = (Expr.Variable) right2;
					assignVarableSubtract(variable, valueToAssign);
				} else if (right2 instanceof Expr.Cup) {
					assignCupAdd(valueToAssign, right2);
				} else if (right2 instanceof Expr.Pocket) {
					assignPocketAdd(valueToAssign, right2);
				} else if (right2 instanceof Expr.Box) {
					assignBoxAdd(valueToAssign, right2);
				} else if (expression instanceof Expr.Knot) {
					assignKnotAdd(valueToAssign, expression);
				} else if (expression instanceof Expr.Tonk) {
					assignTonkAdd(valueToAssign, expression);
				} else if (expression instanceof Expr.Binary) {
					assignBinaryAdd(valueToAssign, expression);
				} else if (at instanceof Expr.Literal) {
					if (((Literal) at).value instanceof Integer) {
						((Literal) at).value = ((Integer) ((Literal) at).value) + valueToAssign;
					} else if (((Literal) at).value instanceof Double) {
						((Literal) at).value = ((Double) ((Literal) at).value) + valueToAssign;
					} else if (((Literal) at).value instanceof Bin) {
						((Literal) at).value = Bin.add(((Bin) ((Literal) at).value), new Bin(valueToAssign));
					}
				}
			} else if (expression instanceof Expr.Binary) {
				assignBinaryAdd(valueToAssign, expression);
			} else if (expression instanceof Expr.Variable) {
				assignVarableSubtract((Expr.Variable) expression, valueToAssign);
			} else if (expression instanceof Expr.Cup) {
				assignCupAdd(valueToAssign, expression);
			} else if (expression instanceof Expr.Pocket) {
				assignPocketAdd(valueToAssign, expression);
			} else if (expression instanceof Expr.Box) {
				assignBoxAdd(valueToAssign, expression);
			} else if (expression instanceof Expr.Knot) {
				assignKnotAdd(valueToAssign, expression);
			} else if (expression instanceof Expr.Tonk) {
				assignTonkAdd(valueToAssign, expression);
			} else if (at instanceof Expr.Literal) {
				if (((Literal) at).value instanceof Integer) {
					((Literal) at).value = ((Integer) ((Literal) at).value) + valueToAssign;
				} else if (((Literal) at).value instanceof Double) {
					((Literal) at).value = ((Double) ((Literal) at).value) + valueToAssign;
				} else if (((Literal) at).value instanceof Bin) {
					((Literal) at).value = Bin.add(((Bin) ((Literal) at).value), new Bin(valueToAssign));
				}
			}
		}
	}

	private void assignBinaryAdd(int valueToAssign, Expr expression) {
		Expr right2 = ((Expr.Binary) expression).right;
		Expr left2 = ((Expr.Binary) expression).left;
		if (right2 instanceof Expr.Variable && left2 instanceof Expr.Variable) {
			Variable variable1 = (Expr.Variable) right2;
			assignVarableSubtract(variable1, valueToAssign);
			Variable variable = (Expr.Variable) left2;
			assignVarableSubtract(variable, valueToAssign);
		} else if (right2 instanceof Expr.Cup && left2 instanceof Expr.Variable) {
			assignCupAdd(valueToAssign, right2);
			Variable variable = (Expr.Variable) left2;
			assignVarableSubtract(variable, valueToAssign);
		} else if (right2 instanceof Expr.Variable && left2 instanceof Expr.Cup) {
			assignCupAdd(valueToAssign, left2);
			Variable variable = (Expr.Variable) right2;
			assignVarableSubtract(variable, valueToAssign);
		} else if (right2 instanceof Expr.Cup && left2 instanceof Expr.Cup) {
			assignCupAdd(valueToAssign, right2);
			assignCupAdd(valueToAssign, left2);
		} else if (right2 instanceof Expr.Pocket && left2 instanceof Expr.Variable) {
			assignPocketAdd(valueToAssign, right2);
			Variable variable = (Expr.Variable) left2;
			assignVarableSubtract(variable, valueToAssign);
		} else if (right2 instanceof Expr.Variable && left2 instanceof Expr.Pocket) {
			assignPocketAdd(valueToAssign, left2);
			Variable variable = (Expr.Variable) right2;
			assignVarableSubtract(variable, valueToAssign);
		} else if (right2 instanceof Expr.Pocket && left2 instanceof Expr.Pocket) {
			assignPocketAdd(valueToAssign, right2);
			assignPocketAdd(valueToAssign, left2);
		} else if (right2 instanceof Expr.Pocket && left2 instanceof Expr.Cup) {
			assignPocketAdd(valueToAssign, right2);
			assignCupAdd(valueToAssign, left2);
		} else if (right2 instanceof Expr.Cup && left2 instanceof Expr.Pocket) {
			assignCupAdd(valueToAssign, right2);
			assignPocketAdd(valueToAssign, left2);
		}

	}

	private void assignBinaryMultiply(int valueToAssign, Expr expression) {
		Expr right2 = ((Expr.Binary) expression).right;
		Expr left2 = ((Expr.Binary) expression).left;
		if (right2 instanceof Expr.Variable && left2 instanceof Expr.Variable) {
			Variable variable1 = (Expr.Variable) right2;
			assignVarableMult(variable1, valueToAssign);
			Variable variable = (Expr.Variable) left2;
			assignVarableMult(variable, valueToAssign);
		} else if (right2 instanceof Expr.Cup && left2 instanceof Expr.Variable) {
			assignCupMultiply(valueToAssign, right2);
			Variable variable = (Expr.Variable) left2;
			assignVarableMult(variable, valueToAssign);
		} else if (right2 instanceof Expr.Variable && left2 instanceof Expr.Cup) {
			assignCupMultiply(valueToAssign, left2);
			Variable variable = (Expr.Variable) right2;
			assignVarableMult(variable, valueToAssign);
		} else if (right2 instanceof Expr.Cup && left2 instanceof Expr.Cup) {
			assignCupMultiply(valueToAssign, right2);
			assignCupMultiply(valueToAssign, left2);
		} else if (right2 instanceof Expr.Pocket && left2 instanceof Expr.Variable) {
			assignPocketMultiply(valueToAssign, right2);
			Variable variable = (Expr.Variable) left2;
			assignVarableMult(variable, valueToAssign);
		} else if (right2 instanceof Expr.Variable && left2 instanceof Expr.Pocket) {
			assignPocketMultiply(valueToAssign, left2);
			Variable variable = (Expr.Variable) right2;
			assignVarableSubtract(variable, valueToAssign);
		} else if (right2 instanceof Expr.Pocket && left2 instanceof Expr.Pocket) {
			assignPocketMultiply(valueToAssign, right2);
			assignPocketMultiply(valueToAssign, left2);
		} else if (right2 instanceof Expr.Pocket && left2 instanceof Expr.Cup) {
			assignPocketMultiply(valueToAssign, right2);
			assignCupMultiply(valueToAssign, left2);
		} else if (right2 instanceof Expr.Cup && left2 instanceof Expr.Pocket) {
			assignCupMultiply(valueToAssign, right2);
			assignPocketMultiply(valueToAssign, left2);
		}

	}

	private void assignCupMultiply(int valueToAssign, Expr right2) {
		Environment previous = environment;
		try {
			environment = new Environment(environment);
			for (Declaration declaration : ((Cup) right2).expression) {
				multiplyByValueAndAssign(declaration, valueToAssign);
			}
		} finally {
			this.environment = previous;
		}
	}

	private void assignCupAdd(int valueToAssign, Expr right2) {
		Environment previous = environment;
		try {
			environment = new Environment(environment);
			for (Declaration declaration : ((Cup) right2).expression) {
				addOrSubtractAndAssign(declaration, valueToAssign);
			}
		} finally {
			this.environment = previous;
		}
	}

	private void assignPocketMultiply(int valueToAssign, Expr right2) {
		Environment previous = environment;
		try {
			environment = new Environment(environment);
			for (Declaration declaration : ((Pocket) right2).expression) {
				multiplyByValueAndAssign(declaration, valueToAssign);
			}
		} finally {
			this.environment = previous;
		}
	}

	private void assignPocketAdd(int valueToAssign, Expr right2) {
		Environment previous = environment;
		try {
			environment = new Environment(environment);
			for (Declaration declaration : ((Pocket) right2).expression) {
				addOrSubtractAndAssign(declaration, valueToAssign);
			}
		} finally {
			this.environment = previous;
		}
	}

	private void assignKnotAdd(int valueToAssign, Expr right2) {
		Environment previous = environment;
		try {
			environment = new Environment(environment);
			for (Declaration declaration : ((Knot) right2).expression) {
				addOrSubtractAndAssign(declaration, valueToAssign);
			}
		} finally {
			this.environment = previous;
		}
	}

	private void assignTonkAdd(int valueToAssign, Expr right2) {
		Environment previous = environment;
		try {
			environment = new Environment(environment);
			for (Declaration declaration : ((Tonk) right2).expression) {
				addOrSubtractAndAssign(declaration, valueToAssign);
			}
		} finally {
			this.environment = previous;
		}
	}

	private void assignBoxAdd(int valueToAssign, Expr right2) {
		Environment previous = environment;
		try {
			environment = new Environment(environment);
			for (Expr declaration : ((Expr.Box) right2).expression) {
				addOrSubtractAndAssign(declaration, valueToAssign);
			}
		} finally {
			this.environment = previous;
		}
	}

	private void assignVarableMult(Variable variable, int valueToAssign) {
		Object lookUpVariableByName = lookUpVariable(variable.name, variable);
		if (lookUpVariableByName instanceof Stmt.Expression) {
			Expr expression2 = ((Stmt.Expression) lookUpVariableByName).expression;
			if (expression2 instanceof Expr.Literal) {

				if (((Literal) expression2).value instanceof Integer) {
					((Literal) expression2).value = ((Integer) ((Literal) expression2).value) * valueToAssign;
				} else if (((Literal) expression2).value instanceof Double) {
					((Literal) expression2).value = ((Double) ((Literal) expression2).value) * valueToAssign;
				} else if (((Literal) expression2).value instanceof Bin) {
					((Literal) expression2).value = Bin.times(((Bin) ((Literal) expression2).value),
							new Bin(valueToAssign));
				}
				setGlobalOrCurrentEnvironmentVariable(lookUpVariableByName, variable);
			}
		} else if (lookUpVariableByName instanceof Integer) {
			int value = ((Integer) lookUpVariableByName) * valueToAssign;
			setGlobalOrCurrentEnvironmentVariable(value, variable);
		} else if (lookUpVariableByName instanceof Double) {
			double value = ((Double) lookUpVariableByName) * valueToAssign;
			locals.get(variable);
			setGlobalOrCurrentEnvironmentVariable(value, variable);
		} else if (lookUpVariableByName instanceof Bin) {
			Integer value = Bin.times(((Bin) lookUpVariableByName), new Bin(valueToAssign));
			setGlobalOrCurrentEnvironmentVariable(value, variable);
		}
	}

	private void assignVarableSubtract(Variable variable, int valueToAssign) {
		Object lookUpVariableByName = lookUpVariable(variable.name, variable);
		if (lookUpVariableByName instanceof Stmt.Expression) {
			Expr expression2 = ((Stmt.Expression) lookUpVariableByName).expression;
			if (expression2 instanceof Expr.Literal) {

				Object value = ((Literal) expression2).value;
				if (value instanceof Integer) {
					value = ((Integer) value) + valueToAssign;
				} else if (value instanceof Double) {
					value = ((Double) value) + valueToAssign;
				} else if (value instanceof Bin) {
					value = Bin.add(((Bin) value), new Bin(valueToAssign));
				}
				setGlobalOrCurrentEnvironmentVariable(value, variable);
			}
		} else if (lookUpVariableByName instanceof Integer) {
			int value = ((Integer) lookUpVariableByName) + valueToAssign;
			setGlobalOrCurrentEnvironmentVariable(value, variable);
		} else if (lookUpVariableByName instanceof Double) {
			int value = ((Integer) lookUpVariableByName) + valueToAssign;
			setGlobalOrCurrentEnvironmentVariable(value, variable);
		} else if (lookUpVariableByName instanceof Bin) {
			Bin value = Bin.add(((Bin) lookUpVariableByName), new Bin(+valueToAssign));
			setGlobalOrCurrentEnvironmentVariable(value, variable);
		}
	}

	private ArrayList<Object> addBoxInstance(Object theRight) {
		ArrayList<Object> returnedObject = new ArrayList<Object>();

		int rightSize = 1;
		if (theRight instanceof Instance) {
			rightSize = ((Instance) theRight).size();
		}

		for (int rightindex = 0; rightindex < rightSize; rightindex++) {

			Object rightatindex = theRight;
			if (theRight instanceof Instance) {
				rightatindex = ((Instance) theRight).getAt(rightindex);
				if (rightatindex instanceof Stmt.Expression) {
					Expr expression = ((Stmt.Expression) rightatindex).expression;
					rightatindex = evaluate(expression);
				}
			}
			Object rightvalue = rightatindex;
			if (rightatindex instanceof Expr.Literal) {
				rightvalue = ((Expr.Literal) rightatindex).value;
			}

			Object findRootForLeftAndRightAndAdd = findRootForRightAndAdd(rightvalue);
			returnedObject.add(findRootForLeftAndRightAndAdd);
		}

		return returnedObject;
	}

	private ArrayList<Object> subBoxInstance(Object theRight) {
		ArrayList<Object> returnedObject = new ArrayList<Object>();

		int rightSize = 1;
		if (theRight instanceof Instance) {
			rightSize = ((Instance) theRight).size();
		}

		for (int rightindex = 0; rightindex < rightSize; rightindex++) {

			Object rightatindex = theRight;
			if (theRight instanceof Instance) {
				rightatindex = ((Instance) theRight).getAt(rightindex);
				if (rightatindex instanceof Stmt.Expression) {
					Expr expression = ((Stmt.Expression) rightatindex).expression;
					rightatindex = evaluate(expression);
				}
			}
			Object rightvalue = rightatindex;
			if (rightatindex instanceof Expr.Literal) {
				rightvalue = ((Expr.Literal) rightatindex).value;
			}

			Object findRootForLeftAndRightAndAdd = findRootForRightAndSub(rightvalue);
			returnedObject.add(findRootForLeftAndRightAndAdd);
		}

		return returnedObject;
	}

	private Object findRootForRightAndAdd(Object right) {

		right = parse(right);

		Object theRight = right;
		if (theRight instanceof Double) {
			return (double) theRight + 1;
		} else if (theRight instanceof Integer) {
			return (int) theRight + 1;
		} else if (theRight instanceof Bin) {
			return ((Bin) theRight).toInteger() + 1;
		} else if (theRight instanceof Instance) {
			return addBoxInstance(theRight);
		}

		throw new RuntimeError(null, "Operands must be numbers.");

	}

	private Object findRootForRightAndSub(Object right) {

		right = parse(right);

		Object theRight = right;
		if (theRight instanceof Double) {
			return (double) theRight - 1;
		} else if (theRight instanceof Integer) {
			return (int) theRight - 1;
		} else if (theRight instanceof Bin) {
			return ((Bin) theRight).toInteger() - 1;
		} else if (theRight instanceof Instance) {
			return subBoxInstance(theRight);
		}

		throw new RuntimeError(null, "Operands must be numbers.");

	}

	private Object findRootForMinus(Object right) {
		if (((ArrayList<?>) right).size() > 0) {
			if (((ArrayList<?>) right).get(0) instanceof ArrayList) {
				return findRootForMinus(((ArrayList<?>) right).get(0));
			} else if (((ArrayList<?>) right).get(0) instanceof Bin) {
				return ((Bin) ((ArrayList<?>) right).get(0)).negate();
			} else if (((ArrayList<?>) right).get(0) instanceof Double) {
				return -(double) ((ArrayList<?>) right).get(0);
			} else if (((ArrayList<?>) right).get(0) instanceof Integer) {
				return -(int) ((ArrayList<?>) right).get(0);
			}

		}
		return null;
	}

	@Override
	public Object visitYranuExpr(Yranu expr) {

		Object left = evaluate(expr.right);
		left = parse(left);
		switch (expr.operator.type) {
		case QMARK:
			return !isTruthy(left);
		case MINUS:
			audio.onEvent(new InterpreterAudio.AudioEvent(TokenType.MINUS, left.toString(), 0.0, System.nanoTime()));
			if (left instanceof Double)
				return -(double) left;
			if (left instanceof Integer)
				return -(int) left;

			if (left instanceof Bin)
				return ((Bin) left).negate();
			if (left instanceof ArrayList) {

				return findRootForMinus(left);
			}

			throw new RuntimeError(expr.operator, "Operand must be a number.");

		case PLUSPLUS:
			if (!forward) {
				if (left instanceof Double) {

					double value = (double) left + 1.0;
					assignValue(expr, value);

					return value;
				}
				if (left instanceof Integer) {

					int value = (int) left + 1;
					assignValue(expr, value);

					return value;
				}
				if (left instanceof Bin) {

					Bin value = Bin.add((Bin) left, new Bin("1"));
					assignValue(expr, value);

					return value;
				}
				int valueToAssign = 1;

				Object pocketInstance = isPocketInstance(left, valueToAssign);
				Object cupInstance = isCupInstance(left, valueToAssign);
				Object knotInstance = isKnotInstance(left, valueToAssign);
				Object tonkInstance = isTonkInstance(left, valueToAssign);
				Object boxInstance = isBoxInstance(left, valueToAssign);

				if (pocketInstance != null)
					return pocketInstance;
				else if (cupInstance != null)
					return cupInstance;
				else if (boxInstance != null)
					return boxInstance;
				else if (knotInstance != null)
					return knotInstance;
				else if (tonkInstance != null)
					return tonkInstance;
			}
			return left;
		case MINUSMINUS:
			if (!forward) {
				if (left instanceof Double) {

					double value = (double) left - 1.0;
					assignValue(expr, value);

					return value;
				}
				if (left instanceof Integer) {

					int value = (int) left - 1;
					assignValue(expr, value);

					return value;
				}
				if (left instanceof Bin) {

					Bin value = Bin.subtract((Bin) left, new Bin("1"));
					assignValue(expr, value);

					return value;
				}
				int valueToAssign = -1;

				Object pocketInstance = isPocketInstance(left, valueToAssign);
				Object cupInstance = isCupInstance(left, valueToAssign);
				Object knotInstance = isKnotInstance(left, valueToAssign);
				Object tonkInstance = isTonkInstance(left, valueToAssign);
				Object boxInstance = isBoxInstance(left, valueToAssign);

				if (pocketInstance != null)
					return pocketInstance;
				else if (cupInstance != null)
					return cupInstance;
				else if (boxInstance != null)
					return boxInstance;
				else if (knotInstance != null)
					return knotInstance;
				else if (tonkInstance != null)
					return tonkInstance;
			}
			return left;
		default:
			return null;
		}

	}

	private void assignValue(Expr expr, Object value) {

		Expr expr2 = expr;
		while ((expr2 instanceof Expr.Yranu) || (expr2 instanceof Expr.Unary)) {

			if (expr2 instanceof Expr.Yranu) {
				expr2 = ((Expr.Yranu) expr2).right;
			}

			if (expr2 instanceof Expr.Unary) {
				expr2 = ((Expr.Unary) expr2).right;
			}

		}
		if (expr2 instanceof Expr.Variable) {
			setGlobalOrCurrentEnvironmentVariable(value, (Variable) expr2);
		} else if (expr2 instanceof Pocket) {
			Token identifier = new Token(TokenType.IDENTIFIER, ((Pocket) expr2).identifier.lexeme, null, null, null,
					((Pocket) expr2).identifier.column, ((Pocket) expr2).identifier.line,
					((Pocket) expr2).identifier.start, ((Pocket) expr2).identifier.finish);
//			Token identifier = new Token(TokenType.IDENTIFIER, ((Pocket) expr2).identifier.lexeme + "varravargssgra",
//					null, null, null, ((Pocket) expr2).identifier.column, ((Pocket) expr2).identifier.line,
//					((Pocket) expr2).identifier.start, ((Pocket) expr2).identifier.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			setGlobalOrCurrentEnvironmentVariable(value, variable);
		} else if (expr2 instanceof Cup) {
			Token identifier = new Token(TokenType.IDENTIFIER, ((Cup) expr2).identifier.lexeme, null, null, null,
					((Cup) expr2).identifier.column, ((Cup) expr2).identifier.line, ((Cup) expr2).identifier.start,
					((Cup) expr2).identifier.finish);
//			Token identifier = new Token(TokenType.IDENTIFIER, ((Cup) expr2).identifier.lexeme + "varravargssgra", null,
//					null, null, ((Cup) expr2).identifier.column, ((Cup) expr2).identifier.line,
//					((Cup) expr2).identifier.start, ((Cup) expr2).identifier.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			setGlobalOrCurrentEnvironmentVariable(value, variable);
		}

	}

	void setGlobalOrCurrentEnvironmentVariable(Object value, Expr.Variable variable) {
		Integer distance = locals.get(variable);
		if (distance != null)
			environment.assignAt(distance, variable.name, value, value, this);
		else
			globals.assign(variable.name, value, value, this);
	}

	@Override
	public Void visitReturnStmt(Stmt.Return stmt) {
		if (forward) {
			Object value = null;
			if (stmt.expression != null)
				value = evaluate(stmt.expression);
			throw new Returns(value);
		}
		return null;
	}

	@Override
	public Object visitContainsExpr(Contains expr) {
		if (forward) {
			if (expr.container instanceof Expr.Variable) {

				Object lookUpVariable = lookUpVariable(((Expr.Variable) expr.container).name, expr.container);
				Instance lookUpContainer = (Instance) lookUpVariable;
				Pocket poc = ((Expr.Pocket) expr.contents);
				if (poc.expression.size() < 3) {
					throw new RuntimeException("expected one parameter found none");
				} else if (poc.expression.size() > 3) {
					throw new RuntimeException("expected one parameter found more then one");
				}
				Stmt stmt = poc.expression.get(1);

				if (lookUpContainer instanceof BoxInstance) {
					return ((BoxInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof PocketInstance) {
					return ((PocketInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof CupInstance) {
					return ((CupInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof KnotInstance) {
					return ((KnotInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof TonkInstance) {
					return ((TonkInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof TkpInstance) {
					return ((TkpInstance) lookUpContainer).contains(stmt);
				}
			}
		}
		return null;
	}

	private boolean isEqual(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null)
			return false;

		return a.equals(b);
	}

	@Override
	public Object visitMonoExpr(Mono expr) {
		if (forward) {

			return findMono(expr);
		}
		return null;
	}

	private Double convertStringToDouble(String evaluate) {
		String regex = "^\\d*\\.?\\d+$";

		if (evaluate == null || !evaluate.matches(regex)) {
			throw new IllegalArgumentException(
					"Invalid input: The string must contain only numerical values and at most one decimal point.");
		}

		return Double.parseDouble(evaluate);
	}

	@Override
	public Object visitLogExpr(Log expr) {
		if (forward) {
			Double evaluateDoubleValue = findDoubleValue(expr.value) - 1;
			Double evaluateDoubleValueBase = findDoubleValue(expr.valueBase) - 1;

			return Math.log1p(evaluateDoubleValue) / Math.log1p(evaluateDoubleValueBase);
		}
		return null;
	}

	private Double findDoubleValue(Expr value) {
		Object evaluateValue = evaluate(value);
		Double evaluateDoubleValue = 0.0;
		if (evaluateValue instanceof Integer) {
			evaluateDoubleValue = Double.valueOf(Integer.toString((Integer) evaluateValue));
		} else if (evaluateValue instanceof Double) {
			evaluateDoubleValue = (Double) evaluateValue;
		} else if (evaluateValue instanceof Bin) {
			evaluateDoubleValue = ((Bin) evaluateValue).toDouble();

		}
		return evaluateDoubleValue;
	}

	@Override
	public Object visitFactorialExpr(Factorial expr) {
		if (forward) {
			Object evaluate = evaluate(expr.value);
			Double evaluateDouble = 0.0;
			Integer evaluateInteger = 0;
			if (evaluate instanceof Integer) {
				evaluateInteger = (Integer) evaluate;

				int i, fact = 1;
				for (i = 1; i <= evaluateInteger; i++) {
					fact = fact * i;
				}
				return fact;
			} else if (evaluate instanceof Double) {
				evaluateDouble = (Double) evaluate;
				Double nfactorial = factorial(evaluateDouble);

				return nfactorial;
			} else if (evaluate instanceof Bin) {
				evaluateInteger = ((Bin) evaluate).toInteger();
				int i;
				int fact = 1;
				for (i = 1; i <= evaluateInteger; i++) {
					fact = fact * i;
				}
				return fact;
			}

			return -1;
		}
		return null;
	}

	private double factorial(Double evaluateDouble) {
		return evaluateDouble * gammaOfN(evaluateDouble);
	}

	private Double gammaOfN(Double evaluateDouble) {
		if (evaluateDouble == .5) {
			return findNthRootOfRemainder(2.0, Math.PI);
		} else if (evaluateDouble < 0) {
			return 1.0;
		}
		Double gamma = factorial(evaluateDouble - 1);
		return gamma;
	}

	private boolean isTruthy(Object right) {
		if (right == null)
			return false;
		if (right instanceof Boolean)
			return (boolean) right;

		return true;
	}

	@Override
	public Void visitRenameStmt(Rename stmt) {
		if (forward) {
			File file = new File((String) evaluate(stmt.filePathAndName));

			if (file.renameTo(new File((String) evaluate(stmt.filenewname)))) {
				System.out.println("File Renamed successfully");
			} else {
				System.out.println("Failed to Rename the file");
			}
		}
		return null;
	}

	@Override
	public Void visitMoveStmt(Move stmt) {
		if (forward) {
			String evaluate = (String) evaluate(stmt.OringialfilePathAndFile);
			String[] split = evaluate.split("/");
			String fn = split[split.length - 1];
			File file = new File(evaluate);

			String evaluate2 = (String) evaluate(stmt.newfilePath);
			if (file.renameTo(new File(evaluate2 + "/" + fn))) {

				System.out.println("File moved successfully");
			} else {
				System.out.println("Failed to move the file");
			}
		}
		return null;
	}

	@Override
	public Object visitTnirpStmt(Tnirp stmt) {
		Object value = "";
		if (!forward) {
			value = evaluate(stmt.expression);
			System.out.print(stringify(value,stmt.expression));
		}
		return value;
	}


	@Override
	public Void visitNruterStmt(Nruter stmt) {
		if (!forward) {
			Object value = null;
			if (stmt.expression != null)
				value = evaluate(stmt.expression);
			throw new Snruter(value);
		}
		return null;
	}

	@Override
	public Void visitEvasStmt(Evas stmt) {
		if (!forward) {
			try {
				String filePathAndName = (String) evaluate(stmt.filePathFileName);
				String[] split = filePathAndName.split("/");
				String folderPath = "";
				if (split[split.length - 1].contains(".")) {
					for (int i = 0; i < split.length - 1; i++) {
						folderPath += split[i] + "/";
					}
				} else {
					for (int i = 0; i < split.length; i++) {
						folderPath += split[i] + "/";
					}
				}
				folderPath = folderPath.substring(0, folderPath.length() - 1);

				String str = "";
				Object boxInstance = evaluate(stmt.objecttosave);
				if (boxInstance != null) {
					str = boxInstance.toString();

					BufferedWriter writer = new BufferedWriter(new FileWriter(filePathAndName));
					writer.write(str);

					writer.close();
				}

			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Void visitDaerStmt(Daer stmt) {
		if (!forward) {
			try {
				File myObj = new File((String) evaluate(stmt.filePath));
				java.util.Scanner myReader = new java.util.Scanner(myObj);
				String data = "";
				while (myReader.hasNextLine()) {
					data += myReader.nextLine();

				}

				if (stmt.objectToReadInto instanceof Expr.Variable) {
					Object value = evaluate(new Expr.Literal(data));
					Integer distance = locals.get((Expr.Variable) stmt.objectToReadInto);
					if (distance != null)
						environment.assignAt(distance, ((Expr.Variable) stmt.objectToReadInto).name, value, value,
								this);
					else
						globals.assign(((Expr.Variable) stmt.objectToReadInto).name, value, value, this);
				}

				myReader.close();
			} catch (FileNotFoundException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Void visitEmanerStmt(Emaner stmt) {
		if (!forward) {
			File file = new File((String) evaluate(stmt.filePathAndName));

			if (file.renameTo(new File((String) evaluate(stmt.filenewname)))) {
				System.out.println("File Renamed successfully");
			} else {
				System.out.println("Failed to Rename the file");
			}
		}
		return null;
	}

	@Override
	public Void visitEvomStmt(Evom stmt) {
		if (!forward) {
			String evaluate = (String) evaluate(stmt.OringialfilePathAndFile);
			String[] split = evaluate.split("/");
			String fn = split[split.length - 1];
			File file = new File(evaluate);

			String evaluate2 = (String) evaluate(stmt.newfilePath);
			if (file.renameTo(new File(evaluate2 + "/" + fn))) {

				System.out.println("File moved successfully");
			} else {
				System.out.println("Failed to move the file");
			}
		}
		return null;
	}

	@Override
	public Object visitTnemngissaExpr(Tnemngissa expr) {
		if (!forward) {
			Object value = evaluate(expr.value);
			Integer distance = locals.get(expr);
			if (distance != null)
				environment.assignAt(distance, expr.name, value, value, this);
			else
				globals.assign(expr.name, value, value, this);
			return value;
		}
		return null;
	}

	@Override
	public Object visitYranibExpr(Yranib expr) {
		Object left = null;
		Object right = null;

		if (expr.left instanceof Pocket || expr.left instanceof Cup) {
			left = evaluate(expr.left);
			right = evaluate(expr.right);
		} else if (expr.right instanceof Pocket || expr.right instanceof Cup) {
			right = evaluate(expr.right);
			left = evaluate(expr.left);
		} else {
			left = evaluate(expr.left);
			right = evaluate(expr.right);
		}

		Object rawLeft = left, rawRight = right;
		if (!isVecBinaryOp(expr.operator.type)) {
			left = parseBinData(left);
			right = parseBinData(right);
		}

		switch (expr.operator.type) {
		case NOTEQUALS:
			return !isEqual(left, right);
		case EQUALSNOT:
			return !isEqual(left, right);
		case EQUALSEQUALS:
			return isEqual(left, right);

		case GREATERTHEN:
			return greaterthen(left, right);
		case GREATERTHENEQUAL:
			return greaterequalthen(left, right);
		case LESSTHEN:
			return lessthen(left, right);
		case LESSTHENEQUAL:
			return lessequalthen(left, right);
		case EQUALLESSTHEN:
			return greaterequalthen(left, right);
		case EQUALGREATERTHEN:
			return lessequalthen(left, right);

		case MINUS:

			return sub(left, right);
		case EQUALSMINUS:
			return sub(left, right, expr.left, expr.operator);
		case MINUSEQUALS:
			return sub(left, right, expr.right, expr.operator);

		case EQUALSPLUS:
			return add(left, right, expr.left, expr.operator);
		case PLUSEQUALS:
			return add(left, right, expr.right, expr.operator);
		case PLUS:
			return add(left, right, expr.left, expr.right);
		case MOD:
			return mod(left, right);
		case MODEQUAL:
			return mod(left, right, expr.right, expr.operator);
		case EQUALMOD:
			return mod(left, right, expr.left, expr.operator);

		case FORWARDSLASH:
			return div(left, right);
		case BACKSLASH:
			Object div = div(right, left);
			return div;
		case EQUALDIVIDEFORWARD:
			return div(right, left, expr.left, expr.operator);
		case EQUALDIVIDEBACKWARD:
			return div(right, left, expr.right, expr.operator);

		case TIMES:
			return times(left, right);
		case TIMESEQUAL:
			return times(left, right, expr.right, expr.operator);
		case EQUALTIMES:
			return times(left, right, expr.left, expr.operator);

		case POWER:
			return power(left, right);
		case EQUALPOWER:
			return power(left, right, expr.left, expr.operator);
		case POWEREQUAL:
			return power(left, right, expr.right, expr.operator);
		case YROOT:
			return toory(expr.right,expr.left);
		case TOORY:
			return toory(expr.left,expr.right);

		case NIM: case MIN: return bitwiseMin(left, right);
		case XAM: case MAX: return bitwiseMax(left, right);
		case DNAB: case BAND: return bitwiseAnd(left, right);
		case ROB:  case BOR:  return bitwiseOr(left, right);
		case ROXB: case BXOR: return bitwiseXor(left, right);
		case TFELB: case BLEFT:  return bitwiseLshift(left, right);
		case THGIRB: case BRIGHT: return bitwiseRshift(left, right);

		case TODV: case SSORC: case DDAV: case BUSV: case ELACSV:
			return VecMatHelper.dispatchVecBinary(expr.operator.type, rawLeft, rawRight,
			                                      expr.operator, invertedMode, this);

		case DNA:
			if (!forward)
				return and(left, right);
			else
				return null;
		case AND:
			if (forward)
				return and(left, right);
			else
				return null;
		case RO:
			if (!forward)
				return or(left, right);
			else
				return false;
		case OR:
			if (forward)
				return or(left, right);
			else
				return false;
		default:
			return null;
		}

	}

	private double toDouble(Object v) {
		if (v instanceof Double)  return (Double) v;
		if (v instanceof Integer) return ((Integer) v).doubleValue();
		if (v instanceof Bin)     return ((Bin) v).toDouble();
		return 0.0;
	}
	private static boolean isVecBinaryOp(TokenType t) {
		switch (t) {
		case VDOT: case TODV: case CROSS: case SSORC:
		case VADD: case DDAV: case VSUB:  case BUSV:
		case VSCALE: case ELACSV: return true;
		default: return false;
		}
	}

	private Object bitwiseMin(Object l, Object r)    { return Math.min(toDouble(l), toDouble(r)); }
	private Object bitwiseMax(Object l, Object r)    { return Math.max(toDouble(l), toDouble(r)); }
	private Object bitwiseAnd(Object l, Object r)    { return (double)((long)toDouble(l) & (long)toDouble(r)); }
	private Object bitwiseOr(Object l, Object r)     { return (double)((long)toDouble(l) | (long)toDouble(r)); }
	private Object bitwiseXor(Object l, Object r)    { return (double)((long)toDouble(l) ^ (long)toDouble(r)); }
	private Object bitwiseLshift(Object l, Object r) { return (double)((long)toDouble(l) << (int)toDouble(r)); }
	private Object bitwiseRshift(Object l, Object r) { return (double)((long)toDouble(l) >> (int)toDouble(r)); }

	private Object toory(Object left1, Object right1) {
	   var left = (left1 instanceof Expr.Literal) ? ((Expr.Literal) left1).value : left1;
	   var right = (right1 instanceof Expr.Literal) ? ((Expr.Literal) right1).value : right1;	   
		
		if (left instanceof Integer) {
			if (right instanceof Integer) {
				if ((Integer)left == 0) {
			        throw new IllegalArgumentException("nth root with n = 0 is undefined.");
			    }

			    if ((Integer)right < 0 && (Integer)left % 2 == 0) {
			        throw new IllegalArgumentException("Even root of a negative number is undefined in real numbers.");
			    }

			    return Math.pow((Integer)right, 1.0 / (Integer)left);
			} else if (right instanceof Double) {
				if ((Integer)left == 0) {
			        throw new IllegalArgumentException("nth root with n = 0 is undefined.");
			    }

			    if ((Double)right < 0 && (Integer)left % 2 == 0) {
			        throw new IllegalArgumentException("Even root of a negative number is undefined in real numbers.");
			    }

			    return Math.pow((Double)right, 1.0 / (Integer)left);
			} else return null;
		} else if (left instanceof Double) {
			if (right instanceof Integer) {
				if ((Double)left == 0) {
			        throw new IllegalArgumentException("nth root with n = 0 is undefined.");
			    }

			    if ((Integer)right < 0 && (Double)left % 2 == 0) {
			        throw new IllegalArgumentException("Even root of a negative number is undefined in real numbers.");
			    }

			    return Math.pow((Integer)right, 1.0 / (Double)left);
			} else if (right instanceof Double) {
				if ((Double)left == 0) {
			        throw new IllegalArgumentException("nth root with n = 0 is undefined.");
			    }

			    if ((Double)right < 0 && (Double)left % 2 == 0) {
			        throw new IllegalArgumentException("Even root of a negative number is undefined in real numbers.");
			    }

			    return Math.pow((Double)right, 1.0 / (Double)left);
			}else return  null;
		} else return null;


		
		
		
		
	}

	@Override
	public Object visitOnomExpr(Onom expr) {
		if (!forward) {
			return findOnom(expr);
		}
		return null;
	}

	private Object findOnom(Expr expr) {
		Object evaluate = null;
		TokenType type = null;
		Token opToken = null;
		if (expr instanceof Monoonom) {
			evaluate = evaluate(((Monoonom) expr).value);
			type = ((Monoonom) expr).operatorBackward.type;
			opToken = ((Monoonom) expr).operatorBackward;
		} else if (expr instanceof Onom) {
			evaluate = evaluate(((Onom) expr).value);
			type = ((Onom) expr).operator.type;
			opToken = ((Onom) expr).operator;
		}

		// Vector/matrix ops — dispatch before scalar extraction
		if (evaluate instanceof BoxInstance) {
			Object vecResult = VecMatHelper.dispatchVecMono((BoxInstance) evaluate, type, opToken, invertedMode, this);
			if (vecResult != null) return vecResult;
		}
		// String ops — dispatch before numeric extraction
		Object strResult = applyStringOp(evaluate, type, opToken);
		if (strResult != null) return strResult;


		Double result = 0.0;
		Double evaluateDouble = 0.0;
		if (evaluate instanceof Integer) {
			evaluateDouble = Double.valueOf(Integer.toString((Integer) evaluate));
		} else if (evaluate instanceof Double) {
			evaluateDouble = (Double) evaluate;
		} else if (evaluate instanceof Bin) {
			evaluateDouble = ((Bin) evaluate).toDouble();

		} else if (evaluate instanceof String) {
			evaluateDouble = convertStringToDouble((String) evaluate);

		} else if (evaluate.getClass() == ArrayList.class) {
			evaluateDouble = ((ArrayList<Integer>) evaluate).get(0).doubleValue();

		} else if (evaluate instanceof BoxInstance) {
			evaluateDouble = ((Integer)((BoxInstance) evaluate).body.get(0)).doubleValue();	}


		switch (type) {
		case NIS:
			result = Math.sin(evaluateDouble);
			break;
		case SOC:
			result = Math.cos(evaluateDouble);
			break;
		case NAT:
			result = Math.tan(evaluateDouble);
			break;

		case HNIS:
			result = Math.sinh(evaluateDouble);
			break;

		case HSOC:
			result = Math.cosh(evaluateDouble);
			break;

		case HNAT:
			result = Math.tanh(evaluateDouble);
			break;
		case NL:
			result = Math.log1p(evaluateDouble - 1);
			break;
		case PXE:
			result = Math.exp(evaluateDouble);
			break;
		case SLENSREF:
			result = new MathInterpreter().S(BigDecimal.valueOf(evaluateDouble)).doubleValue();
			break;
		case CLENSERF:
			result = new MathInterpreter().C(BigDecimal.valueOf(evaluateDouble)).doubleValue();
			break;
		case SBA:   result = Math.abs(evaluateDouble);   break;
		case TRQS:  result = Math.sqrt(evaluateDouble);  break;
		case ROOLF: result = Math.floor(evaluateDouble); break;
		case LIEC:  result = Math.ceil(evaluateDouble);  break;
		case DNUOR: result = (double) Math.round(evaluateDouble); break;
		case NGIS:  result = Math.signum(evaluateDouble); break;
		case NISA:  result = Math.asin(evaluateDouble);  break;
		case SOCA:  result = Math.acos(evaluateDouble);  break;
		case NATA:  result = Math.atan(evaluateDouble);  break;
		case HNISA: result = Math.log(evaluateDouble + Math.sqrt(evaluateDouble * evaluateDouble + 1)); break;
		case HSOCA: result = Math.log(evaluateDouble + Math.sqrt(evaluateDouble * evaluateDouble - 1)); break;
		case HNATA: result = 0.5 * Math.log((1 + evaluateDouble) / (1 - evaluateDouble)); break;
		case TONB:  result = (double)(~(long)(double) evaluateDouble); break;

		default:
			break;
		}

		return result;
	}

	@Override
	public Object visitGolExpr(Gol expr) {
		if (!forward) {
			Double evaluateDoubleValue = findDoubleValue(expr.value) - 1;
			Double evaluateDoubleValueBase = findDoubleValue(expr.valueBase) - 1;

			return Math.log1p(evaluateDoubleValue) / Math.log1p(evaluateDoubleValueBase);
		}
		return null;
	}

	@Override
	public Object visitDeriveExpr(Expr.Derive expr) {
		if (!forward) return null;
		double h = 1e-8;
		if (expr.atPoint != null) {
			double point = toMathDouble(evaluate(expr.atPoint));
			if (expr.constantC != null || isInverted()) {
				double C = expr.constantC != null ? toMathDouble(evaluate(expr.constantC)) : 0.0;
				if (expr.byVar == null) {
					Object fn = evaluate(expr.expression);
					if (fn instanceof BoxFunction) return simpsonIntegrate((BoxFunction) fn, 0.0, point, 1000) + C;
					throw new RuntimeError(expr.operator, "derive antiderivative: expression must be a function when .by. is omitted");
				}
				return simpsonIntegrateExpr(expr.expression, expr.byVar, 0.0, point, 1000) + C;
			}
			if (expr.byVar == null) {
				Object fn = evaluate(expr.expression);
				if (fn instanceof BoxFunction) {
					double fPlus  = toMathDouble(((BoxFunction) fn).call(this, java.util.Collections.singletonList(point + h)));
					double fMinus = toMathDouble(((BoxFunction) fn).call(this, java.util.Collections.singletonList(point - h)));
					return (fPlus - fMinus) / (2 * h);
				}
				throw new RuntimeError(expr.operator, "derive: expression must be a function when .by. is omitted");
			}
			return numericalDerivative(expr.expression, expr.byVar, point, h);
		}
		if (expr.constantC != null)
			throw new RuntimeError(expr.operator, "derive .constant.: requires .at.(x) to evaluate the antiderivative");
		return null;
	}

	@Override
	public Object visitEviredExpr(Expr.Evired expr) {
		if (forward) return null;
		double h = 1e-8;
		if (expr.atPoint != null) {
			double point = toMathDouble(evaluate(expr.atPoint));
			if (expr.constantC != null || isInverted()) {
				double C = expr.constantC != null ? toMathDouble(evaluate(expr.constantC)) : 0.0;
				if (expr.byVar == null) {
					Object fn = evaluate(expr.expression);
					if (fn instanceof BoxFunction) return simpsonIntegrate((BoxFunction) fn, 0.0, point, 1000) + C;
					throw new RuntimeError(expr.operator, "evired antiderivative: expression must be a function when .yb. is omitted");
				}
				return simpsonIntegrateExpr(expr.expression, expr.byVar, 0.0, point, 1000) + C;
			}
			if (expr.byVar == null) {
				Object fn = evaluate(expr.expression);
				if (fn instanceof BoxFunction) {
					double fPlus  = toMathDouble(((BoxFunction) fn).call(this, java.util.Collections.singletonList(point + h)));
					double fMinus = toMathDouble(((BoxFunction) fn).call(this, java.util.Collections.singletonList(point - h)));
					return (fPlus - fMinus) / (2 * h);
				}
				throw new RuntimeError(expr.operator, "evired: expression must be a function when .yb. is omitted");
			}
			return numericalDerivative(expr.expression, expr.byVar, point, h);
		}
		if (expr.constantC != null)
			throw new RuntimeError(expr.operator, "evired .tnatsnoc.: requires .ta.(x) to evaluate the antiderivative");
		return null;
	}

	@Override
	public Object visitIntegrateExpr(Expr.Integrate expr) {
		if (!forward) return null;
		double a = toMathDouble(evaluate(expr.from));
		if (expr.target != null) {
			double V = toMathDouble(evaluate(expr.target));
			if (expr.byVar == null && !(expr.expression instanceof Expr.Variable)) {
				Object fn = evaluate(expr.expression);
				if (fn instanceof BoxFunction) return inverseIntegrateFunction((BoxFunction) fn, a, V, expr.operator);
			}
			return inverseIntegrateExpr(expr.expression, expr.byVar, a, V, expr.operator);
		}
		double b = toMathDouble(evaluate(expr.to));
		if (isInverted()) {
			// inside puc: .to.(b) flips — b becomes the target value, find upper bound
			if (expr.byVar == null && !(expr.expression instanceof Expr.Variable)) {
				Object fn = evaluate(expr.expression);
				if (fn instanceof BoxFunction) return inverseIntegrateFunction((BoxFunction) fn, a, b, expr.operator);
			}
			return inverseIntegrateExpr(expr.expression, expr.byVar, a, b, expr.operator);
		}
		if (expr.byVar == null && !(expr.expression instanceof Expr.Variable)) {
			Object fn = evaluate(expr.expression);
			if (fn instanceof BoxFunction) return simpsonIntegrate((BoxFunction) fn, a, b, 1000);
		}
		return simpsonIntegrateExpr(expr.expression, expr.byVar, a, b, 1000);
	}

	@Override
	public Object visitEtargetniExpr(Expr.Etargetni expr) {
		if (forward) return null;
		double a = toMathDouble(evaluate(expr.from));
		if (expr.target != null) {
			double V = toMathDouble(evaluate(expr.target));
			if (expr.byVar == null && !(expr.expression instanceof Expr.Variable)) {
				Object fn = evaluate(expr.expression);
				if (fn instanceof BoxFunction) return inverseIntegrateFunction((BoxFunction) fn, a, V, expr.operator);
			}
			return inverseIntegrateExpr(expr.expression, expr.byVar, a, V, expr.operator);
		}
		double b = toMathDouble(evaluate(expr.to));
		if (isInverted()) {
			if (expr.byVar == null && !(expr.expression instanceof Expr.Variable)) {
				Object fn = evaluate(expr.expression);
				if (fn instanceof BoxFunction) return inverseIntegrateFunction((BoxFunction) fn, a, b, expr.operator);
			}
			return inverseIntegrateExpr(expr.expression, expr.byVar, a, b, expr.operator);
		}
		if (expr.byVar == null && !(expr.expression instanceof Expr.Variable)) {
			Object fn = evaluate(expr.expression);
			if (fn instanceof BoxFunction) return simpsonIntegrate((BoxFunction) fn, a, b, 1000);
		}
		return simpsonIntegrateExpr(expr.expression, expr.byVar, a, b, 1000);
	}

	@Override
	public Object visitClampExpr(Expr.Clamp expr) {
		Object rawX  = evaluate(expr.value);
		Object rawLo = evaluate(expr.lo);
		Object rawHi = evaluate(expr.hi);
		double x  = toMathDouble(rawX);
		double lo = toMathDouble(rawLo);
		double hi = toMathDouble(rawHi);
		if (lo > hi) throw new RuntimeError(expr.operator, "clamp: lo (" + lo + ") > hi (" + hi + ")");
		return Math.max(lo, Math.min(hi, x));
	}

	private double toMathDouble(Object obj) {
		if (obj instanceof Double)  return (Double) obj;
		if (obj instanceof Integer) return ((Integer) obj).doubleValue();
		if (obj instanceof Bin)     return ((Bin) obj).toDouble();
		if (obj instanceof String)  return convertStringToDouble((String) obj);
		if (obj instanceof BoxInstance) {
			Object inner = ((BoxInstance) obj).body.get(0);
			return toMathDouble(inner);
		}
		throw new RuntimeError(null, "cannot convert to number: " + obj);
	}

	private double numericalDerivative(Expr expression, Expr byVar, double point, double h) {
		String varName = ((Expr.Variable) byVar).name.lexeme;
		Token varTok   = ((Expr.Variable) byVar).name;
		Object saved = null; boolean wasDefined = false;
		try { saved = environment.get(varTok, false); wasDefined = true; } catch (Exception e) {}
		try {
			environment.define(varName, null, point + h);
			double fPlus = toMathDouble(evaluate(expression));
			environment.define(varName, null, point - h);
			double fMinus = toMathDouble(evaluate(expression));
			return (fPlus - fMinus) / (2 * h);
		} finally {
			if (wasDefined) environment.define(varName, null, saved);
		}
	}

	private double simpsonIntegrate(BoxFunction fn, double a, double b, int n) {
		if (n % 2 != 0) n++;
		double h = (b - a) / n;
		double sum = 0;
		for (int k = 0; k <= n; k++) {
			double x = a + k * h;
			double fx = toMathDouble(fn.call(this, java.util.Collections.singletonList(x)));
			sum += (k == 0 || k == n) ? fx : (k % 2 == 1 ? 4 * fx : 2 * fx);
		}
		return sum * h / 3;
	}

	private double simpsonIntegrateExpr(Expr expression, Expr byVar, double a, double b, int n) {
		String varName;
		Token varTok;
		if (byVar != null) {
			Expr.Variable v = (Expr.Variable) byVar;
			varName = v.name.lexeme;
			varTok  = v.name;
		} else {
			varName = "x";
			varTok  = new Token(TokenType.IDENTIFIER, "x", null, null, null, -1, -1, -1, -1);
		}
		Object saved = null;
		boolean wasDefined = false;
		try { saved = environment.get(varTok, false); wasDefined = true; } catch (Exception e) {}
		if (n % 2 != 0) n++;
		double h = (b - a) / n;
		double sum = 0;
		try {
			for (int k = 0; k <= n; k++) {
				double x = a + k * h;
				environment.define(varName, null, x);
				double fx = toMathDouble(evaluate(expression));
				sum += (k == 0 || k == n) ? fx : (k % 2 == 1 ? 4 * fx : 2 * fx);
			}
		} finally {
			if (wasDefined) environment.define(varName, null, saved);
		}
		return sum * h / 3;
	}

	private double inverseIntegrateFunction(BoxFunction fn, double a, double V, Token op) {
		if (Math.abs(V) < 1e-12) return a;
		double step = V > 0 ? 1.0 : -1.0;
		double lo = a, hi = a + step;
		double gLo = -V;
		double gHi = simpsonIntegrate(fn, a, hi, 100) - V;
		for (int i = 0; i < 64 && gLo * gHi > 0; i++) {
			step *= 2;
			hi = a + step;
			gHi = simpsonIntegrate(fn, a, hi, 100) - V;
		}
		if (gLo * gHi > 0) throw new RuntimeError(op, "integrate .target.: no upper bound found for the given target value");
		for (int i = 0; i < 200; i++) {
			double mid = (lo + hi) / 2;
			if (Math.abs(hi - lo) < 1e-10) break;
			double gMid = simpsonIntegrate(fn, a, mid, 100) - V;
			if (gLo * gMid < 0) { hi = mid; gHi = gMid; }
			else                 { lo = mid; gLo = gMid; }
		}
		return (lo + hi) / 2;
	}

	private double inverseIntegrateExpr(Expr expression, Expr byVar, double a, double V, Token op) {
		if (Math.abs(V) < 1e-12) return a;
		double step = V > 0 ? 1.0 : -1.0;
		double lo = a, hi = a + step;
		double gLo = -V;
		double gHi = simpsonIntegrateExpr(expression, byVar, a, hi, 100) - V;
		for (int i = 0; i < 64 && gLo * gHi > 0; i++) {
			step *= 2;
			hi = a + step;
			gHi = simpsonIntegrateExpr(expression, byVar, a, hi, 100) - V;
		}
		if (gLo * gHi > 0) throw new RuntimeError(op, "integrate .target.: no upper bound found for the given target value");
		for (int i = 0; i < 200; i++) {
			double mid = (lo + hi) / 2;
			if (Math.abs(hi - lo) < 1e-10) break;
			double gMid = simpsonIntegrateExpr(expression, byVar, a, mid, 100) - V;
			if (gLo * gMid < 0) { hi = mid; gHi = gMid; }
			else                 { lo = mid; gLo = gMid; }
		}
		return (lo + hi) / 2;
	}

	@Override
	public Object visitLairotcafExpr(Lairotcaf expr) {
		if (!forward) {
			Object evaluate = evaluate(expr.value);
			Double evaluateDouble = 0.0;
			Integer evaluateInteger = 0;
			if (evaluate instanceof Integer) {
				evaluateInteger = (Integer) evaluate;

				int i, fact = 1;
				for (i = 1; i <= evaluateInteger; i++) {
					fact = fact * i;
				}
				return fact;
			} else if (evaluate instanceof Double) {
				evaluateDouble = (Double) evaluate;
				int i;
				double fact = 1.0;
				for (i = 1; i <= evaluateDouble; i++) {
					fact = fact * i;
				}
				return fact;
			} else if (evaluate instanceof Bin) {
				evaluateInteger = ((Bin) evaluate).toInteger();
				int i;
				int fact = 1;
				for (i = 1; i <= evaluateInteger; i++) {
					fact = fact * i;
				}
				return fact;
			}

			return -1;
		}
		return null;
	}

	@Override
	public Object visitLlacExpr(Llac expr) {
		if (!forward) {
			Object callee = evaluate(expr.callee);
			List<Object> arguments = new ArrayList<>();
			for (Expr argument : expr.arguments) {
				arguments.add(evaluate(argument));
			}

			if (!(callee instanceof BoxCallable)) {
				throw new RuntimeError(expr.calleeToken, "Can only call functions and classes.");
			}

			BoxCallable function = (BoxCallable) callee;
			if (function instanceof BoxFunction && ((BoxFunction) function).isForward)
				throw new RuntimeError(expr.calleeToken, "direction mismatch: cannot call forward function while running backward");

			if (arguments.size() != function.arity()) {
				throw new RuntimeError(expr.calleeToken,
						"Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
			}

			return function.call(this, arguments);
		}
		return null;
	}

	public void resolve(Expr expr, int i) {
		if (!locals.containsKey(expr))
			locals.put(expr, i);
	}

	public void resolve(Var stmt, int i) {
		Expr plotonicnerd = (Expr) lookUpVariableByName((Variable) (stmt.variable));
		locals.put(stmt.variable, i);
	}

	public void setForward(boolean forward) {
		this.forward = forward;
	}

	public boolean isInverted() {
		return invertedMode;
	}

	public void setInverted(boolean inverted) {
		this.invertedMode = inverted;
	}

	@Override
	public Object visitFunDeclDeclaration(FunDecl declaration) {
		if (declaration.function instanceof FunctionLink)
			return declaration.function.accept(this);
		Function fn = (Function) declaration.function;

		if (fn.forwardIdentifier != null) {
			environment.define(fn.forwardIdentifier.lexeme, fn.forwardIdentifier, null);
			BoxFunction fwd = new BoxFunction(fn.sharedCup, fn.forwardIdentifier.lexeme,
					fn.forwardPrametersType, fn.forwardPrametersNames, environment, true, false);
			environment.assign(fn.forwardIdentifier, null, fwd);
		}

		if (fn.backwardIdentifier != null) {
			environment.define(fn.backwardIdentifier.lexeme, fn.backwardIdentifier, null);
			BoxFunction bwd = new BoxFunction(fn.sharedCup, fn.backwardIdentifier.lexeme,
					fn.backwardPrametersType, fn.backwardPrametersNames, environment, false, false);
			environment.assign(fn.backwardIdentifier, null, bwd);
		}

		return null;
	}

	@Override
	public Object visitStmtDeclDeclaration(StmtDecl declaration) {

		return declaration.statement.accept(this);
	}

	@Override
	public Object visitFunctionFun(Function fun) {

		return null;
	}

	@Override
	public Object visitIfStmt(If stmt) {
		if (forward) {
			executeIfStmt(stmt);
		}
		return null;
	}

	private void executeIfStmt(If stmt) {
		Object evaluate = evaluate(stmt.ifPocket);

		evaluate = findValueOfInstanceFromIdentifier(evaluate);
		if (evaluate instanceof ArrayList<?> && ((ArrayList<?>) evaluate).size() == 1) {
			Object poc = ((ArrayList<?>) evaluate).get(0);
			if (poc instanceof Boolean) {
				if (((Boolean) poc) == true) {
					evaluate(stmt.ifCup);
				} else {
					if (stmt.elseIfStmt != null) {
						executeIfStmt((Stmt.If) stmt.elseIfStmt);
					} else if (stmt.elseCup != null) {
						evaluate(stmt.elseCup);
					}
				}
			}
		}
	}

	private Object findValueOfInstanceFromIdentifier(Object evaluate) {
		if (evaluate instanceof PocketInstance) {
			Token identifier = new Token(TokenType.IDENTIFIER,
					((Pocket) ((PocketInstance) evaluate).expr).identifier.lexeme, null, null, null,
					((Pocket) ((PocketInstance) evaluate).expr).identifier.column,
					((Pocket) ((PocketInstance) evaluate).expr).identifier.line,
					((Pocket) ((PocketInstance) evaluate).expr).identifier.start,
					((Pocket) ((PocketInstance) evaluate).expr).identifier.finish);
//			Token identifier = new Token(TokenType.IDENTIFIER,
//					((Pocket) ((PocketInstance) evaluate).expr).identifier.lexeme + "varravargssgra", null, null, null,
//					((Pocket) ((PocketInstance) evaluate).expr).identifier.column,
//					((Pocket) ((PocketInstance) evaluate).expr).identifier.line,
//					((Pocket) ((PocketInstance) evaluate).expr).identifier.start,
//					((Pocket) ((PocketInstance) evaluate).expr).identifier.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			evaluate = lookUpVariableByName(variable);
		} else if (evaluate instanceof CupInstance) {
			Token identifier = new Token(TokenType.IDENTIFIER, ((Cup) ((CupInstance) evaluate).expr).identifier.lexeme,
					null, null, null, ((Cup) ((CupInstance) evaluate).expr).identifier.column,
					((Cup) ((CupInstance) evaluate).expr).identifier.line,
					((Cup) ((CupInstance) evaluate).expr).identifier.start,
					((Cup) ((CupInstance) evaluate).expr).identifier.finish);
//			Token identifier = new Token(TokenType.IDENTIFIER,
//					((Cup) ((CupInstance) evaluate).expr).identifier.lexeme + "varravargssgra", null, null, null,
//					((Cup) ((CupInstance) evaluate).expr).identifier.column,
//					((Cup) ((CupInstance) evaluate).expr).identifier.line,
//					((Cup) ((CupInstance) evaluate).expr).identifier.start,
//					((Cup) ((CupInstance) evaluate).expr).identifier.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			evaluate = lookUpVariableByName(variable);
		}
		return evaluate;
	}

	private Object findValueOfInstanceFromReifitnedi(Object evaluate) {
		if (evaluate instanceof PocketInstance) {
			Token identifier = new Token(TokenType.IDENTIFIER,
					((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.lexeme, null, null, null,
					((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.column,
					((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.line,
					((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.start,
					((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.finish);
//			((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.lexeme + "varravargssgra", null, null, null,
//			((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.column,
//			((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.line,
//			((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.start,
//			((Pocket) ((PocketInstance) evaluate).expr).reifitnedi.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			evaluate = lookUpVariableByName(variable);
		} else if (evaluate instanceof CupInstance) {
			Token identifier = new Token(TokenType.IDENTIFIER, ((Cup) ((CupInstance) evaluate).expr).reifitnedi.lexeme,
					null, null, null, ((Cup) ((CupInstance) evaluate).expr).reifitnedi.column,
					((Cup) ((CupInstance) evaluate).expr).reifitnedi.line,
					((Cup) ((CupInstance) evaluate).expr).reifitnedi.start,
					((Cup) ((CupInstance) evaluate).expr).reifitnedi.finish);
//			((Cup) ((CupInstance) evaluate).expr).reifitnedi.lexeme + "varravargssgra", null, null, null,
//			((Cup) ((CupInstance) evaluate).expr).reifitnedi.column,
//			((Cup) ((CupInstance) evaluate).expr).reifitnedi.line,
//			((Cup) ((CupInstance) evaluate).expr).reifitnedi.start,
//			((Cup) ((CupInstance) evaluate).expr).reifitnedi.finish);

			Expr.Variable variable = new Expr.Variable(identifier);
			evaluate = lookUpVariableByName(variable);
		}
		return evaluate;
	}

	@Override
	public Object visitSaveStmt(Save stmt) {
		if (forward) {
			try {
				Object evaluate = evaluate(stmt.filePathFileName);
				String filePathAndName = (String) evaluate;
				String[] split = filePathAndName.split("/");
				String folderPath = "";
				if (split[split.length - 1].contains(".")) {
					for (int i = 0; i < split.length - 1; i++) {
						folderPath += split[i] + "/";
					}
				} else {
					for (int i = 0; i < split.length; i++) {
						folderPath += split[i] + "/";
					}
				}
				folderPath = folderPath.substring(0, folderPath.length() - 1);

				String str = "";
				Object boxInstance = evaluate(stmt.objecttosave);
				if (boxInstance != null) {
					str = boxInstance.toString();

					BufferedWriter writer = new BufferedWriter(new FileWriter(filePathAndName));
					writer.write(str);

					writer.close();
				}

			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Object visitExpelStmt(Expel stmt) {
		if (forward) {
			try {
				String filePathAndName = (String) evaluate(stmt.filePath);
				String[] split = filePathAndName.split("/");
				String folderPath = "";
				if (split[split.length - 1].contains(".")) {
					for (int i = 0; i < split.length - 1; i++) {
						folderPath += split[i] + "/";
					}
				} else {
					for (int i = 0; i < split.length; i++) {
						folderPath += split[i] + "/";
					}
				}
				folderPath = folderPath.substring(0, folderPath.length() - 1);

				String str = "";
				Object boxInstance = evaluate(stmt.toExpell);
				if (boxInstance != null) {
					str = boxInstance.toString();

					BufferedWriter writer = new BufferedWriter(new FileWriter(filePathAndName));
					writer.write(str);

					writer.close();
				}

				Integer integer = locals.get(stmt.toExpell);
				Token name = getNameForExpr(stmt.toExpell);

				if (integer != null) {
					environment.assignAt(integer, name, null, null, this);
				} else {
					globals.assign(name, null, null, this);
				}
			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}

		} else {
		}
		return null;
	}

	private Token getNameForExpr(Expr expression) {
		if (expression instanceof Expr.Knot) {
			return ((Expr.Knot) expression).identifier;
		} else if (expression instanceof Expr.Cup) {
			return ((Expr.Cup) expression).identifier;
		} else if (expression instanceof Expr.Pocket) {
			return ((Expr.Pocket) expression).identifier;
		} else if (expression instanceof Expr.Box) {
			return ((Expr.Box) expression).identifier;
		} else if (expression instanceof Expr.Variable) {
			return ((Expr.Variable) expression).name;
		} else if (expression instanceof Expr.Tonk) {
			return ((Expr.Tonk) expression).identifier;
		}
		return null;
	}

	@Override
	public Object visitReadStmt(Read stmt) {
		if (forward) {
			try {
				String seedFilePath = (String) evaluate(stmt.filePath);
				File myObj = new File(seedFilePath);
				java.util.Scanner myReader = new java.util.Scanner(myObj);
				String data = "";
				while (myReader.hasNextLine()) {
					data += myReader.nextLine();

				}
				if (stmt.objectToReadInto instanceof Expr.Variable) {

					Object value = evaluate(new Expr.Literal(data));
					String seedName = ((Expr.Variable) stmt.objectToReadInto).name.lexeme;
					Integer distance = locals.get((Expr.Variable) stmt.objectToReadInto);
					if (distance != null) {
						environment.assignAt(distance, ((Expr.Variable) stmt.objectToReadInto).name, value, value,
								this);
						environment.tagAsSeedGlobal(seedName, seedFilePath);
					} else {
						globals.assign(((Expr.Variable) stmt.objectToReadInto).name, value, value, this);
						globals.tagAsSeedGlobal(seedName, seedFilePath);
					}
				}

				myReader.close();
			} catch (FileNotFoundException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		}
		return null;
	}

	static void installShutdownHook(AutoCloseable c) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				c.close();
			} catch (Exception ignored) {
			}
		}, "AudioShutdown"));
	}

	@Override
	public Object visitConsumeStmt(Consume stmt) {
		if (forward) {
			try {
				String path = (String) evaluate(stmt.filePath);
				Object container = evaluate(stmt.boxToFill);
				List<Object> body = null;
				if (container instanceof BoxInstance)    body = ((BoxInstance) container).body;
				else if (container instanceof CupInstance)    body = ((CupInstance) container).body;
				else if (container instanceof PocketInstance) body = ((PocketInstance) container).body;
				else if (container instanceof KnotInstance)   body = ((KnotInstance) container).body;
				else if (container instanceof TonkInstance)   body = ((TonkInstance) container).body;
				else if (container instanceof TkpInstance)    body = ((TkpInstance) container).body;
				if (body == null) throw new RuntimeError(stmt.keyword, "consume: target is not a container");
				try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(path))) {
					String line;
					while ((line = reader.readLine()) != null) {
						String s = line.strip();
						if (!s.isEmpty()) body.add(ContainerPersistence.parseValue(s));
					}
				}
				notifyContainerModified(container);
			} catch (IOException e) {
				throw new RuntimeError(stmt.keyword, "consume: " + e.getMessage());
			}
		}
		return null;
	}

	@Override
	public Object visitAssertStmt(Assert stmt) {
		if (forward) {
			Object val = evaluate(stmt.condition);
			boolean cond = val instanceof Boolean ? (Boolean) val : val != null;
			if (!cond) throw new RuntimeError(stmt.keyword, "assertion failed: " + stringify(val));
		}
		return null;
	}

	@Override
	public Object visitVarStmt(Var stmt) {
		if (stmt.initilizer != null) {
			Object evaluate = evaluate(stmt.initilizer);
			String reverse = reverse(stmt.name.lexeme);
			if (evaluate instanceof CupInstance) {
				java.util.List<Object> _cupBody = ((CupInstance) evaluate).body;
				Object open0  = _cupBody.isEmpty() ? null : _cupBody.get(0);
				Object close1 = _cupBody.isEmpty() ? null : _cupBody.get(_cupBody.size() - 1);
				if (open0 instanceof Stmt.Expression) {
					Expr expression = ((Stmt.Expression) open0).expression;
					if (expression instanceof Expr.CupOpen) {
						((Expr.CupOpen) expression).ctrl.identifierToken.lexeme = stmt.name.lexeme;
						((Expr.CupOpen) expression).ctrl.lexeme = stmt.name.lexeme + "{";
					}
				}
				if (close1 instanceof Stmt.Expression) {
					Expr expression = ((Stmt.Expression) close1).expression;
					if (expression instanceof Expr.CupClosed) {
						((Expr.CupClosed) expression).ctrl.reifitnediToken.lexeme = reverse;
						((Expr.CupClosed) expression).ctrl.lexeme = "}" + reverse;
					}
				}
				environment.define(stmt.name.lexeme, stmt.type, stmt.num, evaluate, this);
				environment.define(reverse, stmt.type, stmt.num, evaluate, this);
			} else if (evaluate instanceof PocketInstance) {
				if (stmt.type != null && stmt.type.type == TokenType.TEKCOP) {
					PocketInstance srcPkt = (PocketInstance) evaluate;
					TkpInstance tkp = new TkpInstance(srcPkt.boxClass,
						new java.util.ArrayList<>(srcPkt.body), stmt.initilizer, this);
					Expr.Lifetime lt = srcPkt.getLifetime();
					if (lt != null) tkp.setLifetime(lt);
					tkp.windowSize = srcPkt.windowSize;
					tkp.starvationThreshold = srcPkt.starvationThreshold;
					tkp.starvationPolicy = srcPkt.starvationPolicy;
					environment.define(stmt.name.lexeme, stmt.type, tkp, tkp, this);
					environment.define(reverse, stmt.type, tkp, tkp, this);
					Token tkpName = new Token(TokenType.IDENTIFIER, stmt.name.lexeme,
							null, null, null, -1, -1, -1, -1);
					tkp.startIndependent(environment, tkpName);
				} else {
					environment.define(stmt.name.lexeme, stmt.type, evaluate, evaluate, this);
					environment.define(reverse, stmt.type, evaluate, evaluate, this);
					if (((PocketInstance) evaluate).hasFlowsInBody()) {
						Token pktName = new Token(TokenType.IDENTIFIER, stmt.name.lexeme,
								null, null, null, -1, -1, -1, -1);
						((PocketInstance) evaluate).startIndependent(environment, pktName);
					}
				}
			} else if (evaluate instanceof BoxInstance) {



				environment.define(stmt.name.lexeme, stmt.type,evaluate, evaluate, this);
				environment.define(reverse, stmt.type, evaluate, evaluate, this);
			} else if (evaluate instanceof KnotInstance) {
				environment.define(stmt.name.lexeme, stmt.type, stmt.num, evaluate, this);
				environment.define(reverse, stmt.type, stmt.num, evaluate, this);

			} else if (evaluate instanceof TonkInstance) {
				environment.define(stmt.name.lexeme, stmt.type, stmt.num, evaluate, this);
				environment.define(reverse, stmt.type, stmt.num, evaluate, this);

			} else {
				Object boxed = Boxer.box(evaluate, this);
				environment.define(stmt.name.lexeme, stmt.type, boxed, boxed, this);

			}
		} else {
			environment.define(stmt.name.lexeme, stmt.type, stmt.num, null, this);
		}


		return null;
	}

	// Walk the entire environment chain and call probeLifetime() on every
	// PocketInstance and TkpInstance. Called after each top-level/block statement
	// so DEPENDENT and CONDITIONAL lifetimes are enforced without waiting for the
	// next container operation.
	void probeLifetimes() {
		Environment env = environment;
		while (env != null) {
			for (Object val : env.allLocalValues()) {
				if (val instanceof PocketInstance) {
					PocketInstance p = (PocketInstance) val;
					if (p.isAlive()) p.probeLifetime();
					else if (p.isStripping()) p.destroy();
				} else if (val instanceof TkpInstance) ((TkpInstance) val).probeLifetime();
			}
			env = env.enclosing;
		}
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

	@Override
	public Object visitRavStmt(Rav stmt) {

		if (stmt.initilizer != null) {
			Object evaluate = evaluate(stmt.initilizer);
			String reverse = reverse(stmt.name.lexeme);
			if (evaluate instanceof CupInstance) {
				java.util.List<Object> _cupBody = ((CupInstance) evaluate).body;
				Object open0  = _cupBody.isEmpty() ? null : _cupBody.get(0);
				Object close1 = _cupBody.isEmpty() ? null : _cupBody.get(_cupBody.size() - 1);
				if (open0 instanceof Stmt.Expression) {
					Expr expression = ((Stmt.Expression) open0).expression;
					if (expression instanceof Expr.CupOpen) {
						((Expr.CupOpen) expression).ctrl.identifierToken.lexeme = stmt.name.lexeme;
						((Expr.CupOpen) expression).ctrl.lexeme = stmt.name.lexeme + "{";
					}
				}
				if (close1 instanceof Stmt.Expression) {
					Expr expression = ((Stmt.Expression) close1).expression;
					if (expression instanceof Expr.CupClosed) {
						((Expr.CupClosed) expression).ctrl.reifitnediToken.lexeme = reverse;
						((Expr.CupClosed) expression).ctrl.lexeme = "}" + reverse;
					}
				}
				environment.define(stmt.name.lexeme, stmt.type, stmt.num, evaluate, this);
				environment.define(reverse, stmt.type, stmt.num, evaluate, this);
			} else if (evaluate instanceof PocketInstance) {
				if (stmt.type != null && stmt.type.type == TokenType.TEKCOP) {
					Expr ravInitExpr = stmt.initilizer instanceof Stmt.Expression
							? ((Stmt.Expression) stmt.initilizer).expression : null;
					TkpInstance tkp = new TkpInstance(
						((PocketInstance) evaluate).boxClass,
						new java.util.ArrayList<>(((PocketInstance) evaluate).body),
						ravInitExpr,
						this);
					Expr.Lifetime lt2 = ((PocketInstance) evaluate).getLifetime();
					if (lt2 != null) tkp.setLifetime(lt2);
					environment.define(stmt.name.lexeme, stmt.type, tkp, tkp, this);
					environment.define(reverse, stmt.type, tkp, tkp, this);
					Token tkpName = new Token(TokenType.IDENTIFIER, stmt.name.lexeme,
							null, null, null, -1, -1, -1, -1);
					tkp.startIndependent(environment, tkpName);
				} else {
					environment.define(stmt.name.lexeme, stmt.type, evaluate, evaluate, this);
					environment.define(reverse, stmt.type, evaluate, evaluate, this);
					if (((PocketInstance) evaluate).hasFlowsInBody()) {
						Token pktName = new Token(TokenType.IDENTIFIER, stmt.name.lexeme,
								null, null, null, -1, -1, -1, -1);
						((PocketInstance) evaluate).startIndependent(environment, pktName);
					}
				}
			} else if (evaluate instanceof BoxInstance) {



				environment.define(stmt.name.lexeme, stmt.type,evaluate, evaluate, this);
				environment.define(reverse, stmt.type, evaluate, evaluate, this);
			} else if (evaluate instanceof KnotInstance) {
				environment.define(stmt.name.lexeme, stmt.type, stmt.num, evaluate, this);
				environment.define(reverse, stmt.type, stmt.num, evaluate, this);

			} else if (evaluate instanceof TonkInstance) {
				environment.define(stmt.name.lexeme, stmt.type, stmt.num, evaluate, this);
				environment.define(reverse, stmt.type, stmt.num, evaluate, this);

			} else {
				Object boxed = Boxer.box(evaluate, this);
				environment.define(stmt.name.lexeme, stmt.type, boxed, boxed, this);

			}
		} else {
			environment.define(stmt.name.lexeme, stmt.type, stmt.num, null, this);
		}

		return null;
	}

	@Override
	public Object visitGetExpr(Get expr) {
		Object object = evaluate(expr.object);
		if (applySettingIfRecognized(object, expr.name.lexeme)) return null;
		if (forward) {
			if (expr.name.lexeme.equals("boxify"))   return doBoxify(object);
			if (expr.name.lexeme.equals("deboxify")) return doDeboxify(object);
			if (object instanceof Instance) {
				return ((Instance) object).get(expr.name);
			}

			if (object instanceof BoxClass) {
				Instance call = (Instance) ((BoxClass) object).call(this, new ArrayList<>());
				return call.get(expr.name);
			}

			return object;
		}
		return null;
	}

	// ---- boxify / deboxify ---------------------------------------------------

	/** Decompose a string into a BoxInstance whose body is one single-char BoxInstance per character. */
	BoxInstance doBoxify(Object object) {
		Object raw = Boxer.unbox(object);
		String str;
		if (raw instanceof String) {
			str = (String) raw;
		} else if (raw instanceof Double || raw instanceof Boolean) {
			str = String.valueOf(raw);
		} else {
			// Non-string: wrap as-is
			return (BoxInstance) Boxer.box(raw, this);
		}
		java.util.List<Object> chars = new ArrayList<>();
		for (char c : str.toCharArray()) {
			chars.add(Boxer.box(String.valueOf(c), this));
		}
		return new BoxInstance(null, chars, null, this);
	}

	/** Recursively flatten any Instance's body into a single string, returned as a BoxInstance. */
	BoxInstance doDeboxify(Object object) {
		StringBuilder sb = new StringBuilder();
		flattenToString(object, sb);
		return (BoxInstance) Boxer.box(sb.toString(), this);
	}

	void flattenToString(Object obj, StringBuilder sb) {
		if (obj instanceof BoxInstance) {
			java.util.List<Object> body = ((BoxInstance) obj).body;
			if (body.size() == 1) {
				Object raw = body.get(0);
				if (raw instanceof String)  { sb.append((String) raw); return; }
				if (raw instanceof Double)  { sb.append(raw); return; }
				if (raw instanceof Boolean) { sb.append(raw); return; }
				if (raw == null) return;
				flattenToString(raw, sb);
			} else {
				for (Object item : body) flattenToString(item, sb);
			}
			return;
		}
		if (obj instanceof Instance) {
			java.util.List<Object> body = ((Instance) obj).getBody();
			if (body != null) {
				for (Object item : body) flattenToString(item, sb);
			}
			return;
		}
		if (obj instanceof String)  { sb.append((String) obj); return; }
		if (obj instanceof Double)  { sb.append(obj); return; }
		if (obj instanceof Boolean) { sb.append(obj); return; }
	}

	// ---- Settings pattern detection -----------------------------------------

	/** Returns true if object+name form a recognized settings pattern and the setting was applied. */
	private boolean applySettingIfRecognized(Object object, String name) {
		// Pattern: SETTING.CONTAINER  (object is a SettingToken, name is a container alias)
		if (object instanceof SettingToken) {
			String canonical = canonicalContainer(name);
			if (canonical != null) { activateSetting((SettingToken) object, canonical); return true; }
		}
		// Pattern: CONTAINER.SETTING  (object is a container instance, name is a setting keyword)
		String containerName = canonicalContainer(object);
		if (containerName != null) {
			SettingToken st = settingTokenForName(name);
			if (st != null) { activateSetting(st, containerName); return true; }
		}
		return false;
	}

	private void activateSetting(SettingToken st, String canonical) {
		switch (st.kind) {
			case DUMP_PRIORITY: activeDumpPriority.add(canonical); break;
			case THREAD:
				if (canonical.equals("TRASH")) trashThreaded = true;
				else if (canonical.equals("VOID")) voidThreaded  = true;
				break;
			case DYNAMIC_SAFTY: dynamicPolicy = true; break;
		}
	}

	private String canonicalContainer(Object obj) {
		if (obj == bin)   return "BIN";
		if (obj == gap)   return "GAP";
		if (obj == non)   return "NON";
		if (obj == limbo) return "LIMBO";
		if (obj == trash) return "TRASH";
		if (obj == voidC) return "VOID";
		return null;
	}

	private String canonicalContainer(String alias) {
		switch (alias) {
			case "BIN": case "NIB":     return "BIN";
			case "GAP": case "PAG":     return "GAP";
			case "NON":                 return "NON";
			case "TRASH": case "HSART": return "TRASH";
			case "VOID":  case "DIOV":  return "VOID";
			case "LIMBO": case "OBMIL": return "LIMBO";
		}
		return null;
	}

	private SettingToken settingTokenForName(String name) {
		switch (name) {
			case "dumpPriority": case "ytirorpPmud": return dumpPriorityToken;
			case "THREAD":       case "DAERHT":      return threadToken;
			case "DYNAMICSAFTY": case "YTFASCIMANYD": return dynamicSaftyToken;
		}
		return null;
	}

	@Override
	public Object visitSetExpr(Set expr) {
		if (forward) {
			Object evaluate = evaluate(expr.value);
			Integer integer = locals.get(expr.object);
			if (integer != null) {
				environment.assignAt(integer, expr.name, evaluate, evaluate, this);
			} else {
				globals.assign(expr.name, evaluate, evaluate, this);
			}

		}
		return null;
	}

	@Override
	public Object visitSniatnocExpr(Sniatnoc expr) {
		if (!forward) {

			if (expr.container instanceof Expr.Variable) {

				Object lookUpVariable = lookUpVariable(((Expr.Variable) expr.container).name, expr.container);
				Instance lookUpContainer = (Instance) lookUpVariable;
				Pocket poc = ((Expr.Pocket) expr.contents);
				if (poc.expression.size() < 3) {
					throw new RuntimeException("expected one parameter found none");
				} else if (poc.expression.size() > 3) {
					throw new RuntimeException("expected one parameter found more then one");
				}
				Stmt stmt = poc.expression.get(1);

				if (lookUpContainer instanceof BoxInstance) {
					return ((BoxInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof PocketInstance) {

					return ((PocketInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof CupInstance) {
					return ((CupInstance) lookUpContainer).contains(stmt);

				} else if (lookUpContainer instanceof KnotInstance) {

					return ((KnotInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof TonkInstance) {

					return ((TonkInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof TkpInstance) {
					return ((TkpInstance) lookUpContainer).contains(stmt);
				}
			}
		}

		return null;
	}

	@Override
	public Object visitTegExpr(Teg expr) {
		Object object = evaluate(expr.object);
		if (applySettingIfRecognized(object, expr.name.lexeme)) return null;
		if (!forward) {
			if (expr.name.lexeme.equals("yfixob"))   return doBoxify(object);
			if (expr.name.lexeme.equals("yfixobed")) return doDeboxify(object);
			if (object instanceof Instance) {
				return ((Instance) object).get(expr.name);
			}

			return object;
		}
		return null;
	}

	@Override
	public Object visitTesExpr(Tes expr) {
		if (!forward) {
			Object evaluate = evaluate(expr.value);
			Integer integer = locals.get(expr.object);
			if (integer != null) {
				environment.assignAt(integer, expr.name, evaluate, evaluate, this);
			} else {
				globals.assign(expr.name, evaluate, evaluate, this);
			}

		}
		return null;
	}

	@Override
	public Object visitLiteralCharExpr(LiteralChar expr) {
		return expr.value;
	}

	@Override
	public Object visitCupExpr(Cup expr) {
		Object name = lookUpVariable(expr.identifier, expr);

		if (name == null) {
			buildClass(expr);
			name = lookUpVariable(expr.identifier, expr);
			Environment previous = environment;
			try {

				environment = new Environment(environment);
				for (int i = 0; i < expr.expression.size(); i++) {
					execute(expr.expression.get(i));

				}
			} finally {
				this.environment = previous;
			}
		} else {

			Environment previous = environment;
			try {

				environment = new Environment(environment);
				for (int i = 0; i < expr.expression.size(); i++) {
					execute(expr.expression.get(i));

				}
			} finally {
				this.environment = previous;
			}
		}
		return null;
	}

	@Override
	public Object visitPocketExpr(Pocket expr) {
		Object name = lookUpVariable(expr.identifier, expr);
		ArrayList<Object> notnull = new ArrayList<>();
		Object class1 =null;
		if (name == null) {
			 class1 = buildClass(expr);
			name = lookUpVariable(expr.identifier, expr);
			Environment previous = environment;
			try {

				environment = new Environment(environment);
				for (int i = 0; i < expr.expression.size(); i++) {
					Object execute = execute(expr.expression.get(i));
					if (execute != null)
						notnull.add(execute);

				}
			} finally {
				this.environment = previous;
			}
		} else {

			Environment previous = environment;
			try {

				environment = new Environment(environment);
				for (int i = 0; i < expr.expression.size(); i++) {
					Object execute = execute(expr.expression.get(i));
					if (execute != null)
						notnull.add(execute);

				}
			} finally {
				this.environment = previous;
			}
		}

		return class1;

	}

	@Override
	public Object visitKnotExpr(Knot expr) {
		if (!forward) return null;
		Object name = lookUpVariable(expr.identifier, expr);
		Object class1 =null;
		if (name == null) {
			class1 = buildClass(expr);
			name = lookUpVariable(expr.identifier, expr);
			Environment previous = environment;
			try {

				environment = new Environment(environment);

				KnotRunner knotRunner = new KnotRunner(expr, expr.expression, this);
				knotRunner.runKnot();

			} finally {
				this.environment = previous;
			}
		} else {

			Environment previous = environment;
			try {

				environment = new Environment(environment);

				KnotRunner knotRunner = new KnotRunner(expr, expr.expression, this);
				knotRunner.runKnot();
				
			} finally {
				this.environment = previous;
			}
		}

		return class1;
	}

	@Override
	public Object visitTonkExpr(Tonk expr) {
		if (forward) return null;
		Object name = lookUpVariable(expr.identifier, expr);

		Object class1 =null;
		if (name == null) {
			class1 = buildClass(expr);
			name = lookUpVariable(expr.identifier, expr);
			Environment previous = environment;
			try {

				environment = new Environment(environment);

				KnotRunner knotRunner = new KnotRunner(expr, expr.expression, this);
				knotRunner.runTonk();

			} finally {
				this.environment = previous;
			}
		} else {

			Environment previous = environment;
			try {

				environment = new Environment(environment);

				KnotRunner knotRunner = new KnotRunner(expr, expr.expression, this);
				knotRunner.runTonk();

			} finally {
				this.environment = previous;
			}
		}

		return class1;
	}

	@Override
	public Object visitBoxExpr(Expr.Box expr) {
		Object name = lookUpVariable(expr.identifier, expr);
		if (name == null) {
			return buildClass(expr);
		}
		return name;
	}

	@Override
	public Object visitFiStmt(Fi stmt) {
		if (!forward) {
			return executeFiStmt(stmt);

		}
		return null;
	}

	private Object executeFiStmt(Fi stmt) {
		Object evaluate = evaluate(stmt.ifPocket);

		evaluate = findValueOfInstanceFromReifitnedi(evaluate);
		if (evaluate instanceof ArrayList<?> && ((ArrayList<?>) evaluate).size() == 1) {
			Object poc = ((ArrayList<?>) evaluate).get(0);
			if (poc instanceof Boolean) {
				if (((Boolean) poc) == true) {
					return evaluate(stmt.ifCup);
				} else {
					if (stmt.elseIfStmt != null) {
						return executeFiStmt((Stmt.Fi) stmt.elseIfStmt);
					} else if (stmt.elseCup != null) {
						return evaluate(stmt.elseCup);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Object visitCallExpr(Call expr) {
		if (forward) {
			Object callee = evaluate(expr.callee);
			List<Object> arguments = new ArrayList<>();
			for (Expr argument : expr.arguments) {
				arguments.add(evaluate(argument));
			}

			if (!(callee instanceof BoxCallable)) {
				throw new RuntimeError(expr.calleeToken, "Can only call functions and classes.");
			}

			BoxCallable function = (BoxCallable) callee;
			if (function instanceof BoxFunction && !((BoxFunction) function).isForward)
				throw new RuntimeError(expr.calleeToken, "direction mismatch: cannot call backward function while running forward");

			if (arguments.size() != function.arity()) {
				throw new RuntimeError(expr.calleeToken,
						"Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
			}

			return function.call(this, arguments);
		}
		return null;
	}

	@Override
	public Object visitSwapExpr(Swap expr) {
		Object s1 = evaluate(expr.swap1);
		Object s2 = evaluate(expr.Swap2);
		Object s3 = s1;
		s1 = s2;
		s2 = s3;
		if (expr.swap1 instanceof Expr.Variable) {
			if (expr.Swap2 instanceof Expr.Variable) {
				Integer integer = locals.get((Expr.Variable) expr.swap1);
				Integer integer2 = locals.get((Expr.Variable) expr.Swap2);
				assignForSwap((Expr.Variable) expr.swap1, s1, integer);
				assignForSwap((Expr.Variable) expr.Swap2, s2, integer2);
			} else if (expr.Swap2 instanceof Expr.Get) {
				Integer integer = locals.get((Expr.Variable) expr.swap1);
				Integer integer2 = locals.get((Expr.Get) expr.Swap2);
				assignForSwap((Expr.Variable) expr.swap1, s1, integer);
				assignForSwap((Expr.Get) expr.Swap2, s2, integer2);

			} else if (expr.Swap2 instanceof Expr.Teg) {
				Integer integer = locals.get((Expr.Variable) expr.swap1);
				Integer integer2 = locals.get((Expr.Teg) expr.Swap2);
				assignForSwap((Expr.Variable) expr.swap1, s1, integer);
				assignForSwap((Expr.Teg) expr.Swap2, s2, integer2);

			}
		} else if (expr.swap1 instanceof Expr.Get) {
			if (expr.Swap2 instanceof Expr.Variable) {
				Integer integer = locals.get((Expr.Get) expr.swap1);
				Integer integer2 = locals.get((Expr.Variable) expr.Swap2);
				assignForSwap((Expr.Get) expr.swap1, s1, integer);
				assignForSwap((Expr.Variable) expr.Swap2, s2, integer2);
			} else if (expr.Swap2 instanceof Expr.Get) {
				Integer integer = locals.get((Expr.Get) expr.swap1);
				Integer integer2 = locals.get((Expr.Get) expr.Swap2);
				assignForSwap((Expr.Get) expr.swap1, s1, integer);
				assignForSwap((Expr.Get) expr.Swap2, s2, integer2);

			} else if (expr.Swap2 instanceof Expr.Teg) {
				Integer integer = locals.get((Expr.Get) expr.swap1);
				Integer integer2 = locals.get((Expr.Teg) expr.Swap2);
				assignForSwap((Expr.Get) expr.swap1, s1, integer);
				assignForSwap((Expr.Teg) expr.Swap2, s2, integer2);

			}
		} else if (expr.swap1 instanceof Expr.Teg) {
			if (expr.Swap2 instanceof Expr.Variable) {
				Integer integer = locals.get((Expr.Teg) expr.swap1);
				Integer integer2 = locals.get((Expr.Variable) expr.Swap2);
				assignForSwap((Expr.Teg) expr.swap1, s1, integer);
				assignForSwap((Expr.Variable) expr.Swap2, s2, integer2);
			} else if (expr.Swap2 instanceof Expr.Get) {
				Integer integer = locals.get((Expr.Teg) expr.swap1);
				Integer integer2 = locals.get((Expr.Get) expr.Swap2);
				assignForSwap((Expr.Teg) expr.swap1, s1, integer);
				assignForSwap((Expr.Get) expr.Swap2, s2, integer2);

			} else if (expr.Swap2 instanceof Expr.Teg) {
				Integer integer = locals.get((Expr.Teg) expr.swap1);
				Integer integer2 = locals.get((Expr.Teg) expr.Swap2);
				assignForSwap((Expr.Teg) expr.swap1, s1, integer);
				assignForSwap((Expr.Teg) expr.Swap2, s2, integer2);

			}
		}

		return null;
	}

	private void assignForSwap(Expr.Variable expr, Object s1, Integer integer) {
		if (integer != null) {
			environment.assignAt(integer, expr.name, s1, s1, this);
		} else {
			globals.assign(expr.name, s1, s1, this);
		}
	}

	private void assignForSwap(Expr.Get expr, Object s1, Integer integer) {
		if (integer != null) {
			environment.assignAt(integer, expr.name, s1, s1, this);
		} else {
			globals.assign(expr.name, s1, s1, this);
		}
	}

	private void assignForSwap(Expr.Teg expr, Object s1, Integer integer) {
		if (integer != null) {
			environment.assignAt(integer, expr.name, s1, s1, this);
		} else {
			globals.assign(expr.name, s1, s1, this);
		}
	}

	@Override
	public Object visitTemplatVarStmt(TemplatVar stmt) {
		if (forward) {
			if (!templates.contains(stmt.name.lexeme) && templates.contains(stmt.superclass.lexeme)) {
				Expr.Variable supclass = new Expr.Variable(stmt.superclass);
				BoxCallable superBoxClass = (BoxCallable) lookUpVariable(supclass.name, supclass);
				boolean isContractTemplate = superBoxClass instanceof BoxClass
						&& ((BoxClass) superBoxClass).templateLinkNames != null
						&& !((BoxClass) superBoxClass).templateLinkNames.isEmpty();
				if (isContractTemplate) contractContextStack.push(true);
				Object call = null;
				RuntimeError instantiationError = null;
				try {
					call = superBoxClass.call(this, new ArrayList<>());
				} catch (RuntimeError e) {
					instantiationError = new RuntimeError(stmt.name,
							"instantiation of '" + stmt.name.lexeme + "' from template '" + stmt.superclass.lexeme
									+ "' failed: " + e.getMessage());
					call = null;
				} catch (RuntimeException e) {
					instantiationError = new RuntimeError(stmt.name,
							"instantiation of '" + stmt.name.lexeme + "' from template '" + stmt.superclass.lexeme
									+ "' aborted: " + e.getMessage());
					call = null;
				} finally {
					if (isContractTemplate) contractContextStack.pop();
				}
				if (instantiationError != null) throw instantiationError;
				String reverse = reverse(stmt.name.lexeme);
				environment.define(stmt.name.lexeme, null, null);
				environment.define(reverse, null, null);
				if (call instanceof CupInstance && !((CupInstance) call).body.isEmpty()) {
					Object open0 = ((CupInstance) call).body.get(0);
					Object close1 = ((CupInstance) call).body.get(((CupInstance) call).body.size() - 1);
					if (open0 instanceof Stmt.Expression) {
						Expr expression = ((Stmt.Expression) open0).expression;
						if (expression instanceof Expr.CupOpen) {
							((Expr.CupOpen) expression).ctrl.identifierToken.lexeme = stmt.name.lexeme;
							((Expr.CupOpen) expression).ctrl.lexeme = stmt.name.lexeme + "{";
						}
					}
					if (close1 instanceof Stmt.Expression) {
						Expr expression = ((Stmt.Expression) close1).expression;
						if (expression instanceof Expr.CupClosed) {
							((Expr.CupClosed) expression).ctrl.reifitnediToken.lexeme = reverse;
							((Expr.CupClosed) expression).ctrl.lexeme = "}" + reverse;
						}
					}
				} else if (call instanceof PocketInstance) {
					Object open0 = ((PocketInstance) call).body.get(0);
					Object close1 = ((PocketInstance) call).body.get(((PocketInstance) call).body.size() - 1);
					if (open0 instanceof Stmt.Expression) {
						Expr expression = ((Stmt.Expression) open0).expression;
						if (expression instanceof Expr.PocketOpen) {
							((Expr.PocketOpen) expression).ctrl.identifierToken.lexeme = stmt.name.lexeme;
							((Expr.PocketOpen) expression).ctrl.lexeme = stmt.name.lexeme + "(";
						}
					}
					if (close1 instanceof Stmt.Expression) {
						Expr expression = ((Stmt.Expression) close1).expression;
						if (expression instanceof Expr.PocketClosed) {
							((Expr.PocketClosed) expression).ctrl.reifitnediToken.lexeme = reverse;
							((Expr.PocketClosed) expression).ctrl.lexeme = ")" + reverse;
						}
					}
				}
				Token reifi = new Token(TokenType.IDENTIFIER, reverse, null, null, null, stmt.name.column,
						stmt.name.line, stmt.name.start, stmt.name.finish);
				environment.assign(stmt.name, null, call);
				environment.assign(reifi, null, call);
			}
		} else {
			if (templates.contains(stmt.name.lexeme) && !templates.contains(stmt.superclass.lexeme)) {
				Expr.Variable supclass = new Expr.Variable(stmt.name);
				BoxCallable superBoxClass = (BoxCallable) lookUpVariable(supclass.name, supclass);
				Object call = superBoxClass.call(this, new ArrayList<>());
				String reverse = reverse(stmt.superclass.lexeme);
				environment.define(stmt.superclass.lexeme, null, null);
				environment.define(reverse, null, null);
				if (call instanceof CupInstance && !((CupInstance) call).body.isEmpty()) {
					Object open0 = ((CupInstance) call).body.get(0);
					Object close1 = ((CupInstance) call).body.get(((CupInstance) call).body.size() - 1);
					if (open0 instanceof Stmt.Expression) {
						Expr expression = ((Stmt.Expression) open0).expression;
						if (expression instanceof Expr.CupOpen) {
							((Expr.CupOpen) expression).ctrl.identifierToken.lexeme = stmt.name.lexeme;
							((Expr.CupOpen) expression).ctrl.lexeme = stmt.name.lexeme + "{";
						}
					}
					if (close1 instanceof Stmt.Expression) {
						Expr expression = ((Stmt.Expression) close1).expression;
						if (expression instanceof Expr.CupClosed) {
							((Expr.CupClosed) expression).ctrl.reifitnediToken.lexeme = reverse;
							((Expr.CupClosed) expression).ctrl.lexeme = "}" + reverse;
						}
					}
				} else if (call instanceof PocketInstance && !((PocketInstance) call).body.isEmpty()) {
					Object open0 = ((PocketInstance) call).body.get(0);
					Object close1 = ((PocketInstance) call).body.get(((PocketInstance) call).body.size() - 1);
					if (open0 instanceof Stmt.Expression) {
						Expr expression = ((Stmt.Expression) open0).expression;
						if (expression instanceof Expr.PocketOpen) {
							((Expr.PocketOpen) expression).ctrl.identifierToken.lexeme = stmt.name.lexeme;
							((Expr.PocketOpen) expression).ctrl.lexeme = stmt.name.lexeme + "(";
						}
					}
					if (close1 instanceof Stmt.Expression) {
						Expr expression = ((Stmt.Expression) close1).expression;
						if (expression instanceof Expr.PocketClosed) {
							((Expr.PocketClosed) expression).ctrl.reifitnediToken.lexeme = reverse;
							((Expr.PocketClosed) expression).ctrl.lexeme = ")" + reverse;
						}
					}
				}
				Token reifi = new Token(TokenType.IDENTIFIER, reverse, null, null, null, stmt.name.column,
						stmt.name.line, stmt.name.start, stmt.name.finish);
				environment.assign(stmt.superclass, null, call);
				environment.assign(reifi, null, call);
			}
		}
		return null;
	}

	public String generateContractManifest(String instanceName, BoxInstance instance) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n  \"instance\": \"").append(instanceName).append("\",\n");
		sb.append("  \"contractConfidence\": ").append(instance.contractConfidence).append(",\n");
		sb.append("  \"lazyBindingCount\": ").append(instance.lazyBindingCount).append(",\n");
		sb.append("  \"eagerBindingCount\": ").append(instance.eagerBindingCount).append(",\n");
		sb.append("  \"bindings\": [\n");
		java.util.List<Object> body = instance.getBody();
		boolean first = true;
		if (body != null) {
			for (Object item : body) {
				if (item instanceof Expr.Get) {
					if (!first) sb.append(",\n");
					sb.append("    {\"type\": \"lazy\", \"path\": \"").append(buildGetPath((Expr.Get) item)).append("\"}");
					first = false;
				} else if (item instanceof BoxInstance) {
					if (!first) sb.append(",\n");
					sb.append("    {\"type\": \"eager\", \"value\": \"").append(item).append("\"}");
					first = false;
				}
			}
		}
		if (!first) sb.append("\n");
		sb.append("  ]\n}");
		return sb.toString();
	}

	private String buildGetPath(Expr.Get get) {
		java.util.Deque<String> parts = new java.util.ArrayDeque<>();
		Expr cursor = get;
		while (cursor instanceof Expr.Get) {
			parts.push(((Expr.Get) cursor).name.lexeme);
			cursor = ((Expr.Get) cursor).object;
		}
		StringBuilder path = new StringBuilder();
		if (cursor instanceof Expr.Variable)
			path.append(((Expr.Variable) cursor).name.lexeme);
		while (!parts.isEmpty()) path.append(".").append(parts.pop());
		return path.toString();
	}

	public String generateExportPreamble() {
		StringBuilder sb = new StringBuilder();
		for (String name : globals.getSeedGlobalNames()) {
			String filePath = globals.getSeedGlobalPath(name);
			if (filePath != null)
				sb.append("read ").append(filePath).append(" into ").append(name).append("\n");
		}
		return sb.toString();
	}

	@Override
	public Object visitIfiStmt(Ifi stmt) {
		if (forward) {
			executeIfiStmtForwards(stmt);
		} else {
			executeIfiStmtBackwards(stmt);
		}
		return null;
	}

	private void executeIfiStmtForwards(Ifi stmt) {
		Object evaluate = evaluate(stmt.ifPocket);

		evaluate = findValueOfInstanceFromReifitnedi(evaluate);
		if (evaluate instanceof ArrayList<?> && ((ArrayList<?>) evaluate).size() == 1) {
			Object poc = ((ArrayList<?>) evaluate).get(0);
			if (poc instanceof Boolean) {
				if (((Boolean) poc) == true) {
					evaluate(((Stmt.Fi) stmt.elseIf).ifCup);
				} else {
					evaluateFiBackwards(((Stmt.Fi) stmt.elseIf));
				}
			}
		}
	}

	private void executeIfiStmtBackwards(Ifi stmt) {
		executeFiForIfi((Fi) stmt.elseIf);
	}

	private Object executeFiForIfi(Fi elseIf) {
		if (elseIf.elseIfStmt != null) {
			Object executeFiForIfi = executeFiForIfi((Fi) elseIf.elseIfStmt);
			if (executeFiForIfi == null) {
				Object evaluate = evaluate(elseIf);
				return evaluate;
			} else
				return executeFiForIfi;

		} else {
			Object evaluate = evaluate(elseIf);
			return evaluate;
		}

	}

	private void evaluateFiBackwards(Fi stmt) {
		Object evaluate;
		Expr ifPocket = ((Stmt.Fi) stmt).ifPocket;
		Stmt.Fi elseIfStmt = (Fi) ((Stmt.Fi) stmt).elseIfStmt;
		if (elseIfStmt != null) {
			evaluate = evaluate(ifPocket);

			evaluate = findValueOfInstanceFromReifitnedi(evaluate);
			if (evaluate instanceof ArrayList<?> && ((ArrayList<?>) evaluate).size() == 1) {
				Object poc2 = ((ArrayList<?>) evaluate).get(0);
				if (poc2 instanceof Boolean) {
					if (((Boolean) poc2) == true) {
						evaluate(((Stmt.Fi) elseIfStmt).ifCup);
					} else {
						evaluateFiBackwards(elseIfStmt);
					}
				}
			}
		}
	}

	@Override
	public Object visitTemplateExpr(Template expr) {

		return buildClass(expr);
	}

	private Object lookUpVariableByName(Variable name) {
//		java.util.Set<Expr> keySet = locals.keySet();
//		Expr exprToFind = null;
//		if (name != null) {
//			for (Expr keyExpr : keySet) {
//				if (keyExpr instanceof Expr.Variable) {
//
//					if (((Expr.Variable) keyExpr).name.lexeme == name.lexeme) {
//						exprToFind = keyExpr;
//					}
//				}
//			}
//		}
		Integer distance = locals.get(name);
		if (name != null) {
			if (distance != null) {
				return environment.getAt(distance, name.name.lexeme);
			} else {
				return globals.get(name.name, false);
			}
		}
		return null;
	}
	
	private Object lookUpVariableTypeByName(Variable name) {
		Integer distance = locals.get(name);
		if (name != null) {
			if (distance != null) {
				Object at = environment.getType(name.name);
				return at;
			} else {
				Object object = globals.getType(name.name);
				return object;
			}
		}
		return null;
	}

	private Object buildClass(Expr expr) {
		Object superclass = null;
		if (expr instanceof Expr.Cup) {
			Token identifier = ((Expr.Cup) expr).identifier;
			Token reifitnedi = ((Expr.Cup) expr).reifitnedi;
			this.classes.add(identifier.lexeme);
			this.classes.add(reifitnedi.lexeme);
			return buildCupClass(expr, superclass);
		} else if (expr instanceof Expr.Pocket) {
			Token identifier = ((Expr.Pocket) expr).identifier;
			Token reifitnedi = ((Expr.Pocket) expr).reifitnedi;
			this.classes.add(identifier.lexeme);
			this.classes.add(reifitnedi.lexeme);
			return buildPocketClass(expr, superclass);

		} else if (expr instanceof Expr.Box) {
			return buildBoxClass(expr, superclass);

		} else if (expr instanceof Expr.Knot) {
			return buildKnotClass(expr, superclass);

		} else if (expr instanceof Expr.Tonk) {
			return buildTonkClass(expr, superclass);

		} else if (expr instanceof Expr.Template) {

			Expr container = ((Template) expr).container;
			Token templateIdentifier = null;
			if (container instanceof Expr.Pocket) {
				templateIdentifier = ((Expr.Pocket) container).identifier;
				this.templates.add(templateIdentifier.lexeme);
				this.templates.add(((Expr.Pocket) container).reifitnedi.lexeme);
			} else if (container instanceof Expr.Cup) {
				templateIdentifier = ((Expr.Cup) container).identifier;
				this.templates.add(templateIdentifier.lexeme);
				this.templates.add(((Expr.Cup) container).reifitnedi.lexeme);
			}
			Object result = buildClass(container);

			// store link names on the BoxClass and run conformance check
			if (templateIdentifier != null) {
				Object registered = globals.get(templateIdentifier, false);
				if (registered == null)
					registered = environment.get(templateIdentifier, false);
				if (registered instanceof BoxClass) {
					BoxClass templateClass = (BoxClass) registered;
					List<String> lnames = new ArrayList<>();
					for (Token lt : ((Template) expr).linkNames)
						lnames.add(lt.lexeme);
					templateClass.templateLinkNames = lnames;

					// conformance check: every link method must be in template body or base
					for (Token lt : ((Template) expr).linkNames) {
						Object linkObj = globals.get(lt, false);
						if (linkObj == null) linkObj = environment.get(lt, false);
						if (linkObj instanceof BoxClass && ((BoxClass) linkObj).isLink()) {
							BoxClass linkClass = (BoxClass) linkObj;
							for (String methodName : linkClass.getMethods().keySet()) {
								if (templateClass.findMethod(methodName) == null
										|| templateClass.findMethod(methodName).isLinkSignature()) {
									throw new RuntimeError(templateIdentifier,
											"template '" + templateIdentifier.lexeme
											+ "' does not implement link method '" + methodName + "'");
								}
							}
						}
					}
				}
			}
			// lazy template resolution: bind any type registry entries waiting for this template
			if (templateIdentifier != null)
				resolveTemplateInRegistry(templateIdentifier.lexeme);
			return result;
		} else if (expr instanceof Expr.Link) {
			Expr container = ((Expr.Link) expr).container;
			if (container instanceof Expr.Pocket) {
				Token identifier = ((Expr.Pocket) container).identifier;
				Token reifitnedi = ((Expr.Pocket) container).reifitnedi;
				this.links.add(identifier.lexeme);
				this.links.add(reifitnedi.lexeme);
			} else if (container instanceof Expr.Cup) {
				Token identifier = ((Expr.Cup) container).identifier;
				Token reifitnedi = ((Expr.Cup) container).reifitnedi;
				this.links.add(identifier.lexeme);
				this.links.add(reifitnedi.lexeme);

			}
			return buildLink(expr);
		}
		return null;
	}

	private Token buildLink(Expr expr) {
		Expr container = ((Expr.Link) expr).container;

		Token identifier;
		Token reifitnedi;
		List<Declaration> declarations;

		if (container instanceof Expr.Cup) {
			identifier = ((Expr.Cup) container).identifier;
			reifitnedi = ((Expr.Cup) container).reifitnedi;
			declarations = ((Expr.Cup) container).expression;
		} else if (container instanceof Expr.Pocket) {
			identifier = ((Expr.Pocket) container).identifier;
			reifitnedi = ((Expr.Pocket) container).reifitnedi;
			declarations = new ArrayList<>();
			for (Stmt s : ((Expr.Pocket) container).expression)
				if (s instanceof Stmt.Expression && ((Stmt.Expression) s).expression instanceof Declaration)
					declarations.add((Declaration) ((Stmt.Expression) s).expression);
		} else {
			return null;
		}

		environment.define(identifier.lexeme, identifier, null);
		environment.define(reifitnedi.lexeme, reifitnedi, null);

		Map<String, BoxFunction> methods = new HashMap<>();
		for (Declaration method : declarations) {
			if (method instanceof Declaration.FunDecl) {
				if (((Declaration.FunDecl) method).function instanceof FunctionLink) {
					FunctionLink fl = (FunctionLink) ((Declaration.FunDecl) method).function;
					if (fl.forwardIdentifier != null) {
						BoxFunction fwd = new BoxFunction(null, fl.forwardIdentifier.lexeme,
								fl.forwardPrametersType, fl.forwardPrametersNames, environment, true, true);
						methods.put(fl.forwardIdentifier.lexeme, fwd);
					}
					if (fl.backwardIdentifier != null) {
						BoxFunction bwd = new BoxFunction(null, fl.backwardIdentifier.lexeme,
								fl.backwardPrametersType, fl.backwardPrametersNames, environment, false, true);
						methods.put(fl.backwardIdentifier.lexeme, bwd);
					}
				}
			}
		}

		BoxClass boxClass = new BoxClass(identifier.lexeme, null, null, methods,
				TokenType.CUPCONTAINER, false, null, environment, container, true);

		environment.assign(identifier, null, boxClass);
		environment.assign(reifitnedi, null, boxClass);
		return identifier;
	}

	private Object buildTonkClass(Expr expr, Object superclass) {
		if (((Tonk) expr).identifier.identifierToken != null) {
			superclass = lookUpVariableByName(new Variable(((Tonk) expr).identifier.identifierToken));

			if (!(superclass instanceof BoxClass)) {
				throw new RuntimeError(((Tonk) expr).identifier.identifierToken, "Superclass must be a class.");
			}

		}
		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Tonk) expr).identifier.lexeme, null, null, null,
				((Expr.Tonk) expr).identifier.column, ((Expr.Tonk) expr).identifier.line,
				((Expr.Tonk) expr).identifier.start, ((Expr.Tonk) expr).identifier.finish);

		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Tonk) expr).reifitnedi.lexeme, null, null, null,
				((Expr.Tonk) expr).reifitnedi.column, ((Expr.Tonk) expr).reifitnedi.line,
				((Expr.Tonk) expr).reifitnedi.start, ((Expr.Tonk) expr).reifitnedi.finish);
//		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Tonk) expr).identifier.lexeme + "varravargssgra",
//				null, null, null, ((Expr.Tonk) expr).identifier.column, ((Expr.Tonk) expr).identifier.line,
//				((Expr.Tonk) expr).identifier.start, ((Expr.Tonk) expr).identifier.finish);
//		
//		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Tonk) expr).reifitnedi.lexeme + "varravargssgra",
//				null, null, null, ((Expr.Tonk) expr).reifitnedi.column, ((Expr.Tonk) expr).reifitnedi.line,
//				((Expr.Tonk) expr).reifitnedi.start, ((Expr.Tonk) expr).reifitnedi.finish);

		environment.define(identifier.lexeme, null, null);
		environment.define(reifitnedi.lexeme, null, null);
		environment.define(((Tonk) expr).identifier.lexeme, ((Tonk) expr).identifier, null);
		environment.define(((Tonk) expr).reifitnedi.lexeme, ((Tonk) expr).reifitnedi, null);

		if (((Tonk) expr).identifier.identifierToken != null) {
			environment = new Environment(environment);

		}

		ArrayList<Object> primarys = new ArrayList<>();
		for (Stmt stmt : ((Expr.Tonk) expr).expression) {
			primarys.add(stmt);
		}

		BoxClass boxClass = new BoxClass(((Tonk) expr).identifier.lexeme, (BoxClass) superclass, primarys, null,
				TokenType.CUPCONTAINER, false, null, environment, expr, false);

		if (superclass != null) {
			environment = environment.enclosing;
		}
		Object call = boxClass.call(this, new ArrayList<Object>());
		environment.assign(identifier, null, call);
		environment.assign(reifitnedi, null, call);
		environment.assign(((Tonk) expr).identifier, null, boxClass);
		environment.assign(((Tonk) expr).reifitnedi, null, boxClass);

		return call;
	}

	private Object buildKnotClass(Expr expr, Object superclass) {
		if (((Knot) expr).identifier.identifierToken != null) {
			superclass = lookUpVariableByName(new Variable(((Knot) expr).identifier.identifierToken));

			if (!(superclass instanceof BoxClass)) {
				throw new RuntimeError(((Knot) expr).identifier.identifierToken, "Superclass must be a class.");
			}

		}
		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Knot) expr).identifier.lexeme, null, null, null,
				((Expr.Knot) expr).identifier.column, ((Expr.Knot) expr).identifier.line,
				((Expr.Knot) expr).identifier.start, ((Expr.Knot) expr).identifier.finish);

		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Knot) expr).reifitnedi.lexeme, null, null, null,
				((Expr.Knot) expr).reifitnedi.column, ((Expr.Knot) expr).reifitnedi.line,
				((Expr.Knot) expr).reifitnedi.start, ((Expr.Knot) expr).reifitnedi.finish);
//		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Knot) expr).identifier.lexeme + "varravargssgra",
//				null, null, null, ((Expr.Knot) expr).identifier.column, ((Expr.Knot) expr).identifier.line,
//				((Expr.Knot) expr).identifier.start, ((Expr.Knot) expr).identifier.finish);
//		
//		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Knot) expr).reifitnedi.lexeme + "varravargssgra",
//				null, null, null, ((Expr.Knot) expr).reifitnedi.column, ((Expr.Knot) expr).reifitnedi.line,
//				((Expr.Knot) expr).reifitnedi.start, ((Expr.Knot) expr).reifitnedi.finish);

		environment.define(identifier.lexeme, null, null);
		environment.define(reifitnedi.lexeme, null, null);
		environment.define(((Knot) expr).identifier.lexeme, ((Knot) expr).identifier, null);
		environment.define(((Knot) expr).reifitnedi.lexeme, ((Knot) expr).reifitnedi, null);

		if (((Knot) expr).identifier.identifierToken != null) {
			environment = new Environment(environment);

		}

		ArrayList<Object> primarys = new ArrayList<>();
		for (Stmt stmt : ((Expr.Knot) expr).expression) {

			primarys.add(stmt);
		}

		BoxClass boxClass = new BoxClass(((Knot) expr).identifier.lexeme, (BoxClass) superclass, primarys, null,
				TokenType.CUPCONTAINER, false, null, environment, expr, false);

		if (superclass != null) {
			environment = environment.enclosing;
		}
		Object call = boxClass.call(this, new ArrayList<Object>());
		environment.assign(identifier, null, call);
		environment.assign(reifitnedi, null, call);
		environment.assign(((Knot) expr).identifier, null, boxClass);
		environment.assign(((Knot) expr).reifitnedi, null, boxClass);
		return call;

	}

	private Object buildBoxClass(Expr expr, Object superclass) {
		if (((Expr.Box) expr).identifier.identifierToken != null) {
			superclass = lookUpVariableByName(new Variable(((Expr.Box) expr).identifier.identifierToken));

			if (!(superclass instanceof BoxClass)) {
				throw new RuntimeError(((Expr.Box) expr).identifier.identifierToken, "Superclass must be a class.");
			}

		}
		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Box) expr).identifier.lexeme, null, null, null,
				((Expr.Box) expr).identifier.column, ((Expr.Box) expr).identifier.line,
				((Expr.Box) expr).identifier.start, ((Expr.Box) expr).identifier.finish);
		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Box) expr).reifitnedi.lexeme, null, null, null,
				((Expr.Box) expr).reifitnedi.column, ((Expr.Box) expr).reifitnedi.line,
				((Expr.Box) expr).reifitnedi.start, ((Expr.Box) expr).reifitnedi.finish);
//		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Box) expr).identifier.lexeme + "varravargssgra", null,
//				null, null, ((Expr.Box) expr).identifier.column, ((Expr.Box) expr).identifier.line,
//				((Expr.Box) expr).identifier.start, ((Expr.Box) expr).identifier.finish);
//		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Box) expr).reifitnedi.lexeme + "varravargssgra", null,
//				null, null, ((Expr.Box) expr).reifitnedi.column, ((Expr.Box) expr).reifitnedi.line,
//				((Expr.Box) expr).reifitnedi.start, ((Expr.Box) expr).reifitnedi.finish);

		environment.define(identifier.lexeme, null, null);
		environment.define(reifitnedi.lexeme, null, null);
		environment.define(((Expr.Box) expr).identifier.lexeme, ((Expr.Box) expr).identifier, null);
		environment.define(((Expr.Box) expr).reifitnedi.lexeme, ((Expr.Box) expr).reifitnedi, null);

		if (((Expr.Box) expr).identifier.identifierToken != null) {
			environment = new Environment(environment);

		}

		ArrayList<Object> primarys = new ArrayList<>();
		for (Expr stmt : ((Expr.Box) expr).expression) {
			primarys.add(stmt);
		}

		BoxClass boxClass = new BoxClass(((Expr.Box) expr).identifier.lexeme, (BoxClass) superclass, primarys, null,
				TokenType.POCKETCONTAINER, false, null, environment, expr, false);

		if (superclass != null) {
			environment = environment.enclosing;
		}
		Object call = boxClass.call(this, new ArrayList<Object>());
		environment.assign(identifier, null, call);
		environment.assign(reifitnedi, null, call);
		environment.assign(((Expr.Box) expr).identifier, null, boxClass);
		environment.assign(((Expr.Box) expr).reifitnedi, null, boxClass);
		return call;

	}

	private Object buildPocketClass(Expr expr, Object superclass) {
		if (((Expr.Pocket) expr).identifier.identifierToken != null) {
			superclass = lookUpVariableByName(new Variable(((Expr.Pocket) expr).identifier.identifierToken));

			if (!(superclass instanceof BoxClass)) {
				throw new RuntimeError(((Expr.Pocket) expr).identifier.identifierToken, "Superclass must be a class.");
			}

		}
		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Pocket) expr).identifier.lexeme, null, null, null,
				((Expr.Pocket) expr).identifier.column, ((Expr.Pocket) expr).identifier.line,
				((Expr.Pocket) expr).identifier.start, ((Expr.Pocket) expr).identifier.finish);
		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Pocket) expr).reifitnedi.lexeme, null, null, null,
				((Expr.Pocket) expr).reifitnedi.column, ((Expr.Pocket) expr).reifitnedi.line,
				((Expr.Pocket) expr).reifitnedi.start, ((Expr.Pocket) expr).reifitnedi.finish);
//		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Pocket) expr).identifier.lexeme + "varravargssgra",
//				null, null, null, ((Expr.Pocket) expr).identifier.column, ((Expr.Pocket) expr).identifier.line,
//				((Expr.Pocket) expr).identifier.start, ((Expr.Pocket) expr).identifier.finish);
//		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Pocket) expr).reifitnedi.lexeme + "varravargssgra",
//				null, null, null, ((Expr.Pocket) expr).reifitnedi.column, ((Expr.Pocket) expr).reifitnedi.line,
//				((Expr.Pocket) expr).reifitnedi.start, ((Expr.Pocket) expr).reifitnedi.finish);

		environment.define(identifier.lexeme, null, null);
		environment.define(reifitnedi.lexeme, null, null);
		environment.define(((Expr.Pocket) expr).identifier.lexeme, ((Expr.Pocket) expr).identifier, null);
		environment.define(((Expr.Pocket) expr).reifitnedi.lexeme, ((Expr.Pocket) expr).reifitnedi, null);

		if (((Expr.Pocket) expr).identifier.identifierToken != null) {
			environment = new Environment(environment);

		}

		ArrayList<Object> primarys = new ArrayList<>();
		for (Stmt stmt : ((Expr.Pocket) expr).expression) {
			primarys.add(stmt);
		}

		BoxClass boxClass = new BoxClass(((Pocket) expr).identifier.lexeme, (BoxClass) superclass, primarys, null,
				TokenType.POCKETCONTAINER, false, null, environment, expr, false);

		if (superclass != null) {
			environment = environment.enclosing;
		}

		Object call = boxClass.call(this, new ArrayList<Object>());
		if (call instanceof PocketInstance && ((Expr.Pocket) expr).lifetime != null) {
			((PocketInstance) call).setLifetime(((Expr.Pocket) expr).lifetime);
		}
		environment.assign(identifier, null, call);
		environment.assign(reifitnedi, null, call);
		environment.assign(((Pocket) expr).identifier, null, boxClass);
		environment.assign(((Pocket) expr).reifitnedi, null, boxClass);
		return call;

	}

	private Token buildCupClass(Expr expr, Object superclass) {
		if (((Expr.Cup) expr).identifier.identifierToken != null) {
			superclass = lookUpVariableByName(new Variable(((Expr.Cup) expr).identifier.identifierToken));

			if (!(superclass instanceof BoxClass)) {
				throw new RuntimeError(((Expr.Cup) expr).identifier.identifierToken, "Superclass must be a class.");
			}

		}

		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Cup) expr).identifier.lexeme, null, null, null,
				((Expr.Cup) expr).identifier.column, ((Expr.Cup) expr).identifier.line,
				((Expr.Cup) expr).identifier.start, ((Expr.Cup) expr).identifier.finish);
		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Cup) expr).reifitnedi.lexeme, null, null, null,
				((Expr.Cup) expr).reifitnedi.column, ((Expr.Cup) expr).reifitnedi.line,
				((Expr.Cup) expr).reifitnedi.start, ((Expr.Cup) expr).reifitnedi.finish);
//		Token identifier = new Token(TokenType.IDENTIFIER, ((Expr.Cup) expr).identifier.lexeme + "varravargssgra",
//				null, null, null, ((Expr.Cup) expr).identifier.column, ((Expr.Cup) expr).identifier.line,
//				((Expr.Cup) expr).identifier.start, ((Expr.Cup) expr).identifier.finish);
//		Token reifitnedi = new Token(TokenType.IDENTIFIER, ((Expr.Cup) expr).reifitnedi.lexeme + "varravargssgra",
//				null, null, null, ((Expr.Cup) expr).reifitnedi.column, ((Expr.Cup) expr).reifitnedi.line,
//				((Expr.Cup) expr).reifitnedi.start, ((Expr.Cup) expr).reifitnedi.finish);

		environment.define(identifier.lexeme, null, null);
		environment.define(reifitnedi.lexeme, null, null);
		environment.define(((Expr.Cup) expr).identifier.lexeme, ((Expr.Cup) expr).identifier, null);
		environment.define(((Expr.Cup) expr).reifitnedi.lexeme, ((Expr.Cup) expr).reifitnedi, null);

		if (((Expr.Cup) expr).identifier.identifierToken != null) {
			environment = new Environment(environment);

		}

		Map<String, BoxFunction> methods = new HashMap<>();
		for (Declaration method : ((Expr.Cup) expr).expression) {
			if (method instanceof Declaration.FunDecl) {
				if (((Function) ((Declaration.FunDecl) method).function).forwardIdentifier != null) {
					String fname = ((Function) ((Declaration.FunDecl) method).function).forwardIdentifier.lexeme;
					Expr body = ((Function) ((Declaration.FunDecl) method).function).sharedCup;
					List<Token> fparamtypes = ((Function) ((Declaration.FunDecl) method).function).forwardPrametersType;
					List<Token> fparamsNames = ((Function) ((Declaration.FunDecl) method).function).forwardPrametersNames;
					BoxFunction boxFunction1 = new BoxFunction(body, fname, fparamtypes, fparamsNames, environment,
							true, false);
					methods.put(fname, boxFunction1);
				}
				if (((Function) ((Declaration.FunDecl) method).function).backwardIdentifier != null) {
					String bname = ((Function) ((Declaration.FunDecl) method).function).backwardIdentifier.lexeme;
					Expr body = ((Function) ((Declaration.FunDecl) method).function).sharedCup;
					List<Token> bparamtypes = ((Function) ((Declaration.FunDecl) method).function).backwardPrametersType;
					List<Token> bparamsNames = ((Function) ((Declaration.FunDecl) method).function).backwardPrametersNames;

					BoxFunction boxFunction0 = new BoxFunction(body, bname, bparamtypes, bparamsNames, environment,
							false, false);
					methods.put(bname, boxFunction0);

				}
			}
		}

		ArrayList<Object> primarys = new ArrayList<>();
		for (Declaration declaration : ((Expr.Cup) expr).expression) {

			primarys.add(declaration);
		}

		BoxClass boxClass = new BoxClass(((Expr.Cup) expr).identifier.lexeme, (BoxClass) superclass, primarys, methods,
				TokenType.CUPCONTAINER, false, null, environment, expr, false);
		for (BoxFunction fn : methods.values()) { if (fn.ownerClass == null) fn.ownerClass = boxClass; }

		if (superclass != null) {
			environment = environment.enclosing;
		}

		Object call = boxClass.call(this, new ArrayList<Object>());
		environment.assign(identifier, null, call);
		environment.assign(reifitnedi, null, call);
		environment.assign(((Expr.Cup) expr).identifier, null, boxClass);
		environment.assign(((Expr.Cup) expr).reifitnedi, null, boxClass);
		return ((Expr.Cup) expr).identifier;
	}

	@Override
	public Object visitFunctionLinkFun(FunctionLink fun) {
		if (fun.forwardIdentifier != null) {
			environment.define(fun.forwardIdentifier.lexeme, fun.forwardIdentifier, null);
			BoxFunction fwd = new BoxFunction(null, fun.forwardIdentifier.lexeme,
					fun.forwardPrametersType, fun.forwardPrametersNames, environment, true, true);
			environment.assign(fun.forwardIdentifier, null, fwd);
		}
		if (fun.backwardIdentifier != null) {
			environment.define(fun.backwardIdentifier.lexeme, fun.backwardIdentifier, null);
			BoxFunction bwd = new BoxFunction(null, fun.backwardIdentifier.lexeme,
					fun.backwardPrametersType, fun.backwardPrametersNames, environment, false, true);
			environment.assign(fun.backwardIdentifier, null, bwd);
		}
		return null;
	}

	@Override
	public Object visitLinkExpr(Link expr) {
		buildClass(expr);
		return null;
	}

	@Override
	public Object visitInferExpr(Expr.Infer expr) {
		Object val = evaluate(expr.value);
		String str = inferenceStringify(val);
		if (expr.isBackward) str = new StringBuilder(str).reverse().toString();
		return runInferenceMatcher(str);
	}

	private String inferenceStringify(Object val) {
		if (val instanceof String) return (String) val;
		if (val instanceof Double) {
			double d = (Double) val;
			java.math.BigDecimal bd = java.math.BigDecimal.valueOf(d).stripTrailingZeros();
			String s = bd.toPlainString();
			return s;
		}
		if (val instanceof Integer) return String.valueOf(val);
		if (val instanceof BoxInstance) {
			List<Object> body = ((BoxInstance) val).body;
			if (!body.isEmpty() && body.get(0) instanceof String)
				return (String) body.get(0);
		}
		return String.valueOf(val);
	}

	private BoxInstance runInferenceMatcher(String value) {
		for (UserTypeEntry type : userTypeRegistry) {
			BoxInstance result = tryMatchType(value, type.slots);
			if (result != null) return result;
		}
		return makeGroup(value);
	}

	private BoxInstance tryMatchType(String value, java.util.List<Expr.SlotDescriptor> slots) {
		int[] pos = {0};
		java.util.List<BoxInstance> groups = new java.util.ArrayList<>();
		for (int i = 0; i < slots.size(); i++) {
			BoxInstance group = matchSlot(value, pos, slots.get(i), slots, i);
			if (group == null) return null;
			groups.add(group);
		}
		if (pos[0] != value.length()) return null;
		java.util.List<Object> outerBody = new java.util.ArrayList<>(groups);
		return new BoxInstance(null, outerBody, null, this);
	}

	private BoxInstance matchSlot(String v, int[] pos, Expr.SlotDescriptor slot,
			java.util.List<Expr.SlotDescriptor> allSlots, int slotIdx) {
		boolean optional = slot.mult == Expr.SlotDescriptor.Multiplicity.OPT_ONE
				|| slot.mult == Expr.SlotDescriptor.Multiplicity.OPT_MANY;
		boolean many = slot.mult == Expr.SlotDescriptor.Multiplicity.MANY
				|| slot.mult == Expr.SlotDescriptor.Multiplicity.OPT_MANY;
		if (slot.literalSlot) {
			String lit = slot.values.isEmpty() ? "" : slot.values.get(0);
			if (pos[0] + lit.length() <= v.length() && v.startsWith(lit, pos[0])) {
				pos[0] += lit.length();
				return makeGroup(lit);
			}
			if (optional) return makeEmptyGroup();
			return null;
		}
		if (many) return matchMany(v, pos, slot, allSlots, slotIdx, optional);
		return matchOne(v, pos, slot, optional);
	}

	private BoxInstance matchOne(String v, int[] pos, Expr.SlotDescriptor slot, boolean optional) {
		if (slot.values.isEmpty()) {
			if (pos[0] < v.length()) {
				pos[0]++;
				return makeGroup(String.valueOf(v.charAt(pos[0] - 1)));
			}
			if (optional) return makeEmptyGroup();
			return null;
		}
		for (String val : slot.values) {
			if (!val.isEmpty() && v.startsWith(val, pos[0])) {
				pos[0] += val.length();
				return makeGroup(val);
			}
		}
		if (slot.values.contains("")) return makeGroup("");
		if (optional) return makeEmptyGroup();
		return null;
	}

	private BoxInstance matchMany(String v, int[] pos, Expr.SlotDescriptor slot,
			java.util.List<Expr.SlotDescriptor> allSlots, int slotIdx, boolean optional) {
		String anchor = null;
		for (int j = slotIdx + 1; j < allSlots.size(); j++) {
			Expr.SlotDescriptor next = allSlots.get(j);
			if (next.literalSlot && !next.values.isEmpty()) {
				anchor = next.values.get(0);
				break;
			}
		}
		int end;
		if (anchor != null) {
			int anchorIdx = v.indexOf(anchor, pos[0]);
			if (anchorIdx < 0) {
				if (optional) return makeEmptyGroup();
				return null;
			}
			end = anchorIdx;
		} else {
			end = v.length();
		}
		StringBuilder sb = new StringBuilder();
		for (int k = pos[0]; k < end; k++) {
			String ch = String.valueOf(v.charAt(k));
			if (slot.values.isEmpty() || slot.values.contains(ch)) {
				sb.append(ch);
			} else {
				if (optional) return makeEmptyGroup();
				return null;
			}
		}
		if (sb.length() == 0 && !optional) return null;
		pos[0] = end;
		return sb.length() == 0 ? makeEmptyGroup() : makeGroup(sb.toString());
	}

	private BoxInstance makeGroup(String value) {
		java.util.List<Object> body = new java.util.ArrayList<>();
		body.add(value);
		return new BoxInstance(null, body, null, this);
	}

	private BoxInstance makeEmptyGroup() {
		return new BoxInstance(null, new java.util.ArrayList<>(), null, this);
	}

	@Override
	public Object visitUserTypeExpr(Expr.UserType expr) {
		java.util.List<Expr.SlotDescriptor> slots = parseSlotDescriptors(expr.rawSlotTokens);
		validateAnchorRule(slots, expr.typeName.lexeme);
		expr.slots = slots;
		java.util.List<String> linkNames = new java.util.ArrayList<>();
		for (Token lt : expr.linkNames)
			linkNames.add((String) lt.lexeme);
		String templateName = expr.templateName != null ? (String) expr.templateName.lexeme : null;
		UserTypeEntry entry = new UserTypeEntry((String) expr.typeName.lexeme, linkNames, templateName, slots);
		userTypeRegistry.add(entry);
		// if template was already declared before this type, resolve immediately
		if (templateName != null) {
			Object registered = globals.get(
					new Token(TokenType.IDENTIFIER, templateName, templateName, null, null, -1,-1,-1,-1), false);
			if (registered instanceof BoxClass)
				entry.resolvedTemplate = (BoxClass) registered;
		}
		return null;
	}

	private static boolean isWhitespace(Token t) {
		return t.type == TokenType.SPACE || t.type == TokenType.NEWLINE
				|| t.type == TokenType.TAB || t.type == TokenType.SPACERETURN;
	}

	private java.util.List<Expr.SlotDescriptor> parseSlotDescriptors(java.util.List<Token> raw) {
		java.util.List<Expr.SlotDescriptor> result = new java.util.ArrayList<>();
		int i = 0;
		while (i < raw.size()) {
			// skip whitespace and commas between slots
			if (isWhitespace(raw.get(i)) || raw.get(i).type == TokenType.COMMA) { i++; continue; }
			// each slot starts with @categoryFwd
			if (raw.get(i).type != TokenType.AT)
				throw new RuntimeError(null, "expected '@' at start of slot descriptor");
			i++; // consume @
			while (i < raw.size() && isWhitespace(raw.get(i))) i++;
			if (i >= raw.size() || raw.get(i).type != TokenType.IDENTIFIER)
				throw new RuntimeError(null, "expected category name after '@'");
			String category = (String) raw.get(i).lexeme;
			i++; // consume category name
			while (i < raw.size() && isWhitespace(raw.get(i))) i++;
			// parse multiplicity and slot kind
			boolean optional = false;
			if (i < raw.size() && raw.get(i).type == TokenType.TIMES) {
				optional = true;
				i++;
				while (i < raw.size() && isWhitespace(raw.get(i))) i++;
			}
			if (i >= raw.size() || raw.get(i).type != TokenType.DOLLAR)
				throw new RuntimeError(null, "expected '$' in slot pattern for category " + category);
			i++; // consume first $
			boolean many = false;
			if (i < raw.size() && raw.get(i).type == TokenType.DOLLAR) {
				many = true;
				i++; // consume second $
			}
			while (i < raw.size() && isWhitespace(raw.get(i))) i++;
			// literal slot: $"value"
			if (!many && i < raw.size() && raw.get(i).type == TokenType.STRING) {
				String litVal = (String) raw.get(i).literal;
				i++;
				// consume closing category and @
				i = consumeClosingCategory(raw, i, category);
				Expr.SlotDescriptor.Multiplicity mult = optional
						? Expr.SlotDescriptor.Multiplicity.OPT_ONE
						: Expr.SlotDescriptor.Multiplicity.ONE;
				java.util.List<String> vals = new java.util.ArrayList<>();
				vals.add(litVal);
				result.add(new Expr.SlotDescriptor(category, mult, true, false, vals));
				continue;
			}
			// value slot: must be UNDERSCORE
			if (i >= raw.size() || raw.get(i).type != TokenType.UNDERSCORE)
				throw new RuntimeError(null, "expected '_' or string literal after '$' for category " + category);
			i++; // consume _
			while (i < raw.size() && isWhitespace(raw.get(i))) i++;
			// optional constraint: ^[...] or ^X[...] or ^x[...]
			boolean exclusiveOr = false;
			java.util.List<String> values = new java.util.ArrayList<>();
			if (i < raw.size() && raw.get(i).type == TokenType.POWER) {
				i++; // consume ^
				while (i < raw.size() && isWhitespace(raw.get(i))) i++;
				if (i < raw.size() && raw.get(i).type == TokenType.IDENTIFIER) {
					String xMarker = ((String) raw.get(i).lexeme).toLowerCase();
					if (xMarker.equals("x")) {
						exclusiveOr = true;
						i++;
						while (i < raw.size() && isWhitespace(raw.get(i))) i++;
					}
				}
				if (i >= raw.size() || raw.get(i).type != TokenType.OPENSQUARE)
					throw new RuntimeError(null, "expected '[' after '^' in slot constraint for category " + category);
				i++; // consume [
				int depth = 1;
				while (i < raw.size() && depth > 0) {
					if (isWhitespace(raw.get(i))) { i++; continue; }
					if (raw.get(i).type == TokenType.OPENSQUARE) { depth++; i++; continue; }
					if (raw.get(i).type == TokenType.CLOSEDSQUARE) {
						depth--;
						if (depth == 0) { i++; break; }
						i++; continue;
					}
					if (raw.get(i).type == TokenType.COMMA) { i++; continue; }
					if (raw.get(i).type == TokenType.STRING) {
						values.add((String) raw.get(i).literal);
						i++;
					} else {
						throw new RuntimeError(null, "expected string value in slot constraint for category " + category);
					}
				}
			}
			// consume closing category and @
			i = consumeClosingCategory(raw, i, category);
			Expr.SlotDescriptor.Multiplicity mult;
			if (optional) {
				mult = many ? Expr.SlotDescriptor.Multiplicity.OPT_MANY : Expr.SlotDescriptor.Multiplicity.OPT_ONE;
			} else {
				mult = many ? Expr.SlotDescriptor.Multiplicity.MANY : Expr.SlotDescriptor.Multiplicity.ONE;
			}
			result.add(new Expr.SlotDescriptor(category, mult, false, exclusiveOr, values));
		}
		return result;
	}

	private int consumeClosingCategory(java.util.List<Token> raw, int i, String openCategory) {
		while (i < raw.size() && isWhitespace(raw.get(i))) i++;
		if (i < raw.size() && raw.get(i).type == TokenType.IDENTIFIER) i++; // closing name
		while (i < raw.size() && isWhitespace(raw.get(i))) i++;
		if (i < raw.size() && raw.get(i).type == TokenType.AT) i++; // closing @
		return i;
	}

	Object getatByCategory(BoxInstance box, String catName, Token operator) {
		if (typeContextStack.isEmpty())
			throw new RuntimeError(operator,
					"category-name access '" + catName + "' requires a type-aware context");
		UserTypeEntry entry = typeContextStack.peek();
		for (int i = 0; i < entry.slots.size(); i++) {
			if (catName.equals(entry.slots.get(i).category)) {
				if (i >= box.body.size())
					throw new RuntimeError(operator, "group index " + i + " out of bounds for category '" + catName + "'");
				return box.body.get(i);
			}
		}
		throw new RuntimeError(operator,
				"no category '" + catName + "' in current type '" + entry.typeName + "'");
	}

	Object tategByCategory(BoxInstance box, String catName, Token operator) {
		String fwd = new StringBuilder(catName).reverse().toString();
		return getatByCategory(box, fwd, operator);
	}

	private void resolveTemplateInRegistry(String templateName) {
		Object registered = globals.get(
				new Token(TokenType.IDENTIFIER, templateName, templateName, null, null, -1,-1,-1,-1), false);
		if (!(registered instanceof BoxClass)) return;
		BoxClass bc = (BoxClass) registered;
		for (UserTypeEntry entry : userTypeRegistry) {
			if (entry.resolvedTemplate == null && templateName.equals(entry.templateName))
				entry.resolvedTemplate = bc;
		}
	}

	private void validateAnchorRule(java.util.List<Expr.SlotDescriptor> slots, String typeName) {
		for (int i = 0; i < slots.size() - 1; i++) {
			Expr.SlotDescriptor a = slots.get(i);
			Expr.SlotDescriptor b = slots.get(i + 1);
			boolean aMulti = a.mult == Expr.SlotDescriptor.Multiplicity.MANY
					|| a.mult == Expr.SlotDescriptor.Multiplicity.OPT_MANY;
			boolean bMulti = b.mult == Expr.SlotDescriptor.Multiplicity.MANY
					|| b.mult == Expr.SlotDescriptor.Multiplicity.OPT_MANY;
			if (aMulti && bMulti && !a.literalSlot && !b.literalSlot)
				throw new RuntimeError(null,
						"type '" + typeName + "': adjacent $$_ groups with no literal anchor between them");
		}
	}

	@Override
	public Object visitPocketOpenExpr(PocketOpen expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPocketClosedExpr(PocketClosed expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitCupOpenExpr(CupOpen expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitCupClosedExpr(CupClosed expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBoxOpenExpr(BoxOpen expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBoxClosedExpr(BoxClosed expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFlowFwdExpr(Expr.FlowFwd expr) {
		Flow.BracketType bt = bracketTypeFromToken(expr.bracket);
		return new Flow(Flow.Direction.FORWARD, bt, 1);
	}

	@Override
	public Object visitFlowBwdExpr(Expr.FlowBwd expr) {
		Flow.BracketType bt = bracketTypeFromToken(expr.bracket);
		return new Flow(Flow.Direction.BACKWARD, bt, 1);
	}

	private Flow.BracketType bracketTypeFromToken(Token t) {
		switch (t.type) {
			case OPENPAREN:  case CLOSEDPAREN:  return Flow.BracketType.PAREN;
			case OPENBRACE:  case CLOSEDBRACE:  return Flow.BracketType.BRACE;
			case OPENSQUARE: case CLOSEDSQUARE: return Flow.BracketType.SQUARE;
			default: return Flow.BracketType.PAREN;
		}
	}

	@Override
	public Object visitSaveevasStmt(Saveevas stmt) {
		if (forward) {
			try {
				String filePathAndName = (String) evaluate(stmt.filePathFileName);
				String[] split = filePathAndName.split("/");
				String folderPath = "";
				if (split[split.length - 1].contains(".")) {
					for (int i = 0; i < split.length - 1; i++) {
						folderPath += split[i] + "/";
					}
				} else {
					for (int i = 0; i < split.length; i++) {
						folderPath += split[i] + "/";
					}
				}
				folderPath = folderPath.substring(0, folderPath.length() - 1);

				String str = "";
				Object boxInstance = evaluate(stmt.objecttosave);
				if (boxInstance != null) {
					str = boxInstance.toString();

					BufferedWriter writer = new BufferedWriter(new FileWriter(filePathAndName));
					writer.write(str);

					writer.close();
				}

			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		} else {

			try {
				String filePathAndName = (String) evaluate(stmt.filePathFileName);
				String[] split = filePathAndName.split("/");
				String folderPath = "";
				if (split[split.length - 1].contains(".")) {
					for (int i = 0; i < split.length - 1; i++) {
						folderPath += split[i] + "/";
					}
				} else {
					for (int i = 0; i < split.length; i++) {
						folderPath += split[i] + "/";
					}
				}
				folderPath = folderPath.substring(0, folderPath.length() - 1);

				String str = "";
				Object boxInstance = evaluate(stmt.objecttosave);
				if (boxInstance != null) {
					str = boxInstance.toString();

					BufferedWriter writer = new BufferedWriter(new FileWriter(filePathAndName));
					writer.write(str);

					writer.close();
				}

			} catch (IOException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}

		}

		return null;
	}

	@Override
	public Object visitReaddaerStmt(Readdaer stmt) {
		if (forward) {
			try {
				File myObj = new File((String) evaluate(stmt.filePath));
				java.util.Scanner myReader = new java.util.Scanner(myObj);
				String data = "";
				while (myReader.hasNextLine()) {
					data += myReader.nextLine();

				}

				if (stmt.objectToReadInto instanceof Expr.Variable) {
					Object value = evaluate(new Expr.Literal(data));
					Integer distance = locals.get((Expr.Variable) stmt.objectToReadInto);
					if (distance != null)
						environment.assignAt(distance, ((Expr.Variable) stmt.objectToReadInto).name, value, value,
								this);
					else
						globals.assign(((Expr.Variable) stmt.objectToReadInto).name, value, value, this);
				}

				myReader.close();
			} catch (FileNotFoundException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		} else {
			try {
				File myObj = new File((String) evaluate(stmt.filePath));
				java.util.Scanner myReader = new java.util.Scanner(myObj);
				String data = "";
				while (myReader.hasNextLine()) {
					data += myReader.nextLine();

				}

				if (stmt.objectToReadInto instanceof Expr.Variable) {
					Object value = evaluate(new Expr.Literal(data));
					Integer distance = locals.get((Expr.Variable) stmt.objectToReadInto);
					if (distance != null)
						environment.assignAt(distance, ((Expr.Variable) stmt.objectToReadInto).name, value, value,
								this);
					else
						globals.assign(((Expr.Variable) stmt.objectToReadInto).name, value, value, this);
				}

				myReader.close();
			} catch (FileNotFoundException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}

		}
		return null;

	}

	@Override
	public Object visitRenameemanerStmt(Renameemaner stmt) {
		File file = new File((String) evaluate(stmt.filePathAndName));

		if (file.renameTo(new File((String) evaluate(stmt.filenewname)))) {
			System.out.println("File Renamed successfully");
		} else {
			System.out.println("Failed to Rename the file");
		}
		return null;
	}

	@Override
	public Object visitMoveevomStmt(Moveevom stmt) {

		File file = new File((String) evaluate(stmt.OringialfilePathAndFile));

		if (file.renameTo(new File((String) evaluate(stmt.newfilePath)))) {
			file.delete();
			System.out.println("File moved successfully");
		} else {
			System.out.println("Failed to move the file");
		}

		return null;
	}

	@Override
	public Object visitMonoonomExpr(Monoonom expr) {
		if (forward) {
			return findMono(expr);
		} else {
			return findOnom(expr);
		}
	}

	@Override
	public Object visitBinaryyranibExpr(Binaryyranib expr) {
		Object left = null;
		Object right = null;

		if (expr.left instanceof Pocket || expr.left instanceof Cup) {
			left = evaluate(expr.left);
			right = evaluate(expr.right);
		} else if (expr.right instanceof Pocket || expr.right instanceof Cup) {
			right = evaluate(expr.right);
			left = evaluate(expr.left);
		} else {
			left = evaluate(expr.left);
			right = evaluate(expr.right);
		}

		left = parseBinData(left);
		right = parseBinData(right);
		Object forwardReturn = null;
		switch (expr.operatorForward.type) {
		case NOTEQUALS:
			forwardReturn = !isEqual(left, right);
		case EQUALSNOT:
			forwardReturn = !isEqual(left, right);
		case EQUALSEQUALS:
			forwardReturn = isEqual(left, right);

		case GREATERTHEN:
			forwardReturn = greaterthen(left, right);
		case GREATERTHENEQUAL:
			forwardReturn = greaterequalthen(left, right);
		case LESSTHEN:
			forwardReturn = lessthen(left, right);
		case LESSTHENEQUAL:
			forwardReturn = lessequalthen(left, right);
		case EQUALLESSTHEN:
			forwardReturn = greaterequalthen(left, right);
		case EQUALGREATERTHEN:
			forwardReturn = lessequalthen(left, right);

		case MINUS:

			forwardReturn = sub(left, right);
		case EQUALSMINUS:
			forwardReturn = sub(left, right, expr.left, expr.operatorForward);
		case MINUSEQUALS:
			forwardReturn = sub(left, right, expr.right, expr.operatorForward);

		case EQUALSPLUS:
			forwardReturn = add(left, right, expr.left, expr.operatorForward);
		case PLUSEQUALS:
			forwardReturn = add(left, right, expr.right, expr.operatorForward);
		case PLUS:
			forwardReturn = add(left, right, expr.left, expr.right);
		case MOD:
			forwardReturn = mod(left, right);
		case MODEQUAL:
			forwardReturn = mod(left, right, expr.right, expr.operatorForward);
		case EQUALMOD:
			forwardReturn = mod(left, right, expr.left, expr.operatorForward);

		case FORWARDSLASH:
			forwardReturn = div(left, right);
		case BACKSLASH:
			Object div = div(right, left);
			forwardReturn = div;
		case EQUALDIVIDEFORWARD:
			forwardReturn = div(right, left, expr.left, expr.operatorForward);
		case EQUALDIVIDEBACKWARD:
			forwardReturn = div(right, left, expr.right, expr.operatorForward);

		case TIMES:
			forwardReturn = times(left, right);
		case TIMESEQUAL:
			forwardReturn = times(left, right, expr.right, expr.operatorForward);
		case EQUALTIMES:
			forwardReturn = times(left, right, expr.left, expr.operatorForward);

		case POWER:
			forwardReturn = power(left, right);
		case EQUALPOWER:
			forwardReturn = power(left, right, expr.left, expr.operatorForward);
		case POWEREQUAL:
			forwardReturn = power(left, right, expr.right, expr.operatorForward);
		case YROOT:
			forwardReturn = null;
		case TOORY:
			forwardReturn = null;

		case DNA:
			if (!forward)
				forwardReturn = and(left, right);
			else
				forwardReturn = null;
		case AND:
			if (forward)
				forwardReturn = and(left, right);
			else
				forwardReturn = null;
		case RO:
			if (!forward)
				forwardReturn = or(left, right);
			else
				forwardReturn = false;
		case OR:
			if (forward)
				forwardReturn = or(left, right);
			else
				forwardReturn = false;
		default:
			forwardReturn = null;
		}

		Object backwardReturn = null;
		switch (expr.operatorForward.type) {
		case NOTEQUALS:
			backwardReturn = !isEqual(left, right);
		case EQUALSNOT:
			backwardReturn = !isEqual(left, right);
		case EQUALSEQUALS:
			backwardReturn = isEqual(left, right);

		case GREATERTHEN:
			backwardReturn = greaterthen(left, right);
		case GREATERTHENEQUAL:
			backwardReturn = greaterequalthen(left, right);
		case LESSTHEN:
			backwardReturn = lessthen(left, right);
		case LESSTHENEQUAL:
			backwardReturn = lessequalthen(left, right);
		case EQUALLESSTHEN:
			backwardReturn = greaterequalthen(left, right);
		case EQUALGREATERTHEN:
			backwardReturn = lessequalthen(left, right);

		case MINUS:

			backwardReturn = sub(left, right);
		case EQUALSMINUS:
			backwardReturn = sub(left, right, expr.left, expr.operatorBackward);
		case MINUSEQUALS:
			backwardReturn = sub(left, right, expr.right, expr.operatorBackward);

		case EQUALSPLUS:
			backwardReturn = add(left, right, expr.left, expr.operatorBackward);
		case PLUSEQUALS:
			backwardReturn = add(left, right, expr.right, expr.operatorBackward);
		case PLUS:
			backwardReturn = add(left, right, expr.left, expr.right);
		case MOD:
			backwardReturn = mod(left, right);
		case MODEQUAL:
			backwardReturn = mod(left, right, expr.right, expr.operatorBackward);
		case EQUALMOD:
			backwardReturn = mod(left, right, expr.left, expr.operatorBackward);

		case FORWARDSLASH:
			backwardReturn = div(left, right);
		case BACKSLASH:
			Object div = div(right, left);
			backwardReturn = div;
		case EQUALDIVIDEFORWARD:
			backwardReturn = div(right, left, expr.left, expr.operatorBackward);
		case EQUALDIVIDEBACKWARD:
			backwardReturn = div(right, left, expr.right, expr.operatorBackward);

		case TIMES:
			backwardReturn = times(left, right);
		case TIMESEQUAL:
			backwardReturn = times(left, right, expr.right, expr.operatorBackward);
		case EQUALTIMES:
			backwardReturn = times(left, right, expr.left, expr.operatorBackward);

		case POWER:
			backwardReturn = power(left, right);
		case EQUALPOWER:
			backwardReturn = power(left, right, expr.left, expr.operatorBackward);
		case POWEREQUAL:
			backwardReturn = power(left, right, expr.right, expr.operatorBackward);
		case YROOT:
			backwardReturn = null;
		case TOORY:
			backwardReturn = null;

		case DNA:
			if (!forward)
				backwardReturn = and(left, right);
			else
				backwardReturn = null;
		case AND:
			if (forward)
				backwardReturn = and(left, right);
			else
				backwardReturn = null;
		case RO:
			if (!forward)
				backwardReturn = or(left, right);
			else
				backwardReturn = false;
		case OR:
			if (forward)
				backwardReturn = or(left, right);
			else
				backwardReturn = false;
		default:
			backwardReturn = null;
		}

		ArrayList<Declaration> expression = new ArrayList<>();
		if (forwardReturn instanceof Expr)
			expression.add((Expr) forwardReturn);
		else if (forwardReturn instanceof Stmt)
			expression.add((Stmt) forwardReturn);
		else if (forwardReturn instanceof Parser.Fun)
			expression.add((Parser.Fun) forwardReturn);
		else if (forwardReturn instanceof Declaration)
			expression.add((Declaration) forwardReturn);
		else if (forwardReturn instanceof Integer)
			expression.add(new Expr.Literal(forwardReturn));
		else if (forwardReturn instanceof Boolean)
			expression.add(new Expr.Literal(forwardReturn));

		if (backwardReturn instanceof Expr)
			expression.add((Expr) backwardReturn);
		else if (backwardReturn instanceof Stmt)
			expression.add((Stmt) backwardReturn);
		else if (backwardReturn instanceof Parser.Fun)
			expression.add((Parser.Fun) backwardReturn);
		else if (backwardReturn instanceof Declaration)
			expression.add((Declaration) backwardReturn);
		else if (backwardReturn instanceof Integer)
			expression.add(new Expr.Literal(backwardReturn));
		else if (backwardReturn instanceof Boolean)
			expression.add(new Expr.Literal(backwardReturn));
		BigInteger uniqueNum = generateUniqueNum();
		String reverse = reverse(uniqueNum.toString());
		Token identifier = new Token(TokenType.IDENTIFIER, uniqueNum.toString() + "cup", null, null, null, -1, -1, -1,
				-1);
		Token reifitnedi = new Token(TokenType.IDENTIFIER, "puc" + reverse, null, null, null, -1, -1, -1, -1);
		return new Expr.Cup(identifier, expression, "", reifitnedi);

	}

	private BigInteger generateUniqueNum() {

		Random rand = new Random();
		BigInteger upperLimit = new BigInteger("999999999");
		BigInteger result = new BigInteger(upperLimit.bitLength(), rand);
		result.add(upperLimit);
		while (identifiers.contains(result)) {
			rand = new Random();
			result = new BigInteger(upperLimit.bitLength(), rand);
			result.add(upperLimit);
		}
		this.identifiers.add(result);
		return result;
	}

	@Override
	public Object visitLoggolExpr(Loggol expr) {
		Double evaluateDoubleValue = findDoubleValue(expr.value) - 1;
		Double evaluateDoubleValueBase = findDoubleValue(expr.valueBase) - 1;

		return Math.log1p(evaluateDoubleValue) / Math.log1p(evaluateDoubleValueBase);

	}

	@Override
	public Object visitCallllacExpr(Callllac expr) {
		if (forward) {
			Object callee = evaluate(expr.calleeForward);
			List<Object> arguments = new ArrayList<>();
			for (Expr argument : expr.arguments) {
				arguments.add(evaluate(argument));
			}

			if (!(callee instanceof BoxCallable)) {
				throw new RuntimeError(expr.calleeTokenForward, "Can only call functions and classes.");
			}

			BoxCallable function = (BoxCallable) callee;
			if (arguments.size() != function.arity()) {
				throw new RuntimeError(expr.calleeTokenForward,
						"Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
			}

			return function.call(this, arguments);
		} else {
			Object callee = evaluate(expr.calleeBackward);
			List<Object> arguments = new ArrayList<>();
			for (Expr argument : expr.arguments) {
				arguments.add(evaluate(argument));
			}

			if (!(callee instanceof BoxCallable)) {
				throw new RuntimeError(expr.calleeTokenBackward, "Can only call functions and classes.");
			}

			BoxCallable function = (BoxCallable) callee;
			if (arguments.size() != function.arity()) {
				throw new RuntimeError(expr.calleeTokenBackward,
						"Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
			}

			return function.call(this, arguments);

		}

	}

	@Override
	public Object visitExpressiontmtsExpr(Expressiontmts expr) {
		if (forward) {
			Object result = findMono(expr);

			return result;
		} else {
			if (expr.tnemetatsToken.type == TokenType.TNIRP) {
				Object value = findMono(expr);
				return value;
			} else if (expr.tnemetatsToken.type == TokenType.NRUTER) {
				Object value = null;
				if (expr.expression != null)
					value = findMono(expr);
				throw new Snruter(value);
			}
		}
		return null;
	}

	boolean isAggContainerToken(TokenType t) {
		switch (t) {
			case SUM: case MUS: case PRODUCT: case TCUDORP:
			case MEAN: case NAEM: case SORT: case TROS:
			case MINOF: case FONIM: case MAXOF: case FOMAM:
			case FIRST: case TSRIF: case LAST: case TSAL:
			case FLIP: case PILF: case FLAT: case TALF:
				return true;
			default: return false;
		}
	}

	List<Object> getContainerBody(Object obj) {
		if (obj instanceof BoxInstance) return ((BoxInstance) obj).body;
		return null;
	}

	List<Double> numericItems(List<Object> body) {
		List<Double> nums = new java.util.ArrayList<>();
		for (Object item : body) {
			Object raw = (item instanceof BoxInstance && ((BoxInstance) item).body.size() == 1)
				? ((BoxInstance) item).body.get(0) : item;
			if (raw instanceof Double)  nums.add((Double) raw);
			else if (raw instanceof Integer) nums.add(((Integer) raw).doubleValue());
		}
		return nums;
	}

	BoxInstance makeResultBox(List<Object> items) {
		return new BoxInstance(null, new java.util.ArrayList<>(items), null, this);
	}

	Object applyContainerAggregate(List<Object> body, TokenType type, Token opToken) {
		switch (type) {
		case SUM: case MUS: {
			if (invertedMode) { double p = 1.0; for (double d : numericItems(body)) p *= d; return p; }
			double s = 0.0; for (double d : numericItems(body)) s += d; return s;
		}
		case PRODUCT: case TCUDORP: {
			if (invertedMode) { double s = 0.0; for (double d : numericItems(body)) s += d; return s; }
			double p = 1.0; for (double d : numericItems(body)) p *= d; return p;
		}
		case MEAN: case NAEM: {
			List<Double> nums = numericItems(body);
			if (nums.isEmpty()) return 0.0;
			double s = 0.0; for (double d : nums) s += d; return s / nums.size();
		}
		case SORT: case TROS: {
			List<Object> sorted = new java.util.ArrayList<>(body);
			boolean descending = invertedMode;
			sorted.sort((a, b) -> {
				boolean aNum = (a instanceof Double) || (a instanceof Integer);
				boolean bNum = (b instanceof Double) || (b instanceof Integer);
				if (aNum && bNum) {
					double da = (a instanceof Double) ? (Double) a : ((Integer) a).doubleValue();
					double db = (b instanceof Double) ? (Double) b : ((Integer) b).doubleValue();
					return descending ? Double.compare(db, da) : Double.compare(da, db);
				}
				if (aNum) return descending ? 1 : -1;
				if (bNum) return descending ? -1 : 1;
				String sa = stringify(a), sb = stringify(b);
				return descending ? sb.compareTo(sa) : sa.compareTo(sb);
			});
			return makeResultBox(sorted);
		}
		case MINOF: case FONIM: {
			List<Double> nums = numericItems(body);
			if (nums.isEmpty()) throw new RuntimeError(opToken, "minof: no numeric items");
			if (invertedMode) return nums.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
			return nums.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
		}
		case MAXOF: case FOMAM: {
			List<Double> nums = numericItems(body);
			if (nums.isEmpty()) throw new RuntimeError(opToken, "maxof: no numeric items");
			if (invertedMode) return nums.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
			return nums.stream().mapToDouble(Double::doubleValue).max().getAsDouble();
		}
		case FIRST: case TSRIF: {
			if (body.isEmpty()) return null;
			return invertedMode ? body.get(body.size() - 1) : body.get(0);
		}
		case LAST: case TSAL: {
			if (body.isEmpty()) return null;
			return invertedMode ? body.get(0) : body.get(body.size() - 1);
		}
		case FLIP: case PILF: {
			List<Object> flipped = new java.util.ArrayList<>(body);
			java.util.Collections.reverse(flipped);
			return makeResultBox(flipped);
		}
		case FLAT: case TALF: {
			List<Object> flat = new java.util.ArrayList<>();
			for (Object item : body) {
				if (item instanceof BoxInstance) flat.addAll(((BoxInstance) item).body);
				else flat.add(item);
			}
			return makeResultBox(flat);
		}
		default: return null;
		}
	}

	private Object applyStringOp(Object value, TokenType type, Token opToken) {
		switch (type) {
		case LEN: case NEL:
			return (double) stringify(value).length();
		case UPPER: case REPPU:
			return invertedMode ? stringify(value).toLowerCase() : stringify(value).toUpperCase();
		case LOWER: case REWOL:
			return invertedMode ? stringify(value).toUpperCase() : stringify(value).toLowerCase();
		case STRREV: case VERUTS:
			return new StringBuilder(stringify(value)).reverse().toString();
		case TRIM: case MIRT:
			return stringify(value).trim();
		case NUM: case MUN: {
			String s = stringify(value).trim();
			try { return Double.parseDouble(s); }
			catch (NumberFormatException e) {
				throw new RuntimeError(opToken, "num: cannot convert '" + s + "' to number");
			}
		}
		default:
			return null;
		}
	}

	private Object findMono(Expr expr) {
		Object evaluate = null;
		TokenType type = null;
		Token opToken = null;
		if (expr instanceof Expressiontmts) {
			type = ((Expressiontmts) expr).expressionToken.type;
			opToken = ((Expressiontmts) expr).expressionToken;
			evaluate = evaluate(((Expressiontmts) expr).expression);
		} else if (expr instanceof Monoonom) {
			evaluate = evaluate(((Monoonom) expr).value);
			type = ((Monoonom) expr).operatorForward.type;
			opToken = ((Monoonom) expr).operatorForward;
		} else if (expr instanceof Mono) {
			evaluate = evaluate(((Mono) expr).value);
			type = ((Mono) expr).operator.type;
			opToken = ((Mono) expr).operator;
		}

		// Vector/matrix ops — dispatch before scalar extraction
		if (evaluate instanceof BoxInstance) {
			Object vecResult = VecMatHelper.dispatchVecMono((BoxInstance) evaluate, type, opToken, invertedMode, this);
			if (vecResult != null) return vecResult;
		}
		// String ops — dispatch before numeric extraction
		Object strResult = applyStringOp(evaluate, type, opToken);
		if (strResult != null) return strResult;


		Double result = 0.0;
		Double evaluateDouble = 0.0;
		if (evaluate instanceof Integer) {
			evaluateDouble = Double.valueOf(Integer.toString((Integer) evaluate));
		} else if (evaluate instanceof Double) {
			evaluateDouble = (Double) evaluate;
		} else if (evaluate instanceof Bin) {
			evaluateDouble = ((Bin) evaluate).toDouble();

		} else if (evaluate instanceof String) {
			evaluateDouble = convertStringToDouble((String) evaluate);

		} else if (evaluate.getClass() == ArrayList.class) {
			evaluateDouble = ((ArrayList<Integer>) evaluate).get(0).doubleValue();

		} else if (evaluate instanceof BoxInstance) {
			evaluateDouble = ((Integer)((BoxInstance) evaluate).body.get(0)).doubleValue();	}

		switch (type) {
		case SIN:
			result = Math.sin(evaluateDouble);
			break;
		case COS:
			result = Math.cos(evaluateDouble);
			break;
		case TAN:
			result = Math.tan(evaluateDouble);
			break;

		case SINH:
			result = Math.sinh(evaluateDouble);
			break;

		case COSH:
			result = Math.cosh(evaluateDouble);
			break;

		case TANH:
			result = Math.tanh(evaluateDouble);
			break;
		case LN:
			result = Math.log1p(evaluateDouble - 1);
			break;
		case EXP:
			result = Math.exp(evaluateDouble);
			break;
		case FRESNELS:
			result = new MathInterpreter().S(BigDecimal.valueOf(evaluateDouble)).doubleValue();
			break;
		case FRESNELC:
			result = new MathInterpreter().C(BigDecimal.valueOf(evaluateDouble)).doubleValue();
			break;
		case ABS:   result = Math.abs(evaluateDouble);   break;
		case SQRT:  result = Math.sqrt(evaluateDouble);  break;
		case FLOOR: result = Math.floor(evaluateDouble); break;
		case CEIL:  result = Math.ceil(evaluateDouble);  break;
		case ROUND: result = (double) Math.round(evaluateDouble); break;
		case SIGN:  result = Math.signum(evaluateDouble); break;
		case ASIN:  result = Math.asin(evaluateDouble);  break;
		case ACOS:  result = Math.acos(evaluateDouble);  break;
		case ATAN:  result = Math.atan(evaluateDouble);  break;
		case ASINH: result = Math.log(evaluateDouble + Math.sqrt(evaluateDouble * evaluateDouble + 1)); break;
		case ACOSH: result = Math.log(evaluateDouble + Math.sqrt(evaluateDouble * evaluateDouble - 1)); break;
		case ATANH: result = 0.5 * Math.log((1 + evaluateDouble) / (1 - evaluateDouble)); break;
		case BNOT:  result = (double)(~(long)(double) evaluateDouble); break;

		default:
			break;
		}
		return result;
	}

	@Override
	public Object visitStmttmtSStmt(StmttmtS stmt) {
		if (forward) {
			if (stmt.keywordForward.type == TokenType.PRINT) {
				Object value = evaluate(stmt.expression);
				audio.onEvent(new InterpreterAudio.AudioEvent(TokenType.PRINT, stringify(value), 0.0, System.nanoTime()));
				System.out.print(stringify(value));
				return value;
			} else if (stmt.keywordForward.type == TokenType.RETURN) {
				Object value = null;
				if (stmt.expression != null)
					value = evaluate(stmt.expression);
				throw new Returns(value);
			}
		} else {
			if (stmt.keywordBackward.type == TokenType.TNIRP) {
				Object value = evaluate(stmt.expression);
				audio.onEvent(new InterpreterAudio.AudioEvent(TokenType.PRINT, stringify(value), 0.0, System.nanoTime()));
				System.out.print(stringify(value));
				return value;
			} else if (stmt.keywordBackward.type == TokenType.NRUTER) {
				Object value = null;
				if (stmt.expression != null)
					value = evaluate(stmt.expression);
				throw new Snruter(value);
			}
		}
		return null;
	}

	@Override
	public Object visitStmtnoisserpxeStmt(Parser.Stmt.Stmtnoisserpxe stmt) {
		if (forward) {
			if (stmt.statementToken.type == TokenType.PRINT) {
				Object value = findOnomStmt(stmt);
				return value;
			} else if (stmt.statementToken.type == TokenType.RETURN) {
				Object value = null;
				if (stmt.expression != null)
					value = findOnomStmt(stmt);
				throw new Returns(value);
			}
		} else {
			return findOnomStmt(stmt);
		}
		return null;
	}

	private Object findOnomStmt(Parser.Stmt.Stmtnoisserpxe stmt) {
		Double result = 0.0;
		Object evaluate = evaluate(stmt.expression);
		Double evaluateDouble = 0.0;
		if (evaluate instanceof Integer) {
			evaluateDouble = Double.valueOf(Integer.toString((Integer) evaluate));
		} else if (evaluate instanceof Double) {
			evaluateDouble = (Double) evaluate;
		} else if (evaluate instanceof Bin) {
			evaluateDouble = ((Bin) evaluate).toDouble();

		} else if (evaluate instanceof String) {
			evaluateDouble = convertStringToDouble((String) evaluate);

		}

		switch (stmt.noisserpxeToken.type) {
		case NIS:
			result = Math.sin(evaluateDouble);
			break;
		case SOC:
			result = Math.cos(evaluateDouble);
			break;
		case NAT:
			result = Math.tan(evaluateDouble);
			break;

		case HNIS:
			result = Math.sinh(evaluateDouble);
			break;

		case HSOC:
			result = Math.cosh(evaluateDouble);
			break;

		case HNAT:
			result = Math.tanh(evaluateDouble);
			break;

		default:
			break;
		}

		return result;
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public Object visitAssignmenttnemgissaExpr(Assignmenttnemgissa expr) {
		if (forward) {
			Object value = evaluate(expr.value);
			if (value == null) {
				Object current = environment.get(expr.nameForward, false);
				if (current instanceof PocketInstance) ((PocketInstance) current).beginDeath();
			}
			Object boxed = Boxer.box(value, this);
			Integer distance = locals.get(expr.nameForward);
			if (distance != null)
				environment.assignAt(distance, expr.nameForward, expr.value, boxed, this);
			else
				globals.assign(expr.nameForward, expr.value, boxed, this);
			return boxed;
		} else {
			Object value = evaluate(expr.value);
			if (value == null) {
				Object current = environment.get(expr.nameBackward, false);
				if (current instanceof PocketInstance) ((PocketInstance) current).beginDeath();
			}
			Object boxed = Boxer.box(value, this);
			Integer distance = locals.get(expr.nameBackward);
			if (distance != null)
				environment.assignAt(distance, expr.nameBackward, expr.value, boxed, this);
			else
				globals.assign(expr.nameBackward, expr.value, boxed, this);
			return boxed;
		}
	}

	public void executeExprBlock(List<Stmt> expression, Environment environment1) {

		for (Stmt stmt : expression) {
			execute(stmt);
			probeLifetimes();
		}

	}

	public void executeCup(List<Declaration> expression, Environment environment1) {

		for (Declaration stmt : expression) {
			execute(stmt);
			probeLifetimes();
		}

	}

	public void executeKnot(Knot body, Environment environment1) {

		new KnotRunner(body, body.expression, this);

	}

	public void executeTonk(Tonk body, Environment environment1) {

		new KnotRunner(body, body.expression, this);

	}

	public void executeCupExpr(Cup cup, Environment environment1)throws Snruter {

		environment = environment1;
		for (int i = forward ? 0 : cup.expression.size() - 1; i >= 0
				&& i < cup.expression.size(); i = forward ? i + 1 : i - 1) {
			evaluate(cup.expression.get(i));
		}

	}

	

	@Override
	public Object visitAdditiveExpr(Additive expr) {
		Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
		if (expr.operator.type == TokenType.ADD) {
			if (invertedMode) {
				if (object instanceof CupInstance) return ((CupInstance) object).pop(expr.operator);
				if (object instanceof PocketInstance) return ((PocketInstance) object).pop(expr.operator);
				if (object instanceof BoxInstance) return ((BoxInstance) object).pop(expr.operator);
				if (object instanceof KnotInstance) return ((KnotInstance) object).pop(expr.operator);
				if (object instanceof TonkInstance) return ((TonkInstance) object).pop(expr.operator);
				if (object instanceof TkpInstance) return ((TkpInstance) object).pop(expr.operator);
			} else {
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				tkp.add(expr.operator, expr.toadd);
			}
			}

		} else if (expr.operator.type == TokenType.PUSH) {
			if (invertedMode) {
				if (object instanceof CupInstance) return ((CupInstance) object).pop(expr.operator);
				if (object instanceof PocketInstance) return ((PocketInstance) object).pop(expr.operator);
				if (object instanceof BoxInstance) return ((BoxInstance) object).pop(expr.operator);
				if (object instanceof KnotInstance) return ((KnotInstance) object).pop(expr.operator);
				if (object instanceof TonkInstance) return ((TonkInstance) object).pop(expr.operator);
				if (object instanceof TkpInstance) return ((TkpInstance) object).pop(expr.operator);
			} else {
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				tkp.push(expr.operator, expr.toadd);
			}
			}
		}
		return null;
	}

	@Override
	public Object visitSetatExpr(Setat expr) {
		Object object2 = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));

		if (invertedMode) {
			Token addTok = new Token(TokenType.ADD, "add", null, null, null, -1, -1, -1, -1);
			if (object2 instanceof CupInstance) ((CupInstance) object2).add(addTok, expr.toset);
			else if (object2 instanceof PocketInstance) ((PocketInstance) object2).add(addTok, expr.toset);
			else if (object2 instanceof BoxInstance) ((BoxInstance) object2).add(addTok, expr.toset);
			else if (object2 instanceof KnotInstance) ((KnotInstance) object2).add(addTok, expr.toset);
			else if (object2 instanceof TonkInstance) ((TonkInstance) object2).add(addTok, expr.toset);
			else if (object2 instanceof TkpInstance) ((TkpInstance) object2).add(addTok, expr.toset);
			return null;
		}
		if (object2 instanceof CupInstance) {
			CupInstance cup = ((CupInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof PocketInstance) {
			PocketInstance cup = ((PocketInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof BoxInstance) {
			BoxInstance cup = ((BoxInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof KnotInstance) {
			KnotInstance cup = ((KnotInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof TonkInstance) {
			TonkInstance cup = ((TonkInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof TkpInstance) {
			TkpInstance tkp = ((TkpInstance) object2);
			tkp.setat(expr.index, expr.toset);
		}
		return null;
	}

	@Override
	public Object visitSubExpr(Sub expr) {
		Object object2 = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
		if (object2 instanceof CupInstance) {
			CupInstance cup = ((CupInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof PocketInstance) {
			PocketInstance cup = ((PocketInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof BoxInstance) {
			BoxInstance cup = ((BoxInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof KnotInstance) {
			KnotInstance cup = ((KnotInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof TonkInstance) {
			TonkInstance cup = ((TonkInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof TkpInstance) {
			TkpInstance tkp = ((TkpInstance) object2);
			return tkp.sub(expr.start, expr.end);
		}
		return null;
	}

	@Override
	public Object visitBusExpr(Bus expr) {
		Object object2 = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
		if (object2 instanceof CupInstance) {
			CupInstance cup = ((CupInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof PocketInstance) {
			PocketInstance cup = ((PocketInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof BoxInstance) {
			BoxInstance cup = ((BoxInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof KnotInstance) {
			KnotInstance cup = ((KnotInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof TonkInstance) {
			TonkInstance cup = ((TonkInstance) object2);
			return cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof TkpInstance) {
			TkpInstance tkp = ((TkpInstance) object2);
			return tkp.sub(expr.start, expr.end);
		}
		return null;
	}

	@Override
	public Object visitTatesExpr(Tates expr) {
		Object object2 = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
		if (object2 instanceof CupInstance) {
			CupInstance cup = ((CupInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof PocketInstance) {
			PocketInstance cup = ((PocketInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof BoxInstance) {
			BoxInstance cup = ((BoxInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof KnotInstance) {
			KnotInstance cup = ((KnotInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof TonkInstance) {
			TonkInstance cup = ((TonkInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof TkpInstance) {
			TkpInstance tkp = ((TkpInstance) object2);
			tkp.setat(expr.index, expr.toset);
		}
		return null;
	}

	@Override
	public Object visitEvitiddaExpr(Evitidda expr) {
		Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
		if (expr.operator.type == TokenType.DDA) {
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				cup.add(expr.operator, expr.toadd);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				tkp.add(expr.operator, expr.toadd);
			}
		} else if (expr.operator.type == TokenType.HSUP) {
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				cup.push(expr.operator, expr.toadd);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				tkp.push(expr.operator, expr.toadd);
			}
		}
		return null;
	}

	@Override
	public Object visitParamContOpExpr(ParamContOp expr) {
		if (expr.operator.type == TokenType.REMOVE) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (invertedMode) {
				if (object instanceof CupInstance) return ((CupInstance) object).pop(expr.operator);
				if (object instanceof PocketInstance) return ((PocketInstance) object).pop(expr.operator);
				if (object instanceof BoxInstance) return ((BoxInstance) object).pop(expr.operator);
				if (object instanceof KnotInstance) return ((KnotInstance) object).pop(expr.operator);
				if (object instanceof TonkInstance) return ((TonkInstance) object).pop(expr.operator);
				if (object instanceof TkpInstance) return ((TkpInstance) object).pop(expr.operator);
			}
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.remove(expr.operator, expr.index.value);
			}
		} else if (expr.operator.type == TokenType.GETAT) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (invertedMode) {
				if (object instanceof CupInstance) return ((CupInstance) object).pop(expr.operator);
				if (object instanceof PocketInstance) return ((PocketInstance) object).pop(expr.operator);
				if (object instanceof BoxInstance) return ((BoxInstance) object).pop(expr.operator);
				if (object instanceof KnotInstance) return ((KnotInstance) object).pop(expr.operator);
				if (object instanceof TonkInstance) return ((TonkInstance) object).pop(expr.operator);
				if (object instanceof TkpInstance) return ((TkpInstance) object).pop(expr.operator);
			}
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				if (expr.index.value instanceof String)
					return getatByCategory(cup, (String) expr.index.value, expr.operator);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.getat(expr.operator, expr.index.value);
			}
		}

		return null;
	}

	@Override
	public Object visitNonParamContOpExpr(NonParamContOp expr) {
		if (expr.operator.type == TokenType.SIZE) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.size(expr.operator);
			}
		} else if (expr.operator.type == TokenType.CLEAR) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.clear(expr.operator);
			}
		} else if (expr.operator.type == TokenType.EMPTY) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.empty(expr.operator);
			}
		} else if (expr.operator.type == TokenType.POP) {
			if (invertedMode) return null;
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.pop(expr.operator);
			}
		} else if (expr.operator.type == TokenType.ALIVE) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof PocketInstance) {
				return ((PocketInstance) object).isAlive();
			}
			if (object instanceof TkpInstance) {
				return ((TkpInstance) object).isAlive();
			}
			return true;
		}
		if (isAggContainerToken(expr.operator.type)) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			List<Object> body = getContainerBody(object);
			if (body != null) return applyContainerAggregate(body, expr.operator.type, expr.operator);
		}
		return null;
	}

	@Override
	public Object visitPoTnocMarapNonExpr(PoTnocMarapNon expr) {
		if (expr.operator.type == TokenType.EZIS) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.size(expr.operator);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.size(expr.operator);
			}
		} else if (expr.operator.type == TokenType.RAELC) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.clear(expr.operator);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.clear(expr.operator);
			}
		} else if (expr.operator.type == TokenType.YTPME) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.empty(expr.operator);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.empty(expr.operator);
			}
		} else if (expr.operator.type == TokenType.POP) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.pop(expr.operator);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.pop(expr.operator);
			}
		} else if (expr.operator.type == TokenType.EVILA) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof PocketInstance) {
				return ((PocketInstance) object).isAlive();
			}
			if (object instanceof TkpInstance) {
				return ((TkpInstance) object).isAlive();
			}
			return true;
		}
		if (isAggContainerToken(expr.operator.type)) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			List<Object> body = getContainerBody(object);
			if (body != null) return applyContainerAggregate(body, expr.operator.type, expr.operator);
		}
		return null;
	}

	@Override
	public Object visitPoTnocMarapExpr(PoTnocMarap expr) {
		if (expr.operator.type == TokenType.EVOMER) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.remove(expr.operator, expr.index.value);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.remove(expr.operator, expr.index.value);
			}
		} else if (expr.operator.type == TokenType.TATEG) {
			Object object = lookUpVariable(((Expr.Variable) (expr.callee)).name, ((Expr.Variable) (expr.callee)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				if (expr.index.value instanceof String)
					return tategByCategory(cup, (String) expr.index.value, expr.operator);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				return cup.getat(expr.operator, expr.index.value);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				return tkp.getat(expr.operator, expr.index.value);
			}
		}
		return null;
	}

	@Override
	public Object visitAddittiddaExpr(Addittidda expr) {
		Object object = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
				((Expr.Variable) (expr.calleeForward)));
		Object object2 = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
				((Expr.Variable) (expr.calleeBackward)));
		if (expr.operatorForward.type == TokenType.ADD) {
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				cup.add(expr.operatorForward, expr.toadd);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				cup.add(expr.operatorForward, expr.toadd);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				cup.add(expr.operatorForward, expr.toadd);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				cup.add(expr.operatorForward, expr.toadd);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				cup.add(expr.operatorForward, expr.toadd);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				tkp.add(expr.operatorForward, expr.toadd);
			}
		} else if (expr.operatorForward.type == TokenType.PUSH) {
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				cup.push(expr.operatorForward, expr.toadd);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				cup.push(expr.operatorForward, expr.toadd);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				cup.push(expr.operatorForward, expr.toadd);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				cup.push(expr.operatorForward, expr.toadd);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				cup.push(expr.operatorForward, expr.toadd);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				tkp.push(expr.operatorForward, expr.toadd);
			}
		}

		if (expr.operatorBackward.type == TokenType.DDA) {
			if (object2 instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object2);
				cup.add(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object2);
				cup.add(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object2);
				cup.add(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object2);
				cup.add(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object2);
				cup.add(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object2);
				tkp.add(expr.operatorBackward, expr.toadd);
			}
		} else if (expr.operatorBackward.type == TokenType.HSUP) {
			if (object2 instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object2);
				cup.push(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object2);
				cup.push(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object2);
				cup.push(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object2);
				cup.push(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object2);
				cup.push(expr.operatorBackward, expr.toadd);
			}
			if (object2 instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object2);
				tkp.push(expr.operatorBackward, expr.toadd);
			}
		}
		return null;
	}

	@Override
	public Object visitParCoOppOoCraPExpr(ParCoOppOoCraP expr) {
		Object forward = null;
		Object backward = null;
		if (expr.operatorForward.type == TokenType.REMOVE) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
					((Expr.Variable) (expr.calleeForward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				forward = cup.remove(expr.operatorForward, expr.index.value);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				forward = cup.remove(expr.operatorForward, expr.index.value);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				forward = cup.remove(expr.operatorForward, expr.index.value);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				forward = cup.remove(expr.operatorForward, expr.index.value);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				forward = cup.remove(expr.operatorForward, expr.index.value);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				forward = tkp.remove(expr.operatorForward, expr.index.value);
			}
		} else if (expr.operatorForward.type == TokenType.GETAT) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
					((Expr.Variable) (expr.calleeForward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				forward = cup.getat(expr.operatorForward, expr.index.value);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				forward = cup.getat(expr.operatorForward, expr.index.value);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				forward = cup.getat(expr.operatorForward, expr.index.value);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				forward = cup.getat(expr.operatorForward, expr.index.value);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				forward = cup.getat(expr.operatorForward, expr.index.value);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				forward = tkp.getat(expr.operatorForward, expr.index.value);
			}
		}
		if (expr.operatorBackward.type == TokenType.EVOMER) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
					((Expr.Variable) (expr.calleeBackward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				backward = cup.remove(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				backward = cup.remove(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				backward = cup.remove(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				backward = cup.remove(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				backward = cup.remove(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				backward = tkp.remove(expr.operatorBackward, expr.index.value);
			}
		} else if (expr.operatorBackward.type == TokenType.TATEG) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
					((Expr.Variable) (expr.calleeBackward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				backward = cup.getat(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				backward = cup.getat(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				backward = cup.getat(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				backward = cup.getat(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				backward = cup.getat(expr.operatorBackward, expr.index.value);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				backward = tkp.getat(expr.operatorBackward, expr.index.value);
			}
		}

		ArrayList<Declaration> expression = new ArrayList<>();
		if (forward instanceof Expr)
			expression.add((Expr) forward);
		else if (forward instanceof Stmt)
			expression.add((Stmt) forward);
		else if (forward instanceof Parser.Fun)
			expression.add((Parser.Fun) forward);
		else if (forward instanceof Declaration)
			expression.add((Declaration) forward);

		if (backward instanceof Expr)
			expression.add((Expr) backward);
		else if (backward instanceof Stmt)
			expression.add((Stmt) backward);
		else if (backward instanceof Parser.Fun)
			expression.add((Parser.Fun) backward);
		else if (backward instanceof Declaration)
			expression.add((Declaration) backward);

		return new Expr.Cup(((Expr.Variable) (expr.calleeForward)).name, expression, "",
				((Expr.Variable) (expr.calleeBackward)).name);
	}

	@Override
	public Object visitNoPaCoOOoCaPoNExpr(NoPaCoOOoCaPoN expr) {
		Object forward = null;
		Object backward = null;
		if (expr.operatorForward.type == TokenType.SIZE) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
					((Expr.Variable) (expr.calleeForward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				forward = cup.size(expr.operatorForward);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				forward = cup.size(expr.operatorForward);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				forward = cup.size(expr.operatorForward);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				forward = cup.size(expr.operatorForward);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				forward = cup.size(expr.operatorForward);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				forward = tkp.size(expr.operatorForward);
			}
		} else if (expr.operatorForward.type == TokenType.CLEAR) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
					((Expr.Variable) (expr.calleeForward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				forward = cup.clear(expr.operatorForward);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				forward = cup.clear(expr.operatorForward);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				forward = cup.clear(expr.operatorForward);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				forward = cup.clear(expr.operatorForward);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				forward = cup.clear(expr.operatorForward);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				forward = tkp.clear(expr.operatorForward);
			}
		} else if (expr.operatorForward.type == TokenType.EMPTY) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
					((Expr.Variable) (expr.calleeForward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				forward = cup.empty(expr.operatorForward);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				forward = cup.empty(expr.operatorForward);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				forward = cup.empty(expr.operatorForward);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				forward = cup.empty(expr.operatorForward);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				forward = cup.empty(expr.operatorForward);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				forward = tkp.empty(expr.operatorForward);
			}
		} else if (expr.operatorForward.type == TokenType.POP) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
					((Expr.Variable) (expr.calleeForward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				forward = cup.pop(expr.operatorForward);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				forward = cup.pop(expr.operatorForward);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				forward = cup.pop(expr.operatorForward);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				forward = cup.pop(expr.operatorForward);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				forward = cup.pop(expr.operatorForward);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				forward = tkp.pop(expr.operatorForward);
			}
		} else if (expr.operatorForward.type == TokenType.ALIVE) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
					((Expr.Variable) (expr.calleeForward)));
			if (object instanceof PocketInstance) {
				forward = ((PocketInstance) object).isAlive();
			} else if (object instanceof TkpInstance) {
				forward = ((TkpInstance) object).isAlive();
			} else {
				forward = true;
			}
		}

		if (expr.operatorBackward.type == TokenType.EZIS) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
					((Expr.Variable) (expr.calleeBackward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				backward = cup.size(expr.operatorBackward);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				backward = cup.size(expr.operatorBackward);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				backward = cup.size(expr.operatorBackward);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				backward = cup.size(expr.operatorBackward);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				backward = cup.size(expr.operatorBackward);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				backward = tkp.size(expr.operatorBackward);
			}
		} else if (expr.operatorBackward.type == TokenType.RAELC) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
					((Expr.Variable) (expr.calleeBackward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				backward = cup.clear(expr.operatorBackward);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				backward = cup.clear(expr.operatorBackward);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				backward = cup.clear(expr.operatorBackward);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				backward = cup.clear(expr.operatorBackward);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				backward = cup.clear(expr.operatorBackward);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				backward = tkp.clear(expr.operatorBackward);
			}
		} else if (expr.operatorBackward.type == TokenType.YTPME) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
					((Expr.Variable) (expr.calleeBackward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				backward = cup.empty(expr.operatorBackward);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				backward = cup.empty(expr.operatorBackward);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				backward = cup.empty(expr.operatorBackward);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				backward = cup.empty(expr.operatorBackward);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				backward = cup.empty(expr.operatorBackward);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				backward = tkp.empty(expr.operatorBackward);
			}
		} else if (expr.operatorBackward.type == TokenType.POP) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
					((Expr.Variable) (expr.calleeBackward)));
			if (object instanceof CupInstance) {
				CupInstance cup = ((CupInstance) object);
				backward = cup.pop(expr.operatorBackward);
			}
			if (object instanceof PocketInstance) {
				PocketInstance cup = ((PocketInstance) object);
				backward = cup.pop(expr.operatorBackward);
			}
			if (object instanceof BoxInstance) {
				BoxInstance cup = ((BoxInstance) object);
				backward = cup.pop(expr.operatorBackward);
			}
			if (object instanceof KnotInstance) {
				KnotInstance cup = ((KnotInstance) object);
				backward = cup.pop(expr.operatorBackward);
			}
			if (object instanceof TonkInstance) {
				TonkInstance cup = ((TonkInstance) object);
				backward = cup.pop(expr.operatorBackward);
			}
			if (object instanceof TkpInstance) {
				TkpInstance tkp = ((TkpInstance) object);
				backward = tkp.pop(expr.operatorBackward);
			}
		} else if (expr.operatorBackward.type == TokenType.EVILA) {
			Object object = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
					((Expr.Variable) (expr.calleeBackward)));
			if (object instanceof PocketInstance) {
				backward = ((PocketInstance) object).isAlive();
			} else if (object instanceof TkpInstance) {
				backward = ((TkpInstance) object).isAlive();
			} else {
				backward = true;
			}
		}
		ArrayList<Declaration> expression = new ArrayList<>();
		if (forward instanceof Expr)
			expression.add((Expr) forward);
		else if (forward instanceof Stmt)
			expression.add((Stmt) forward);
		else if (forward instanceof Parser.Fun)
			expression.add((Parser.Fun) forward);
		else if (forward instanceof Declaration)
			expression.add((Declaration) forward);
		else if (forward instanceof Integer)
			expression.add(new Expr.Literal(forward));
		else if (forward instanceof Boolean)
			expression.add(new Expr.Literal(forward));

		if (backward instanceof Expr)
			expression.add((Expr) backward);
		else if (backward instanceof Stmt)
			expression.add((Stmt) backward);
		else if (backward instanceof Parser.Fun)
			expression.add((Parser.Fun) backward);
		else if (backward instanceof Declaration)
			expression.add((Declaration) backward);
		else if (backward instanceof Integer)
			expression.add(new Expr.Literal(backward));
		else if (backward instanceof Boolean)
			expression.add(new Expr.Literal(backward));

		BigInteger uniqueNum = generateUniqueNum();
		String reverse = reverse(uniqueNum.toString());
		Token identifier = new Token(TokenType.IDENTIFIER, uniqueNum.toString() + "cup", null, null, null, -1, -1, -1,
				-1);
		Token reifitnedi = new Token(TokenType.IDENTIFIER, "puc" + reverse, null, null, null, -1, -1, -1, -1);
		return new Expr.Cup(identifier, expression, "", reifitnedi);
	}

	@Override
	public Object visitSetattatesExpr(Setattates expr) {
		Object object2 = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
				((Expr.Variable) (expr.calleeForward)));
		Object object3 = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
				((Expr.Variable) (expr.calleeForward)));
		if (object2 instanceof CupInstance) {
			CupInstance cup = ((CupInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof PocketInstance) {
			PocketInstance cup = ((PocketInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof BoxInstance) {
			BoxInstance cup = ((BoxInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof KnotInstance) {
			KnotInstance cup = ((KnotInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof TonkInstance) {
			TonkInstance cup = ((TonkInstance) object2);
			cup.setat(expr.index, expr.toset);
		}
		if (object2 instanceof TkpInstance) {
			TkpInstance tkp = ((TkpInstance) object2);
			tkp.setat(expr.index, expr.toset);
		}

		if (object3 instanceof CupInstance) {
			CupInstance cup = ((CupInstance) object3);
			cup.setat(expr.index, expr.toset);
		}
		if (object3 instanceof PocketInstance) {
			PocketInstance cup = ((PocketInstance) object3);
			cup.setat(expr.index, expr.toset);
		}
		if (object3 instanceof BoxInstance) {
			BoxInstance cup = ((BoxInstance) object3);
			cup.setat(expr.index, expr.toset);
		}
		if (object3 instanceof KnotInstance) {
			KnotInstance cup = ((KnotInstance) object3);
			cup.setat(expr.index, expr.toset);
		}
		if (object3 instanceof TonkInstance) {
			TonkInstance cup = ((TonkInstance) object3);
			cup.setat(expr.index, expr.toset);
		}
		if (object3 instanceof TkpInstance) {
			TkpInstance tkp = ((TkpInstance) object3);
			tkp.setat(expr.index, expr.toset);
		}

		return null;
	}

	@Override
	public Object visitSubbusExpr(Subbus expr) {
		Object object2 = lookUpVariable(((Expr.Variable) (expr.calleeForward)).name,
				((Expr.Variable) (expr.calleeForward)));
		Object object3 = lookUpVariable(((Expr.Variable) (expr.calleeBackward)).name,
				((Expr.Variable) (expr.calleeBackward)));
		Object forwardReturn = null;
		Object backwardReturn = null;

		if (object2 instanceof CupInstance) {
			CupInstance cup = ((CupInstance) object2);
			forwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof PocketInstance) {
			PocketInstance cup = ((PocketInstance) object2);
			forwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof BoxInstance) {
			BoxInstance cup = ((BoxInstance) object2);
			forwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof KnotInstance) {
			KnotInstance cup = ((KnotInstance) object2);
			forwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof TonkInstance) {
			TonkInstance cup = ((TonkInstance) object2);
			forwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object2 instanceof TkpInstance) {
			TkpInstance tkp = ((TkpInstance) object2);
			forwardReturn = tkp.sub(expr.start, expr.end);
		}

		if (object3 instanceof CupInstance) {
			CupInstance cup = ((CupInstance) object3);
			backwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object3 instanceof PocketInstance) {
			PocketInstance cup = ((PocketInstance) object3);
			backwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object3 instanceof BoxInstance) {
			BoxInstance cup = ((BoxInstance) object3);
			backwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object3 instanceof KnotInstance) {
			KnotInstance cup = ((KnotInstance) object3);
			backwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object3 instanceof TonkInstance) {
			TonkInstance cup = ((TonkInstance) object3);
			backwardReturn = cup.sub(expr.start, expr.end);
		}
		if (object3 instanceof TkpInstance) {
			TkpInstance tkp = ((TkpInstance) object3);
			backwardReturn = tkp.sub(expr.start, expr.end);
		}
		ArrayList<Declaration> expression = new ArrayList<>();
		if (forwardReturn instanceof Expr)
			expression.add((Expr) forwardReturn);
		else if (forwardReturn instanceof Stmt)
			expression.add((Stmt) forwardReturn);
		else if (forwardReturn instanceof Parser.Fun)
			expression.add((Parser.Fun) forwardReturn);
		else if (forwardReturn instanceof Declaration)
			expression.add((Declaration) forwardReturn);
		else if (forwardReturn instanceof Integer)
			expression.add(new Expr.Literal(forwardReturn));
		else if (forwardReturn instanceof Boolean)
			expression.add(new Expr.Literal(forwardReturn));

		if (backwardReturn instanceof Expr)
			expression.add((Expr) backwardReturn);
		else if (backwardReturn instanceof Stmt)
			expression.add((Stmt) backwardReturn);
		else if (backwardReturn instanceof Parser.Fun)
			expression.add((Parser.Fun) backwardReturn);
		else if (backwardReturn instanceof Declaration)
			expression.add((Declaration) backwardReturn);
		else if (backwardReturn instanceof Integer)
			expression.add(new Expr.Literal(backwardReturn));
		else if (backwardReturn instanceof Boolean)
			expression.add(new Expr.Literal(backwardReturn));

		BigInteger uniqueNum = generateUniqueNum();
		String reverse = reverse(uniqueNum.toString());
		Token identifier = new Token(TokenType.IDENTIFIER, uniqueNum.toString() + "cup", null, null, null, -1, -1, -1,
				-1);
		Token reifitnedi = new Token(TokenType.IDENTIFIER, "puc" + reverse, null, null, null, -1, -1, -1, -1);
		return new Expr.Cup(identifier, expression, "", reifitnedi);
	}

	@Override
	public Object visitContainssniatnocExpr(Containssniatnoc expr) {

		if (forward) {
			if (expr.contForward instanceof Expr.Variable) {

				Object lookUpVariable = lookUpVariable(((Expr.Variable) expr.contForward).name, expr.contForward);
				Instance lookUpContainer = (Instance) lookUpVariable;
				Pocket poc = ((Expr.Pocket) expr.contentsShared);
				if (poc.expression.size() < 3) {
					throw new RuntimeException("expected one parameter found none");
				} else if (poc.expression.size() > 3) {
					throw new RuntimeException("expected one parameter found more then one");
				}
				Stmt stmt = poc.expression.get(1);

				if (lookUpContainer instanceof BoxInstance) {
					return ((BoxInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof PocketInstance) {
					return ((PocketInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof CupInstance) {
					return ((CupInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof KnotInstance) {
					return ((KnotInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof TonkInstance) {
					return ((TonkInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof TkpInstance) {
					return ((TkpInstance) lookUpContainer).contains(stmt);
				}
			}
		} else {

			if (expr.contBackward instanceof Expr.Variable) {

				Object lookUpVariable = lookUpVariable(((Expr.Variable) expr.contBackward).name, expr.contBackward);
				Instance lookUpContainer = (Instance) lookUpVariable;
				Pocket poc = ((Expr.Pocket) expr.contentsShared);
				if (poc.expression.size() < 3) {
					throw new RuntimeException("expected one parameter found none");
				} else if (poc.expression.size() > 3) {
					throw new RuntimeException("expected one parameter found more then one");
				}
				Stmt stmt = poc.expression.get(1);

				if (lookUpContainer instanceof BoxInstance) {
					return ((BoxInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof PocketInstance) {

					return ((PocketInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof CupInstance) {
					return ((CupInstance) lookUpContainer).contains(stmt);

				} else if (lookUpContainer instanceof KnotInstance) {

					return ((KnotInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof TonkInstance) {

					return ((TonkInstance) lookUpContainer).contains(stmt);
				} else if (lookUpContainer instanceof TkpInstance) {
					return ((TkpInstance) lookUpContainer).contains(stmt);
				}
			}

		}

		return null;
	}

	public void setFlatlanderFaceBook(FlatLandFacebook flatLanderFaceBook) {
		this.flatLanderFaceBook = flatLanderFaceBook;
		// TODO Auto-generated method stub

	}

	public void setFlatland(ViewableFlatLand viewableFlatLand) {
		this.viewableFlatLand = viewableFlatLand;
		// TODO Auto-generated method stub

	}

	public void setEvents(EventHandler events) {
		this.events = events;
		// TODO Auto-generated method stub

	}

	@Override
	public Object visitFLCreateStmt(FLCreate stmt) {
		Object theRequestie = new Object();
		while (!FlatLandFacebook.getInstance().requestToken(theRequestie)) {
		}

		FlatLanderWrper flatlander = new FlatLanderWrper(stmt.x, stmt.y, stmt.z, stmt.name, 1.2, stmt.collidiable,
				stmt.shouldPhysicsApply, typeOfEntity(stmt.FlatLanderType), Color.getColor(stmt.Color));
		try {
			flatlander.setSprite(new SkeletonTwo("res/zombie_n_skeleton2.png", 100));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FlatLandFacebook.getInstance().add(flatlander, theRequestie);
		FlatLandFacebook.getInstance().releaseToken(theRequestie);
		return null;
	}

	private TypeOfEntity typeOfEntity(String flatLanderType) {
		if (flatLanderType.equalsIgnoreCase("monster")) {
			return TypeOfEntity.MONSTER;
		} else if (flatLanderType.equalsIgnoreCase("player")) {
			return TypeOfEntity.PLAYER;
		} else if (flatLanderType.equalsIgnoreCase("terrain")) {
			return TypeOfEntity.TERRAIN;
		} else if (flatLanderType.equalsIgnoreCase("item")) {
			return TypeOfEntity.ITEM;
		}
		return TypeOfEntity.MONSTER;
	}

	@Override
	public Object visitFLMoveStmt(FLMove stmt) {
		FlatLandFacebook instance = flatLanderFaceBook.getInstance();
		ArrayList<FlatLander> flatlanderFaceBook2 = instance.getFlatlanderFaceBook();
		flatlanderFaceBook2.forEach(flatlander -> {
			if (flatlander.getName().equals(stmt.name)) {
				flatlander.setX(stmt.x);
				flatlander.setY(stmt.y);
			}
		});

		return null;
	}

	@Override
	public Object visitFLDestroyStmt(FLDestroy stmt) {
		FlatLandFacebook instance = flatLanderFaceBook.getInstance();
		ArrayList<FlatLander> flatlanderFaceBook2 = instance.getFlatlanderFaceBook();
		Object theRequestie = new Object();
		while (!FlatLandFacebook.getInstance().requestToken(theRequestie)) {
		}

		FlatLander flatlander2 = null;
		for (FlatLander flatLander : flatlanderFaceBook2) {
			if (flatLander.getName().equalsIgnoreCase(stmt.name)) {
				flatlander2 = flatLander;
				break;
			}
		}
		instance.remove(flatlander2);
		FlatLandFacebook.getInstance().releaseToken(theRequestie);
		return null;
	}

	@Override
	public Object visitFLECreateStmt(FLECreate stmt) {
		events.addEvent(new GameEvent(stmt.x, stmt.y, stmt.name, stmt.keyword.lexeme));
		return null;
	}

	@Override
	public Object visitFLEDestroyStmt(FLEDestroy stmt) {
		events.removeEvent(stmt.name);
		return null;
	}

	@Override
	public Object visitFLsetValueStmt(FLsetValue stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitLiteralBoolExpr(LiteralBool expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitLiteralLoobExpr(LiteralLoob expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitRunStmt(Run stmt) {
		if (forward) {
			try {
				File myObj = new File((String) evaluate(stmt.filePathToScriptToExecute));
				try (java.util.Scanner myReader = new java.util.Scanner(myObj)) {
					String data = "";
					while (myReader.hasNextLine()) {
						data += myReader.nextLine();

					}
					Scanner scanner = new Scanner(data);
					List<Token> tokens = scanner.scanTokensFirstPass();

					Grouper grouper = new Grouper((ArrayList<Token>) tokens);
					ArrayList<Token> toks = grouper.scanTokensSecondPass();

					ParserTest parser = new ParserTest(toks, true, false);
					List<Declaration> statements = parser.parse();

					// interpreter.setForward(!forward);
					this.setForward(forward);
					Resolver resolver = new Resolver(this);
					resolver.resolve(statements);

					this.interpret(statements);
				}

			} catch (FileNotFoundException e) {
				System.out.println("An error occurred.");
				e.printStackTrace();
			}
		}
		return null;
	}

	static void installShutdownHook(InterpreterAudio c) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				((AutoCloseable) c).close();
			} catch (Exception ignored) {
			}
		}, "AudioShutdown"));
	}

	@Override
	public Object visitNurStmt(Nur stmt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitEOFExpr(EOF expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitTypeExpr(Type expr) {
	Expr target = expr.target;
		
		if(target instanceof Expr.Variable) {
			Object lookUpVariable = lookUpVariableTypeByName(((Expr.Variable)target));
			return lookUpVariable;
		}
		return null;
	}

	@Override
	public Object visitEpytExpr(Epyt expr) {
		// TODO Auto-generated method stub
		Expr target = expr.target;
		
		if(target instanceof Expr.Variable) {
			Object lookUpVariable = lookUpVariableTypeByName(((Expr.Variable)target));
			return lookUpVariable;
		}
		return null;
	}




}
