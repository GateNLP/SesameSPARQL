/*
 *  EndpointSPARQLQuery.java
 *
 * Copyright (c) 2013-2020, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 3, 29 June 2007.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Johann Petrak, 8/3/2013
 *
 * For details on the configuration options, see the user guide:
 * http://gate.ac.uk/cgi-bin/userguide/sec:creole-model:config
 */

package gate.tool.sesame_sparql;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
// TODO: needed for later Sesame versions
//import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import org.apache.commons.io.FileUtils;

/**
 * Simple class to handle SPARQL queries against a HTTP SPARQL endpoint. 
 * 
 * 
 */
public class SparqlEndpoint {

  /**
   * Create instance and initialize from endpoint URL
   * 
   * @param endpointURL
   */
  public SparqlEndpoint(final URL endpointURL) {
    init(endpointURL.toString());
  }

  /**
   * Create instance and initialize from endpoint URL as String
   * 
   * @param endpoint
   */
  public SparqlEndpoint(String endpoint) {
    init(endpoint);
  }

  private void init(String endpoint) {
    this.endpoint = endpoint;
    
    java.net.URL endpointurl;
    try {
      endpointurl = new java.net.URL(endpoint);
    } catch (MalformedURLException e1) {
      throw new RuntimeException("Endpoint URL is malformed: "+endpoint,e1);
    }
    
    repository = new HTTPRepository(endpoint);

    String userpass = endpointurl.getUserInfo();
    if(userpass != null) {
      String[] userpassfields = userpass.split(":");
      if(userpassfields.length != 2) {
        throw new RuntimeException("URL has login data but looks strange");
      } else {
        repository.setUsernameAndPassword(userpassfields[0],userpassfields[1]);
      }
    }    
    try {
      repository.initialize();
    } catch (RepositoryException e) {
      throw new RuntimeException("Could not initialize HTTPRepository "
          + endpoint, e);
    }

    try {
      connection = repository.getConnection();
    } catch (RepositoryException e) {
      throw new RuntimeException(
          "Could not get Connection for " + endpoint, e);
    }
    factory = repository.getValueFactory();
  }

  
  HTTPRepository repository = null;
  RepositoryConnection connection = null;
  String endpoint;
  ValueFactory factory;

  /**
   * Close and abandon all resources of an instance. This should be called as
   * soon as possible after processing is finished.
   */
  public void close() {
    try {
      connection.close();
    } catch (RepositoryException e) {
      throw new RuntimeException("Could not close connection for "
          + endpoint, e);
    }
  }

  /**
   * Get the repository connection.
   * 
   * @return Sesame repository connection instance
   */
  public RepositoryConnection getConnection() {
    return connection;
  }

  /**
   * Get the repository object.
   * 
   * @return Sesame repository instance.
   */
  public HTTPRepository getRepository() {
    return repository;
  }

  /**
   * Create and return a tuple query object for a SPARQL query.
   * 
   * @param queryString
   * @return Sesame tuple query object ready to be used.
   */
  public TupleQuery createSesameTupleQuery(String queryString) {
    TupleQuery query = null;
    try {
      query = getConnection().prepareTupleQuery(QueryLanguage.SPARQL,
          queryString);
      query.setIncludeInferred(includeInferred);
      query.setMaxQueryTime(maxQueryTime);
      // TODO: depending on options, set maxquerytime and includeinferred here!
      
      System.err.println("Query created, type is "+query.getClass());
      System.err.println("Max query time is "+query.getMaxQueryTime());
      System.err.println("Includeinferred is "+query.getIncludeInferred());
    } catch (Exception e) {
      throw new RuntimeException("Could not create SPARQL query for "
          + endpoint + ":\n" + queryString + "\n", e);
    }
    return query;
  }

  /**
   * Main class for running queries from the command line.
   * 
   * @param argv
   * @throws QueryEvaluationException
   */
  public static void main(String[] argv) {

    // TODO: make sure we close things before passing on an exception!!!
    
    //if(argv.length != 2) {
    //  throw new RuntimeException("Need 2 parameters: endpointURL queryFileName");
    //}
    
    Options options = new Options();
    options.addOption("h", false, "Show help information");
    // TODO: not implemented yet
    //options.addOption("e", true, "What to do: one of 'query', 'ask', 'update', default is 'query'");
    options.addOption("i", true, "Query input file (required)");
    options.addOption("o", true, "Outfile");
    options.addOption("b", true, "Batchsize for queries, default: retrieve all at once");
    options.addOption("u", true, "endpoint URL, can include user:password@ prefix (required)");
    options.addOption("ii", true, "include inferred: true or false, default=true");
    options.addOption("mt", true, "maximum query time in seconds, default=3600");
    options.addOption("ph", true, "print headers: true or false, default=false");
    options.addOption("ee", true, "error on invalid strings: true or false, default=false");
    
    CommandLineParser parser = new GnuParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse( options, argv);
    } catch (ParseException e1) {
      System.err.println("Could not parse arguments: "+Arrays.toString(argv));
      e1.printStackTrace(System.err);
      System.exit(1);
    }
    if(cmd.hasOption('h')) {
      HelpFormatter helpFormatter = new HelpFormatter();
      helpFormatter.printHelp("./bin/query.sh [options]", options);
      System.exit(0);
    }
    
