package fi.aalto.drumbeat.drumbeatUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import fi.aalto.drumbeat.apicalls.bimserver.BIMFileLoader;
import fi.hut.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.hut.cs.drumbeat.ifc.convert.ifc2rdf.util.Ifc2RdfExportUtil;
import fi.hut.cs.drumbeat.ifc.convert.step2ifc.util.IfcParserUtil;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;
import fi.hut.cs.drumbeat.ifc.data.schema.IfcSchema;
import fi.hut.cs.drumbeat.ifc.processing.IfcModelAnalyser;
import fi.hut.cs.drumbeat.rdf.RdfUtils;
import fi.hut.cs.drumbeat.rdf.modelfactory.JenaModelFactoryBase;
import fi.hut.cs.drumbeat.rdf.modelfactory.MemoryJenaModelFactory;

/* 
 The MIT License (MIT)

 Copyright (c) 2015 Jyrki Oraskari
 Copyright (c) 2014 Pieter Pauwels (modifications - pipauwel.pauwels@ugent.be / pipauwel@gmail.com)
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
public class DrumbeatFileHandler implements Receiver, SucceededListener,
		FailedListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3353293504542269792L;
	public File file;
	private final DrumbeatinterfaceUI parent;
	private final String uploads;

	public DrumbeatFileHandler(DrumbeatinterfaceUI parent, String uploads) {
		this.parent = parent;
		this.uploads = uploads;
		initializeIFC2RDFConverters();
	}

	public OutputStream receiveUpload(String filename, String mimeType) {
		file = null;
		if ((filename == null) || filename.length() == 0) {
			Notification n = new Notification("A file has to be selected", " ",
					Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return null;
		}
		if (!filename.toLowerCase().endsWith(".ifc")) {
			Notification n = new Notification(
					"The file extension has to be .ifc", " ",
					Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return null;

		}

		// Create upload stream
		FileOutputStream fos = null; // Stream to write to
		try {
			// Open the file for writing.
			file = new File(uploads + filename);
			fos = new FileOutputStream(file);
		} catch (final java.io.FileNotFoundException e) {
			Notification n = new Notification("Could not open file ",
					e.getMessage(), Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return null;
		}
		return fos; // Return the output stream to write to
	}

	public void uploadSucceeded(SucceededEvent event) {
		handleNewFile(file);
	}

	public void receiveFileFromURL(String url_string) {
		if (url_string == null || url_string.length() == 0) {
			return;
		}
		if (!url_string.toLowerCase().endsWith(".ifc")) {
			Notification n = new Notification(
					"The file extension has to be .ifc", " ",
					Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return;
		}
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpget = new HttpGet(url_string);
		HttpResponse response;
		String urlfile = null;
		if (url_string != null) {
			urlfile = url_string.substring(url_string.lastIndexOf('/') + 1,
					url_string.length());
		}
		if (urlfile == null) {
			return;
		}

		try {
			response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = entity.getContent();
				File targetFile = new File(uploads + urlfile);
				OutputStream outputStream = new FileOutputStream(targetFile);
				IOUtils.copy(inputStream, outputStream);
				outputStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Notification n = new Notification("Could not read the URL: ",
					e.getMessage(), Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return;
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File readyFile = new File(uploads + urlfile);
		if (readyFile.exists())
			handleNewFile(readyFile);
	}

	private void handleNewFile(File file) {
		Properties prop = new Properties();
		OutputStream output = null;
		if (file == null)
			return;

		String realEstate = parent.getSite();
		// set the properties value
		String[] fname_array = file.getName().split("\\.");

		try {

			output = new FileOutputStream(DrumbeatConstants.marmotta_import_config_file);

			prop.setProperty("context",
					DrumbeatConstants.marmotta_contexts_baseurl	+ realEstate + "." + fname_array[0]);
			prop.setProperty("label", realEstate + "." + fname_array[0]);
			parent.createDataset(fname_array[0]);
			// save properties to project root folder
			prop.store(output, null);
		} catch (Exception e) {
			e.printStackTrace();
			Notification n = new Notification("File name error", file.getName()
					+ " ; " + e.getMessage(), Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return;
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		
		if (!convertIFC2RDFDefault(file, realEstate + "." + fname_array[0]))
				return; // Stop if the conversion fails
		
		try {
			BIMFileLoader bfl = new BIMFileLoader();
			bfl.load(file);
		} catch (Exception e) {
			Notification n = new Notification("BIMFileLoader error ",
					e.getMessage(), Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(50000);
			n.show(Page.getCurrent());
			return;
		}
		Notification.show("File was uploaded");
		parent.updateData();
	}

	private void initializeIFC2RDFConverters() {
		// Default converter
		try {
			PropertyConfigurator.configure(DrumbeatConstants.ifc_conversion_log_settings);
			ConfigurationDocument.load(DrumbeatConstants.ifc_conversion_config);
			@SuppressWarnings("unused")
			List<IfcSchema> schemas = IfcParserUtil
					.parseSchemas(DrumbeatConstants.ifc_schema);
		} catch (Exception e) {
			// The notification is not possible at this phase, since the screen
			// is not ready yet
			e.printStackTrace();
		}

	}

	
	
	private boolean convertIFC2RDFDefault(File file, String name) {
		try {
			IfcModel model = IfcParserUtil.parseModel(file.getAbsolutePath());
			ComplexProcessorConfiguration groundingConfiguration = IfcModelAnalyser
					.getDefaultGroundingRuleSets();
			IfcModelAnalyser modelAnalyser = new IfcModelAnalyser(model);
			modelAnalyser.groundNodes(groundingConfiguration);

			OntModel modelExport = ModelFactory
					.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF);
			Ifc2RdfExportUtil.exportModelToJenaModel(modelExport, model);

			RdfUtils.exportJenaModelToRdfFile(modelExport,
					DrumbeatConstants.marmotta_import_directory+ file.getName(),
					RDFFormat.TURTLE, false);
			JenaModelFactoryBase jenaModelFactory = new MemoryJenaModelFactory();

			try {
				Model metaModelGraph = jenaModelFactory.createModel();

				try {
					metaModelGraph.read(DrumbeatConstants.metadata_location, "TURTLE");
				} catch (Exception e) {
					e.printStackTrace();
				}

				Ifc2RdfExportUtil.exportMetaModelToJenaModel(DrumbeatConstants.metadata_base+name,metaModelGraph,
						model); 
				FileOutputStream fout = new FileOutputStream(new File(
						DrumbeatConstants.metadata_location));
				RDFDataMgr.write(fout, metaModelGraph, RDFFormat.TURTLE_PRETTY);
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Notification n = new Notification("Default IFC2RDF export error ",
					e.getMessage(), Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return false;
		}
		return true;
	}



	@Override
	public void uploadFailed(FailedEvent event) {
		Notification n = new Notification("Uploading the file failed.",
				Notification.Type.ERROR_MESSAGE);
		n.setDelayMsec(1000);
		n.show(Page.getCurrent());
	}

	public static void main(String[] args) {

	}
};
