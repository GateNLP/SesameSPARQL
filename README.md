# Sesame Tool for Accessing a SPARQL endpoint and fetching query results from there.

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
 -o <arg>    Outfile
 -ph <arg>   print headers: true or false, default=false
 -u <arg>    endpoint URL (required)
```
