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

	private String BACKEND_ENDPOINT_URL = "http://bysykkel-prod.appspot.com/json";
	private String STATIONS_ARRAY_NAME = "stationsData";
	
	Map<Integer, JSONObject> backendData;
	
	
	public Map<Integer, JSONObject> getBackendData() {
	return backendData;
}
	
	public void loadData() throws JSONException {
		backendData = getBackendData(BACKEND_ENDPOINT_URL);
	}
	
	 Map<Integer, JSONObject> getBackendData(String backendEndpointURL) throws JSONException {

			Map<Integer, JSONObject> backendDataMap = new HashMap<Integer, JSONObject>();
			JSONArray jsonArray = getStationsJSONArrayFromBackend(backendEndpointURL);
           
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
		
		 JSONArray getStationsJSONArrayFromBackend(String backendEndpointURL) {

						JSONArray stationsArray;
						try {
							JSONObject dataObject;
							dataObject = new JSONObject(getHTMLPage(backendEndpointURL));
							stationsArray = dataObject.getJSONArray(STATIONS_ARRAY_NAME);
						} catch (Exception e) {
							Log.e("Backend", "Invalid JSONObject", e);
							stationsArray = new JSONArray();
						}
						 return stationsArray;
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
