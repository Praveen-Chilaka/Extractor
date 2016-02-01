package extractor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MongoDBConnector {

	public void postAndCreate(String dbName, String col, String input) {
		int responseCode = this.post(dbName, col, input);
		//System.out.println("dbName "+dbName+" col "+ col);
		//System.out.println("Intial Response "+responseCode);
		if(responseCode == 404 || responseCode == 500) {
			responseCode = this.createCollection(dbName, col);
			if(201 != responseCode) {
				responseCode = this.createDB(dbName);
				if(201 != responseCode) {
					System.out.println("Could not create db -->"+dbName);
				}
				responseCode = this.createCollection(dbName, col);
			}
			if(responseCode == 406 || responseCode == 409) {
				System.out.println("Conflict for symbol"+col);
			} else if (responseCode == 201) {
				responseCode = this.post(dbName, col, input);
			} else {
				System.out.println("Response "+responseCode+" unknow for "+col);
			}
		} else if (responseCode == 201) {
			System.out.println("Response "+responseCode+" success for "+col);
		}
	}
	
	/**
	 * Method to post data to MonogDB server
	 * 
	 * @param db
	 * @param col
	 * @param input
	 */
	public int post(String db, String col, String input) {
		int responseCode = 0;
		try {

			URL url = new URL("http://localhost:8080/" + db + "/" + col);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();
			responseCode = conn.getResponseCode();
			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return responseCode;
	}
	
	/**
	 * 
	 * @param db
	 * @param col
	 * @return
	 */
	public int createCollection(String db, String col) {
		int returnCode = 0;
		try {

			URL url = new URL("http://localhost:8080/"+db+"/"+col);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json");
			
			returnCode = conn.getResponseCode();
			conn.disconnect();
		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return returnCode;
	}

	/*
	 * 
	 */
	public int createDB(String db) {
		int returnCode = 0;
		try {

			URL url = new URL("http://localhost:8080/"+db);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json");
			
			returnCode = conn.getResponseCode();
			conn.disconnect();
		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		System.out.println("returnCode ::"+returnCode);
		return returnCode;
	}	
	
}
