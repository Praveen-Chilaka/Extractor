package extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import extractor.vo.GoogleStockData;
import extractor.vo.StockData;

public class ExtractorMain {
	String url = "http://www.eoddata.com/";

	public static void main(String[] args) {

		try {

			// Load the configurations
			Map<String, String> argMap = new HashMap<String, String>();
			for (String val : args) {
				System.out.println("Argument given ::" + val);
				argMap.put(val.split("=")[0], val.split("=")[1]);
			}
			Configuration c = new Configuration();
			Map<String, String> configMap = c.loadProperties(argMap.get("config"));
			/*
			 * for (Map.Entry<String, String> entry : configMap.entrySet()) {
			 * System.out.println("Key : " + entry.getKey() + " Value : " +
			 * entry.getValue()); }
			 */
			// Call the function based on the input
			if (argMap.get(ConfigConstants.FUNC_OPTION) != null) {
				int function = Integer.valueOf(argMap.get(ConfigConstants.FUNC_OPTION));
				ExtractorMain em = new ExtractorMain();
				em.eodFunctions(function, configMap);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param type
	 */
	public void eodFunctions(int type, Map<String, String> configMap) {

		EODConnector eod = new EODConnector();
		MongoDBConnector mDb = new MongoDBConnector();
		// make sure cookies is turn on
		CookieHandler.setDefault(new CookieManager());
		try {

			switch (type) {
			// Load Fundamentals from EOD Website/Google
			case 1: {
				this.loadSymbolList(eod, "NASDAQ", configMap.get(ConfigConstants.PATH)+"NASDAQ.txt",
						configMap.get(ConfigConstants.TICK_COLL_N_NASDAQ));
				System.out.println("NASDAQ Symbol loaded successfully");
				this.loadSymbolList(eod, "NYSE", configMap.get(ConfigConstants.PATH)+"NYSE.txt",
						configMap.get(ConfigConstants.TICK_COLL_N_NYSE));
				System.out.println("NYSE Symbol loaded successfully");
				this.loadSymbolList(eod, "AMEX", configMap.get(ConfigConstants.PATH)+"AMEX.txt",
						configMap.get(ConfigConstants.TICK_COLL_N_AMEX));
				System.out.println("AMEX Symbol loaded successfully");
				this.loadSymbolList(eod, "INDEX", configMap.get(ConfigConstants.PATH)+"INDEX.txt",
						configMap.get(ConfigConstants.TICK_COLL_N_INDEX));
				System.out.println("INDEX Symbol loaded successfully");
			}
			case 2: // Download SymbolList from EOD WebSite
			{
				// 1. Send a "GET" request, so that you can extract the form's
				// data.
				String page = eod.GetPageContent(url, 1, null);
				String postParams = eod.getFormParams(page, "praveen.chilakalapudi@gmail.com", "Aeiou@123");
				// 2. Construct above post's content and then send a POST
				// request
				// for
				// authentication
				eod.sendPost(url, postParams);
				this.downloadSymbolList(eod, configMap);
			}
			case 3: // Download EOD Data from EOD WebSite
			{
				List<String> l = this.extractSym("/Users/Praveen/Praveen/VMs/SharedFolders/EODData/NASDAQ.txt");
				for (String sym : l) {
					GoogleFinanceDataDownloader gfdd = new GoogleFinanceDataDownloader();
					// String fileName = gfdd.downloadHistoricalEODData(sym);
					// System.out.println("Historic EOD for "+sym+" downloaded
					// successfully & Path is ==>"+fileName);
					// String command = ""+fileName;
					// this.executeCommand("");
				}
			}
			case 4: // Download PreMarket data from Google
			{
				System.out.println("Downloading PreMarket Data...@ " + System.currentTimeMillis());
				GoogleFinanceDataDownloader gfdd = new GoogleFinanceDataDownloader();
				List<String> l = this.extractSymbols("/Users/Praveen/Praveen/VMs/SharedFolders/EODData/NASDAQ.txt");
				for (String sym : l) {
					gfdd.downloadPreMarketData(
							"http://www.google.com/finance/info?infotype=infoquoteall&q=" + sym + "&format=json");
					System.out.println("Downloaded preMarket data for Symbols :" + sym);
				}
			}
			case 5: // Download PostMarket data from Google
			{
			}
			case 6: // Download Intra-day prices
			{
				String dbName = "test_s";
				List<String> l = this.extractSym("/Users/Praveen/Praveen/VMs/SharedFolders/EODData/NASDAQ.txt");
				for (String sym : l) {
					GoogleFinanceDataDownloader gfdd = new GoogleFinanceDataDownloader();
					List<StockData> sd = gfdd.downloadIntraDayPrices(sym);
					for (StockData stock : sd) {
						Gson gson = new Gson();
						String intrDay = gson.toJson(stock);
						// System.out.println("Created jsonFundamentals for :" +
						// sym +" are "+ intrDay);
						mDb.postAndCreate(dbName, sym, intrDay);
					}
				}
			}
			case 7: // Download Options
			{
				String dbName = "test_o";
				this.downloadEODOptionsData("/Users/Praveen/Praveen/VMs/SharedFolders/EODData/NASDAQ.txt", dbName,
						"nasdaq_options");
				System.out.println("NASDAQ Symbols Options loaded successfully");
				this.downloadEODOptionsData("/Users/Praveen/Praveen/VMs/SharedFolders/EODData/NYSE.txt", dbName,
						"nyse_options");
				System.out.println("NYSE Symbols Options loaded successfully");
				this.downloadEODOptionsData("/Users/Praveen/Praveen/VMs/SharedFolders/EODData/AMEX.txt", dbName,
						"amex_options");
				System.out.println("AMEX Symbols Options loaded successfully");
				// this.downloadEODOptionsData("/Users/Praveen/Praveen/VMs/SharedFolders/EODData/INDEX.txt",
				// dbName,"index_fundamentals");
				// System.out.println("INDEX Symbol loaded successfully");
			}
			case 8: // Download and import Historical EOD data
			{

				List<String> l = this.extractSym("/Users/Praveen/Praveen/VMs/SharedFolders/EODData/NASDAQ.txt");
				for (String sym : l) {
					GoogleFinanceDataDownloader gfdd = new GoogleFinanceDataDownloader();
					// String fileName = gfdd.downloadHistoricalEODData(sym);
					// System.out.println("Historic EOD for "+sym+" downloaded
					// successfully & Path is ==>"+fileName);
					// String command = ""+fileName;
					// this.executeCommand("");
				}
			}

			}
		} catch (Exception e) {

		}

	}

	/**
	 * This method downloads the symbol list in txt file.
	 * 
	 * @param eod
	 * @throws Exception
	 */
	public void downloadSymbolList(EODConnector eod, Map<String, String> configMap) throws Exception {

		String symbolListURL = configMap.get(ConfigConstants.EOD_S_LIST);
		eod.GetPageContent(symbolListURL, 2, configMap.get(ConfigConstants.PATH));
		String nyseURL = symbolListURL.replaceFirst("NASDAQ", "NYSE");
		eod.GetPageContent(nyseURL, 2, configMap.get(ConfigConstants.PATH));
		String amexURL = symbolListURL.replaceFirst("NASDAQ", "AMEX");
		eod.GetPageContent(amexURL, 2, configMap.get(ConfigConstants.PATH));
		String indexURL = symbolListURL.replaceFirst("NASDAQ", "INDEX");
		eod.GetPageContent(indexURL, 2, configMap.get(ConfigConstants.PATH));

	}

	/**
	 * This method loads symbol list to MongoDB collection with names
	 * stocks_NYSE,stocks_NASDAQ & stocks_AMEX
	 * 
	 * @throws Exception
	 */
	public void loadSymbolList(EODConnector eod, String exchange, String fileName, String collName) throws Exception {

		// Read the txt file
		BufferedReader br = null;
		String strLine = "";
		MongoDBConnector mDb = new MongoDBConnector();
		GoogleFinanceDataDownloader gfdd = new GoogleFinanceDataDownloader();
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((strLine = br.readLine()) != null) {
				System.out.println(strLine);

				BreakIterator boundary = BreakIterator.getWordInstance();
				boundary.setText(strLine);
				int start = boundary.first();
				int end = boundary.next();
				String symbol = strLine.substring(start, end);
				int isIndex = symbol.indexOf(".IDX");
				symbol = symbol.replace(".IDX", "");
				if (!"Symbol".equalsIgnoreCase(symbol)) {
					System.out.println("Fetching datas for :: " + symbol);
					String result = eod.GetPageContent(
							"http://www.eoddata.com/stockquote/" + exchange + "/" + symbol + ".htm", 1, null);
					Map<String, String> jsonMap = eod.getQuoteFundamentals(result, symbol);
					if (isIndex < 0) {
						// Call google to some additional data
						GoogleStockData gsd = gfdd.getStockFundamentals(symbol);
						if (gsd != null) {
							jsonMap.put("Name", gsd.getName());
							jsonMap.put("Inst Own", gsd.getInst_own());
							jsonMap.put("Exchange", exchange);
						}
					}
					Gson gson = new Gson();
					String jsonFundamentals = gson.toJson(jsonMap);
					System.out.println("Created jsonFundamentals for :" + symbol + " are " + jsonFundamentals);
					mDb.postAndCreate(symbol, collName, jsonFundamentals);
					// break; // Remove after testing
				}

			}
		} catch (

		FileNotFoundException e)

		{
			System.err.println("Unable to find the file: fileName");
		} catch (

		IOException e)

		{
			System.err.println("Unable to read the file: fileName");
		}

	}

	/**
	 * Returns list of all symbols.
	 * 
	 * @param fileName
	 * @return
	 */
	private List<String> extractSymbols(String fileName) {

		BufferedReader br = null;
		String strLine = "";
		List<String> symbolList = null;
		List<String> symbolList100s = new ArrayList<String>();
		;
		int sCount = 0;

		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((strLine = br.readLine()) != null) {
				// System.out.println(strLine);

				BreakIterator boundary = BreakIterator.getWordInstance();
				boundary.setText(strLine);
				int start = boundary.first();
				int end = boundary.next();
				String symbol = strLine.substring(start, end);
				if (!"Symbol".equalsIgnoreCase(symbol)) {
					// System.out.println("Fetching datas for :: " + symbol);
					if (symbolList == null || sCount > 99) {
						// System.out.println("symbolList :"+symbolList+" sCount
						// :"+sCount);
						symbolList = new ArrayList<String>();
						sCount = 0;
					}
					symbolList.add(symbol);
					sCount = sCount + 1;
					if (sCount > 99) {
						symbolList100s.add(StringUtils.join(symbolList, ','));
					}
				}

			}
		} catch (

		FileNotFoundException e)

		{
			System.err.println("Unable to find the file: fileName");
		} catch (

		IOException e)

		{
			System.err.println("Unable to read the file: fileName");
		}
		return symbolList100s;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	private List<String> extractSym(String fileName) {

		BufferedReader br = null;
		String strLine = "";
		List<String> symbolList = new ArrayList<String>();

		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((strLine = br.readLine()) != null) {
				// System.out.println(strLine);

				BreakIterator boundary = BreakIterator.getWordInstance();
				boundary.setText(strLine);
				int start = boundary.first();
				int end = boundary.next();
				String symbol = strLine.substring(start, end);
				if (!"Symbol".equalsIgnoreCase(symbol)) {
					// System.out.println("Fetching datas for :: " + symbol);
					symbolList.add(symbol);
				}

			}
		} catch (

		FileNotFoundException e)

		{
			System.err.println("Unable to find the file: fileName");
		} catch (

		IOException e)

		{
			System.err.println("Unable to read the file: fileName");
		}
		return symbolList;
	}

	/*
	 * 
	 * 
	 */
	public void downloadEODOptionsData(String fileName, String dbName, String collName) {
		// Read the txt file
		BufferedReader br = null;
		String strLine = "";
		MongoDBConnector mDb = new MongoDBConnector();
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((strLine = br.readLine()) != null) {
				System.out.println(strLine);

				BreakIterator boundary = BreakIterator.getWordInstance();
				boundary.setText(strLine);
				int start = boundary.first();
				int end = boundary.next();
				String symbol = strLine.substring(start, end);
				if (!"Symbol".equalsIgnoreCase(symbol)) {
					System.out.println("Fetching datas for :: " + symbol);
					GoogleFinanceDataDownloader gfdd = new GoogleFinanceDataDownloader();
					String optionsString = gfdd.getAllCurrentOptions(symbol);
					if (null != optionsString) {
						mDb.postAndCreate(dbName, symbol, optionsString);
						System.out.println("Posted options data");
					}
					// break; // Remove after testing
				}

			}
		} catch (

		FileNotFoundException e)

		{
			System.err.println("Unable to find the file: fileName");
		} catch (

		IOException e)

		{
			System.err.println("Unable to read the file: fileName");
		}

	}

	/**
	 * 
	 * @param command
	 * @return
	 */
	private String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

}
