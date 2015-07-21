package br.ufpe.cin.aac3.gryphon.example;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import br.ufpe.cin.aac3.gryphon.Gryphon;
import br.ufpe.cin.aac3.gryphon.GryphonConfig;
import br.ufpe.cin.aac3.gryphon.GryphonUtil;
import br.ufpe.cin.aac3.gryphon.model.Database;
import br.ufpe.cin.aac3.gryphon.model.Ontology;

public class MScExperiment {
	public static void main(String[] args) {
		// 1. Configure
		GryphonConfig.setWorkingDirectory(new File("integrationMScExperiment"));
		GryphonConfig.setLogEnabled(true);
		GryphonConfig.setShowGryphonLogoOnConsole(true);
		Gryphon.init();

		try {
			Ontology globalOnt = new Ontology("integrativo", new URI(GryphonUtil.getCurrentURI() + "mscExperiment/integrativO.owl"));
			Ontology localOnt1 = new Ontology("ncbi", new URI(GryphonUtil.getCurrentURI() + "mscExperiment/ncbi.rdf"));
			Ontology localOnt2 = new Ontology("uniprot", new URI(GryphonUtil.getCurrentURI() + "mscExperiment/uniprot-homocysteine.rdf"));
			Database localDB1 = new Database("useastdb.ensembl.org", 3306, "anonymous", "", "ensembl_ontology_79", Database.DBMS.MySQL);
			
			Gryphon.setGlobalOntology(globalOnt);
			Gryphon.addLocalOntology(localOnt1);
			Gryphon.addLocalOntology(localOnt2);
			Gryphon.addLocalDatabase(localDB1);
		} catch(URISyntaxException e){
			e.printStackTrace();
		}

		// 2. Align (and Map) the Sources
		Gryphon.align();

		// 3. Query Using SPARQL
		String strQuery = getQuery1();
		Gryphon.query(strQuery);
		
		
		System.exit(0);
	} 

	// Q1: Retrieve  metabolic process related to homocysteine
	// DL Query: MetabolicProcess and (hasParticipant some homocysteine)
	private static String getQuery1(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT ?metProcess ?homocysteine "
			+ "WHERE { "
				+ "?metProcess rdfs:label ?label1 . "
				+ "?homocysteine rdfs:label ?label2 . "
				+ "?metProcess btl2:hasParticipant ?homocysteine . "
				+ "filter(regex(?label1, \"metabolic process\")) . "
				+ "filter(regex(?label2, \"homocysteine\"))"
			+ "}";
	}
	
	// Q2: Retrieve proteins related to the metabolism of homocysteine in Drosophila melanogaster
	// DL Query: Protein and (isParticipantIn some (MetabolicProcess and  (hasParticipant some homocysteine)))
	private static String getQuery2(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT ?protein ?metProcess ?homocysteine "
			+ "WHERE { "
				+ "?protein rdfs:label ?label1 . "
				+ "?metProcess rdfs:label ?label2 . "
				+ "?homocysteine rdfs:label ?label3 . "
				+ "?protein btl2:isParticipantIn ?metProcess . "
				+ "?metProcess btl2:hasParticipant ?homocysteine . "
				+ "filter(regex(?label1, \"protein\")) . "
				+ "filter(regex(?label2, \"metabolic process\")) . "
				+ "filter(regex(?label3, \"homocysteine\")) "
			+ "}";
	}
	
	// Q3: Retrieve metabolic processes homocysteine is participant in Bos taurus
	// DL Query: MetabolicProcess and (hasParticipant some homocysteine) and (isLocatedIn some BosTaurus)
	private static String getQuery3(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "SELECT ?metProcess ?homocysteine ?bosTaurus "
			+ "WHERE { "
				+ "?metProcess rdfs:label ?label1 . "
				+ "?homocysteine rdfs:label ?label2 . "
				+ "?bosTaurus rdfs:label ?label3 . "
				+ "?metProcess btl2:hasParticipant ?homocysteine . "
				+ "?metProcess btl2:isLocatedIn ?bosTaurus . "
				+ "filter(regex(?label1, \"metabolic process\")) . "
				+ "filter(regex(?label2, \"homocysteine\")) . "
				+ "filter(regex(?label3, \"bos taurus\")) "
			+ "}";
	}
	
	// Q4: Retrieve molecules that regulates the disposition on realizing molecular processes that have homocysteine as participant in Arapdopsis haliana
	// DL Query: MolecularEntity and (regulates some (MolecularActivity and (hasPart some (MetabolicProcess and (hasParticipant some homocysteine) and (isIncludedIn some ArapdopsisThaliana)))))
	private static String getQuery4(){
		return ""
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX btl2: <http://purl.org/biotop/btl2.owl#> "
			+ "PREFIX go: <http://purl.obolibrary.org/obo/go#> "
			+ "SELECT ?molEntity ?molActivity ?metProcess ?homocysteine ?araThaliana " 
			+ "WHERE { "
				+ "?molEntity rdfs:label ?label1 . "
				+ "?molActivity rdfs:label ?label2 . "
				+ "?metProcess rdfs:label ?label3 . "
				+ "?homocysteine rdfs:label ?label4 . "
				+ "?araThaliana rdfs:label ?label5 . "
				+ "?molEntity go:regulates ?molActivity . "
				+ "?molActivity btl2:hasPart ?metProcess . "
				+ "?metProcess btl2:hasParticipant ?homocysteine . "
				+ "?metProcess btl2:isIncludedIn ?araThaliana . "
				+ "filter(regex(?label1, \"molecular entity\")) . "
				+ "filter(regex(?label2, \"molecular activity\")) . " 
				+ "filter(regex(?label3, \"metabolic process\")) . " 
				+ "filter(regex(?label4, \"homocysteine\")) . "
				+ "filter(regex(?label5, \"arapdopsis thaliana\"))"
			+ "}";
	}
}