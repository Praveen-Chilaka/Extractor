package extractor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class EODConnector {

	private List<String> cookies;
	private HttpURLConnection conn;

	private final String USER_AGENT = "Mozilla/5.0";
	private static final int BUFFER_SIZE = 4096;

	public String GetPageContent(String url, int step) throws Exception {

		URL obj = new URL(url);
		conn = (HttpURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		if (step == 1) {
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// Get the response cookies
			setCookies(conn.getHeaderFields().get("Set-Cookie"));

			return response.toString();

		} else if (step == 2) {

			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String fileName = "";
				String disposition = conn.getHeaderField("Content-Disposition");
				String contentType = conn.getContentType();
				int contentLength = conn.getContentLength();

				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						if(disposition.indexOf("txt", 0) > 0) {
							fileName = disposition.substring(index + 9, disposition.length());
						} else {
							fileName = disposition.substring(index + 10, disposition.length() - 1);
						}
					}
				}

				System.out.println("Content-Type = " + contentType);
				System.out.println("Content-Disposition = " + disposition);
				System.out.println("Content-Length = " + contentLength);
				System.out.println("fileName = " + fileName);

				// opens input stream from the HTTP connection
				InputStream inputStream = conn.getInputStream();
				String saveFilePath = "/Users/Praveen/Praveen/VMs/SharedFolders/EODData" + File.separator + fileName;

				// opens an output stream to save into file
				FileOutputStream outputStream = new FileOutputStream(saveFilePath);

				int bytesRead = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				outputStream.close();
				inputStream.close();

				System.out.println("File"+fileName+" downloaded....");
			} else {
				System.out.println("No file to download. Server replied HTTP code: " + responseCode);
			}

		}

		return "File Downloaded successfully";
	}

	public String getFormParams(String html, String username, String password) throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");
		System.out.println("HTML Raw... :" + html);
		Document doc = Jsoup.parse(html);

		// Google form id
		Element loginform = doc.getElementById("aspnetForm");
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");
			// System.out.println("Name:"+key+"--"+value);
			if (key.equals("ctl00$cph1$lg1$txtEmail"))
				value = username;
			else if (key.equals("ctl00$cph1$lg1$txtPassword"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		System.out.println("Result:" + result);
		return result.toString();
	}
	
	/**
	 * Method to create JSON format for Fundamentals
	 * 
	 */
	public Map<String, String> getQuoteFundamentals(String html, String symbol) {
		Document doc = Jsoup.parse(html);
		Element loginform = doc.getElementById("aspnetForm");
		Elements inputElements = loginform.getElementsByTag("tr");
		boolean fundDataPresent = false;
		Map<String, String> fundamentalData = new HashMap<String, String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.text();
			//System.out.println("Key TD:" + key);
			if(key.equalsIgnoreCase("FUNDAMENTALS")) {
				fundDataPresent = true;
				fundamentalData.put("Symbol",symbol);
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
				fundamentalData.put("Date",String.valueOf(df.format(new Date())));
			} else if (fundDataPresent) {
				if(key.indexOf(':') > -1) {
				fundamentalData.put(key.substring(0, key.indexOf(':')).trim(),key.substring(key.indexOf(':')+1,key.length()).trim());
				//System.out.println(key.substring(0, key.indexOf(':')).trim());
				//System.out.println(key.substring(key.indexOf(':')+1,key.length()).trim());
				} else {
					break;
				}
			}
		}
		return fundamentalData;
	}

	public String extractDownloadURL(String html) throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");
		System.out.println("HTML Raw... :" + html);
		Document doc = Jsoup.parse(html);
		String downloadURL = "";
		// Google form id
		Element loginform = doc.getElementById("aspnetForm");
		Elements inputElements = loginform.getElementsByTag("a");
		// List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("href");
			// String value = inputElement.attr("value");
			// String dUrl = inputElement.attr("onclick");
			// System.out.println("Name:"+key+"--"+value);
			System.out.println("Key:" + key);
			if (key.indexOf("/data/filedownload.aspx?e=NASDAQ") > -1) {
				downloadURL = String.valueOf("http://www.eoddata.com/" + key);
				break;
			}

		}

		System.out.println("downloadURL:" + downloadURL);
		return downloadURL.toString();
	}

	public void sendPost(String url, String postParams) throws Exception {

		URL obj = new URL(url);
		conn = (HttpURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", "www.eoddata.com");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", "http://www.eoddata.com");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// System.out.println(response.toString());

	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

}
