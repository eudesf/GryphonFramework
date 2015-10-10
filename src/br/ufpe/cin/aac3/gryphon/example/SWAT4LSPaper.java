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

public final class SWAT4LSPaper {
	public static void main(String[] args) {
		// 1. Configure
		GryphonConfig.setWorkingDirectory(new File("integrationSWAT4LSPaper"));
		GryphonConfig.setLogEnabled(true);
		GryphonConfig.setShowLogo(true);
		Gryphon.init();

		try {
			// 2. Set the global ontology and local sources
			Ontology globalOnt = new Ontology("integrativo", new URI(GryphonUtil.getCurrentURI() + "SWAT4LSPaper/integrativO.owl"), new File("SWAT4LSPaper/sources"));
			Database localDB = new Database("localhost", 3306, "root", "", "uniprot", Gryphon.DBMS.MySQL);
			
			Gryphon.setGlobalOntology(globalOnt);
			Gryphon.addLocalDatabase(localDB);

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
				+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
				+ "SELECT DISTINCT ?organism "
				+ "WHERE { "
					+ "?organismId a btl2:organism ;"
					+ "rdfs:label ?organism ."
					+ "?homocysteineId a btl2:MonoMolecularEntity ."
					+ "?organismId btl2:includes ?homocysteineId ."
				+ "} LIMIT 10";
	}
	
	// Q1: Retrieve organisms that include homocysteine
	private static String getQuery1(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT DISTINCT ?organism "
			+ "WHERE { "
				+ "?organismId a btl2:organism ;"
				+ "rdfs:label ?organism ."
				+ "?homocysteineId a btl2:MonoMolecularEntity ."
				+ "?organismId btl2:includes ?homocysteineId ."
			+ "}";
	}
	
	// Q2: Retrieve biological processes that is included in organisms
	private static String getQuery2(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT DISTINCT ?biologicalProcess ?organism "
			+ "WHERE { "
				+ "?biologicalProcessId a go:biological_process ;"
				+ "rdfs:label ?biologicalProcess ."
				+ "?organismId a btl2:organism ;"
				+ "rdfs:label ?organism ."
				+ "?biologicalProcessId btl2:isIncludedIn ?organismId ."
			+ "}";
	}
	
	// Q3: Retrieve biological processes and the cellular components where they can be located
	private static String getQuery3(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT DISTINCT ?biologicalProcess ?cellularComponent "
			+ "WHERE { "
				+ "?biologicalProcessId a go:biological_process ;"
				+ "rdfs:label ?biologicalProcess ."
				+ "?cellularComponentId a go:cellular_component ;"
				+ "rdfs:label ?cellularComponent ."
				+ "?biologicalProcessId btl2:isIncludedIn ?cellularComponentId ."
			+ "}";
	}
	
	// Q4: Retrieve biological processes promoted by proteins
	private static String getQuery4(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#> "
			+ "PREFIX pr: <http://purl.obolibrary.org/obo/pr#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT DISTINCT ?biologicalProcess ?proteinName "
			+ "WHERE { "
				+ "?biologicalProcessId a go:biological_process ;"
				+ "rdfs:label ?biologicalProcess ."
				+ "?proteinNameId a pr:PR_000000001 ;"
				+ "rdfs:label ?proteinName ."
				+ "?biologicalProcessId btl2:hasAgent ?proteinNameId ."
			+ "}";
	}
}