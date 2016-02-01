package extractor.vo;

import java.util.List;

public class AllCurrentOptions {
	
	private String symbol;
	private List<Options> options;
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public List<Options> getOptions() {
		return options;
	}
	public void setOptions(List<Options> opt) {
		this.options = opt;
	}

}
