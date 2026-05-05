package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Box.Token.TokenType;
import Parser.Expr;

public class BoxClass implements BoxCallable {

	public String name;
	private Map<String, BoxFunction> methods;
	private BoxClass superclass;
	public List<Object> contents;
	public TokenType type;
	private boolean enforce;

	private TypesOfObject typesOfObject;
	Environment closure;
	Expr body;
	private boolean isLink;
	public List<String> templateLinkNames;

	public BoxClass(String name, BoxClass superclass, ArrayList<Object> boxPrimarys, Map<String, BoxFunction> methods,
			TokenType type, boolean enforce, TypesOfObject typesOfObject, Environment closure, Expr body, boolean isLink) {
		this.name = name;
		this.superclass = superclass;
		this.contents = boxPrimarys;
		this.methods = methods;
		this.type = type;
		this.enforce = enforce;

		this.typesOfObject = typesOfObject;
		this.closure = closure;
		this.body = body;
		this.isLink = isLink;

	}

	@Override
	public String toString() {
		String contents = "";
		StringBuilder sb = new StringBuilder(name);
		if (type == TokenType.CUPCONTAINER)
			contents = name + "{ ";
		if (type == TokenType.POCKETCONTAINER)
			contents = name + "( ";

		for (Object object : this.contents) {
			if (object != null) {
				contents += object.toString() + " ";
			} else
				contents += "null ";
		}
		if (type == TokenType.CUPCONTAINER)
			contents += " }" + sb.reverse().toString();
		if (type == TokenType.POCKETCONTAINER)
			contents += " )" + sb.reverse().toString();
		return contents;
	}


	@Override
	public int arity() {
		BoxFunction initilizer = findMethod("init");
		if (initilizer == null)
			return 0;
		return initilizer.arity();
	}

	public BoxFunction findMethod(String lexeme) {
		if (methods != null) {
			if (methods.containsKey(lexeme)) {
				return methods.get(lexeme);
			}
		}
		if (superclass != null)
			return superclass.findMethod(lexeme);

		return null;
	}

	public boolean isLink() {
		return isLink;
	}

	public Map<String, BoxFunction> getMethods() {
		return methods != null ? methods : new java.util.HashMap<>();
	}

	public void setContentsAt(Integer integer, Object expression) {

	}

	public Object get(String lexeme) {
		if(methods.containsKey(lexeme)) {
			return methods.get(lexeme);
		}
		return null;
	}

	public void setContentsAtEnd(String data) {
		Parser.Expr.Literal literal = new Expr.Literal(data);
		contents.add(literal);
	}

	public boolean compairPrimarys(BoxClass boxClass) {
		boolean contains = false;
		for (Object object : boxClass.contents) {
			contains = contents.contains(object);
			if (!contains)
				break;
		}
		return contains;
	}

	


	public int size() {

		return contents.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Instance instance = null;

		if (body instanceof Expr.Knot) {
			instance = new KnotInstance(this, new ArrayList<>(((Expr.Knot) body).expression), body,interpreter);

		} else if (body instanceof Expr.Cup) {
			if (((Expr.Cup) body).isPuc)
				instance = new PucInstance(this, new ArrayList<>(((Expr.Cup) body).expression), body, interpreter);
			else
				instance = new CupInstance(this, new ArrayList<>(((Expr.Cup) body).expression), body, interpreter);

		} else if (body instanceof Expr.Pocket) {
			PocketInstance pkt = new PocketInstance(this, new ArrayList<>(((Expr.Pocket) body).expression), body, interpreter);
			pkt.windowSize = ((Expr.Pocket) body).windowSize;
			pkt.starvationThreshold = ((Expr.Pocket) body).starvationThreshold;
			pkt.starvationPolicy = ((Expr.Pocket) body).starvationPolicy;
			instance = pkt;

		} else if (body instanceof Expr.Tonk) {
			instance = new TonkInstance(this, new ArrayList<>(((Expr.Tonk) body).expression), body,interpreter);

		} else if (body instanceof Expr.Box) {
			if (((Expr.Box) body).isXob)
				instance = new XobInstance(this, new ArrayList<>(((Expr.Box) body).expression), body, interpreter);
			else
				instance = new BoxInstance(this, new ArrayList<>(((Expr.Box) body).expression), body, interpreter);

		}
		return instance;
	}



}