    String whatDo = cmd.getOptionValue('e');    
    if(whatDo == null) {
      whatDo = "query";
    }
    if(whatDo.equals("query") || whatDo.equals("ask") || whatDo.equals("update")) {
      // fine
    } else {
      System.err.println("Option -e must be one of 'query', 'ask', 'update'");
      System.exit(1);
    }
    if(!whatDo.equals("query")) {
      System.err.println("Only 'query' supported for now");
      System.exit(1);
    }
    
    String endpointURL = cmd.getOptionValue('u');
    if(endpointURL == null) {
      System.err.println("Option -u endpointURL needed for query");
      System.exit(1);
    }
    String queryFileName = cmd.getOptionValue('i');
    if(queryFileName == null) {
      System.err.println("Option -i inputSPARQLFile needed for query");
      System.exit(1);
    }
    
    String includeInferredString = cmd.getOptionValue("ii");
    if(includeInferredString == null ||includeInferredString.isEmpty()) {
      includeInferredString = "true";
    }
    boolean includeInferred = true;    
    if(includeInferredString.equals("true") || includeInferredString.equals("false")) {
      if(includeInferredString.equals("false")) {
        includeInferred = false;
      }
    } else {
      System.err.println("Option -ii needs parameter true or false (default: true)");
      System.exit(1);
    }
    
    int maxQueryTime = 3600;
    String maxQueryTimeString = cmd.getOptionValue("mt");
    if(maxQueryTimeString != null) {
      maxQueryTime = Integer.parseInt(maxQueryTimeString);
    }
    
    boolean printHeaders = false;
    String printHeadersString = cmd.getOptionValue("ph");
    if(printHeadersString != null) {
      printHeaders = Boolean.parseBoolean(printHeadersString);
    }
    
