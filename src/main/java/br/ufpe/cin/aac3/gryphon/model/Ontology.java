package br.ufpe.cin.aac3.gryphon.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import br.ufpe.cin.aac3.gryphon.Gryphon;
import br.ufpe.cin.aac3.gryphon.GryphonUtil;

public class Ontology {
	protected File alignFile = null;
	protected File resultFile = null;
	
	protected String name = null;
	protected URI uri = null;
	
	public Ontology(String name, URI uri) {
		alignFile = new File(Gryphon.getAlignFolder().getAbsolutePath(), "ont_" + name + ".rdf");
		resultFile = new File(Gryphon.getResultFolder().getAbsolutePath(), "ont_" + name + ".json");
		
		this.name = name;
		this.uri = uri;
		
		GryphonUtil.logInfo("Loading ontology: " + uri.toString());
		if(!new File(uri).exists()){
			try {
				throw new FileNotFoundException("Ontology not found: " + uri.toString());
			} catch(Exception e){
				GryphonUtil.logError("Ontology not found: " + uri.toString());
			}
		}
	}
	
	public File getAlignFile() {
		return alignFile;
	}
	
	public File getResultFile() {
		return resultFile;
	}
	
	public String getName() {
		return name;
	}
	
	public URI getURI() {
		return uri;
	}
}