package fi.aalto.drumbeat.apicalls.bimserver;

import java.io.File;
import java.util.List;

import org.bimserver.LocalDevPluginLoader;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.emf.MetaDataManager;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SUser;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.shared.BimServerClientFactory;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.ServiceException;

import fi.aalto.drumbeat.drumbeatUI.DrumbeatConstants;
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
public class BIMFileLoader {

	public void load(File file) throws Exception {
			final BimServerClientInterface client = setupJson();
			if (client == null)
				System.err.println("Client is null");			
			String[] fname_array=file.getName().split("\\.");
			//String project_name = fname_array[0]+"_"+(new SimpleDateFormat("yyyyMMddHHmms").format(new Date()));
			SProject project=null;
			    try
			    {
				project = client.getBimsie1ServiceInterface().addProject(
						fname_array[0], DrumbeatConstants.bimserver_upload_schema);
			    }
			    catch(Exception e)
			    {
			    	e.printStackTrace();
			    	List<SProject> deleted=client.getBimsie1ServiceInterface().getAllProjects(true, false);
			    	for(SProject d:deleted)
			    	{
			    	 if(d.getName().equals(fname_array[0]))
			    	 {
			          //Only the first match		 
					  project=d;
					  client.getBimsie1ServiceInterface().undeleteProject(d.getOid());
					  break;
			    	 }
			    	}
			    }
			    if(project==null)
			    	throw new Exception("The project could not be created.");
				SDeserializerPluginConfiguration deserializer = client
						.getBimsie1ServiceInterface()
						.getSuggestedDeserializerForExtension("ifc",
								project.getOid());
				SUser user=client.getServiceInterface().getUserByUserName(DrumbeatConstants.bimserver_user);
				client.getServiceInterface().addUserToProject(user.getOid(), project.getOid());  // Gives "drumcsbeat@gmail.com" rights to see the new project 
				client.checkin(project.getOid(), fname_array[0],
						deserializer.getOid(), false, true, file);

		
	}
	
	private  BimServerClientInterface setupJson() {
		try {
			PluginManager pluginManager = LocalDevPluginLoader.createPluginManager(new File("home"));
			MetaDataManager metaDataManager = new MetaDataManager(pluginManager);
			pluginManager.setMetaDataManager(metaDataManager);
			BimServerClientFactory factory = new JsonBimServerClientFactory(metaDataManager, DrumbeatConstants.bimserver_api_url);
			return factory.create(new UsernamePasswordAuthenticationInfo(DrumbeatConstants.bimserver_upload_user, DrumbeatConstants.bimserver_upload_password));
		} catch (PluginException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		}
		return null;
	}

}