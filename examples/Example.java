package br.ufpe.cin.aac3.gryphon.example;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import br.ufpe.cin.aac3.gryphon.Gryphon;
import br.ufpe.cin.aac3.gryphon.GryphonConfig;
import br.ufpe.cin.aac3.gryphon.GryphonUtil;
import br.ufpe.cin.aac3.gryphon.model.Database;
import br.ufpe.cin.aac3.gryphon.model.Ontology;

public class Example {
	public static void main(String[] args) {
		// 1. Configure
		GryphonConfig.setWorkingDirectory(new File("integrationExample"));
		GryphonConfig.setLogEnabled(true);
		GryphonConfig.setShowGryphonLogoOnConsole(true);
		Gryphon.init();
		
		loadExample1();
		// or
		//loadExample2();
		
		// 2. Align (and Map) the Sources
		Gryphon.align();

		// 3. Query Using SPARQL
		String strQuery = 
				 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+"SELECT ?x ?y "
				+"WHERE { ?x rdf:type ?y }";
		Gryphon.query(strQuery);
		
		
		System.exit(0);
	} 
	
	// 2 Ontologies, 1 Database
	private static void loadExample1() {		
		try {
			Ontology globalOntBibtex = new Ontology("globalBibtex", new URI(GryphonUtil.getCurrentURI() + "examples/ex1/global_bibtex.owl"));
			Ontology localOnt1 = new Ontology("bibtex", new URI(GryphonUtil.getCurrentURI() + "examples/ex1/bibtex.owl"));
			Ontology localOnt2 = new Ontology("publication", new URI(GryphonUtil.getCurrentURI() + "examples/ex1/publication.owl"));
			Database localDB1 = new Database("localhost", 3306, "root", "", "bibtex", Database.DBMS.MySQL);
			
			Gryphon.setGlobalOntology(globalOntBibtex);
			Gryphon.addLocalOntology(localOnt1);
			Gryphon.addLocalOntology(localOnt2);
			Gryphon.addLocalDatabase(localDB1);
		} catch(URISyntaxException e){
			e.printStackTrace();
		}
	}
	
	// 3 Ontologies
	private static void loadExample2() {		
		try {
			Ontology global = new Ontology("globalHuman", new URI(GryphonUtil.getCurrentURI() + "examples/ex2/human.owl"));
			Ontology localOnt1 = new Ontology("fly", new URI(GryphonUtil.getCurrentURI() + "examples/ex2/fly.owl"));
			Ontology localOnt2 = new Ontology("mouse", new URI(GryphonUtil.getCurrentURI() + "examples/ex2/mouse.owl"));
			Ontology localOnt3 = new Ontology("zebraFish", new URI(GryphonUtil.getCurrentURI() + "examples/ex2/zebrafish.owl"));
			
			Gryphon.setGlobalOntology(global);
			Gryphon.addLocalOntology(localOnt1);
			Gryphon.addLocalOntology(localOnt2);
			Gryphon.addLocalOntology(localOnt3);
		} catch(URISyntaxException e){ 
			e.printStackTrace(); 
		}
	}
}