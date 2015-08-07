package br.ufpe.cin.aac3.gryphon;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public final class GryphonConfig {
	private static File workingDirectory = new File("integration");
	private static boolean logEnabled = true;
	private static boolean showLogo = true;

	public static File getWorkingDirectory() {
		return workingDirectory;
	}
	
	public static void setWorkingDirectory(File workingDirectory) {
		if(!workingDirectory.exists()){
			workingDirectory.mkdirs();
		}
		GryphonConfig.workingDirectory = workingDirectory;
	}
	
	public static boolean isLogEnabled() {
		return logEnabled;
	}
	
	public static void setLogEnabled(boolean logEnabled) {
		if(!logEnabled){
			Logger.getRootLogger().setLevel(Level.OFF);
		}
		GryphonConfig.logEnabled = logEnabled;
	}
	
	public static boolean isShowLogo() {
		return showLogo;
	}
	
	public static void setShowLogo(boolean showLogo) {
		GryphonConfig.showLogo = showLogo;
	}
}