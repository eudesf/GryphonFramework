package br.ufpe.cin.aac3.gryphon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.config.MemoryStoreConfig;

import br.ufpe.cin.aac3.gryphon.model.Database;
import br.ufpe.cin.aac3.gryphon.model.Ontology;

public final class Gryphon {
	public static final String VERSION = "1.0";
	private static final String REPOSITORY_ID = "gryphon-repo";

	private static File alignFolder;
	private static File mapFolder;
	private static File resultFolder;
	
	private static Ontology globalOntology;
	private static List<Ontology> localOntologies;
	private static List<Database> localDatabases;
	
	public enum ResultFormat {
		JSON, XML, CSV
	}

	private Gryphon() { }

	/**
	 * Initiates Gryphon Framework
	 */
	public static void init(){
		PropertyConfigurator.configure("log4j.properties");
		
		if(GryphonConfig.isShowLogo() && GryphonConfig.isLogEnabled()){
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
	
	/**
	 * Aligns ontologies and maps databases
	 */
	public static void alignAndMap() {
		if(!localOntologies.isEmpty()){
			GryphonUtil.logInfo("Aligning ontologies...");
			for (Ontology ontology : localOntologies) {
				alignOntology(ontology.getURI(), ontology.getAlignFile());
				GryphonUtil.logInfo(String.format("> Ontology %s was aligned", ontology.getName()));
			}
		}
		
		if(!localDatabases.isEmpty()){
			GryphonUtil.logInfo("Mapping databases...");
			for (Database database : localDatabases) {
				mapDatabase(database);
				// TODO Need improvement
//				alignOntology(database.getMapRDFFile().toURI(), database.getAlignFile());
				GryphonUtil.logInfo(String.format("> Database %s was mapped", database.getDbName()));
			}
		}
	}
	
	/**
	 * Aligns local ontologies with <b>AgreementMakerLight</b> command-line
	 * @param localOntologyURI The URI of local ontology
	 * @param alignFile The alignment file 
	 */
	private static void alignOntology(URI localOntologyURI, File alignFile) {
		try {
			File jarFile = new File("libs/aml/AgreementMakerLight.jar");
			String cmd = String.format("cd \"%s\" && java -jar \"%s\" -s \"%s\" -t \"%s\" -o \"%s\" -m", jarFile.getParentFile().getAbsolutePath(), jarFile.getAbsolutePath(), new File(globalOntology.getURI()).getAbsolutePath(), new File(localOntologyURI).getAbsolutePath(), alignFile.getAbsolutePath());
			if(globalOntology.getLocalImports() != null){
				cmd += " -l " + globalOntology.getLocalImports().getAbsolutePath();
			}
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", cmd);
			Process process = processBuilder.start();
			process.waitFor();
		} catch (Exception e) {
			GryphonUtil.logError(e.getMessage());
		}
	}
	
	/**
	 * Maps local databases with <b>D2RQ</b> command-line
	 * @param db The database to be mapped
	 */
	private static void mapDatabase(Database db) {
		try {
			File scriptFile = new File("libs/d2rq/generate-mapping" + (GryphonUtil.isWindows() ? ".bat" : ""));
			String cmd = String.format("%s \"%s\" -o \"%s\" -u \"%s\" -p \"%s\" \"%s\"", (GryphonUtil.isWindows() ? "" : "bash"), scriptFile.getAbsolutePath(), db.getMapTTLFile().getAbsolutePath(), db.getUsername(), db.getPassword(), db.getJdbcURL());
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", cmd);
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null){}
			process.waitFor();

			// TODO Need improvement
//			String mapping = FileUtils.readFileToString(db.getMapTTLFile(), "utf-8");
//			Files.write(Paths.get(db.getMapTTLFile().toURI()), mapping.getBytes());
//			String d2rqNS = "http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#";
//			String rdfNS = "http://localhost:2020/vocab/";
//			FileWriter fileWriter = new FileWriter(db.getMapRDFFile());
//			Model ttlModel = FileManager.get().loadModel(db.getMapTTLFile().toURI().toString());
//			OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
//			owlModel.createOntology(rdfNS);
//			
//			for (ResIterator i = ttlModel.listSubjects(); i.hasNext();) {
//				Resource r = i.nextResource();
//				if(r.isResource()){
//					String type = r.getProperty(RDF.type).getObject().toString();
//					if(type.endsWith("ClassMap")){
//						OntClass klass = owlModel.createClass(rdfNS + r.getLocalName());
//						try {
//							klass.addLabel(r.getProperty(ttlModel.createProperty(d2rqNS + "classDefinitionLabel")).getObject().toString(), null);
//						} catch(Exception e){ }
//					} else if(type.endsWith("PropertyBridge")){
//						String klass = r.getProperty(ttlModel.createProperty(d2rqNS + "belongsToClassMap")).getResource().getLocalName();
//						DatatypeProperty property = owlModel.createDatatypeProperty(rdfNS + r.getLocalName());
//						property.setDomain(ttlModel.createResource(rdfNS + klass));
//						try {
//							String label = r.getProperty(ttlModel.createProperty(d2rqNS + "propertyDefinitionLabel")).getObject().toString();
//							property.addLabel(label, null);
//						} catch(Exception e){ }
//					}
//				}
//			}
//			
//			RDFWriter writer = owlModel.getWriter("RDF/XML-ABBREV");
//			writer.setProperty("xmlbase", rdfNS);
//			writer.write(owlModel, fileWriter, rdfNS);
		} catch (Exception e) {
			GryphonUtil.logError(e.getMessage());
		}
	}

	/**
	 * Rewrites the SPARQL query to each local ontology and local database
	 * @param strQueryGlobal The query that needs to be rewritten
	 */
	public static void query(String strQueryGlobal, ResultFormat resultFormat){
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
					execOntologyQuery(queryLocal, ontology.getResultFile(), resultFormat);
				}

				repositoryConnection.close();
			}
		} catch(Exception e){
			GryphonUtil.logError(e.getMessage());
		}
		
		for(Database database : localDatabases){
			execDataBaseQuery(strQueryGlobal, database.getMapTTLFile(), database.getResultFile(), resultFormat);
		}
	}

	/**
	 * Rewrites SPARQL queries using <b>Mediation</b>
	 * @param query The query that needs to be rewritten
	 * @param alignFile The alignment file 
	 * @return The rewritten query
	 */
	private static String queryRewrite(String query, File alignFile) {
		try {
			File jarFile = new File("libs/mediation/Mediation.jar");
			String cmd = String.format("cd \"%s\" && java -jar \"%s\" \"%s\" \"%s\"", jarFile.getParentFile().getAbsolutePath(), jarFile.getAbsolutePath(), alignFile.getAbsolutePath(), query.replace("\n", " "));
			ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", cmd);
			Process process = processBuilder.start();
			process.waitFor();
			return GryphonUtil.getStringFromStream(process.getInputStream());
		} catch (Exception e) {
			GryphonUtil.logError(e.getMessage());
			return null;
		}
	}
	
	/**
	 * Runs a SPARQL query using <b>Sesame</b> on ontology
	 * @param query The SPARQL query
	 * @param resultFile The file where the results will be saved
	 */
	private static void execOntologyQuery(TupleQuery query, File resultFile, ResultFormat resultFormat) {
		try {
			switch (resultFormat) {
				case XML: 
					resultFile = new File(resultFile.getAbsolutePath() + ".xml");
					SPARQLResultsXMLWriter xmlWriter = new SPARQLResultsXMLWriter(new FileOutputStream(resultFile));
					query.evaluate(xmlWriter);
					break;
				case CSV: 
					resultFile = new File(resultFile.getAbsolutePath() + ".csv");
					SPARQLResultsCSVWriter csvWriter = new SPARQLResultsCSVWriter(new FileOutputStream(resultFile));
					query.evaluate(csvWriter);
					break;
				case JSON:
				default:
					resultFile = new File(resultFile.getAbsolutePath() + ".json");
					SPARQLResultsJSONWriter jsonWriter = new SPARQLResultsJSONWriter(new FileOutputStream(resultFile));
					query.evaluate(jsonWriter);
			}
		} catch(Exception e){
			GryphonUtil.logError(e.getMessage());
		}
	}

	/**
	 * Runs a SPARQL query using <b>D2RQ</b> on database
	 * @param strQuery The SPARQL query
	 * @param mapFile The database mapping file
	 * @param resultFile The file where the results will be saved
	 */
	private static void execDataBaseQuery(String strQuery, File mapFile, File resultFile, ResultFormat resultFormat) {
		String strResultFormat = null;
		switch (resultFormat) {
			case XML: 
				resultFile = new File(resultFile.getAbsolutePath() + ".xml");
				strResultFormat = "xml";
				break;
			case CSV: 
				resultFile = new File(resultFile.getAbsolutePath() + ".csv");
				strResultFormat = "csv";
				break;
			case JSON: 
			default:
				resultFile = new File(resultFile.getAbsolutePath() + ".json");
				strResultFormat = "json";
		}
		try {
			File batFile = new File("libs\\d2rq\\d2r-query" + (GryphonUtil.isWindows() ? ".bat" : ""));
			String cmd = String.format("\"%s\" -f %s -t 9999 \"%s\" \"%s\" > \"%s\"", batFile.getAbsolutePath(), strResultFormat, mapFile.getAbsolutePath(), strQuery.replaceAll("\n", " "), resultFile.getAbsoluteFile()); 
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (Exception e) {
			GryphonUtil.logError(e.getMessage());
		}
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