package br.ufpe.cin.aac3.gryphon.model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import br.ufpe.cin.aac3.gryphon.Gryphon;
import br.ufpe.cin.aac3.gryphon.GryphonUtil;

public final class Database {
	protected File mapTTLFile = null;
	protected File mapRDFFile = null;
	protected File alignFile = null;
	protected File resultFile = null;
	
	protected String jdbcDriverClass = null;
	protected String jdbcURL = null;
	protected String host = null;
	protected String username = null;
	protected String password = null;
	protected String dbName = null;
	protected int port = 0;
	
	public Database(String host, int port, String username, String password, String dbName, Gryphon.DBMS dbms) {
		mapTTLFile = new File(Gryphon.getMapFolder().getAbsolutePath(), "db_" + host + "_" + port + "_" + dbName + ".ttl");
		mapRDFFile = new File(Gryphon.getMapFolder().getAbsolutePath(), "db_" + host + "_" + port + "_" + dbName + ".rdf");
		alignFile = new File(Gryphon.getAlignFolder().getAbsolutePath(), "db_" + host + "_" + port + "_" + dbName  + ".rdf");
		resultFile = new File(Gryphon.getResultFolder().getAbsolutePath(), "db_" + host + "_" + port + "_" + dbName);
		
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.dbName = dbName;

		switch (dbms) {
			case MySQL:
				jdbcURL = String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
				jdbcDriverClass = "com.mysql.jdbc.Driver";
				break;
			case PostgreSQL:
				jdbcURL = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
				jdbcDriverClass = "org.postgresql.Driver";
				break;
		}
		
		GryphonUtil.logInfo("Connecting to database: " + jdbcURL);
		if(!testConnection()){
			try {
				throw new SQLException("Can't connect to database: " + jdbcURL); 
			} catch (Exception e) { }
		}
	}
	
	public boolean testConnection() {
		Properties props = new Properties();
		props.put("user", username);
		props.put("password", password);
		
		try {
			Connection connection = DriverManager.getConnection(jdbcURL, props);
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}
	
	public File getAlignFile() {
		return alignFile;
	}
	
	public File getMapTTLFile() {
		return mapTTLFile;
	}
	
	public File getMapRDFFile() {
		return mapRDFFile;
	}
	
	public File getResultFile() {
		return resultFile;
	}

	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getDbName() {
		return dbName;
	}
	
	public String getJdbcURL() {
		return jdbcURL;
	}
	
	public String getJdbcDriverClass() {
		return jdbcDriverClass;
	}
}