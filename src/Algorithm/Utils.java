package Algorithm;

public class Utils {
	public static String getPair(int src, int dst) {
		String ret = null;
		if (src < dst) {
			ret = src + "-" + dst;
		} else {
			ret = dst + "-" + src;
		}
		return ret;
	}
	
	public static String getPair(String src, String dst) {
		return src + "-" + dst;
	}
}