    boolean errorOnInvalidStrings = false;
    String errorOnInvalidStringsString = cmd.getOptionValue("ee");
    if(errorOnInvalidStringsString != null) {
      errorOnInvalidStrings = Boolean.parseBoolean(errorOnInvalidStringsString);
    }
    
    
    String outFileName = cmd.getOptionValue('o');
    PrintStream outps = null;
    try {
      if (outFileName == null) {
        // make sure system.out uses encoding utf-8
        outps = new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8");
      } else {
        outps = new PrintStream(outFileName, "UTF-8");
      }
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Could not open output stream", e);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Could not open output stream", e);
    }
    
    
    String sparqlQuery = "";
    try {
      sparqlQuery = FileUtils.readFileToString(new File(queryFileName), "UTF-8");
    } catch (IOException e) {
      throw new RuntimeException("Could not read SPARQL query from file "+queryFileName,e);
    }
    SparqlEndpoint sq = new SparqlEndpoint(endpointURL);

    sq.setIncludeInferred(includeInferred);
    sq.setMaxQueryTime(maxQueryTime);
    sq.setPrintHeaders(printHeaders);
    sq.setErrorOnInvalidStrings(errorOnInvalidStrings);
    
    MyResultHandler handler = new MyResultHandler();
    handler.setOuput(outps);
    handler.setErrorOnInvalidStrings(errorOnInvalidStrings);
    handler.setPrintHeaders(printHeaders);
        
    if(cmd.getOptionValue('b') == null) {
      long rows = sq.runTupleQuery(sparqlQuery, handler);
      System.err.println("Rows retrieved: "+rows);
    } else {
      // we have got a batchsize parameter
      int batchSize = Integer.parseInt(cmd.getOptionValue('b'));
      System.err.println("Retrieving data in batches of size "+batchSize);
      int start = 0;
      long totalRows = 0;
      int i = 0;
      long lastRows = 0;
      while(true) {   
        String batchQuery = sparqlQuery + " OFFSET "+start+" LIMIT "+batchSize;
        long rows = sq.runTupleQuery(batchQuery, handler);
        if(rows-lastRows < batchSize) {
          totalRows = rows;
          break;
        } else {
          lastRows = rows;
        }
        i++;
        start = start+batchSize;
      }
      System.err.println("Total number of rows: "+totalRows);
      System.err.println("Batches: "+i);
    }
    try {
      outps.close();
    } catch (Exception ex) {
      
    }

  }
  
  public long runTupleQuery(String sparqlQuery, MyResultHandler handler) {
    TupleQuery query = this.createSesameTupleQuery(sparqlQuery);
    System.err.println("Tuple query created: "+query.getClass());
    try {
      query.evaluate(handler);
    } catch (QueryEvaluationException | TupleQueryResultHandlerException e) {
      throw new RuntimeException("Could not evaluate query",e);
    } 
    return handler.getRows();
  }
  
  
  public static Writer setOutput(String outFileName) {
    Writer ow = null;
    if(outFileName != null) {
      try {
        ow = new BufferedWriter ( new FileWriter ( new File(outFileName) ) );
      } catch (IOException e) {
        try {
          if(ow != null) {
            ow.close();
          }
        } catch (IOException ex) {
        }
        e.printStackTrace();
        throw new RuntimeException("Exception when opening output file: "+outFileName,e);
      }
    }
    return ow;
  }
  
  
  public static class MyResultHandler implements TupleQueryResultHandler {

    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {
      // nothing needed here      
    }

    public void setOuput(PrintStream outps) {
      this.outps = outps;
    }
    
    PrintStream outps = null;
    
    protected boolean errorOnInvalidStrings = true;
    public void setErrorOnInvalidStrings(boolean v) {
      errorOnInvalidStrings = v;
    }
    protected boolean printHeaders = false;
    public void setPrintHeaders(boolean v) {
      printHeaders = v;
    }
    
    private boolean headersWritten = false;
  
   /* 
    @Override
    public void handleLinks(List<String> linkUrls) throws QueryResultHandlerException {
    }

    @Override 
    public void handleBoolean(boolean value) throws QueryResultHandlerException {
    }
   */ 
    
    @Override
    public void handleSolution(BindingSet bs)
        throws TupleQueryResultHandlerException {

      int column = 0;
      StringBuilder buf = new StringBuilder();
      for (String header : bindingNames) {
        column++;
        Value value = bs.getValue(header);
        // TODO: check if we should do the conversion to string in other ways
        // for some kinds of values, maybe depending on option settigns?
        // E.g. convert literal values to turtle representation?
        String valueString;
	if(value == null || value.stringValue() == null) {
          valueString = "";  // TODO: maybe allow to change representation of null?
        } else {
          valueString = value.stringValue();
        }
        Matcher trailingNLMatcher = trailingNL.matcher(valueString);
        Matcher embeddedMatcher = embedded.matcher(valueString);
        if (errorOnInvalidStrings) {
          if (trailingNLMatcher.find() || embeddedMatcher.find()) {
            throw new RuntimeException("Invalid string in row " + (rows+1)
                + " column " + column + " value " + valueString);
          }
        } else {
          valueString = trailingNLMatcher.replaceAll("");
          valueString = embeddedMatcher.replaceAll(" ");
        }
        buf.append(valueString);
        if (column < bindingNames.size()) {
          buf.append(colSep);
        }
      }
      buf.append("\n");
      outps.print(buf);
      rows++;
      if(rows % 1000 == 0) {
        System.err.println("Rows written: "+rows);
      }
    }    

    @Override
    public void startQueryResult(List<String> arg0)
        throws TupleQueryResultHandlerException {
      bindingNames = arg0;
      if(printHeaders && !headersWritten) {
        headersWritten = true;
        boolean first = true;
        for(String name : arg0) {
          if(first) {
            first = false;
          } else {
            outps.print("\t");
          }
          outps.print(name);
        }
        outps.println();
      }
    }
    
    List<String> bindingNames;
    long rows = 0;
    
    public long getRows() { return rows; }
    Pattern trailingNL = Pattern.compile("\\n$");
    String colSep = "\t";
    Pattern embedded = Pattern.compile("\\n(?!$)|"+Pattern.quote(colSep));
    
  }

    // option values that influence the behavior
    // TODO: actually implement different behaviors and option processing!
    protected boolean printHeaders = false;
    public void setPrintHeaders(boolean v) {
      printHeaders = v;
    }
    protected boolean errorOnInvalidStrings = true;
    public void setErrorOnInvalidStrings(boolean v) {
      errorOnInvalidStrings = v;
    }
    boolean includeInferred = true;
    public void setIncludeInferred(boolean v) {
      includeInferred = v;
    }
    int maxQueryTime = 3600;
    public void setMaxQueryTime(int v) {
      maxQueryTime = v;
    }

  
}
