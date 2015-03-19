package fi.ni.drumbeatinterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.PropertyConfigurator;
import org.buildingsmart.IfcConvertor;
import org.buildingsmart.IfcReader;

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

import fi.hut.cs.drumbeat.common.config.ComplexProcessorConfiguration;
import fi.hut.cs.drumbeat.common.config.document.ConfigurationDocument;
import fi.hut.cs.drumbeat.ifc.convert.ifc2rdf.util.Ifc2RdfExportUtil;
import fi.hut.cs.drumbeat.ifc.convert.step2ifc.util.IfcParserUtil;
import fi.hut.cs.drumbeat.ifc.data.model.IfcModel;
import fi.hut.cs.drumbeat.ifc.data.schema.IfcSchema;
import fi.hut.cs.drumbeat.ifc.util.IfcModelAnalyser;
import fi.hut.cs.drumbeat.rdf.RdfUtils;
import fi.ni.bimserver.BIMFileLoader;
import fi.ni.ifc2rdf.lite.ExpressReader;
import fi.ni.ifc2rdf.lite.IFC_ClassModel;

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
	final DrumbeatinterfaceUI parent;
	final String uploads;

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
		if(url_string==null || url_string.length()==0)
		{			
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
		String urlfile=null;
		if(url_string!=null)
		{
		  urlfile=url_string.substring( url_string.lastIndexOf('/')+1, url_string.length() );
		}
		if(urlfile==null)
		{			
			return;
		}
		
		try {			
			response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = entity.getContent();
				File targetFile = new File(uploads+urlfile);
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
	}
	finally
	{
		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	File readyFile = new File(uploads+urlfile);
	if(readyFile.exists())
	   handleNewFile(readyFile);	
	}

	private void handleNewFile(File file) {
		Properties prop = new Properties();
		OutputStream output = null;
		if (file == null)
			return;

		try {

			output = new FileOutputStream("/var/marmotta/home/import/config");

			// set the properties value
			String[] fname_array = file.getName().split("\\.");

			prop.setProperty("context",
					"http://drumbeat.cs.hut.fi/tomcat/marmotta/context/"
							+ fname_array[0]);
			prop.setProperty("label", fname_array[0]);
			parent.createModel(fname_array[0]);
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

		if (parent.converter_selection.getValue().equals("Default")) {
			if (!convertIFC2RDFDefault(file))
				return; // Stop if the conversion fails
		} else if (parent.converter_selection.getValue().equals(
				"Ghent Multimedia Lab, buildingSMART")) {
			if (!convertIFC2RDFBuildingSmart(file))
				return; // Stop if the conversion fails
		} else {
			if (!convertIFC2RDFLite(file))
				return; // Stop if the conversion fails
		}

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

	ExpressReader lite_expressSchema = null;

	private void initializeIFC2RDFConverters() {
		// Default converter
		try {
			PropertyConfigurator.configure("/var/ifc2rdf/log4j.xml");
			ConfigurationDocument.load("/var/ifc2rdf/ifc2rdf-config.xml");
			List<IfcSchema> schemas = IfcParserUtil
					.parseSchemas("/var/ifc2rdf/IFC2X3_TC1.exp");
		} catch (Exception e) {
			// The notification is not possible at this phase, since the screen
			// is not ready yet
			e.printStackTrace();
		}
		// Lite
		try {
			lite_expressSchema = new ExpressReader(
					"/var/ifc2rdf/IFC2X3_TC1.exp");
		} catch (Exception e) {
			// The notification is not possible at this phase, since the screen
			// is not ready yet
			e.printStackTrace();
		}

	}

	private boolean convertIFC2RDFDefault(File file) {
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
					"/var/marmotta/home/import/" + file.getName(),
					RDFFormat.TURTLE, false);
		} catch (Exception e) {
			Notification n = new Notification("Default IFC2RDF export error ",
					e.getMessage(), Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return false;
		}
		return true;
	}

	public boolean convertIFC2RDFLite(File file) {

		try {
			ByteArrayOutputStream strout = new ByteArrayOutputStream();
			BufferedWriter rdfstream = new BufferedWriter(
					new OutputStreamWriter(strout));

			lite_expressSchema.outputRDFS(rdfstream);
			IFC_ClassModel m1 = new IFC_ClassModel(file.getAbsolutePath(),
					lite_expressSchema.getEntities(),
					lite_expressSchema.getTypes(), "r1");
			m1.listRDF(rdfstream); // does close stream!!

			InputStream is = new ByteArrayInputStream(strout.toString()
					.getBytes());

			OntModel model = ModelFactory
					.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF);

			model.read(is, null, "N3");
			File ttl_file = new File("/var/marmotta/home/import/"
					+ file.getName() + ".ttl");
			FileOutputStream fop = new FileOutputStream(ttl_file);
			model.write(fop, "TURTLE");
		} catch (Exception e) {
			Notification n = new Notification("IFC2RDF lite export error ",
					e.getMessage(), Notification.Type.ERROR_MESSAGE);
			n.setDelayMsec(5000);
			n.show(Page.getCurrent());
			return false;
		}
		return true;

	}

	private static String getExpressSchema(String ifc_file) {
		try {
			FileInputStream fstream = new FileInputStream(ifc_file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			try {
				String strLine;
				while ((strLine = br.readLine()) != null) {
					if (strLine.length() > 0) {
						if (strLine.startsWith("FILE_SCHEMA")) {
							if (strLine.indexOf("IFC2X3") != -1)
								return "IFC2X3_TC1";
							if (strLine.indexOf("IFC4") != -1)
								return "IFC4_ADD1";
							if (strLine.indexOf("IFC2X2") != -1)
								return "IFC2X2_ADD1";
							else
								return "";
						}
					}
				}
			} finally {
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public boolean convertIFC2RDFBuildingSmart(File file) {
		try {
			String ifc_file = file.getAbsolutePath();
			String exp = getExpressSchema(ifc_file);

			// check if we are able to convert this: only four schemas are
			// supported
			if (!exp.equalsIgnoreCase("IFC2X3_Final")
					&& !exp.equalsIgnoreCase("IFC2X3_TC1")
					&& !exp.equalsIgnoreCase("IFC4_ADD1")
					&& !exp.equalsIgnoreCase("IFC4"))
				return false;
			// CONVERSION
			OntModel om = null;
			Model model = null;
			InputStream in = null;
			InputStream expin = null;
			try {
				om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
				in = IfcReader.class
						.getResourceAsStream("/org/buildingsmart/resources/"
								+ exp + ".ttl");
				om.read(in, null, "TTL");

				expin = IfcConvertor.class
						.getResourceAsStream("/org/buildingsmart/resources/"
								+ exp + ".exp");
				org.buildingsmart.ExpressReader er = new org.buildingsmart.ExpressReader(
						expin);
				er.readAndBuild();

				IfcConvertor conv = new IfcConvertor(om, er,
						new FileInputStream(ifc_file),
						"http://drumbeat.cs.hut.fi/tomcat/marmotta/resource/");
				model = conv.parseModel();
				File ttl_file = new File("/var/marmotta/home/import/"
						+ file.getName());
				FileOutputStream fop = new FileOutputStream(ttl_file);
				model.write(fop, "TURTLE");
			} finally {
				try {
					in.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				try {
					expin.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

		} catch (Exception e) {
			Notification n = new Notification(
					"IFC2RDF BuildingSmart export error ", e.getMessage(),
					Notification.Type.ERROR_MESSAGE);
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
