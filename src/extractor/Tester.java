package extractor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import extractor.vo.AllCurrentOptions;

public class Tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ObjectMapper mapper = new ObjectMapper();
		GoogleFinanceDataDownloader test = new GoogleFinanceDataDownloader();
		MongoDBConnector mdc = new MongoDBConnector();
		//String url = GoogleFinanceDataDownloader.FIN_URL+"VIPS"+"&output=json";
		//Stock s = test.urlGet(url);
		//try {
			//AllCurrentOptions o = test.getAllCurrentOptions("VIPS");
			//String jsonInString = mapper.writeValueAsString(test.downloadIntraDayPrices("AAPL"));
			//System.out.println(jsonInString);
			mdc.createDB("test");
		//} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}

		/*EODConnector http = new EODConnector();
		
		String result;
		try {
			result = http.GetPageContent("http://www.eoddata.com/stockquote/NASDAQ/AAPL.htm", 1);
			System.out.println(result);

			http.getQuoteFundamentals(result,"AAPL");
			//http.GetPageContent(nasdaqURL, 2);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

}
