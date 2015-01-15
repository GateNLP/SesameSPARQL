package gate.sesame_sparql;

import gate.util.GateRuntimeException;

import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;



/**
 * A class representing a Sesame Query. This class makes it easy to
 * iterate over query results, reuse a query with different variable
 * bindings, and provides special methods for queries where only one
 * column is retrieved. It also wraps most of the Sesame stuff to make
 * most standard usage scenarios easier and it converts all checked 
 * Sesame exceptions to GateRuntimeExceptions to make it easier to 
 * use this from e.g. JAPE. Instances of this class are returned from
 * a SparqlEndpoint instance by using the factory method createTupleQuery.
 * <p>
 * The following steps should be carried out when using an instance of the class:
 * <ol>
 * <li>Set any variables for the query using {@link #setBinding} one or more times. 
 * <li>Evaluate the query using {@link evaluate}. This is optional, if not done,
 * it will be done implicitly in the first call to {@link #hasNext}.
 * <li>Retrieve query results one by one using one of the next... methods
 * <li>If no more results are needed and the query will not be re-used either, 
 * the query MUST BE CLOSED using the
 * {@link #close} function to free the query resources. However, if all
 * results have been returned and {@link #hasNext()} has returned false,
 * the query has been closed automatically. It is allowed to call close
 * on a closed query.
 * <li>To reuse the query, repeat from step 1 or 2.
 * </ol>
 *
 * @author Johann Petrak
 */
public class TupleQueryProcessor {

  private SparqlEndpoint endpoint;
  private TupleQuery sesameTupleQuery;
  private String queryString;
  private enum QueryState { 
    CREATED, INITIALIZED, PREPARED, EVALUATED, PROCESSING, FINISHED };
  private QueryState queryState = QueryState.CREATED; 
  
  
  TupleQueryProcessor(SparqlEndpoint ep, String querystring) {
    queryString = querystring;
    try {
      sesameTupleQuery = ep.getConnection().prepareTupleQuery(QueryLanguage.SPARQL,
          queryString);
    } catch (Exception e) {
      throw new GateRuntimeException("Could not create SPARQL query for "
          + endpoint + ":\n" + queryString + "\n", e);
    }
    queryState = QueryState.INITIALIZED;
  }
  
  /**
   * This will set the values of unbound variables in  a freshly created SPARQL query.
   * If the query already has been evaluated or used, the current query is closed, and
   *  the whole query re-initialized again.
   * @param name
   * @param value
   */
  public void setBinding(String name, Value value) {
    sesameTupleQuery.setBinding(name, value);
  }
  
  public void setBaseURI(String URI) { 
    // TODO
  }
  
}
