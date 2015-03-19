package fi.ni.drumbeatinterface;

import java.io.File;
import java.util.HashMap;
import java.util.List;
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
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
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
	final Table contexts_table = new Table("contexts list");
	final Table bim_projects_table = new Table(
			"as seen by drumcsbeat@gmail.com");
	final public OptionGroup converter_selection = new OptionGroup(
			"Optionally an IFC to RDF converter can be selected");
	final TextField url_textField = new TextField();
	final TextField realEstate_textField = new TextField();
	final Tree realEstates_tree = new Tree("Real estates and connected models");
	final Tree models_tree = new Tree("Models that have a description");
	final Tree realEstates_tree_4upload = new Tree(
			"Attach the model to a real estate");
	// A map for project selections
	Map<Integer, Long> bim_projects = new HashMap<Integer, Long>();

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
		tab_realEstate.setCaption("Real Estates");
		tabsheet.addTab(tab_realEstate);

		Panel p_project = new Panel("Create a new real estate");
		p_project.setWidth("400");
		tab_realEstate.addComponent(realEstates_tree);
		Button removeRealEstate_button = new Button(
				"Remove the selected real estate", new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						
						String realEstate_name = (String) realEstates_tree
								.getValue();
						if (realEstate_name != null
								&& realEstate_name.length() > 0) {
							//Only real estates
							Object parent=realEstates_tree.getParent(realEstate_name);
							if(parent==null)
							{
							   marmotta_sparql.remove_RealEstate(realEstate_name);
							   listRealEstates();
							}
						}
					}
				});
		tab_realEstate.addComponent(removeRealEstate_button);
		tab_realEstate.addComponent(p_project);

		realEstate_textField.setImmediate(true);
		Button newRealEstate_button = new Button("Create",
				new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						String realEstate_name = realEstate_textField
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
		newproject_layout.addComponent(realEstate_textField);
		newproject_layout.addComponent(newRealEstate_button);
		newproject_layout.setSpacing(true);
		p_project.setContent(newproject_layout);

		// ========= MODELS ====================
		VerticalLayout tab_models = new VerticalLayout();
		tab_models.setCaption("Models");
		tabsheet.addTab(tab_models);
		tab_models.addComponent(models_tree);
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

		files_table.addContainerProperty("File name", String.class, null);
		listIFCFiles();
		tab_models.addComponent(files_table);

		// ========= BIMSERVER ====================
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

				if (bim_projects.size() > 0) {
					showBIMProject(project_browser,
							bim_projects.get(bim_projects_table.getValue()));
				}
			}
		});
		tab_bimserver.addComponent(bim_projects_table);
		tab_bimserver.addComponent(project_browser);

		// ========= MARMOTTA ====================
		VerticalLayout tab_marmotta = new VerticalLayout();
		tab_marmotta.setCaption("Marmotta graphs");
		tabsheet.addTab(tab_marmotta);
		contexts_table.addContainerProperty("Graph", String.class, null);
		contexts_table.addContainerProperty("Size", Long.class, null);
		tab_marmotta.addComponent(contexts_table);

		// ========= INTERNAL ====================
		VerticalLayout tab_home = new VerticalLayout();
		tab_home.setCaption("Internal");
		tabsheet.addTab(tab_home);

		BrowserFrame browser = new BrowserFrame("", new ExternalResource(
				"http://drumbeat.cs.hut.fi/home.html"));
		browser.setWidth("1200px");
		browser.setHeight("800px");
		tab_home.addComponent(browser);

		updateData();
		listRealEstates();
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
		realEstates_tree.removeAllItems();
		realEstates_tree_4upload.removeAllItems();
		List<String> realEstate_names = marmotta.httpGetDRUMRealEstates();
		boolean used = false;
		for (String name : realEstate_names) {
			realEstates_tree.addItem(name);
			realEstates_tree_4upload.addItem(name);
			if (!used) {
				realEstates_tree_4upload.setValue(name); // The default value
				used = true;
			}
		}
		
		List<Pair> models=marmotta.httpGetRealEstateModels();		
		for (Pair pair : models) {
			System.out.println("s "+pair.getS1());
			System.out.println("o "+pair.getS2());
			realEstates_tree.addItem(pair.getS2());
			realEstates_tree.setParent(pair.getS2(), pair.getS1());
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