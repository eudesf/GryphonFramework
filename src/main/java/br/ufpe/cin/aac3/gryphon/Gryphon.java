package br.ufpe.cin.aac3.gryphon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

import br.ufpe.cin.aac3.gryphon.model.Database;
import br.ufpe.cin.aac3.gryphon.model.Ontology;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public final class Gryphon {
	public static final String VERSION = "1.0";
	private static final String REPOSITORY_ID = "gryphon-repo";

	private static File alignFolder;
	private static File mapFolder;
	private static File resultFolder;
	
	private static Ontology globalOntology;
	private static List<Ontology> localOntologies;
	private static List<Database> localDatabases;

	private Gryphon() { }

	public static void init(){
		PropertyConfigurator.configure(Gryphon.class.getResource("/log4j.properties"));
		
		if(GryphonConfig.showGryphonLogoOnConsole() && GryphonConfig.isLogEnabled()){
			System.out.println(
				   "\n          _          (`-. "
				 + "\n          \\`----.    ) ^_`)    GRYPHON v" + VERSION
				 + "\n   ,__     \\__   `\\_/  ( `     A Framework for Semantic Integration"
				 + "\n    \\_\\      \\__  `|   }"
				 + "\n       \\  .--' \\__/    }       By Adriel Café, Filipe Santana, Fred Freitas"
				 + "\n       ))/   \\__,<  /_/               {aac3, fss3, fred}@cin.ufpe.br"
				 + "\n       ((|  _/_/ `\\ \\_\\_"
				 + "\n        `\\_____\\\\  )__\\_\\"
				 + "\n"
			);
		}
		
		alignFolder = new File(GryphonConfig.getWorkingDirectory().getAbsolutePath(), "alignments");
		mapFolder = new File(GryphonConfig.getWorkingDirectory().getAbsolutePath(), "mappings");
		resultFolder = new File(GryphonConfig.getWorkingDirectory().getAbsolutePath(), "results");
		
		alignFolder.mkdirs();
		mapFolder.mkdirs();
		resultFolder.mkdirs();
		
		localOntologies = new ArrayList<>();
		localDatabases = new ArrayList<>();
	}
	
	public static void align() {
		align(false);
	}
	
	
	public static void align(boolean async) {
		LinkedHashMap<URI, Process> alignmentProcesses = new LinkedHashMap<>();
		
		if(!localOntologies.isEmpty()){
			GryphonUtil.logInfo("Aligning ontologies...");
			for (Ontology ontology : localOntologies) {
				Process process = startOntologyAlignment(ontology.getURI(), ontology.getAlignFile());
				if (async) {
					alignmentProcesses.put(ontology.getURI(), process);
					GryphonUtil.logInfo(String.format("> Aligning ontology %s in background...", ontology.getName()));
				} else {
					CommandUtil.readCmdResponse(process);
					GryphonUtil.logInfo(String.format("> Ontology %s was aligned", ontology.getName()));
				}
			}
		}
		
		if(!localDatabases.isEmpty()){
			GryphonUtil.logInfo("Mapping and aligning databases...");
			for (Database database : localDatabases) {
				mapDatabase(database);
				
				Process process = startOntologyAlignment(database.getAlignFile().toURI(), database.getAlignFile());
				if (async) {
					alignmentProcesses.put(database.getAlignFile().toURI(), process);
					GryphonUtil.logInfo(String.format("> Database %s was mapped, aligning it...", database.getDbName()));
				} else {
					CommandUtil.readCmdResponse(process);
					GryphonUtil.logInfo(String.format("> Database %s was mapped and aligned", database.getDbName()));
				}
			}
		}
		
		if (async) {
			GryphonUtil.logInfo("> Waiting for ontologies alignment...");
			for (URI uri : alignmentProcesses.keySet()) {
				String response = CommandUtil.readCmdResponse(alignmentProcesses.get(uri));
				GryphonUtil.logInfo(String.format("> Alignment of %s completed. Cmd Response: %s", uri, response));
			}
		}
		
	}
	
	private static Process startOntologyAlignment(URI localOntologyURI, File alignFile) {
		File jarFile = new File("libs/aml/AgreementMakerLight.jar");
		
		return CommandUtil.executeCommandAsync("cd \"%s\" && java -jar \"%s\" -s \"%s\" -t \"%s\" -o \"%s\" -m", 
				jarFile.getParentFile().getAbsolutePath(), 
				jarFile.getAbsolutePath(), 
				new File(globalOntology.getURI()).getAbsolutePath(), 
				new File(localOntologyURI).getAbsolutePath(), 
				alignFile.getAbsolutePath());
	}
	
	
	private static void mapDatabase(Database db) {
		String mapping = null;
		
		try {
			File scriptFile = new File("libs/d2rq/generate-mapping" + (GryphonUtil.isWindows() ? ".bat" : ""));
			
			CommandUtil.executeCommand("\"%s\" -o \"%s\" -u \"%s\" -p \"%s\" \"%s\"", 
					scriptFile.getAbsolutePath(), 
					db.getMapFile().getAbsolutePath(), 
					db.getUsername(), 
					db.getPassword(), 
					db.getJdbcURL());
			
			mapping = FileUtils.readFileToString(db.getMapFile(), "utf-8");
			
			Files.write(Paths.get(db.getMapFile().toURI()), mapping.getBytes());
			String d2rqNS = "http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#";
			String rdfNS = "http://localhost:2020/vocab/";
			FileWriter fileWriter = new FileWriter(db.getAlignFile());
			Model ttlModel = FileManager.get().loadModel(db.getMapFile().toURI().toString());
			OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			owlModel.createOntology(rdfNS);

			for (StmtIterator i = ttlModel.listStatements(); i.hasNext();) {
				Statement s = i.nextStatement();
				if (s.getPredicate().toString().equals(d2rqNS + "class"))
					owlModel.createClass(rdfNS + s.getSubject().getLocalName());
			}

			for (StmtIterator i = ttlModel.listStatements(); i.hasNext();) {
				Statement s = i.nextStatement();
				Resource r = null;
				String id = null;
				
				if (s.getPredicate().toString().equals(d2rqNS + "property")) {
					id = s.getSubject().getLocalName();
					owlModel.createDatatypeProperty(rdfNS + id);					
				} else if (s.getPredicate().toString().equals(d2rqNS + "belongsToClassMap")) {
					id = s.getSubject().getLocalName();
					r = owlModel.getResource(rdfNS + s.getObject().asResource().getLocalName());
					owlModel.getDatatypeProperty(rdfNS + id).setDomain(r);
				}
			}

			owlModel.write(fileWriter, "RDF/XML-ABBREV");
			owlModel.close();
		} catch (Exception e) {
			GryphonUtil.logError(e.getMessage());
		}
	}

	
	public static void query(String strQueryGlobal){
		String strQueryLocal = null;
		TupleQuery queryLocal = null;

		try {
			FileUtils.cleanDirectory(resultFolder);
		} catch(Exception e){ }

		LocalRepositoryManager repositoryManager = new LocalRepositoryManager(GryphonConfig.getWorkingDirectory());
		RepositoryConfig repConfig = new RepositoryConfig(REPOSITORY_ID, new SailRepositoryConfig(new MemoryStoreConfig()));
		try {
			repositoryManager.initialize();
			repositoryManager.addRepositoryConfig(repConfig);
		} catch(Exception e){
			GryphonUtil.logError(e.getMessage());
		}
		
		try {
			Repository repository = repositoryManager.getRepository(REPOSITORY_ID);
			for(Ontology ontology : localOntologies){
				final RepositoryConnection repositoryConnection = repository.getConnection();
				repositoryConnection.add(new File(ontology.getURI()), ontology.getURI().toString(), RDFFormat.RDFXML);
				
				strQueryLocal = queryRewrite(strQueryGlobal, ontology.getAlignFile());
				queryLocal = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, strQueryLocal);
				if(queryLocal != null){
					GryphonUtil.logInfo("\nRewritten query for " + ontology.getName() + ":\n" + strQueryLocal);
					execSPARQLQuery(queryLocal, ontology.getResultFile());
				}

				repositoryConnection.close();
			}
		} catch(Exception e){
			GryphonUtil.logError(e.getMessage());
		}
		
		for(Database database : localDatabases){			
			strQueryLocal = queryRewrite(strQueryGlobal, database.getAlignFile());
			if(strQueryLocal != null){
				GryphonUtil.logInfo("\nRewritten query for " + database.getDbName() + ":\n" + strQueryLocal);
				execSQLQuery(strQueryLocal, database.getMapFile(), database.getResultFile());
			}
		}

		unifyQueries();
	}
	
	private static void execSPARQLQuery(TupleQuery query, File resultFile) {
		try {
			SPARQLResultsJSONWriter sparqlWriter = new SPARQLResultsJSONWriter(new FileOutputStream(resultFile));
			query.evaluate(sparqlWriter);
		} catch(Exception e){
			GryphonUtil.logError(e.getMessage());
		}
	}

	private static void execSQLQuery(String styrQuery, File mapFile, File resultFile) {
		try {
			Query query = QueryFactory.create(styrQuery);
			File batFile = new File("libs/d2rq/d2r-query" + (GryphonUtil.isWindows() ? ".bat" : ""));
			CommandUtil.executeCommand("\"%s\" -f json \"%s\" \"%s\" > \"%s\"", batFile.getAbsolutePath(), mapFile.getAbsolutePath(), query.toString(Syntax.syntaxSPARQL_11), resultFile.getAbsoluteFile());
		} catch (Exception e) {
			GryphonUtil.logError(e.getMessage());
		}
	}

	private static String queryRewrite(String query, File alignFile) {
		File jarFile = new File("libs/mediation/mediation.jar");
		
		return CommandUtil.executeCommand("cd \"%s\" && java -jar \"%s\" \"%s\" \"%s\"", 
				jarFile.getParentFile().getAbsolutePath(), 
				jarFile.getAbsolutePath(), 
				alignFile.getAbsolutePath(), 
				query.replace("\n", " "));
	}
	
	private static void unifyQueries(){
		
	}
	
	public static File getAlignFolder() {
		return alignFolder;
	}
	
	public static File getMapFolder() {
		return mapFolder;
	}
	
	public static File getResultFolder() {
		return resultFolder;
	}

	public static Ontology getGlobalOntology() {
		return globalOntology;
	}
	
	public static void setGlobalOntology(Ontology globalOntology) {
		Gryphon.globalOntology = globalOntology;
	}
	
	public static List<Ontology> getLocalOntologies() {
		return localOntologies;
	}
	
	public static void addLocalOntology(Ontology ont){
		localOntologies.add(ont);
	}
	
	public static void removeLocalOntology(String name){
		for(Iterator<Ontology> i = localOntologies.iterator(); i.hasNext(); ){
			if(i.next().getName().equals(name)){
				i.remove();
				break;
			}
		}
	}
	
	public static void removeLocalOntology(int index){
		localOntologies.remove(index);
	}
	
	public static List<Database> getLocalDatabases() {
		return localDatabases;
	}
	
	public static void addLocalDatabase(Database db){
		localDatabases.add(db);
	}
	
	public static void removeLocalDatabase(String dbName){
		for(Iterator<Database> i = localDatabases.iterator(); i.hasNext(); ){
			if(i.next().getDbName().equals(dbName)){
				i.remove();
				break;
			}
		}
	}
	
	public static void removeLocalDatabase(int index){
		localDatabases.remove(index);
	}
}