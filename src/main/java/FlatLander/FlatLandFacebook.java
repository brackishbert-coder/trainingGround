package FlatLander;

import java.util.ArrayList;

import FlatLander.FlatLander;

public class FlatLandFacebook {

	
	
	private static FlatLandFacebook instance;
	private static boolean lockToken = false;
	private static Object requestee = null;
	private static ArrayList<FlatLander> flatlanderFaceBook = new ArrayList<FlatLander>();
	
	
	public static FlatLandFacebook getInstance() {
		if (instance == null) {
			instance = new FlatLandFacebook();
		}
		return instance;
	}
	public boolean requestToken(Object theRequestie) {
		if(!lockToken) {
			lockToken = true;
			requestee =theRequestie;
			return true;
		}else
			return false;
	}
	
	public void releaseToken(Object theRequestie) {
		if(requestee.equals(theRequestie)) {
			lockToken = false;
		}
	}
	
	

	public static ArrayList<FlatLander> getFlatlanderFaceBook() {
		return flatlanderFaceBook;
	}


	public void add(FlatLander flatlander,Object requestie) {
		if(requestee.equals(requestie)&& lockToken) {
			flatlanderFaceBook.add(flatlander);
			orderByZindex();
		}
		
	}
	private void orderByZindex() {
		flatlanderFaceBook.sort((a, b) -> Integer.compare(a.z, b.z));
		
	}
	public void remove(FlatLander flatlander) {
		flatlanderFaceBook.remove(flatlander);
		
	}
	
	public void clear() {
		flatlanderFaceBook.clear();
	}
	
	
}
