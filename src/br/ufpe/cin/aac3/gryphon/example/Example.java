package br.ufpe.cin.aac3.gryphon.example;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import br.ufpe.cin.aac3.gryphon.Gryphon;
import br.ufpe.cin.aac3.gryphon.Gryphon.ResultFormat;
import br.ufpe.cin.aac3.gryphon.GryphonConfig;
import br.ufpe.cin.aac3.gryphon.GryphonUtil;
import br.ufpe.cin.aac3.gryphon.model.Database;
import br.ufpe.cin.aac3.gryphon.model.Ontology;

public final class Example {
	public static void main(String[] args) {
		// 1. Configure
		GryphonConfig.setWorkingDirectory(new File("integrationExample"));
		GryphonConfig.setLogEnabled(true);
		GryphonConfig.setShowLogo(true);
		Gryphon.init();
		
		// 2. Set the global ontology and local sources
		//loadExample1();
		// or
		//loadExample2();
		
		loadUniprotExample();
		
		// 3. Aligns ontologies and maps databases
		Gryphon.alignAndMap();

		// 4. Query Using SPARQL
//		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//				+ "SELECT DISTINCT ?x ?y "
//				+ "WHERE { ?x a ?y } "
//				+ "LIMIT 100";
		Gryphon.query(loadUniprotQuery1(), ResultFormat.JSON);
	
		GryphonUtil.logInfo("Finished!");
		System.exit(0);
	} 
	
	private static String loadUniprotQuery1() {
		return "SELECT  DISTINCT ?x WHERE {?x a <http://purl.obolibrary.org/obo/GO_0008150> . "
			+ "?x <http://purl.org/biotop/btl2.owl#hasParticipant> ?s2 . "
			+ "?s2 a <http://purl.obolibrary.org/obo/PR_000000001> . "
			+ "?x <http://purl.org/biotop/btl2.owl#isIncludedIn> ?s3 . "
			+ "?s3 a <http://purl.org/biotop/btl2.owl#Organism> . "
			+ "}";
	}

	private static URI getUniprotURI(String file) {
		try {
			return new URI(GryphonUtil.getCurrentURI() + "examples/uniprot/Modularization/" + file);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void loadUniprotExample() {
		Gryphon.setGlobalOntology(new Ontology("integrativo-modified", getUniprotURI("integrativo-gryphon-standalone.owl")));
		Gryphon.addLocalOntology(new Ontology("chebi", getUniprotURI("chebi_module.owl")));
		Gryphon.addLocalOntology(new Ontology("go", getUniprotURI("go_module-modified.owl")));
		Gryphon.addLocalOntology(new Ontology("ncbitaxon", getUniprotURI("ncbitaxon.owl")));
		Gryphon.addLocalOntology(new Ontology("pr", getUniprotURI("pr_module.owl")));
		Gryphon.addLocalOntology(new Ontology("chebi", getUniprotURI("SNOMED_module.owl")));
		Gryphon.addLocalDatabase(new Database("localhost", 3306, "root", "admin123", "uniprot", Gryphon.DBMS.MySQL));
	}
	
	// 2 Ontologies, 1 Database
	private static void loadExample1() {		
		try {
			Ontology globalOntBibtex = new Ontology("globalBibtex", new URI(GryphonUtil.getCurrentURI() + "examples/ex1/global_bibtex.owl"));
			Ontology localOnt1 = new Ontology("bibtex", new URI(GryphonUtil.getCurrentURI() + "examples/ex1/bibtex.owl"));
			Ontology localOnt2 = new Ontology("publication", new URI(GryphonUtil.getCurrentURI() + "examples/ex1/publication.owl"));
			Database localDB1 = new Database("localhost", 3306, "root", "admin123", "bibtex", Gryphon.DBMS.MySQL);
			
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