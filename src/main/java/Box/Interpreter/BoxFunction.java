package Box.Interpreter;

import java.util.List;


import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;

public class BoxFunction implements BoxCallable {

	private final Environment closure;
	private List<Token> params;
	private String name;
	private Expr body;
	private Token type;
	private List<Token> paramsTypes;
	private boolean isLink;
	public final boolean isForward;
	public BoxClass ownerClass; // set after the BoxClass is built; null for top-level functions

	public BoxFunction(Expr body, String name, List<Token> paramsTypes, List<Token> paramsNames, Environment closure, boolean isForward, boolean isLink) {
		this.body = body;
		this.name = name;
		this.paramsTypes = paramsTypes;
		this.params = paramsNames;
		this.isLink = isLink;
		this.isForward = isForward;
		String paramsString = "";
		if(params!=null)
		for (int i = 0; i < params.size(); i++) {
			if (i < params.size() - 1) {

					paramsString += paramsTypes.get(i)+" "+params.get(i).lexeme+ " , ";

			} else {
				paramsString += paramsTypes.get(i)+" "+params.get(i).lexeme;

			}
		}
		String bodyString = "";
		if (body instanceof Expr.Cup) {
			bodyString = ((Expr.Cup) body).lexeme;
		}
		if (body instanceof Expr.Knot) {
			bodyString = ((Expr.Cup) body).lexeme;
		}

		this.type = new Token(TokenType.FUN, name + "( " + paramsString + " )"+bodyString,null,null,null,-1,-1,-1,-1);
		this.closure = closure;

	}



	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		if (body == null)
			throw new RuntimeError(null, "called unimplemented link method '" + name + "'");

		Environment environment1 = new Environment(closure);
		if (params != null) {
			for (int i = 0; i < params.size(); i++) {
					environment1.define(params.get(i).lexeme, paramsTypes.get(i), arguments.get(i));
			}
		}

		// push type context if this function belongs to a template that resolves a UserType
		Interpreter.UserTypeEntry typeCtx = null;
		if (ownerClass != null) {
			for (Interpreter.UserTypeEntry e : interpreter.userTypeRegistry) {
				if (e.resolvedTemplate == ownerClass) { typeCtx = e; break; }
			}
		}
		if (typeCtx != null) interpreter.typeContextStack.push(typeCtx);

		Environment previous = interpreter.environment;
		Object evaluate = null;
		try {
			if (body instanceof Expr.Cup)
				interpreter.executeCupExpr(((Expr.Cup) body), environment1);

		} catch (Returns returnValue) {
			if (!isForward)
				throw new RuntimeError(null, "direction mismatch: backward function caught forward return");
			if (returnValue.value instanceof Expr) {
				evaluate = interpreter.evaluate((Expr) returnValue.value);
			} else {
				evaluate = returnValue.value;
			}

		} catch (Snruter returnValue) {
			if (isForward)
				throw new RuntimeError(null, "direction mismatch: forward function caught backward return");
			if (returnValue.value instanceof Expr) {
				evaluate = interpreter.evaluate((Expr) returnValue.value);
			} else {
				evaluate = returnValue.value;
			}

		} finally {
			interpreter.environment = previous;
			if (typeCtx != null) interpreter.typeContextStack.pop();
		}

		return evaluate;
	}

	@Override
	public int arity() {
		return params == null ? 0 : params.size();
	}

	@Override
	public String toString() {
		return "<fn " + name + ">";
	}

	public String getName() {
		return name;
	}

	public boolean isLinkSignature() {
		return isLink && body == null;
	}

	public BoxFunction bind(Instance boxInstance) {
		Environment environment = new Environment(closure);
		environment.define(name, getType(), boxInstance);
		return new BoxFunction(body, name, paramsTypes, params, environment, isForward, isLink);
	}

	public Token getType() {
		return type;
	}

	@Override
	public BoxFunction findMethod(String lexeme) {
		return null;
	}

}
