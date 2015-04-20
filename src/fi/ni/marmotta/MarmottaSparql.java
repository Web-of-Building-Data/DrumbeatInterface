package fi.ni.marmotta;

import java.util.ArrayList;
import java.util.List;

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

import fi.ni.marmotta.vo.Pair;

/*
 * The MIT License (MIT)

 Copyright (c) 2015 Jyrki Oraskari

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
public class MarmottaSparql {
	final String base = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/";
	final String realEstatesQueryStr;
	final String modelsQueryStr;
	final String realEstateModelsQueryStr;
	final String realEstateModelsQueryStr_simplified;
	final SPARQLRepository repository_query;
	final SPARQLRepository repository_update;	
	
	ValueFactory f=null;
	URI realEstate_class=null;
	URI model_class=null;
	URI realEstate_model_property=null;
	URI implements_property=null;
	
	public MarmottaSparql() {
		repository_query = new SPARQLRepository(
				"http://drumbeat.cs.hut.fi/tomcat/marmotta/sparql/select");
		repository_update = new SPARQLRepository(
				"http://drumbeat.cs.hut.fi/tomcat/marmotta/sparql/update");
		try {
			repository_query.initialize();		
			repository_update.initialize();
			
			f = repository_update.getValueFactory();
			model_class = f.createURI(base,"Model");
			realEstate_class = f.createURI(base, "RealEstate");
			realEstate_model_property = f.createURI(base, "model");
			
			implements_property = f.createURI("http://drumbeat.cs.hut.fi/owl/IFC2X3_Standard#", "implements");
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		realEstatesQueryStr= "select $s where {?s <" + RDF.TYPE + "> <"+ realEstate_class.stringValue() + ">.} LIMIT 100";
		modelsQueryStr= "select $s where {?s <" + RDF.TYPE + "> <"+ model_class.stringValue() + ">.} LIMIT 100";
		realEstateModelsQueryStr="select $s $o where {?s <" + RDF.TYPE + "> <"+ realEstate_class.stringValue() + ">. ?s <" + realEstate_model_property + "> ?o.} LIMIT 100";
		realEstateModelsQueryStr_simplified="select $s $o where {?s <" + realEstate_model_property + "> ?o.} LIMIT 100";
	}

	// Does not work in the Tomcat/Vaadin environment. Some library at the wrong level?
	// Complains about the output format: no result format specified or unsupported result format
	// At a separate test, this works OK
	
	public List<String> getSites() {
		List<String> ret=new ArrayList<String>();
		try {
			RepositoryConnection con_query;
			con_query = repository_query.getConnection();			
			con_query.setNamespace("drumbeat", base);
			
			TupleQuery tupleQuery = con_query.prepareTupleQuery(QueryLanguage.SPARQL,
					realEstatesQueryStr);
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

	public List<String> getModels() {
		List<String> ret=new ArrayList<String>();
		try {
			RepositoryConnection con_query;
			con_query = repository_query.getConnection();			
			con_query.setNamespace("drumbeat", base);
			
			TupleQuery tupleQuery = con_query.prepareTupleQuery(QueryLanguage.SPARQL,
					modelsQueryStr);
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

	public List<String> getRealEstateModels() {
		List<String> ret=new ArrayList<String>();
		try {
			RepositoryConnection con_query;
			con_query = repository_query.getConnection();			
			con_query.setNamespace("drumbeat", base);
			
			TupleQuery tupleQuery = con_query.prepareTupleQuery(QueryLanguage.SPARQL,
					realEstateModelsQueryStr);
			System.out.println(realEstateModelsQueryStr);
			TupleQueryResult result = tupleQuery.evaluate();
			while (result.hasNext()) {
				BindingSet row = result.next();
				String s=row.getBinding("s").toString();
				String o=row.getBinding("o").toString();
				System.out.println(s+" "+o);
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

	public List<String> getRealEstateModelsSimplified() {
		List<String> ret=new ArrayList<String>();
		try {
			RepositoryConnection con_query;
			con_query = repository_query.getConnection();			
			con_query.setNamespace("drumbeat", base);
			
			TupleQuery tupleQuery = con_query.prepareTupleQuery(QueryLanguage.SPARQL,
					realEstateModelsQueryStr);
			System.out.println(realEstateModelsQueryStr);
			TupleQueryResult result = tupleQuery.evaluate();
			while (result.hasNext()) {
				BindingSet row = result.next();
				String s=row.getBinding("s").toString();
				String o=row.getBinding("o").toString();
				System.out.println(s+" "+o);
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
			con_update.begin(); // start the transaction
			URI project = f.createURI(base, project_name);
			con_update.remove(project, RDF.TYPE, realEstate_class);
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void create_Model(String model_name) {
		try {
			RepositoryConnection con_update;
			con_update = repository_update.getConnection();
			con_update.begin(); // start the transaction			
			URI dataset = f.createURI(base, model_name);
			con_update.add(dataset, RDF.TYPE, model_class);
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void remove_model(String model_name) {
		try {
			RepositoryConnection con_update;
			con_update = repository_update.getConnection();
			con_update.begin(); // start the transaction			
			URI dataset = f.createURI(base, model_name);
			con_update.remove(dataset, RDF.TYPE, model_class);
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	
	
	public void attach_model(String realEstate_name,String model_name) {
		try {
			RepositoryConnection con_update;
			con_update = repository_update.getConnection();
			con_update.begin(); // start the transaction			
			URI project = f.createURI(base, realEstate_name);
			URI model = f.createURI(base, model_name);
			con_update.add(project, realEstate_model_property, model);
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void detatch_model(String realEstate_name,String model_name) {
		try {
			RepositoryConnection con_update;
			con_update = repository_update.getConnection();
			con_update.begin(); // start the transaction			
			URI project = f.createURI(base, realEstate_name);
			URI model = f.createURI(base, model_name);
			con_update.remove(project, realEstate_model_property, model);
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void add_linkset2Model(List<Pair> links) {
		try {
			RepositoryConnection con_update;
			con_update = repository_update.getConnection();
			con_update.begin(); // start the transaction
			for(Pair p:links)
			{
			 URI from = f.createURI(p.getS1());
			 URI to = f.createURI(p.getS2());
			 con_update.add(from, implements_property, to);
			}
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void remove_linksetFromModel(List<Pair> links) {
		try {
			RepositoryConnection con_update;
			con_update = repository_update.getConnection();
			con_update.begin(); // start the transaction
			for(Pair p:links)
			{
			 URI from = f.createURI(p.getS1());
			 URI to = f.createURI(p.getS2());
			 con_update.remove(from, implements_property, to);
			}
			con_update.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	
	
	public static void main(String[] args) {
		MarmottaSparql m = new MarmottaSparql();
		/*System.out.println("1");
		m.create_RealEstate("real1");
		m.attach_model("real1", "model1");
		System.out.println("2");
		m.getRealEstateModels();
		System.out.println("3");
		m.remove_RealEstate("real1");
		m.detatch_model("real1", "model1");
		System.out.println(m.getrealEstates());
		m.getRealEstateModels();*/
		
		m.getRealEstateModelsSimplified();
		//System.out.println(m.getModels());
		/*System.out.println(m.getrealEstates());
		m.create_RealEstate("testi");
		System.out.println(m.getrealEstates());
		m.remove_RealEstate("testi");
		System.out.println(m.getrealEstates());*/
	}

}
