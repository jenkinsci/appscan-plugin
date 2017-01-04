package com.ibm.appscan.jenkins.plugin.scanners;


/**
 * This class is not intended for normal use.  It's sole purpose is for compatibility to convert
 * older build steps to use the Describable Scanner implementations.
 */
public class ScannerFactory implements ScannerConstants {

	/**
	 * Creates a Scanner given a type and target.
	 * @param type
	 * @param target
	 * @return
	 */
	public static Scanner getScanner(String type, String target) {
		Scanner ret = null;
		
		switch(type) {
		case DYNAMIC_ANALYZER:
			ret = new DynamicAnalyzer(target);
			break;
		case MOBILE_ANALYZER:
			ret = new MobileAnalyzer(target);
			break;
		case STATIC_ANALYZER:
			ret = new StaticAnalyzer(target);
			break;
		default:
				break;
		}
		return ret;
	}
}
