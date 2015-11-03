package br.ufpe.cin.aac3.gryphon.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import br.ufpe.cin.aac3.gryphon.Gryphon;
import br.ufpe.cin.aac3.gryphon.GryphonUtil;

public final class Ontology {
	protected File alignFile = null;
	protected File resultFile = null;
	
	protected String name = null;
	protected URI uri = null;
	protected File localImports = null;
	
	public Ontology(String name, URI uri) {
		this(name, uri, null);
	}
	
	public Ontology(String name, URI uri, File localImports) {
		alignFile = new File(Gryphon.getAlignFolder().getAbsolutePath(), "ont_" + name + ".rdf");
		resultFile = new File(Gryphon.getResultFolder().getAbsolutePath(), "ont_" + name);
		
		this.name = name;
		this.uri = uri;
		this.localImports = localImports;
		
		GryphonUtil.logInfo("Loading ontology: " + uri.toString());
		if(!new File(uri).exists()){
			try {
				throw new FileNotFoundException("Ontology not found: " + uri.toString());
			} catch(Exception e){ }
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
	
	public File getLocalImports() {
		return localImports;
	}
}