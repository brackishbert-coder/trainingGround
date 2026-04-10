package View;

public interface Observable {
	public void attach(Observer obi);
	public void detach(Observer obi);
	public void notify(String message);
}
