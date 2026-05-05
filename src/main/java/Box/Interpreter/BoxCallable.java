package Box.Interpreter;

import java.util.List;

public interface BoxCallable {

	
	
	public Object call(Interpreter interpreter,List<Object> arguments);
	

	int arity() ;


	public BoxFunction findMethod(String lexeme);
	
	
	
	



}

