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

public final class MScExperiment {
	public static void main(String[] args) {
		// 1. Configure
		GryphonConfig.setWorkingDirectory(new File("integrationMScExperiment"));
		GryphonConfig.setLogEnabled(true);
		GryphonConfig.setShowLogo(true);
		Gryphon.init();

		try {
			// 2. Set the global ontology and local sources
			Ontology globalOnt = new Ontology("integrativo", new URI(GryphonUtil.getCurrentURI() + "mscExperiment/TesteGryphon.owl"), new File("mscExperiment/sources"));
			//Database localDB1 = new Database("localhost", 3306, "root", "", "ensembl", Database.DBMS.MySQL);
			Database localDB2 = new Database("localhost", 3306, "root", "", "uniprot", Database.DBMS.MySQL);
			
			Gryphon.setGlobalOntology(globalOnt);
			//Gryphon.addLocalDatabase(localDB1);
			Gryphon.addLocalDatabase(localDB2);

			// 3. Aligns ontologies and maps databases
			//Gryphon.alignAndMap();

			// 4. Query Using SPARQL
			long startTime = System.currentTimeMillis();
			String query = getQuery1();
			Gryphon.query(query, ResultFormat.JSON);
			long endTime = System.currentTimeMillis();
			System.out.println("Query Duration: " + ((endTime - startTime) / 1000 % 60) + "s");
		} catch(URISyntaxException e){
			e.printStackTrace();
		}
		
		System.exit(0);
	} 

	private static String getTestQuery(){
		return ""
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX pr: <http://purl.obolibrary.org/obo/pr#> "
				+ "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#> "
				+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
				+ "PREFIX integrativo: <http://www.cin.ufpe.br/~integrativo#> "
				+ "PREFIX vocab: <http://localhost:2020/vocab/> "
				+ "SELECT DISTINCT ?organism ?homocysteine "
				+ "WHERE { "
					+ "?org a btl2:organism ;"
					+ "rdfs:label ?organism ."
					+ "?hom a btl2:MonoMolecularEntity ;"
					+ "rdfs:label ?homocysteine ."
					+ "?org btl2:includes ?hom ."
					+ "FILTER(?homocysteine = \"homocysteine\") ."
				+ "}";
	}
	
	// Q1: SELECT ALL ORGANISMS THAT INCLUDES HOMOCYSTEINE
	// ORGANISM btl2:includes HOMOCYSTEINE
	private static String getQuery1(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT DISTINCT ?organism "
			+ "WHERE { "
				+ "?org a btl2:organism ;"
				+ "rdfs:label ?organism ."
				+ "?hom a btl2:MonoMolecularEntity ;"
				+ "rdfs:label ?homocysteine ."
				+ "?org btl2:includes ?hom ."
			+ "} LIMIT 10";
	}
	
	// Q2: SELECT ALL BIOLOGICAL PROCESS THAT IS INCLUDED IN ORGANISMS
	// BIOLOGICAL_PROCESS btl2:isIncludedIn ORGANISM
	private static String getQuery2(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT DISTINCT ?biologicalProcess "
			+ "WHERE { "
				+ "?bio a go:biological_process ;"
				+ "rdfs:label ?biologicalProcess ."
				+ "?org a btl2:organism ;"
				+ "rdfs:label ?organism ."
				+ "?bio btl2:isIncludedIn ?org ."
			+ "} LIMIT 10";
	}
	
	// Q3: SELECT ALL BIOLOGICAL_PROCESS AND THE LOCATION THEY HAPPEN (CELLULAR COMPONENT)
	// BIOLOGICAL PROCESS btl2:isIncludedIn CELLULAR_COMPONENT
	private static String getQuery3(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT DISTINCT ?biologicalProcess ?cellularComponent "
			+ "WHERE { "
				+ "?bio a go:biological_process ;"
				+ "rdfs:label ?biologicalProcess ."
				+ "?cel a go:cellular_component ;"
				+ "rdfs:label ?cellularComponent ."
				+ "?bio btl2:isIncludedIn ?cel ."
			+ "} LIMIT 10";
	}
	
	// SELECT BIOLOGICAL PROCESSES PROMOTED BY PROTEINS
	// BIOLOGICAL PROCESS btl2:hasAgent PROTEIN
	private static String getQuery4(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#> "
			+ "PREFIX pr: <http://purl.obolibrary.org/obo/pr#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT DISTINCT ?biologicalProcess ?proteinName "
			+ "WHERE { "
				+ "?bio a go:biological_process ;"
				+ "rdfs:label ?biologicalProcess ."
				+ "?pro a pr:PR_000000001 ;"
				+ "rdfs:label ?proteinName ."
				+ "?bio btl2:hasAgent ?pro ."
			+ "} LIMIT 10";
	}
}