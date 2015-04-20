package fi.ni.drumbeatinterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import softhema.system.toolkits.ToolkitString;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import fi.ni.bimserver.BIMServerJSONApi;
import fi.ni.marmotta.MarmottaAPI;
import fi.ni.marmotta.MarmottaSparql;
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

@SuppressWarnings("serial")
@Theme("drumbeatinterface")
public class DrumbeatinterfaceUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = DrumbeatinterfaceUI.class)
	public static class Servlet extends VaadinServlet {
	}

	final MarmottaAPI marmotta = new MarmottaAPI();
	final MarmottaSparql marmotta_sparql = new MarmottaSparql();
	final BIMServerJSONApi bimserver_jsonapi = new BIMServerJSONApi();
	private String uploads = "/var/uploads/";
	// private String uploads= "c:/jo/uploads/";
	Upload upload = null;

	DrumbeatFileHandler drumbeat_fileReceiver = new DrumbeatFileHandler(this,
			uploads);
	TabSheet tabsheet = new TabSheet();
	final Table files_table = new Table("Uploaded files");
	final Table contexts_table = new Table("Marmotta Contexts list");
	final Table bim_projects_table = new Table(
			"Click the model to see the BIMServer view of the mdoel.");
	final public OptionGroup converter_selection = new OptionGroup(
			"Optionally an IFC to RDF converter can be selected");
	final TextField url_textField = new TextField();
	
	// The field for inserting a new real estate
	final TextField site_textField = new TextField();
	final Tree sites_tree = new Tree("Sites and connected models");
	final Map<String,TreeNode> treenodes = new HashMap<String,TreeNode>();
	
	final Tree models_tree = new Tree("Models that have a description");
	
	// Model Import Real estates list
	final Tree realEstates_tree_4upload = new Tree("Attach the model to a site");
	
	// A map for project selections
	Map<Integer, Long> bim_projects = new HashMap<Integer, Long>();
	final TextArea sparql_query_area = new TextArea("");
	final RichTextArea sparql_result_rtarea = new RichTextArea();
	final Table links_table = new Table("Links statements (Generation may take some time.)");

	final String query_realEstates="PREFIX drumbeat:   <http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/> \nPREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \nselect $s \nwhere\n{\n?s rdf:type drumbeat:RealEstate .\n}\n LIMIT 100";
	final String query_structural_links=""
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
	
	final String query_template_links=""
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
	
	final String query_structural_project=""
			+"prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+"\nprefix ifc:   <http://drumbeat.cs.hut.fi/owl/IFC2X3_Standard#>"
			+"\n"
			+"\nselect ?s ?name"
			+"\nFROM <http://drumbeat.cs.hut.fi/tomcat/marmotta/context/Structural>"
			+"\nWHERE {"
			+"\n?s rdf:type ifc:IfcProject."
			+"\n?s ifc:name ?name ."
			+"\n}";
			
	final String query_implemens =""
			+"prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+"\nprefix ifc:   <http://drumbeat.cs.hut.fi/owl/IFC2X3_Standard#>"
			+"\n"
			+"\nselect ?s ?o"
			+"\nWHERE {"
			+"\n?s ifc:implements ?o ."
			+"\n} LIMIT 10";

		
	
	final ComboBox contexts_selection = new ComboBox("Select a context");
	final List<Pair> generated_links=new ArrayList<Pair>();
	@Override
	protected void init(VaadinRequest request) {
		String basepath = VaadinService.getCurrent().getBaseDirectory()
				.getAbsolutePath();
		// Image as a file resource
		FileResource resource = new FileResource(new File(basepath
				+ "/WEB-INF/images/drumbeat_banner.jpg"));

		// Show the image in the application
		Image drumbeat_logo = new Image(
				"Aalto University Drumbeat User Interface", resource);
		final VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.addComponent(drumbeat_logo);

		layout.setMargin(true);
		layout.addComponent(tabsheet);

		// ========= REAL ESTATES ====================

		VerticalLayout tab_realEstate = new VerticalLayout();
		tab_realEstate.setCaption("Sites");
		tabsheet.addTab(tab_realEstate);

		Panel p_project = new Panel("Create a sites");
		p_project.setWidth("400");
		tab_realEstate.addComponent(sites_tree);
		Button removeRealEstate_button = new Button(
				"Remove the selected site", new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						
						TreeNode realEstate = (TreeNode) sites_tree.getValue();
						if (realEstate != null) {
							//Only real estates
							Object parent=sites_tree.getParent(realEstate);
							if(parent==null)
							{
							   marmotta_sparql.remove_RealEstate(realEstate.getInternal_name());
							   listRealEstates();
							}
						}
					}
				});
		tab_realEstate.addComponent(removeRealEstate_button);
		tab_realEstate.addComponent(p_project);

		site_textField.setImmediate(true);
		Button newRealEstate_button = new Button("Create",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						String realEstate_name = site_textField
								.getValue();
						if (realEstate_name != null
								&& realEstate_name.length() > 0) {
							marmotta_sparql.create_RealEstate(realEstate_name);
							listRealEstates();
						}
					}
				});
		HorizontalLayout newproject_layout = new HorizontalLayout();
		newproject_layout.setSizeUndefined();
		newproject_layout.addComponent(site_textField);
		newproject_layout.addComponent(newRealEstate_button);
		newproject_layout.setSpacing(true);
		p_project.setContent(newproject_layout);

		// ========= MODELS ====================
		VerticalLayout tab_models = new VerticalLayout();
		tab_models.setCaption("Models");
		tabsheet.addTab(tab_models);
		tab_models.addComponent(models_tree);
		Link void_link = new Link("Void description of the models",
		        new ExternalResource("http://drumbeat.cs.hut.fi/void.ttl"));
		tab_models.addComponent(void_link);
		Panel p_model = new Panel("Upload and convert an IFC file");
		p_model.setWidth("900");
		tab_models.addComponent(p_model);

		HorizontalLayout hor1 = new HorizontalLayout();
		hor1.setSizeFull(); // Use all available space
		hor1.setMargin(true);
		p_model.setContent(hor1);

		converter_selection.addItem("Default");
		converter_selection.addItem("Lite");
		converter_selection.addItem("Ghent Multimedia Lab, buildingSMART");
		converter_selection.setValue("Default");

		VerticalLayout upload_selections = new VerticalLayout();
		hor1.addComponent(upload_selections);

		upload_selections.addComponent(converter_selection);
		upload_selections.addComponent(realEstates_tree_4upload);
		VerticalLayout upload_panels = new VerticalLayout();
		hor1.addComponent(upload_panels);

		Panel p_model_file = new Panel("Upload a file");
		p_model_file.setWidth("400");
		upload_panels.addComponent(p_model_file);

		Panel p_model_url = new Panel("Upload from a URL");
		p_model_url.setWidth("400");
		upload_panels.addComponent(p_model_url);

		// Create the upload with a caption and set receiver later
		Upload upload = new Upload("Select a file and press Upload",
				drumbeat_fileReceiver);
		upload.addSucceededListener(drumbeat_fileReceiver);
		upload.addFailedListener(drumbeat_fileReceiver);
		p_model_file.setContent(upload);

		url_textField.setImmediate(true);
		Button button = new Button("Upload from the URL",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						drumbeat_fileReceiver.receiveFileFromURL(url_textField
								.getValue());
					}
				});

		HorizontalLayout url_upload = new HorizontalLayout();
		url_upload.setSizeUndefined();
		url_upload.addComponent(url_textField);
		url_upload.addComponent(button);
		url_upload.setSpacing(true);
		p_model_url.setContent(url_upload);

		
		// Define two columns for the built-in container
		bim_projects_table.addContainerProperty("Name", String.class, null);
		bim_projects_table.addContainerProperty("Created", String.class, null);
		bim_projects_table.setSelectable(true);
		// Send changes in selection immediately to server.
		bim_projects_table.setImmediate(true);

		final VerticalLayout project_browser = new VerticalLayout();
		// Handle selection change.
		bim_projects_table.addValueChangeListener(new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {

				if (bim_projects.size() > 0) {
					showBIMProject(project_browser,
							bim_projects.get(bim_projects_table.getValue()));
				}
			}
		});
		tab_models.addComponent(bim_projects_table);
		tab_models.addComponent(project_browser);

		
		// ========= Queries ====================
		VerticalLayout tab_queries = new VerticalLayout();
		tab_queries.setCaption("Queries");
		tabsheet.addTab(tab_queries);
		Panel p_sparql = new Panel("Sparql query");
		p_sparql.setWidth("900");		
		VerticalLayout sparql_layout = new VerticalLayout();
		final ComboBox queries = new ComboBox("Select a query");
		queries.setInvalidAllowed(false);
		queries.setNullSelectionAllowed(false);
		queries.setNewItemsAllowed(false);
		queries.addItem("Sites");
		queries.setValue("Sites");
		queries.addItem("Structural model links");
		queries.addItem("Project name");
		queries.addItem("List implements links");
		queries.setWidth("400");
		queries.addListener(new Property.ValueChangeListener() {
	            private static final long serialVersionUID = -5188369735622627751L;

	            public void valueChange(ValueChangeEvent event) {
	                if (queries.getValue() != null) {
	                	if(queries.getValue().equals("Sites"))
	                		sparql_query_area.setValue(query_realEstates);
	                	if(queries.getValue().equals("Structural model links"))
	                		sparql_query_area.setValue(query_structural_links);
	                	if(queries.getValue().equals("Project name"))
	                		sparql_query_area.setValue(query_structural_project);
	                	if(queries.getValue().equals("List implements links"))
	                		sparql_query_area.setValue(query_implemens);
	                }
	            }
	        });
		
		sparql_layout.addComponent(queries);
		sparql_layout.addComponent(sparql_query_area);
		sparql_query_area.setValue(query_realEstates);
				
		Button sparql_button = new Button("Query",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						 sparql_result_rtarea.setValue(marmotta.httpGetQuery2html(sparql_query_area.getValue()));
					}
				});
		
		Button sparql_button_json = new Button("Query and download JSON");		
		OnDemandFileDownloader  jsonDownloader = new  OnDemandFileDownloader(createOnDemandJSON_Resource(),"JSON");
	    jsonDownloader.extend(sparql_button_json);

		Button sparql_button_xml = new Button("Query and download XML");		
		OnDemandFileDownloader  xmlDownloader = new  OnDemandFileDownloader(createOnDemandXMLResource(),"XML");
	    xmlDownloader.extend(sparql_button_xml);

	    
		HorizontalLayout hor_sparql_buttons = new HorizontalLayout();
		hor_sparql_buttons.addComponent(sparql_button);
		hor_sparql_buttons.addComponent(sparql_button_json);
		hor_sparql_buttons.addComponent(sparql_button_xml);
		sparql_layout.addComponent(hor_sparql_buttons);
		sparql_layout.addComponent(sparql_result_rtarea);
		sparql_query_area.setWidth("800");
		sparql_query_area.setHeight("400");
		sparql_result_rtarea.setWidth("800");
		p_sparql.setContent(sparql_layout);
		
		tab_queries.addComponent(p_sparql);
		
		// ========= Links ====================
		VerticalLayout tab_links = new VerticalLayout();
		tab_links.setCaption("Links");
		tabsheet.addTab(tab_links);

		Panel p_links = new Panel("Implements query");
		p_links.setWidth("900");		
		VerticalLayout links_layout = new VerticalLayout();
		contexts_selection.setInvalidAllowed(false);
		contexts_selection.setNullSelectionAllowed(false);
		contexts_selection.setNewItemsAllowed(false);
		contexts_selection.setWidth("800");
		
		links_layout.addComponent(contexts_selection);		
		
				
		Button links_button = new Button("List links",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						if(contexts_selection.getValue()!=null && contexts_selection.getValue().toString().length()>0)
						{
						 String query=ToolkitString.strReplaceLike(query_template_links, "<context>", "<"+contexts_selection.getValue()+">");
						 List<Pair> links=marmotta.httpGetLinks(query);
						 links_table.removeAllItems();
						 generated_links.clear();
						 for(Pair p:links)
						 {
							 Object newItemId = links_table.addItem();
							 Item row = links_table.getItem(newItemId);
							 String short_from=ToolkitString.strReplaceLike(p.getS1(), "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/", "resourse:");
							 String short_to=ToolkitString.strReplaceLike(p.getS2(), "http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/", "resourse:");
							 row.getItemProperty("From").setValue(short_from);
							 row.getItemProperty("property").setValue("ifc:implements");
							 row.getItemProperty("To").setValue(short_to);
                             generated_links.add(p);
						 }
						}
					}
				});
		
	    
		links_layout.addComponent(links_button);
		
		links_table.addContainerProperty("From", String.class, null);
		links_table.addContainerProperty("property", String.class, null);
		links_table.addContainerProperty("To", String.class, null);
		links_table.setSelectable(false);
		
		
		links_layout.addComponent(links_table);
		links_table.setWidth("800");
		
		
		Button insert_links_button = new Button("Insert links to the RDF Store",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {						
						marmotta_sparql.add_linkset2Model(generated_links);
					}
				});
		
		Button remove_links_button = new Button("Remove links from the RDF Store",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {						
						marmotta_sparql.remove_linksetFromModel(generated_links);
					}
				});
		
		
		HorizontalLayout hor_links_buttons = new HorizontalLayout();
		hor_links_buttons.addComponent(insert_links_button);
		hor_links_buttons.addComponent(remove_links_button);
		
		links_layout.addComponent(hor_links_buttons);
		p_links.setContent(links_layout);
		
		tab_links.addComponent(p_links);

		
		
		// ========= Resources ====================
		VerticalLayout tab_internaldata = new VerticalLayout();
		tab_internaldata.setCaption("Internal resources");
		tabsheet.addTab(tab_internaldata);


		BrowserFrame browser = new BrowserFrame("", new ExternalResource(
				"http://drumbeat.cs.hut.fi/home.html"));
		browser.setWidth("1200px");
		browser.setHeight("800px");
		tab_internaldata.addComponent(browser);

		files_table.addContainerProperty("File name", String.class, null);
		listIFCFiles();
		tab_internaldata.addComponent(files_table);


		contexts_table.addContainerProperty("Graph", String.class, null);
		contexts_table.addContainerProperty("Size", Long.class, null);
		tab_internaldata.addComponent(contexts_table);


		updateData();
		listRealEstates();
	}

	private OnDemandStreamResource createOnDemandJSON_Resource() {
		return new OnDemandStreamResource() {

			public InputStream getStream() {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				String result = marmotta.httpGetQuery2json(sparql_query_area
						.getValue());
				try {
					bos.write(result.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new ByteArrayInputStream(bos.toByteArray());
			}
			@Override
			public String getFilename() {
				return "output.srj";
			}
		};
	}

	private OnDemandStreamResource createOnDemandXMLResource() {
		return new OnDemandStreamResource() {

			public InputStream getStream() {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				String result = marmotta.httpGetQuery2XML(sparql_query_area
						.getValue());
				try {
					bos.write(result.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new ByteArrayInputStream(bos.toByteArray());
			}
			@Override
			public String getFilename() {
				return "output.srj";
			}
		};
	}
	
	public interface OnDemandStreamResource extends StreamSource {
		String getFilename();
	}

	class OnDemandFileDownloader extends FileDownloader {

		private static final long serialVersionUID = 1L;

		private final OnDemandStreamResource onDemandStreamResource;
		private final String type;

	
		public OnDemandFileDownloader(OnDemandStreamResource onDemandStreamResource,String type) {
			super(new StreamResource(onDemandStreamResource, ""));
			this.onDemandStreamResource = onDemandStreamResource;
			this.type=type;
		}
		
		 @Override
		  public boolean handleConnectorRequest (VaadinRequest request, VaadinResponse response, String path)
		      throws IOException {
		    getResource().setFilename(onDemandStreamResource.getFilename());
		    return super.handleConnectorRequest(request, response, path);
		  }

		private StreamResource getResource() {
			if(type.equals("JSON"))
			{
			     StreamResource resource = new StreamResource(createOnDemandJSON_Resource(),"output.srj");
			     return (StreamResource) resource ; 
			}
			else
			{
				 StreamResource resource = new StreamResource(createOnDemandXMLResource(),"output.xml");
				 return (StreamResource) resource ; 
				
			}
		}

	}

	
	public String getRealEstate() {
		String realEstate=null;
		try {
			realEstate = 	URLEncoder.encode((String) realEstates_tree_4upload.getValue(), "UTF-8");
		} catch (Exception e) {
			// nothing
		}
		if(realEstate==null)
			return "";
		return realEstate; 
	}
	
	public void createModel(String model_name) {
		marmotta_sparql.create_Model(model_name);
		try {
			String realEstate = (String) realEstates_tree_4upload.getValue();
			if (realEstate != null) {
                      marmotta_sparql.attach_model(realEstate, model_name);
			}
		} catch (Exception e) {
			// nothing
		}

	}

	public void listRealEstates() {
		sites_tree.removeAllItems();
		treenodes.clear();
		realEstates_tree_4upload.removeAllItems();
		List<String> realEstate_names = marmotta.httpGetDRUMRealEstates();
		boolean used = false;
		for (String name : realEstate_names) {
			TreeNode re=new TreeNode(name,name);
			treenodes.put(name, re);
			sites_tree.addItem(re);
			realEstates_tree_4upload.addItem(name);
			if (!used) {
				realEstates_tree_4upload.setValue(name); // The default value
				used = true;
			}
		}
		
		List<Pair> models=marmotta.httpGetRealEstateModels();		
		for (Pair pair : models) {
			TreeNode re=treenodes.get(pair.getS1());
			if(re!=null)
			{
			  TreeNode mo=new TreeNode(pair.getS1()+"."+pair.getS2(),pair.getS2());	
			  sites_tree.addItem(mo);
			  sites_tree.setParent(mo, re);
			}
		}
	}

	public void listModels() {
		models_tree.removeAllItems();
		List<String> model_names = marmotta.httpGetModels();
		System.out.println(model_names);
		for (String name : model_names) {
			models_tree.addItem(name);
		}

	}

	public void updateData() {
		listModels();
		listIFCFiles();
		List<String> contexts=marmotta.httpGetMarmottaContexts();
		if(contexts!=null && contexts.size()>0)
		{
			contexts_selection.removeAllItems();
			boolean set=false;
			for(String c:contexts)
			{
				contexts_selection.addItem(c);
				if(!set)
				{
					contexts_selection.setValue(c);
					set=true;
				}
			}
		}
		marmotta.httpGetMarmottaContexts(contexts_table);
		bimserver_jsonapi.getProjects(bim_projects_table, bim_projects);

	}

	private void listIFCFiles() {

		try {

			try {
				if (files_table.isVisible())
					files_table.removeAllItems();
			} catch (Exception e) {
				// update before table initalization?
				return;
			}

			File folder = new File(uploads);
			File[] listOfFiles = folder.listFiles();

			for (File file : listOfFiles) {
				if (file.isFile()) {
					Object newItemId = files_table.addItem();
					Item row = files_table.getItem(newItemId);
					row.getItemProperty("File name").setValue(file.getName());
				}
			}
			files_table.setPageLength(files_table.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showBIMProject(VerticalLayout layout, long pid) {
		layout.removeAllComponents();
		ExternalResource er = new ExternalResource(
				"http://drumbeat.cs.hut.fi/tomcat/bimviews/?page=Project&poid="
						+ pid);
		BrowserFrame bim_browser = new BrowserFrame("", er);
		bim_browser.setWidth("1200px");
		bim_browser.setHeight("800px");

		layout.addComponent(bim_browser);

	}

}