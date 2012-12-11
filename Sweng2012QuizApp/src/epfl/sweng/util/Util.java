package epfl.sweng.util;


public class Util {

	
	public static boolean isLetterOrDigit(String s) {
		for (int i=0;i<s.length();++i) {
			if (!Character.isLetterOrDigit(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
