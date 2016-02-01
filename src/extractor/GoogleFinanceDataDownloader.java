package extractor;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import extractor.vo.AllCurrentOptions;
import extractor.vo.ExtendedMarketData;
import extractor.vo.GoogleStockData;
import extractor.vo.OptionDate;
import extractor.vo.OptionExpirations;
import extractor.vo.Options;
import extractor.vo.StockData;

public class GoogleFinanceDataDownloader {

	public static String FIN_URL = "https://www.google.com/finance?q=";
	private static String OPTIONS_URL = "https://www.google.com/finance/option_chain";
	private static String FUNDAMENTALS_URL = "";
	private List<String> cookies;

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

	private HttpsURLConnection conn;
	private final String USER_AGENT = "Mozilla/5.0";
	private static final int BUFFER_SIZE = 4096;
	private static final String FILE_LOC = "/Users/Praveen/Praveen/VMs/SharedFolders/EODData";

	public Stock urlGet(String url) {
		ObjectMapper mapper = new ObjectMapper();

		Stock obj = null;
		try {
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			// mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
			// false);
			JsonFactory factory = mapper.getFactory();
			factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
			factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
			factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
			factory.enable(JsonParser.Feature.IGNORE_UNDEFINED);
			JsonParser jp = factory.createParser(new URL(url));
			obj = mapper.readValue(jp, Stock.class);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	public String downloadEODData(String symbol,int type) {
		// http://www.google.com/finance/historical?q=AAPL&startdate=Jan+25+2010&enddate=Jan+24+2016&output=csv
		String url = null;
		if(type == 1) {
			url = "http://www.google.com/finance/historical?q=" + symbol
					+ "&startdate=Jan+1+2010&enddate=Jan+24+2016&output=csv";			
		}
		// Historical Data download
		else if (type == 2) {
			url = "http://www.google.com/finance/historical?q=" + symbol
					+ "&startdate=Jan+1+2010&enddate=Jan+24+2016&output=csv";
		}
		String saveFilePath = null;
		try {
			URL obj = new URL(url);
			HttpURLConnection httpConn = (HttpURLConnection) obj.openConnection();

			// default is GET
			httpConn.setRequestMethod("GET");

			httpConn.setUseCaches(false);

			// act like a browser
			httpConn.setRequestProperty("User-Agent", USER_AGENT);
			httpConn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			httpConn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			if (cookies != null) {
				for (String cookie : this.cookies) {
					httpConn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
				}
			}
			int responseCode = httpConn.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
			// opens input stream from the HTTP connection
			InputStream inputStream;

			inputStream = httpConn.getInputStream();

			saveFilePath = "/Users/Praveen/Praveen/VMs/SharedFolders/EODData/" + symbol + ".csv";

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return saveFilePath;
	}

	/**
	 * Check every 5 mins starting 6 AM CST till 8:30 AM CST Record only when
	 * the data changes but ping every 5 mins
	 * 
	 * @param symbol
	 */
	public void downloadPreMarketData(String hundred_symbols_wurl) {
		// http://www.google.com/finance/info?infotype=infoquoteall&q=AAPL,VIPS,FB&format=json
		ObjectMapper mapper = new ObjectMapper();
		MappingIterator<ExtendedMarketData> obj = null;
		try {
			System.out.println("Objs ::"+hundred_symbols_wurl);
			// mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
			// true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonFactory factory = mapper.getFactory();
			factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
			factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
			factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
			factory.enable(JsonParser.Feature.IGNORE_UNDEFINED);
			JsonParser jp = factory.createParser(new URL(hundred_symbols_wurl));
			obj = mapper.readValues(jp, ExtendedMarketData.class);
			//MappingIterator<ExtendedMarketData> mpit = (MappingIterator<ExtendedMarketData>)obj;
			Gson gson = new Gson();
			System.out.println("Objs ::"+obj.readAll());
			while(obj.hasNext()) {
				//ExtendedMarketData emd = (ExtendedMarketData)obj.next();
				//String jsonF = gson.toJson((ExtendedMarketData)obj.next());
				System.out.println("Created jsonFundamentals ::"+ obj.next());			
			}
			// obj = mapper.readValue(new URL(url), );
			System.out.println("Objs ::"+obj);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Check every 5 mins starting 6 AM CST till 8:30 AM CST Record only when
	 * the data changes but ping every 5 mins
	 * 
	 * @param symbol
	 */
	public void downloadAfterMarketData(String symbol) {
		// http://www.google.com/finance/info?infotype=infoquoteall&q=AAPL,VIPS,FB&format=json
	}

	public void downloadEODData(String symbol) {

	}

	public GoogleStockData getStockFundamentals(String symbol) {
		// http://www.google.com/finance/info?infotype=infoquoteall&q=AAPL,VIPS,FB&format=json
		ObjectMapper mapper = new ObjectMapper();
		GoogleStockData obj = null;
		String url = "http://www.google.com/finance/info?infotype=infoquoteall&q="+symbol+"&format=json";
		try {
			System.out.println("Getting google fundamentals for symbol ::"+symbol);
			// mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
			// true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonFactory factory = mapper.getFactory();
			factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
			factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
			factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
			factory.enable(JsonParser.Feature.IGNORE_UNDEFINED);
			JsonParser jp = factory.createParser(new URL(url));
			obj = mapper.readValue(jp, GoogleStockData.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * downloads intraday data
	 * 
	 * @param symbol
	 * @return
	 */
	public List<StockData> downloadIntraDayPrices(String symbol) {
		// http://www.google.com/finance/getprices?i=300&p=1d&f=d,o,h,l,c,v&df=cpct&q=AAPL

		String url = "http://www.google.com/finance/getprices?i=300&p=1d&f=d,o,h,l,c,v&df=cpct&q=" + symbol;
		List<StockData> intraDayData = new ArrayList<StockData>();
		try {
			URL oracle = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

			String inputLine;
			int timeZoneOffset = 0;
			Long epoch = null;
			int interval = 300;

			while ((inputLine = in.readLine()) != null) {
				// System.out.println(inputLine);
				if (!inputLine.contains("=") && inputLine.contains(",")) {
					if (inputLine.contains("INTERVAL")) {
						interval = Integer.valueOf(inputLine.split("=")[1]);
						System.out.println("Interval --> " + interval);
					}
					if (inputLine.contains("TIMEZONE_OFFSET")) {
						timeZoneOffset = Integer.valueOf(inputLine.split("=")[1]);
						System.out.println("timeZoneOffset --> " + timeZoneOffset);
					}
					StockData sd = new StockData();
					String[] values = inputLine.split(",");

					if (values[0].length() > 2) {
						epoch = Long.valueOf(values[0].replace('a', ' ').trim());
						Instant epochInstant = Instant.ofEpochSecond(epoch);
						Date date = Date.from(epochInstant);
						SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
						sd.setDate(formatter.format(date));
						formatter = new SimpleDateFormat("hh:mm a");
						sd.setTime(formatter.format(date));
						sd.setEpoch(String.valueOf(epoch));
					} else {
						Instant epochInstant = Instant.ofEpochSecond(epoch + interval);
						Date date = Date.from(epochInstant);
						SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
						sd.setDate(formatter.format(date));
						formatter = new SimpleDateFormat("hh:mm a");
						sd.setTime(formatter.format(date));
						sd.setEpoch(String.valueOf(epoch + interval));
						interval = interval + 300;
					}

					sd.setClose(values[1]);
					sd.setHigh(values[2]);
					sd.setLow(values[3]);
					sd.setOpen(values[4]);
					sd.setVolume(values[5]);
					intraDayData.add(sd);
				}
			}
			in.close();
		} catch (IOException io) {
			io.printStackTrace();
		}
		return intraDayData;
	}

	/**
	 * Downloads data from Google option chain
	 * 
	 * @param url
	 * @param step
	 * @return
	 */
	public Object getEODOptionsData(String url, int step) {

		// URL Sample -->
		// https://www.google.com/finance/option_chain?q=VIPS&expd=22&expm=1&expy=2016

		ObjectMapper mapper = new ObjectMapper();
		Object obj = null;
		try {
			// mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,
			// true);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonFactory factory = mapper.getFactory();
			// factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
			factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
			factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
			factory.enable(JsonParser.Feature.IGNORE_UNDEFINED);
			JsonParser jp = factory.createParser(new URL(url));
			if (step == 1) {
				obj = mapper.readValue(jp, OptionExpirations.class);
			} else {
				obj = mapper.readValue(jp, Options.class);
			}
			//System.out.println("obj ::"+obj);
			// obj = mapper.readValue(new URL(url), );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * Formats URL and call download method by date
	 * 
	 * @param symbol
	 * @param date
	 * @return
	 */
	public Options getOptionsByDate(String symbol, OptionDate date) {

		Options options = null;
		String url = "https://www.google.com/finance/option_chain?q=" + symbol + "&expd=" + date.getD() + "&expm="
				+ date.getM() + "&expy=" + date.getY() + "&output=json";
		options = (Options) getEODOptionsData(url, 0);
		return options;

	}

	/**
	 * Iterates and call google to get all options data.
	 * 
	 * @param symbol
	 * @return
	 */
	public String getAllCurrentOptions(String symbol) {

		String initialUrl = "https://www.google.com/finance/option_chain?q=" + symbol + "&output=json";
		OptionExpirations o = (OptionExpirations) getEODOptionsData(initialUrl, 1);
		List<OptionDate> expirations = o.getExpirations();
		if (null != expirations) {
			Iterator<OptionDate> it = expirations.iterator();
	
			AllCurrentOptions curOption = new AllCurrentOptions();
			curOption.setSymbol(symbol);
			List<Options> opt = new ArrayList<Options>();
			while (it.hasNext()) {
				OptionDate od = (OptionDate) it.next();
				Options obd = getOptionsByDate(symbol, od);
				opt.add(obd);
			}
			curOption.setOptions(opt);
			Gson gson = new Gson();
			String jsonOptions = gson.toJson(curOption);
			//System.out.println("jsonOptions ::"+jsonOptions);
			return jsonOptions;
		} else {
			return null;
		}

	}

	public void getHistoricalEODOptionsData(String symbol, Date startDate, Date endDate) {
		// TODO need to figure out where to get this data
	}
}
