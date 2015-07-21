package br.ufpe.cin.aac3.gryphon.model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import br.ufpe.cin.aac3.gryphon.Gryphon;
import br.ufpe.cin.aac3.gryphon.GryphonUtil;

public class Database {
	public enum DBMS {
		MySQL,
		PostgreSQL
	}
	
	protected File mapFile = null;
	protected File alignFile = null;
	protected File resultFile = null;
	
	protected String jdbcDriverClass = null;
	protected String jdbcURL = null;
	protected String host = null;
	protected String username = null;
	protected String password = null;
	protected String dbName = null;
	protected int port = 0;
	
	public Database(String host, int port, String username, String password, String dbName, DBMS dbms) {
		mapFile = new File(Gryphon.getMapFolder().getAbsolutePath(), "db_" + host + "_" + port + "_" + dbName + ".ttl");
		alignFile = new File(Gryphon.getAlignFolder().getAbsolutePath(), "db_" + host + "_" + port + "_" + dbName  + ".rdf");
		resultFile = new File(Gryphon.getResultFolder().getAbsolutePath(), "db_" + host + "_" + port + "_" + dbName  + ".json");
		
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
		
		GryphonUtil.logInfo("Connecting with database: " + jdbcURL);
		if(!testConnection()){
			try {
				throw new SQLException("Can't connect with database: " + jdbcURL); 
			} catch (Exception e) {
				GryphonUtil.logError("Can't connect with database: " + jdbcURL);
			}
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
			e.printStackTrace();
			return false;
		}
	}
	
	public File getAlignFile() {
		return alignFile;
	}
	
	public File getMapFile() {
		return mapFile;
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