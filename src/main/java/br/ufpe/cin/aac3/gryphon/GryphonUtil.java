package br.ufpe.cin.aac3.gryphon;

import java.io.File;

public final class GryphonUtil {
	private static final String currentURI = new File("").toURI().toString();
	
	public static String getCurrentURI(){
		return currentURI.toString();
	}
	
	public static void logInfo(String info){
		if(GryphonConfig.isLogEnabled()){
			System.out.println(info);
		}
	}
	
	public static void logError(String error){
		if(GryphonConfig.isLogEnabled()){
			System.err.println(error);
		}
	}
	
	public static boolean isWindows(){
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("win") >= 0 ? true : false;
	}
}