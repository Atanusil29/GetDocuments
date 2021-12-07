package DocumentDownload;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GetDocument {

	public static String GetOTCSTicketForDocument(String filepath, String nodeid, String ext,String CSurl, String username, String password) {
		
		String finalResult="";
		
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(CSurl+"api/v1/auth");

		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		//Execute and get the response.
		HttpResponse response;
		
		try {
			
			response = httpclient.execute(httppost);
			System.out.println(response);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				
			   InputStream instream = entity.getContent();
			   
			   // NB: does not close inputStream, you can use IOUtils.closeQuietly for that
			   String theString = IOUtils.toString(instream, "UTF-8"); 
			   System.out.println(theString);
			   
			   //Parse it to JSON for ease of reading
			   JSONParser jsonParser = new JSONParser();
			   JSONObject jsonObject = (JSONObject)jsonParser.parse(theString);
			   
			   try {
				   	
				   //Try to obtain the ticket
			        System.out.println(jsonObject.get("ticket"));
			    	String ticket=(String) jsonObject.get("ticket");
			    	
			    	//After authentication, call method to run the WR
			    	finalResult=getDocument(ticket,filepath,nodeid, ext, CSurl);
			    	
			    } finally {
			    	
			    	//close the stream
			        instream.close();
			        
			    }
		}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		return finalResult;
		
	}
	
	public static String getDocument(String ticket,String filepath, String nodeid,String ext, String CSurl) {
		
		//Result that will be passed back
		String result="";
		//Possible error that will be passed back
		String error="";
		
		//Create the downloadURL
	    String dl=CSurl+"api/v1/nodes/"+nodeid+"/content";
	    URL url;
	    
	    try {
	    	
	        url = new URL(dl);
	        
	        //Open the connection
	        HttpURLConnection connect = (HttpURLConnection) url.openConnection();
	        
	        //Set the request headers
	        connect.setRequestProperty("User-Agent","Mozilla/5.0");
	        connect.setRequestProperty("OTCSticket", ticket);
	        connect.setRequestProperty("action", "download");
	        
	        //Check whether connection was OK
	        System.out.println(connect.getResponseCode());
	        if (connect.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        	

		           try (BufferedInputStream in = new BufferedInputStream(connect.getInputStream()); 
		        		   FileOutputStream fileOutputStream = new FileOutputStream(filepath)) {

		               byte dataBuffer[] = new byte[1024];
		               int bytesRead;
		               while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
		                   fileOutputStream.write(dataBuffer, 0, bytesRead);
		               }
		           } catch (IOException e) {
		               e.printStackTrace();
		           }
		           
		           System.out.println("PDF saved");
		           result=filepath;
		           
		        } else {
		        	connect.disconnect();
		        	return "Retrieving the value did not succeed, url response code: "+connect.getResponseCode();
		        }
		        connect.disconnect();
		        System.out.println(filepath);
		        return filepath;
		      
		    } catch (Exception ex) {
		    	return "Connection did not succeed,url response code: "+error;
		    }
		}
	
	
}
