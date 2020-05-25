# GATE plugin for accessing a SPARQL endpoint and fetching query results from there.

This does not implement a GATE resource yet, but can be used through the API (eg. from
a Groovy script or JAPE file) and from the command line (script ./bin/query.sh)

This may change significantly in the future. 
There are many possible ways how this could get extended:
* allow more SPARQL operations apart from querying
* allow to treat query results as something that can be treated as a stream of documents
  and processed (maybe just providing an adapter for a more generic, different plugin
  that can process streams or collections of tsv/csv rows)
* extend to a kind of "lightweight" ontology adapter. Again the abstraction of what
  a lightweight ontology provides could get implemented in a different plugin, or we 
  simply allow each plugin to use their own abstractions.

