package fi.ni.marmotta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

public class MarmottaSparql {
	final String base = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/";
	final String queryStr;
	final SPARQLRepository repository_query;
	final SPARQLRepository repository_update;	
	
	ValueFactory f=null;
	URI realEstate_class=null;

	public MarmottaSparql() {
		repository_query = new SPARQLRepository(
				"http://drumbeat.cs.hut.fi/tomcat/marmotta/sparql/select");
		repository_update = new SPARQLRepository(
				"http://drumbeat.cs.hut.fi/tomcat/marmotta/sparql/update");
		try {
			repository_query.initialize();		
			repository_update.initialize();
			
			f = repository_update.getValueFactory();
			realEstate_class = f.createURI(base, "RealEstate");
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		queryStr= "select $s where {?s <" + RDF.TYPE + "> <"+ realEstate_class.stringValue() + ">} LIMIT 100";
	}

	// Does not work in the Tomcat/Vaadin environment. Some library at the wrong level?
	// Complains about the output format: no result format specified or unsupported result format
	// At a separate test, this works OK
	
	public List<String> getrealEstates() {
		List<String> ret=new ArrayList<String>();
		try {
			RepositoryConnection con_query;
			con_query = repository_query.getConnection();			
			con_query.setNamespace("drumbeat", base);
			
			TupleQuery tupleQuery = con_query.prepareTupleQuery(QueryLanguage.SPARQL,
					queryStr);
			TupleQueryResult result = tupleQuery.evaluate();
			while (result.hasNext()) {
				BindingSet row = result.next();
				String s=row.getBinding("s").toString();
				if(s!=null)
				ret.add(s.substring(s.lastIndexOf('/')+1,s.length()));
				System.out.println();
			}
		} catch (QueryEvaluationException e) {

			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void create_RealEstate(String project_name) {
		try {
			RepositoryConnection con_update;
			con_update = repository_update.getConnection();
			con_update.setNamespace("drumbeat", base);
			con_update.begin(); // start the transaction
			URI project = f.createURI(base, project_name);
			con_update.add(project, RDF.TYPE, realEstate_class);
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void remove_RealEstate(String project_name) {
		try {
			RepositoryConnection con_update;
			con_update = repository_update.getConnection();
			con_update.setNamespace("drumbeat", base);
			con_update.begin(); // start the transaction
			URI project = f.createURI(base, project_name);
			con_update.remove(project, RDF.TYPE, realEstate_class);
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MarmottaSparql m = new MarmottaSparql();
		System.out.println(m.getrealEstates());
		m.create_RealEstate("testi");
		System.out.println(m.getrealEstates());
		m.remove_RealEstate("testi");
		System.out.println(m.getrealEstates());
	}

}
