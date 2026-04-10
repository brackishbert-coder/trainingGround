package FlatLander;

import java.util.Stack;

import Constructs.Construct;


public class FlatLanderMemory {
	
	Stack<Construct> memory;
	
	public FlatLanderMemory(){
		memory= new Stack<Construct>();
	}
	
	
	public void pushConstruct(Construct construct){
		memory.push(construct);
	}
	
	public Construct popConstruct() {
		return memory.pop();
	}
	
	
	public Construct peekConstruct() {
		return memory.peek();
		
	}

}
