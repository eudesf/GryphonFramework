package br.ufpe.cin.aac3.gryphon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import br.ufpe.cin.aac3.gryphon.exception.CommandExecutionException;

public class CommandUtil {
	
	public static ProcessBuilder createCmdProcessBuilder(String cmd) {
		if (GryphonUtil.isWindows()) {
			return new ProcessBuilder("cmd.exe", "/c", cmd);
		} else {
			return new ProcessBuilder("sh", "-c", cmd);
		}
	}

	public static String readStreamText(InputStream in) {
		StringBuilder result = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				if (result.length() > 0) {
					result.append("\n");
				}
				result.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	public static Process executeCommandAsync(String cmd, Object... params) {
		String cmdToExecute = String.format(cmd, params);
		ProcessBuilder processBuilder = createCmdProcessBuilder(cmdToExecute);
		GryphonUtil.logInfo("Executing command: " + cmdToExecute);
		try {
			return processBuilder.start();
		} catch (IOException e) {
			throw new CommandExecutionException("Exception in command: \"" + cmd + "\"", e);
		}
	}
	
	public static String readCmdResponse(Process process) {
		try {
			StreamLineReader readerOut = new StreamLineReader(process.getInputStream(), "out");
			StreamLineReader readerErr = new StreamLineReader(process.getErrorStream(), "err");
			Thread thrOut = new Thread(readerOut);
			Thread thrErr = new Thread(readerErr);
			
			thrOut.start();
			thrErr.start();
			
			process.waitFor();
			
			GryphonUtil.logInfo("Command finished. Exit Code: " + process.exitValue());
			
			return readerOut.getText();
			
		} catch (Exception e) {
			throw new CommandExecutionException("Exception getting command result.", e);
		}
	}
	
	public static String executeCommand(String cmd, Object... params) {
		return readCmdResponse(executeCommandAsync(cmd, params));
	}
}
