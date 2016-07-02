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
//		Gryphon.alignXAndMap();

		// 4. Query Using SPARQL
//		String strQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//				+ "SELECT DISTINCT ?x ?y "
//				+ "WHERE { ?x a ?y } "
//				+ "LIMIT 100";
		//Gryphon.query(loadUniprotQuery1(), ResultFormat.JSON);
		Gryphon.query(BO_isIncludedIn_CO_hasRealization_P_WithLabels(), ResultFormat.JSON);
	
		GryphonUtil.logInfo("Finished!");
		System.exit(0);
	} 

	private static String BO_isIncludedIn_CO_hasRealization_P_WithLabels() {
		return "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"SELECT DISTINCT ?labelx ?labels2 ?labels3 WHERE {?x a <http://purl.obolibrary.org/obo/GO_0008150> .\n" +
			"?x <http://purl.org/biotop/btl2.owl#hasRealization> ?s2 .\n" +
			"?s2 a <http://purl.obolibrary.org/obo/PR_000000001> .\n" +
			"?x <http://purl.org/biotop/btl2.owl#isIncludedIn> ?s3 .\n" +
			"?s3 a <http://purl.bioontology.org/ontology/NCBITAXON/131567> .\n" +
			"?x rdfs:label ?labelx .\n" +
			"?s2 rdfs:label ?labels2 .\n" +
			"?s3 rdfs:label ?labels3 .\n" +
			"} LIMIT 30";
	}
	
	private static String BO_isIncludedIn_CO_WithLabels() {
		return "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
			"SELECT DISTINCT ?labelx ?labels2 WHERE {?x a <http://purl.obolibrary.org/obo/GO_0008150> .\n" +
			"?x <http://purl.org/biotop/btl2.owl#isIncludedIn> ?s1 .\n" +
			"?s1 a <http://purl.bioontology.org/ontology/NCBITAXON/131567> .\n" +
			"?x rdfs:label ?labelx .\n" +
			"?s1 rdfs:label ?labels1 .\n" +
			"} LIMIT 30";
	}
	
	private static String loadBiologicalProcessTest() {
		return "SELECT  DISTINCT ?x WHERE {?x a <http://purl.obolibrary.org/obo/GO_0008150> . } LIMIT 30";
	}

	private static String loadCellularOrganismsTest() {
		return "SELECT  DISTINCT ?x WHERE {?x a <http://purl.bioontology.org/ontology/NCBITAXON/131567> . } LIMIT 30";
	}
	
	private static URI getUniprotURI(String file) {
		try {
			return new URI(GryphonUtil.getCurrentURI() + "examples/uniprot/Modularization/" + file);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void loadUniprotExample() {
		Gryphon.setGlobalOntology(new Ontology("integrativo", getUniprotURI("integrativo.owl")));
//		Gryphon.addLocalOntology(new Ontology("chebi", getUniprotURI("chebi_module.owl")));
//		Gryphon.addLocalOntology(new Ontology("go", getUniprotURI("go_module.owl")));
//		Gryphon.addLocalOntology(new Ontology("ncbitaxon", getUniprotURI("ncbitaxon.owl")));
//		Gryphon.addLocalOntology(new Ontology("pr", getUniprotURI("pr_module.owl")));
//		Gryphon.addLocalOntology(new Ontology("chebi", getUniprotURI("SNOMED_module.owl")));
		Gryphon.addLocalDatabase(new Database("localhost", 3306, "root", "", "uniprot", Gryphon.DBMS.MySQL));
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