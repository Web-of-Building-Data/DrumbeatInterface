package fi.aalto.drumbeat.drumbeatUI;

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
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Link;
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

import fi.aalto.drumbeat.apicalls.bimserver.BIMServerJSONApi;
import fi.aalto.drumbeat.apicalls.marmotta.MarmottaAPI;
import fi.aalto.drumbeat.apicalls.marmotta.MarmottaSparql;
import fi.aalto.drumbeat.apicalls.marmotta.vo.Pair;

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
	final BIMServerJSONApi bimserver_jsonapi = new BIMServerJSONApi(DrumbeatConstants.bimserver_api_json_url,DrumbeatConstants.bimserver_user, DrumbeatConstants.bimserver_password);
	private String uploads = DrumbeatConstants.ifc_uploads_directory;

	DrumbeatFileHandler drumbeat_fileReceiver = new DrumbeatFileHandler(this,
			uploads);
	TabSheet tabsheet = new TabSheet();
	final Table files_table = new Table("Uploaded files");
	final Table contexts_table = new Table("Marmotta Contexts list");
	final ComboBox bim_projects_selection = new ComboBox("BIMServer view of the model");
	
	final TextField url_textField = new TextField();
	
	// The field for inserting a new real estate
	final TextField site_textField = new TextField();
	final Tree sites_tree = new Tree("Sites and connected data sets");
	final Map<String,TreeNode> treenodes = new HashMap<String,TreeNode>();
	
	final Tree datasets_tree = new Tree("Data sets that have a description");
	
	// Model Import Real estates list
	final Tree sites_tree_4upload = new Tree("Attach the data set to a site");
	
	// A map for project selections
	Map<String, Long> bim_projects = new HashMap<String, Long>();
	final TextArea sparql_query_area = new TextArea("");
	final RichTextArea sparql_result_rtarea = new RichTextArea();
	final Table links_table = new Table("Links statements (Generation may take some time.)");

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

		createTab_Sites();
		
		createTab_Datasets();
		
		createTab_Queries();
		
		createTab_Links();

		creatTab_Resources();


		updateData();
		listSites();
	}


	private void createTab_Sites() {
		VerticalLayout tab_site = new VerticalLayout();
		tab_site.setCaption("Sites");
		tabsheet.addTab(tab_site);

		Panel p_project = new Panel("Create a sites");
		p_project.setWidth("400");

		
		HorizontalLayout hor_sites = new HorizontalLayout();
		hor_sites.addComponent(sites_tree);
		final VerticalLayout project_browser4Sites = new VerticalLayout();
		hor_sites.addComponent(project_browser4Sites);
		tab_site.addComponent(hor_sites);
		htmlView_Project(project_browser4Sites,"Structural");
		
		sites_tree.addValueChangeListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 7237016481874141615L;
            
            public void valueChange(ValueChangeEvent event) {
                if (! sites_tree.hasChildren(sites_tree.getValue()))
                {
                   if(sites_tree.getValue()!=null)
                     htmlView_Project(project_browser4Sites,sites_tree.getValue().toString());
                }
            }
        });
        sites_tree.setImmediate(true);
		
		
		Button newRealEstate_button = new Button("Create",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						String realEstate_name = site_textField
								.getValue();
						if (realEstate_name != null
								&& realEstate_name.length() > 0) {
							marmotta_sparql.create_RealEstate(realEstate_name);
							listSites();
						}
					}
				});

		HorizontalLayout newproject_layout = new HorizontalLayout();
		newproject_layout.setSizeUndefined();
		site_textField.setImmediate(true);
		newproject_layout.addComponent(site_textField);
		newproject_layout.addComponent(newRealEstate_button);
		newproject_layout.setSpacing(true);
		p_project.setContent(newproject_layout);
		tab_site.addComponent(p_project);


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
							   listSites();
							}
						}
					}
				});
		tab_site.addComponent(removeRealEstate_button);
	}

	
	@SuppressWarnings("deprecation")
	private void createTab_Datasets() {
		VerticalLayout tab_datasets = new VerticalLayout();
		tab_datasets.setCaption("Data sets");
		tabsheet.addTab(tab_datasets);
		tab_datasets.addComponent(datasets_tree);
		Link void_link = new Link("Void description of the data sets",
		        new ExternalResource("http://drumbeat.cs.hut.fi/void.ttl"));
		tab_datasets.addComponent(void_link);
		Panel p_model = new Panel("Upload and convert an IFC file");
		p_model.setWidth("900");
		tab_datasets.addComponent(p_model);
	
		HorizontalLayout hor1 = new HorizontalLayout();
		hor1.setSizeFull(); // Use all available space
		hor1.setMargin(true);
		p_model.setContent(hor1);
	
	
	
		VerticalLayout upload_selections = new VerticalLayout();
		hor1.addComponent(upload_selections);
	
		upload_selections.addComponent(sites_tree_4upload);
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
	
		bim_projects_selection.setInvalidAllowed(false);
		bim_projects_selection.setNullSelectionAllowed(false);
		bim_projects_selection.setNewItemsAllowed(false);
		bim_projects_selection.setWidth("400");
		final VerticalLayout project_browser = new VerticalLayout();
		bim_projects_selection.addListener(new Property.ValueChangeListener() {
	            private static final long serialVersionUID = -5188369735622627751L;
	
	            public void valueChange(ValueChangeEvent event) {
	                if (bim_projects_selection.getValue() != null) {
	                	htmlView_BIMProject(project_browser,
								bim_projects.get(bim_projects_selection.getValue()));
	                }
	            }
	        });
		
		tab_datasets.addComponent(bim_projects_selection);
		tab_datasets.addComponent(project_browser);
	}


	@SuppressWarnings("deprecation")
	private void createTab_Queries() {
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
	                		sparql_query_area.setValue(DrumbeatConstants.query_sites);
	                	if(queries.getValue().equals("Structural model links"))
	                		sparql_query_area.setValue(DrumbeatConstants.query_structural_links);
	                	if(queries.getValue().equals("Project name"))
	                		sparql_query_area.setValue(DrumbeatConstants.query_structural_project);
	                	if(queries.getValue().equals("List implements links"))
	                		sparql_query_area.setValue(DrumbeatConstants.query_implemens);
	                }
	            }
	        });
		
		sparql_layout.addComponent(queries);
		sparql_layout.addComponent(sparql_query_area);
		sparql_query_area.setValue(DrumbeatConstants.query_sites);
				
		Button sparql_button = new Button("Query",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						 sparql_result_rtarea.setValue(marmotta.httpGetQuery2html(sparql_query_area.getValue()));
					}
				});
		
		Button sparql_button_json = new Button("Query and download JSON");		
		OnDemandFileDownloader  jsonDownloader = new  OnDemandFileDownloader(createOnDemandJSON_Resource(),"JSON",this);
	    jsonDownloader.extend(sparql_button_json);
	
		Button sparql_button_xml = new Button("Query and download XML");		
		OnDemandFileDownloader  xmlDownloader = new  OnDemandFileDownloader(createOnDemandXMLResource(),"XML",this);
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
	}


	private void createTab_Links() {
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
					@SuppressWarnings("unchecked")
					@Override
					public void buttonClick(Button.ClickEvent event) {
						if(contexts_selection.getValue()!=null && contexts_selection.getValue().toString().length()>0)
						{
						 String query=ToolkitString.strReplaceLike(DrumbeatConstants.querytemplate_links, "<context>", "<"+contexts_selection.getValue()+">");
						 List<Pair> links=marmotta.httpGetLinks(query);
						 links_table.removeAllItems();
						 generated_links.clear();
						 for(Pair p:links)
						 {
							 Object newItemId = links_table.addItem();
							 Item row = links_table.getItem(newItemId);
							 String short_from=ToolkitString.strReplaceLike(p.getS1(), DrumbeatConstants.resource_baseurl, "resourse:");
							 String short_to=ToolkitString.strReplaceLike(p.getS2(), DrumbeatConstants.resource_baseurl, "resourse:");
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
	}


	private void creatTab_Resources() {
		VerticalLayout tab_internaldata = new VerticalLayout();
		tab_internaldata.setCaption("Internal resources");
		tabsheet.addTab(tab_internaldata);
	
	
		BrowserFrame browser = new BrowserFrame("", new ExternalResource(
				"http://drumbeat.cs.hut.fi/home.html"));
		browser.setWidth("900px");
		browser.setHeight("450px");
		tab_internaldata.addComponent(browser);
	
		files_table.addContainerProperty("File name", String.class, null);
		listUploaded_IFCFiles();
		tab_internaldata.addComponent(files_table);
	
	
		contexts_table.addContainerProperty("Graph", String.class, null);
		contexts_table.addContainerProperty("Size", Long.class, null);
		tab_internaldata.addComponent(contexts_table);
	}

    /*
     * Returns the on demand JSON resource for JSON export of the SPARQL query
     */
	public OnDemandStreamResource createOnDemandJSON_Resource() {
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

    /*
     * Returns the on demand JSON resource for XML export of the SPARQL query
     */

	public OnDemandStreamResource createOnDemandXMLResource() {
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
	

	/*
	 * Get the currently selected Site at the dataset import form
	 */
	public String getSite() {
		String realEstate=null;
		try {
			realEstate = 	URLEncoder.encode((String) sites_tree_4upload.getValue(), "UTF-8");
		} catch (Exception e) {
			// nothing
		}
		if(realEstate==null)
			return "";
		return realEstate; 
	}
	
	/*
	 * The Datset import calls this to create a new datset description
	 */
	public void createDataset(String dataset_name) {
		marmotta_sparql.create_Model(dataset_name);
		try {
			String site = (String) sites_tree_4upload.getValue();
			if (site != null) {
                      marmotta_sparql.attach_dataset(site, dataset_name);
			}
		} catch (Exception e) {
			// nothing
		}

	}

	/*
	 * Updates the list of the sites saved into the system
	 */

	public void listSites() {
		sites_tree.removeAllItems();
		treenodes.clear();
		sites_tree_4upload.removeAllItems();
		List<String> realEstate_names = marmotta.httpGetSites();
		boolean used = false;
		for (String name : realEstate_names) {
			TreeNode re=new TreeNode(name,name);
			treenodes.put(name, re);
			sites_tree.addItem(re);
			sites_tree_4upload.addItem(name);
			if (!used) {
				sites_tree_4upload.setValue(name); // The default value
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

	/*
	 * Updates the list of the graphs saved into the system
	 */
	public void listDataSets() {
		datasets_tree.removeAllItems();
		List<String> model_names = marmotta.httpGetDatasets();
		System.out.println(model_names);
		for (String name : model_names) {
			datasets_tree.addItem(name);
		}

	}

	/*
	 * Updates data field contents on the screen
	 */
	public void updateData() {
		listDataSets();
		listUploaded_IFCFiles();
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
		bimserver_jsonapi.getProjects(bim_projects_selection, bim_projects);

	}

	@SuppressWarnings("unchecked")
	private void listUploaded_IFCFiles() {

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

	
	private void htmlView_BIMProject(VerticalLayout layout, long pid) {
		layout.removeAllComponents();
		ExternalResource er = new ExternalResource(
				DrumbeatConstants.bimsurfer_project_url	+ pid);
		BrowserFrame bim_browser = new BrowserFrame("", er);
		bim_browser.setWidth("900px");
		bim_browser.setHeight("800px");

		layout.addComponent(bim_browser);

	}

	private void htmlView_Project(VerticalLayout layout, String project_name) {
		layout.removeAllComponents();
		ExternalResource er = new ExternalResource(				
						DrumbeatConstants.drumbeatview_project_url+ project_name);
		BrowserFrame bim_browser = new BrowserFrame("", er);
		bim_browser.setWidth("700px");
		bim_browser.setHeight("650px");

		layout.addComponent(bim_browser);

	}

}