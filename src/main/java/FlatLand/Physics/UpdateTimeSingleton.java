package FlatLand.Physics;


public class UpdateTimeSingleton {
	
	
	
	private static UpdateTimeSingleton instance;
	
	
	private static Double CurrentTime = 0.0;
	
	
	public static UpdateTimeSingleton getInstance() {
		if (instance == null) {
			instance = new UpdateTimeSingleton();
		}
		return instance;
	}


	public static Double getCurrentTime() {
		return CurrentTime;
	}


	public void setCurrentTime(Double currentTime) {
		CurrentTime = currentTime;
	}

}
