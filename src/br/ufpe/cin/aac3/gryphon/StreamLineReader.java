package br.ufpe.cin.aac3.gryphon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamLineReader implements Runnable {

	private StringBuilder text = new StringBuilder();
	private InputStream in;
	private String prefix;
	
	public StreamLineReader(InputStream in, String prefix) {
		super();
		this.in = in;
		this.prefix = prefix;
	}

	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				text.append(line);
				text.append("\n");
				System.out.println(prefix + ":  " + line);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getText() {
		return text.toString();
	}
	
}
