package br.ufpe.cin.aac3.gryphon.model;

import java.io.File;
import java.net.URI;

import br.ufpe.cin.aac3.gryphon.Gryphon;
import br.ufpe.cin.aac3.gryphon.GryphonConfig;

import com.hp.hpl.jena.ontology.OntModel;

public abstract class Ontology {
	protected URI uri = null;
	protected OntModel model = null;
	
	public Ontology(URI uri) {
		this.uri = uri;
	}
	
	public void align(URI globalOntologyURI, String name) {
		File alignmentFile = new File(GryphonConfig.getWorkingDirectory().toFile(), "ont_" + name + ".owl");
		Gryphon.alignOntology(globalOntologyURI, getURI(), alignmentFile);
	}
	
	
	public URI getURI() {
		return uri;
	}
	public OntModel getModel() {
		return model;
	}
}