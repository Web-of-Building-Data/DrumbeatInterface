package fi.ni.marmotta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openrdf.model.vocabulary.RDF;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

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

public class MarmottaAPI {
	final String context_query_url = "http://drumbeat.cs.hut.fi/tomcat/marmotta/context/list?labels=true";
	final String sparql_url_header = "http://drumbeat.cs.hut.fi/tomcat/marmotta/sparql/select?query=";
	final String sparql_url_json_tail = "&output=json";
	final String sparql_url_xml_tail = "&output=xml";
	final String sparql_url_html_tail = "&output=html";
	final String realEstateClass = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/RealEstate";
	final String modelClass = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/Model";
	final String modelProperty = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/model";
	
	public void httpGetMarmottaContexts(Table table) {
	
		try
		{
		  if(table.isVisible())
		    table.removeAllItems();
		}
		catch(Exception e)
		{
			//update before table initalization?
			return;
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(context_query_url);
			HttpResponse http_result = httpClient.execute(request);

			String json = EntityUtils
					.toString(http_result.getEntity(), "UTF-8");
			try {
				JSONParser parser = new JSONParser();
				Object resultObject = parser.parse(json);
				if (resultObject instanceof JSONArray) {
					JSONArray result = (JSONArray) resultObject;
					if (result != null) {
						for (Object obj : result) {

							// Add a row the hard way
							Object newItemId = table.addItem();
							Item row = table.getItem(newItemId);

							JSONObject project = (JSONObject) obj;
							String name = (String) project.get("label");
							if (name != null)
								row.getItemProperty("Graph").setValue(name);
							else
								row.getItemProperty("Graph").setValue("-");

							Long size = (Long) project.get("size");
							if (size != null)
								row.getItemProperty("Size").setValue(size);
							else
								row.getItemProperty("Size").setValue("-");

						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		// Show exactly the currently contained rows (items)
		table.setPageLength(table.size());
	}

	final String contextbase="http://drumbeat.cs.hut.fi/tomcat/marmotta/context/";
	public List<String> httpGetMarmottaContexts() {
		List<String> ret=new ArrayList<String>();
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(context_query_url);
			HttpResponse http_result = httpClient.execute(request);

			String json = EntityUtils
					.toString(http_result.getEntity(), "UTF-8");
			try {
				JSONParser parser = new JSONParser();
				Object resultObject = parser.parse(json);
				if (resultObject instanceof JSONArray) {
					JSONArray result = (JSONArray) resultObject;
					if (result != null) {
						for (Object obj : result) {

							// Add a row the hard way

							JSONObject project = (JSONObject) obj;
							String name = (String) project.get("label");
							Long size = (Long) project.get("size");
							if (name != null & size != null)
								if(size>500)
								  ret.add(contextbase+name);
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	
	public List<String> httpGetDRUMRealEstates() {
		List<String> ret=new ArrayList<String>();
		String queryStr= "select $s where {?s <" + RDF.TYPE + "> <"+realEstateClass + ">} LIMIT 100";
		String url=null;
		try {
			url=sparql_url_header+URLEncoder.encode(queryStr, "UTF-8")+sparql_url_json_tail;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(url);
			HttpResponse http_result = httpClient.execute(request);

			String json = EntityUtils
					.toString(http_result.getEntity(), "UTF-8");
			try {
				JSONParser parser = new JSONParser();
				Object rootObject = parser.parse(json);
				if (rootObject instanceof JSONObject) {
					JSONObject root = (JSONObject) rootObject;
					if (root != null) {
						
						JSONObject results = (JSONObject) root.get("results");
						if (results != null) {
							
							Object bindingsObject = results.get("bindings");
							if (bindingsObject instanceof JSONArray) {
								JSONArray bindings = (JSONArray) bindingsObject;
								for(Object bObject:bindings)
								{
									JSONObject b=(JSONObject) bObject;
									Object sObject=b.get("s");
									JSONObject s=(JSONObject) sObject;
									String value=(String)s.get("value");
									ret.add(value.substring(value.lastIndexOf('/')+1,value.length()));
								}
							}
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return ret;
	}

	public List<String> httpGetModels() {
		List<String> ret=new ArrayList<String>();
		String queryStr= "select $s where {?s <" + RDF.TYPE + "> <"+modelClass + ">} LIMIT 100";
		String url=null;
		try {
			url=sparql_url_header+URLEncoder.encode(queryStr, "UTF-8")+sparql_url_json_tail;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(url);
			HttpResponse http_result = httpClient.execute(request);

			String json = EntityUtils
					.toString(http_result.getEntity(), "UTF-8");
			try {
				JSONParser parser = new JSONParser();
				Object rootObject = parser.parse(json);
				if (rootObject instanceof JSONObject) {
					JSONObject root = (JSONObject) rootObject;
					if (root != null) {
						
						JSONObject results = (JSONObject) root.get("results");
						if (results != null) {
							
							Object bindingsObject = results.get("bindings");
							if (bindingsObject instanceof JSONArray) {
								JSONArray bindings = (JSONArray) bindingsObject;
								for(Object bObject:bindings)
								{
									JSONObject b=(JSONObject) bObject;
									Object sObject=b.get("s");
									JSONObject s=(JSONObject) sObject;
									String value=(String)s.get("value");
									ret.add(value.substring(value.lastIndexOf('/')+1,value.length()));
								}
							}
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return ret;
	}

	public List<Pair> httpGetRealEstateModels() {
		List<Pair> ret=new ArrayList<Pair>();
		String queryStr="select $s $o where {?s <" + RDF.TYPE + "> <"+ realEstateClass + ">. ?s <" + modelProperty + "> ?o.} LIMIT 100";
		String url=null;
		try {
			url=sparql_url_header+URLEncoder.encode(queryStr, "UTF-8")+sparql_url_json_tail;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(url);
			HttpResponse http_result = httpClient.execute(request);

			String json = EntityUtils
					.toString(http_result.getEntity(), "UTF-8");
			try {
				JSONParser parser = new JSONParser();
				Object rootObject = parser.parse(json);
				if (rootObject instanceof JSONObject) {
					JSONObject root = (JSONObject) rootObject;
					if (root != null) {
						
						JSONObject results = (JSONObject) root.get("results");
						if (results != null) {
							
							Object bindingsObject = results.get("bindings");
							if (bindingsObject instanceof JSONArray) {
								JSONArray bindings = (JSONArray) bindingsObject;
								for(Object bObject:bindings)
								{
									JSONObject b=(JSONObject) bObject;
									Object sObject=b.get("s");
									JSONObject s=(JSONObject) sObject;
									String svalue=(String)s.get("value");
									
									
									Object oObject=b.get("o");
									JSONObject o=(JSONObject) oObject;
									String ovalue=(String)o.get("value");
									String stxt=svalue.substring(svalue.lastIndexOf('/')+1,svalue.length());
									String otxt=ovalue.substring(ovalue.lastIndexOf('/')+1,ovalue.length());									
									System.out.println("otxt"+otxt);
									ret.add(new Pair(stxt,otxt));
								}
							}
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return ret;
	}

	
	public String httpGetQuery2html(String queryStr) {
		String url=null;
		try {
			url=sparql_url_header+URLEncoder.encode(queryStr, "UTF-8")+sparql_url_html_tail;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(url);
			HttpResponse http_result = httpClient.execute(request);
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(http_result.getEntity().getContent()));
			 
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				return result.toString();

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}
	
	public String httpGetQuery2json(String queryStr) {
		String url=null;
		try {
			url=sparql_url_header+URLEncoder.encode(queryStr, "UTF-8")+sparql_url_json_tail;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(url);
			HttpResponse http_result = httpClient.execute(request);
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(http_result.getEntity().getContent()));
			 
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				return result.toString();

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}
	
	public String httpGetQuery2XML(String queryStr) {
		String url=null;
		try {
			url=sparql_url_header+URLEncoder.encode(queryStr, "UTF-8")+sparql_url_xml_tail;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(url);
			HttpResponse http_result = httpClient.execute(request);
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(http_result.getEntity().getContent()));
			 
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				return result.toString();

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}


	// Select from. to
	public List<Pair> httpGetLinks(String queryStr) {
		List<Pair> ret=new ArrayList<Pair>();
		String url=null;
		try {
			url=sparql_url_header+URLEncoder.encode(queryStr, "UTF-8")+sparql_url_json_tail;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpGet request = new HttpGet(url);
			HttpResponse http_result = httpClient.execute(request);

			String json = EntityUtils
					.toString(http_result.getEntity(), "UTF-8");
			try {
				JSONParser parser = new JSONParser();
				Object rootObject = parser.parse(json);
				if (rootObject instanceof JSONObject) {
					JSONObject root = (JSONObject) rootObject;
					if (root != null) {
						
						JSONObject results = (JSONObject) root.get("results");
						if (results != null) {
							
							Object bindingsObject = results.get("bindings");
							if (bindingsObject instanceof JSONArray) {
								JSONArray bindings = (JSONArray) bindingsObject;
								for(Object bObject:bindings)
								{
									JSONObject b=(JSONObject) bObject;
									
									Object sObject=b.get("from");
									JSONObject s=(JSONObject) sObject;
									String svalue=(String)s.get("value");
									
									
									Object oObject=b.get("to");
									JSONObject o=(JSONObject) oObject;
									String ovalue=(String)o.get("value");
									
									ret.add(new Pair(svalue,ovalue));
								}
							}
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return ret;
	}

	
}
