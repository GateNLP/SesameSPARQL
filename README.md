# Tool for Accessing a SPARQL endpoint and fetching query results from there.

Simple API and command line tool to retrieve the result of SPARQL queries from a 
Sesame repository.

This is based on code written originally in 2013 and still uses the rather dated 
Sesame libraries from back then.

## Setup 

* clone the repository and change into the directory
* run `mvn package`

This creates a single jar in `./target` which contains all the dependencies.

## Usage


Run the command:
```
./bin/query.sh -u <SPARQL-endpoing-URL> -i <QUERY> [OTHEROPTIONS]
```

The result rows are written to standard output.

To get usage information:
```
./bin/query.sh -h
```

This outputs:
```
usage: ./bin/query.sh [options]
 -b <arg>    Batchsize for queries, default: retrieve all at once
 -ee <arg>   error on invalid strings: true or false, default=false
 -h          Show help information
 -i <arg>    Query input file (required)
 -ii <arg>   include inferred: true or false, default=true
 -mt <arg>   maximum query time in seconds, default=3600
 -ph <arg>   print headers: true or false, default=false
 -u <arg>    endpoint URL (required)
```

### Examples

Query DBPedia and get the URI and English language film title of films starring Charlie Chaplin:

Query file `examples/example2.sparql`:
```
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX res: <http://dbpedia.org/resource/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT DISTINCT ?uri ?string
WHERE {
        ?uri rdf:type dbo:Film .
        ?uri dbo:starring res:Charlie_Chaplin .
        OPTIONAL {?uri rdfs:label ?string . FILTER (lang(?string) = 'en') }
}
```

Run the command to retrieve the result, write to file `chaplin_films.tsv` with column headers:
```
./bin/query.sh -ph true -u  http://DBpedia.org/sparql -i examples/example2.sparql > chaplin_films.tsv 
```
