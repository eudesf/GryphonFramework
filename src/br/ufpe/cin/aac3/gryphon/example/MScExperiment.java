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
	public static void main2(String[] args) {
		System.out.println(getQueryGen1().replaceAll("\\s{1,}", " "));
	}
	
	public static void main(String[] args) {
		// 1. Configure
		GryphonConfig.setWorkingDirectory(new File("integrationMScExperiment"));
		GryphonConfig.setLogEnabled(true);
		GryphonConfig.setShowLogo(false);
		Gryphon.init();

		try {
			// 2. Set the global ontology and local sources
			Ontology globalOntNews = new Ontology("news", new URI(GryphonUtil.getCurrentURI() + "mscExperiment/news.owl"));
			Ontology localOntSioc = new Ontology("sioc", new URI(GryphonUtil.getCurrentURI() + "mscExperiment/sioc.owl"));
			Ontology localOntRnews = new Ontology("rnews", new URI(GryphonUtil.getCurrentURI() + "mscExperiment/rnews.owl"));
			Database localDBJoomla = new Database("localhost", 3306, "root", "", "joomla", Gryphon.DBMS.MySQL);
			Database localDBWordPress = new Database("localhost", 3306, "root", "", "wordpress", Gryphon.DBMS.MySQL);
			
			Gryphon.setGlobalOntology(globalOntNews);
			Gryphon.addLocalOntology(localOntSioc);
			Gryphon.addLocalOntology(localOntRnews);
			Gryphon.addLocalDatabase(localDBJoomla);
			Gryphon.addLocalDatabase(localDBWordPress);

			// 3. Aligns ontologies and maps databases
			//Gryphon.alignAndMap();

			// 4. Query Using SPARQL
			long startTime = System.currentTimeMillis();
			
			String query = getQueryGen1();
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
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX news: <http://ebiquity.umbc.edu/ontology/news.owl#> "
				+ "PREFIX dc: <http://purl.org/dc/terms/> "
				+ "PREFIX sioc: <http://rdfs.org/sioc/ns#> "
				+ "PREFIX vocab: <http://localhost:2020/vocab/> "
				+ "SELECT DISTINCT * "
				+ "WHERE { "
					+ "?x a news:News ; "
					+ "news:title ?title ; "
					+ "news:publishedOn ?date . "
					+ "FILTER (?date >= '2015-08-17'^^xsd:dateTime) . "
				+ "}";
	}
	
	// Q1: Retrieve all news
	private static String getQuery1(){
		return ""
				+ "PREFIX news: <http://ebiquity.umbc.edu/ontology/news.owl#> "
				+ "SELECT DISTINCT ?title ?date ?category "
				+ "WHERE { "
					+ "?x a news:News ; "
					+ "news:title ?title ; "
					+ "news:publishedOn ?date ; "
					+ "news:category ?category . "
				+ "}";
	}
	
	private static String getQueryGen1() {
		
		return "SELECT DISTINCT  ?x" +
			   " WHERE\n" +
			   " { ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/biotop/btl2.owl#Organism> ." + 
			   " ?x <http://purl.org/biotop/btl2.owl#includes> ?s0 ." +
			   " ?s0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/CHEBI_17230>" +
			   " } ";

	}
	
	// Q2: SELECT ALL BIOLOGICAL PROCESS THAT IS INCLUDED IN ORGANISMS
	// BIOLOGICAL_PROCESS btl2:isIncludedIn ORGANISM
	private static String getQuery2(){
		return ""
				+ "PREFIX news: <http://ebiquity.umbc.edu/ontology/news.owl#> "
				+ "SELECT DISTINCT ?title "
				+ "WHERE { "
					+ "?x a news:News ; "
					+ "news:title ?title ; "
					+ "news:category 'Science' . "
				+ "}";
	}
	
	// Q3: Retrieve news that are not from science
	private static String getQuery3(){
		return ""
				+ "PREFIX news: <http://ebiquity.umbc.edu/ontology/news.owl#> "
				+ "SELECT DISTINCT ?title ?category "
				+ "WHERE { "
					+ "?x a news:News ; "
					+ "news:title ?title ; "
					+ "news:category ?category . "
					+ "FILTER NOT EXISTS { "
						+ "FILTER (?category = 'Science') . "
					+ " } . "
				+ "}";
	}
	
	// Q4: Retrieve news starting in 08/17/2015
	private static String getQuery4(){
		return ""
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX news: <http://ebiquity.umbc.edu/ontology/news.owl#> "
				+ "SELECT DISTINCT ?title ?date "
				+ "WHERE { "
					+ "?x a news:News ; "
					+ "news:title ?title ; "
					+ "news:publishedOn ?date . "
					+ "FILTER (?date >= '2015-08-17'^^xsd:dateTime) . "
				+ "} ORDER BY ?date";
	}
	
	// Q5: Retrieve news with 'planet' keyword
	private static String getQuery5(){
		return ""
				+ "PREFIX news: <http://ebiquity.umbc.edu/ontology/news.owl#> "
				+ "SELECT DISTINCT ?title "
				+ "WHERE { "
					+ "?x a news:News ; "
					+ "news:title ?title . "
					+ "FILTER (regex(?title, 'planet', 'i')) . "
				+ "}";
	}
}