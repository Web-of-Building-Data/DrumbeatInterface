package fi.ni.drumbeatinterface;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import fi.ni.bimserver.BIMServerJSONApi;
import fi.ni.marmotta.MarmottaAPI;

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
	final MarmottaAPI marmotta=new MarmottaAPI();
	final BIMServerJSONApi bimserver_jsonapi=new BIMServerJSONApi();
	private String uploads = "/var/uploads/";
	//private String uploads= "c:/jo/uploads/";
	Upload upload=null;

	DrumbeatFileHandler drumbeat_fileReceiver = new DrumbeatFileHandler(this,uploads);
	TabSheet tabsheet = new TabSheet();
	final Table files_table = new Table("Uploaded files");
	final Table contexts_table = new Table("contexts list");
	final Table bim_projects_table = new Table("as seen by drumcsbeat@gmail.com");
	final public OptionGroup converter_selection = new OptionGroup("Optionally an IFC to RDF converter can be selected");
	final TextField url_textField = new TextField();
	// A map for project selections
	Map<Integer, Long> projects = new HashMap<Integer, Long>();

	@Override
	protected void init(VaadinRequest request) {
		String basepath = VaadinService.getCurrent()
                .getBaseDirectory().getAbsolutePath();
		// Image as a file resource
		FileResource resource = new FileResource(new File(basepath +
		                        "/WEB-INF/images/drumbeat_banner.jpg"));

		// Show the image in the application
		Image drumbeat_logo = new Image("Aalto University Drumbeat User Interface", resource);
		final VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.addComponent(drumbeat_logo);

		layout.setMargin(true);
		layout.addComponent(tabsheet);

		VerticalLayout tab_home = new VerticalLayout();
		tab_home.setCaption("Home");
		tabsheet.addTab(tab_home);
		
		BrowserFrame browser = new BrowserFrame("", new ExternalResource(
				"http://drumbeat.cs.hut.fi/home.html"));
		browser.setWidth("1200px");
		browser.setHeight("800px");
		tab_home.addComponent(browser);
		

		VerticalLayout tab_project = new VerticalLayout();
		tab_project.setCaption("Project");
		tabsheet.addTab(tab_project);

		
		VerticalLayout tab_upload = new VerticalLayout();
		tab_upload.setCaption("Upload a model");
		tabsheet.addTab(tab_upload);
		Panel p1 = new Panel("Upload and convert an IFC file");
		p1.setWidth("900");
		tab_upload.addComponent(p1);
		
		HorizontalLayout hor1 = new HorizontalLayout();
		hor1.setSizeFull(); // Use all available space
		hor1.setMargin(true);
		p1.setContent(hor1);
		

		
		converter_selection.addItem("Default");
		converter_selection.addItem("Lite");
		converter_selection.addItem("Ghent Multimedia Lab, buildingSMART");
		converter_selection.setValue("Default");
		hor1.addComponent(converter_selection);
		VerticalLayout upload_panels = new VerticalLayout();
		hor1.addComponent(upload_panels);
		
		Panel p1file = new Panel("Upload a file");
		//p1file.setSizeUndefined();  // to avoid scrollbar
		p1file.setWidth("400");
		upload_panels.addComponent(p1file);
		
		Panel p1url = new Panel("Upload from a URL");
		//p1url.setSizeUndefined();  // to avoid scrollbar
		p1url.setWidth("400");
		upload_panels.addComponent(p1url);

		
		// Create the upload with a caption and set receiver later
		Upload upload = new Upload("Select a file and press Upload", drumbeat_fileReceiver);
		upload.addSucceededListener(drumbeat_fileReceiver);
		upload.addFailedListener(drumbeat_fileReceiver);
		p1file.setContent(upload);
		
		url_textField.setImmediate(true);
	    Button button = new Button("Upload from the URL", new Button.ClickListener() {
	      @Override
	      public void buttonClick(Button.ClickEvent event) {
	    	  drumbeat_fileReceiver.receiveFileFromURL(url_textField.getValue());
	      }
	    });

	    HorizontalLayout url_upload = new HorizontalLayout();
	    url_upload.setSizeUndefined(); 
	    url_upload.addComponent(url_textField);
	    url_upload.addComponent(button);
	    url_upload.setSpacing(true);	    
	    p1url.setContent(url_upload);
		
		files_table.addContainerProperty("File name", String.class, null);
		listIFCFiles();
		tab_upload.addComponent(files_table);

		VerticalLayout tab_bimserver = new VerticalLayout();
		tab_bimserver.setCaption("BIMServer projects");
		tabsheet.addTab(tab_bimserver);


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

				if (projects.size() > 0) {
					showBIMProject(project_browser,
							projects.get(bim_projects_table.getValue()));
				}
			}
		});
		tab_bimserver.addComponent(bim_projects_table);
		tab_bimserver.addComponent(project_browser);

		VerticalLayout tab_marmotta = new VerticalLayout();
		tab_marmotta.setCaption("Marmotta graphs");
		tabsheet.addTab(tab_marmotta);
		contexts_table.addContainerProperty("Graph", String.class, null);
		contexts_table.addContainerProperty("Size", Long.class, null);
		tab_marmotta.addComponent(contexts_table);
        updateData();
	}
	
	public void updateData()
	{
		listIFCFiles();
		marmotta.httpGetMarmottaContexts(contexts_table);
		bimserver_jsonapi.getProjects(bim_projects_table,projects);
		
	}

	private void listIFCFiles() {
		
		try {
			
			try
			{
			  if(files_table.isVisible())
				  files_table.removeAllItems();
			}
			catch(Exception e)
			{
				//update before table initalization?
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