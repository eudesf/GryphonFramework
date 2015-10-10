<p align="center">
    <img src="https://github.com/adrielcafe/GryphonFramework/raw/master/images/gryphon.png">
</p>

Gryphon is a lightweight framework for integrating **ontologies** and **relational databases** in a *very simple* way.

See my [Master's Thesis](https://github.com/adrielcafe/DissertacaoDeMestrado) for details (in *portuguese*)

*Paper coming soon...*

## How to Use

### 1. Configure
```java
// Where the alignments and mappings will be saved? Want to see logs?
GryphonConfig.setWorkingDirectory(new File("myIntegration"));
GryphonConfig.setLogEnabled(true);
GryphonConfig.setShowLogo(true);

// Init Gryphon
Gryphon.init();
```

### 2. Set the global ontology and local sources
```java
// Where are the sources?
Ontology globalOnt = new Ontology("globalOntology", uriToGlobalOntology);
Ontology localOnt1 = new Ontology("localOntology1", uriToLocalOntology1);
Ontology localOnt2 = new Ontology("localOntology2", uriToLocalOntology2);
Database localDB1 = new Database("localhost", 3306, "username", "password", "db1", Gryphon.DBMS.MySQL);
Database localDB2 = new Database("localhost", 3306, "username", "password", "db2", Gryphon.DBMS.PostgreSQL);

Gryphon.setGlobalOntology(globalOnt);
Gryphon.addLocalOntology(localOnt1);
Gryphon.addLocalOntology(localOnt2);
Gryphon.addLocalDatabase(localDB1);
Gryphon.addLocalDatabase(localDB2);
```

### 3. Aligns ontologies and maps databases
```java
// Simple like that (this may take a while)
Gryphon.alignAndMap();
```

### 4. Query Using [SPARQL](http://www.w3.org/TR/sparql11-query/)
```java
// Query must use *Global Ontology* vocabulary
String strQuery = 
	 "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
	+"SELECT ?x ?y "
	+"WHERE { ?x rdf:type ?y }";
Gryphon.query(strQuery, ResultFormat.JSON);
```
**Where are the results?**

Results will be saved on *GryphonConfig.getWorkingDirectory() + "/results"* folder

Supported formats: 
* ResultFormat.JSON
* ResultFormat.XML
* ResultFormat.CSV

## Practical Example
Check out [Example.java](http://github.com/adrielcafe/GryphonFramework/blob/master/src/br/ufpe/cin/aac3/gryphon/example/Example.java) for a complete example.

You'll find ontologies and databases used on examples in [examples folder](http://github.com/adrielcafe/GryphonFramework/tree/master/examples).

## Developed By
* Adriel Café | <aac3@cin.ufpe.br>
* Filipe Santana | <fss3@cin.ufpe.br>
* Fred Freitas | <fred@cin.ufpe.br>

## Acknowledgements
[![CIn-UFPE](https://github.com/adrielcafe/GryphonFramework/raw/master/images/cin.png)](http://www2.cin.ufpe.br)
[![UFPE](https://github.com/adrielcafe/GryphonFramework/raw/master/images/ufpe.png)](http://www.ufpe.br)

This project was founded by [FACEPE](http://www.facepe.br) (IBPG-0342-1.03/13).

Special thanks to:
* [Sesame](http://rdf4j.org)
* [AgreementMakerLight](http://somer.fc.ul.pt/aml.php)
* [D2RQ](http://d2rq.org)
* [Mediation](http://github.com/correndo/mediation)

## License
```
The MIT License (MIT)

Copyright (c) 2015 Adriel Café, Filipe Santana, Fred Freitas

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
