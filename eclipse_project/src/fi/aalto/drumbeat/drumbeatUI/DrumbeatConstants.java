package fi.aalto.drumbeat.drumbeatUI;

public class DrumbeatConstants {
	
	// IFC
	public static final String ifc_schema="/var/ifc2rdf/IFC2X3_TC1.exp";
	public static final String ifc_conversion_config="/var/ifc2rdf/ifc2rdf-config.xml";
	public static final String ifc_conversion_log_settings="/var/ifc2rdf/log4j.xml";
	
	// Meta data
	public static final String metadata_base="http://drumbeat.cs.hut.fi/void/model/";
	public static final String metadata_location="/var/www/void.ttl";
	
	// Sites, data sets
	
	public static final String siteClass = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/RealEstate";
	public static final String datasetClass = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/Model";
	public static final String datasetProperty = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/model";	
	
	public static final String resource_baseurl = "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/";
	                                      
	/*
	 * SPARQL QUERIES
	 */
	
	final static String query_sites="PREFIX drumbeat:   <http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/> \nPREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \nselect $s \nwhere\n{\n?s rdf:type drumbeat:RealEstate .\n}\n LIMIT 100";
	final static String query_structural_links=""
			+"prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>"
            +"\nprefix model: <http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/>"
            +"\nprefix owl:   <http://www.w3.org/2002/07/owl#>"
            +"\nprefix xsd:   <http://www.w3.org/2001/XMLSchema#>"
            +"\nprefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            +"\nprefix ifc:   <http://drumbeat.cs.hut.fi/owl/IFC2X3_Standard#>"
            +"\n"
            +"\nselect ?se ?ae"            
            +"\nFROM <http://drumbeat.cs.hut.fi/tomcat/marmotta/context/Structural>"
            +"\nWHERE {"
            +"\n?se ifc:hasProperties [ ifc:name \"initial_GUID\"^^xsd:string  ;"
            +"\n              ifc:nominalValue [ rdf:value ?a ]]."
            +"\nBIND (URI(CONCAT(\"http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/GUID_\", ?a))"
            +"\nAS ?ae)"
            +"\n}";
	
	final static String querytemplate_links=""
			+"prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>"
            +"\nprefix model: <http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/>"
            +"\nprefix owl:   <http://www.w3.org/2002/07/owl#>"
            +"\nprefix xsd:   <http://www.w3.org/2001/XMLSchema#>"
            +"\nprefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            +"\nprefix ifc:   <http://drumbeat.cs.hut.fi/owl/IFC2X3_Standard#>"
            +"\n"
            +"\nselect ?from ?to"            
            +"\nFROM <context>"
            +"\nWHERE {"
            +"\n?from ifc:hasProperties [ ifc:name \"initial_GUID\"^^xsd:string  ;"
            +"\n              ifc:nominalValue [ rdf:value ?a ]]."
            +"\nBIND (URI(CONCAT(\"http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/GUID_\", ?a))"
            +"\nAS ?to)"
            +"\n}";
	
	final static String query_structural_project=""
			+"prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+"\nprefix ifc:   <http://drumbeat.cs.hut.fi/owl/IFC2X3_Standard#>"
			+"\n"
			+"\nselect ?s ?name"
			+"\nFROM <http://drumbeat.cs.hut.fi/tomcat/marmotta/context/Structural>"
			+"\nWHERE {"
			+"\n?s rdf:type ifc:IfcProject."
			+"\n?s ifc:name ?name ."
			+"\n}";
			
	final static String query_implemens =""
			+"prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+"\nprefix ifc:   <http://drumbeat.cs.hut.fi/owl/IFC2X3_Standard#>"
			+"\n"
			+"\nselect ?s ?o"
			+"\nWHERE {"
			+"\n?s ifc:implements ?o ."
			+"\n} LIMIT 10";

	// Marmotta
	private final static String marmotta_url = "http://drumbeat.cs.hut.fi/tomcat/marmotta/";
	public final static String marmotta_sparql_query_url=marmotta_url+"sparql/select";
	public final static String marmotta_sparql_update_url=marmotta_url+"sparql/update";
	
	final static String marmotta_import_directory="/var/marmotta/home/import/"; 
	final static String marmotta_import_config_file=marmotta_import_directory+"config";
	public final static String marmotta_contexts_baseurl = marmotta_url+"context/";
	
	public final static String marmotta_context_query_url = marmotta_url+"context/list?labels=true";	 
	public final static String marmotta_http_sparql_url_header = marmotta_url+"sparql/select?query=";
	
	// Marmotta Output formats:
	public final static String sparql_url_json_tail = "&output=json";
	public final static String sparql_url_xml_tail = "&output=xml";
	public final static String sparql_url_html_tail = "&output=html";
	
	
	// BIMServer
	public final static String bimserver_api_json_url="http://drumbeat.cs.hut.fi/tomcat/json";
	public final static String bimserver_api_url="http://drumbeat.cs.hut.fi:8080"; 
	public final static String bimserver_user = "drumcsbeat@gmail.com";
	public final static String bimserver_password  = "ajakkjajj1k1u";
	
	public final static String bimserver_upload_user = "kiori@windowslive.com";
	public final static String bimserver_upload_password  = "aakk###gggss";
	
	public final static String bimserver_upload_schema  = "ifc2x3tc1";
	
	// BIM Surfer
	final static String bimsurfer_project_url="http://drumbeat.cs.hut.fi/tomcat/bimviews/?page=Project&poid=";
	final static String drumbeatview_project_url="http://drumbeat.cs.hut.fi/drumbeatview/?project=";

	// Directory constants
	final static String ifc_uploads_directory= "/var/uploads/"; 
	
}