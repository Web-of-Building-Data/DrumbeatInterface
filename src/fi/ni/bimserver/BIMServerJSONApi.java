package fi.ni.bimserver;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.vaadin.data.Item;
import com.vaadin.ui.ComboBox;
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

public class BIMServerJSONApi {
	final String drumbeatAPIurl = "http://drumbeat.cs.hut.fi/tomcat/json";
	private String token = null;
    public BIMServerJSONApi()
    {
    	login("drumcsbeat@gmail.com", "ajakkjajj1k1u");
    }


	private void login(String username, String password) {

		JSONObject login = new JSONObject();

		JSONObject parameters = new JSONObject();
		parameters.put("username", username);
		parameters.put("password", password);

		JSONObject request = new JSONObject();
		request.put("interface", "Bimsie1AuthInterface");
		request.put("method", "login");
		request.put("parameters", parameters);

		login.put("request", request);

		System.out.println(login);
		System.out.println("Result:");
		JSONObject result = http(drumbeatAPIurl, login.toJSONString());
		JSONObject response = (JSONObject) result.get("response");
		if (response != null) {
			String login_result = (String) response.get("result");
			if (login_result != null) {
				token = login_result;
			} else {
				JSONObject bs_exception = (JSONObject) response
						.get("exception");
				if (bs_exception != null) {
					String bs_message = (String) bs_exception.get("message");
					System.err.println("ERROR " + bs_message);
				}
			}
		}

	}

	

	public void getProjects(ComboBox selection,Map<String, Long> projects) {
		
		if (token == null) {
			System.err.println("No login");
			return;
		}
		try
		{
		  if(selection.isVisible())
		    selection.removeAllItems();
		}
		catch(Exception e)
		{
			//update before table initalization?
			return;
		}
		projects.clear();
		int index = 1;
		JSONObject getAllProjects = new JSONObject();

		JSONObject parameters = new JSONObject();
		// parameters.put( "onlyTopLevel","true");

		JSONObject request = new JSONObject();
		request.put("interface", "ServiceInterface");
		request.put("method", "getAllReadableProjects");
		request.put("parameters", parameters);

		getAllProjects.put("token", token);
		getAllProjects.put("request", request);

		System.out.println(getAllProjects);
		System.out.println("Result:");
		JSONObject result = http(drumbeatAPIurl, getAllProjects.toJSONString());
		JSONObject response = (JSONObject) result.get("response");
		if (response != null) {
			JSONArray get_result = (JSONArray) response.get("result");
			if (get_result != null) {
				for (Object obj : get_result) {

					// Add a row the hard way
		
					JSONObject project = (JSONObject) obj;
					String name = (String) project.get("name");
					if (name != null)
						selection.addItem(name);

					Long project_id = (Long) project.get("oid");
					if (project_id != null) {
						projects.put(name, project_id);
					} else
						System.err.println("ERROR no project ID!");

				}
			} else {
				JSONObject bs_exception = (JSONObject) response
						.get("exception");
				if (bs_exception != null) {
					String bs_message = (String) bs_exception.get("message");
					System.err.println("ERROR " + bs_message);
				}
			}
		}

	}

	private JSONObject http(String url, String body) {

		try (CloseableHttpClient httpClient = HttpClientBuilder.create()
				.build()) {
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(body);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);

			HttpResponse http_result = httpClient.execute(request);

			String json = EntityUtils
					.toString(http_result.getEntity(), "UTF-8");
			System.out.println("result: " + json);
			try {
				JSONParser parser = new JSONParser();
				Object resultObject = parser.parse(json);

				if (resultObject instanceof JSONObject) {
					JSONObject result = (JSONObject) resultObject;
					return result;

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	

}
