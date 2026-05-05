package testing;



public class VariableRepository {

	
	
	
	private static VariableRepository instance;
	private int threshold1 = 0 ;
	private int threshold2 = 0 ;
	private int maxVal1 = 0;
	private int maxVal2 = 0;
	private int FLWidth = 1024;
	private int FLHeight = 1024;
	private int kernal1x =1;
	private int kernal1y =1;
	private int kernal2x =1;
	private int kernel2y=1;
	
	private int kernal3x =1;
	private int kernal3y =1;
	private int kernal4x =1;
	private int kernel4y=1;
	private static int point1;
	private static int point4;
	private static int point3;
	private static int point2;
	private  boolean but0;
	private  boolean but1;
	private  boolean but2;
	private  boolean but3;
	private  boolean but4;
	private  boolean but5;
	
	private  boolean but6;
	private  boolean but7;
	
	
	public static VariableRepository getInstance() {
		if (instance == null) {
			instance = new VariableRepository();
		}
		return instance;
	}




	public int getThreshold1() {
		return threshold1;
	}




	public void setThreshold1(int threshold1) {
		this.threshold1 = threshold1;
	}




	public int getThreshold2() {
		return threshold2;
	}




	public void setThreshold2(int threshold2) {
		this.threshold2 = threshold2;
	}




	public int getMaxVal1() {
		return maxVal1;
	}




	public void setMaxVal1(int maxVal1) {
		this.maxVal1 = maxVal1;
	}




	public int getMaxVal2() {
		return maxVal2;
	}




	public void setMaxVal2(int maxVal2) {
		this.maxVal2 = maxVal2;
	}




	public int getFLWidth() {
		return FLWidth;
	}




	public void setFLWidth(int fLWidth) {
		FLWidth = fLWidth;
	}




	public int getFLHeight() {
		return FLHeight;
	}




	public void setFLHeight(int fLHeight) {
		FLHeight = fLHeight;
	}




	public int getKernal1x() {
		return kernal1x;
	}




	public void setKernal1x(int kernal1x) {
		this.kernal1x = kernal1x;
	}




	public int getKernal1y() {
		return kernal1y;
	}




	public void setKernal1y(int kernal1y) {
		this.kernal1y = kernal1y;
	}




	public int getKernal2x() {
		return kernal2x;
	}




	public void setKernal2x(int kernal2x) {
		this.kernal2x = kernal2x;
	}




	public int getKernel2y() {
		return kernel2y;
	}




	public void setKernel2y(int kernel2y) {
		this.kernel2y = kernel2y;
	}




	public int getKernal3x() {
		return kernal3x;
	}




	public void setKernal3x(int kernal3x) {
		this.kernal3x = kernal3x;
	}




	public int getKernal3y() {
		return kernal3y;
	}




	public void setKernal3y(int kernal3y) {
		this.kernal3y = kernal3y;
	}




	public int getKernal4x() {
		return kernal4x;
	}




	public void setKernal4x(int kernal4x) {
		this.kernal4x = kernal4x;
	}




	public int getKernel4y() {
		return kernel4y;
	}




	public void setKernel4y(int kernel4y) {
		this.kernel4y = kernel4y;
	}




	public void setPoint1(Integer valueOf) {
		
		VariableRepository.point1 = (int)valueOf;
		
	}
	public void setPoint2(Integer valueOf) {
		VariableRepository.point2 = (int)valueOf;
		
	}
	public void setPoint3(Integer valueOf) {
		VariableRepository.point3 = (int)valueOf;
		
	}
	public void setPoint4(Integer valueOf) {
		VariableRepository.point4 = (int)valueOf;
		
	}




	public static int getPoint1() {
		
		return point1;
	}




	public static int getPoint4() {
		return point4;
	}




	public static int getPoint3() {
		return point3;
	}







	public static int getPoint2() {
		return point2;
	}




	public  boolean isBut0() {
		return but0;
	}




	public  void setBut0(boolean but0) {
		this.but0 = but0;
	}




	public  boolean isBut1() {
		return but1;
	}




	public  void setBut1(boolean but1) {
		this.but1 = but1;
	}




	public  boolean isBut2() {
		return but2;
	}




	public  void setBut2(boolean but2) {
		this.but2 = but2;
	}




	public  boolean isBut3() {
		return but3;
	}




	public  void setBut3(boolean but3) {
		this.but3 = but3;
	}




	public  boolean isBut4() {
		return but4;
	}




	public  void setBut4(boolean but4) {
		this.but4 = but4;
	}




	public  boolean isBut5() {
		return but5;
	}




	public  void setBut5(boolean but5) {
		this.but5 = but5;
	}




	public  boolean isBut6() {
		return but6;
	}




	public  void setBut6(boolean but6) {
		this.but6 = but6;
	}




	public  boolean isBut7() {
		return but7;
	}




	public  void setBut7(boolean but7) {
		this.but7 = but7;
	}

	

	
	
	
	
	
	
	
}
