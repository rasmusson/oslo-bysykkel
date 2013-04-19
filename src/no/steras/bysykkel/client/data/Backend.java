package no.steras.bysykkel.client.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Backend {

	private String backendEndpointURL = "http://bysykkel-qa.appspot.com/json2";
Map<Integer, JSONObject> backendData;
	
	
	public Map<Integer, JSONObject> getBackendData() {
	return backendData;
}

	public Backend () {
		
	}
	
	public void loadData() throws JSONException {
		backendData = getBackendData(backendEndpointURL);
	}
	
	 Map<Integer, JSONObject> getBackendData(String backendEndpointURL) throws JSONException {

			Map<Integer, JSONObject> backendDataMap = new HashMap<Integer, JSONObject>();
			JSONArray jsonArray = getJSONArrayFromBackend(backendEndpointURL);
           
             for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (isJsonObjectValid(jsonObject)) {
							backendDataMap.put(jsonObject.getInt("id"), jsonObject);
						}
             }
			 
			 return backendDataMap;
		}
		
		String getHTMLPage(String backendEndpointURL) throws IOException, URISyntaxException {
			HttpGet httpGet = new HttpGet();
            HttpClient httpClient = new DefaultHttpClient();
                httpClient.getParams().setIntParameter(
                                CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
                httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
                                10000);
								
								                  
                        httpGet.setURI(new URI(backendEndpointURL));

                        HttpResponse httpResponse = httpClient.execute(httpGet);
						
                        
						return streamToString(httpResponse.getEntity().getContent());

		}
		
		 JSONArray getJSONArrayFromBackend(String backendEndpointURL) {
		 


                

						
						JSONArray array;
						try {
							array = new JSONArray(getHTMLPage(backendEndpointURL));
						} catch (Exception e) {
							Log.e("Backend", "Invalid JSONObject", e);
							array = new JSONArray();
						}
						 return array;
		}
		
		 boolean isJsonObjectValid(JSONObject jsonObject) {
			try {
				jsonObject.getInt("bikesReady");
				jsonObject.getInt("emptyLocks");
				jsonObject.getBoolean("online");
				jsonObject.getInt("id");
			} catch (Exception e) {
				Log.e("Backend", "Invalid JSONObject", e);
				return false; 
			}
			return true; 
			
		}
		
		
		public void populateWithBackEndData(Station station) throws JSONException {
			 JSONObject jsonObject = backendData.get(station.getId());
			 if (jsonObject != null) {
				station.setBikesReady(jsonObject.getInt("bikesReady"));
				station.setLocksReady(jsonObject.getInt("emptyLocks"));
				station.setOnline(jsonObject.getBoolean("online"));
			 }
			 
		}
		
		private String streamToString(InputStream is) throws IOException {
                if (is != null) {
                        Writer writer = new StringWriter();

                        char[] buffer = new char[1024];
                        try {
                                Reader reader = new BufferedReader(new InputStreamReader(is,
                                                "UTF-8"));
                                int n;
                                while ((n = reader.read(buffer)) != -1) {
                                        writer.write(buffer, 0, n);
                                }
                        } finally {
                                is.close();
                        }
                        return writer.toString();
                } else {
                        return "";
                }
        }
}
