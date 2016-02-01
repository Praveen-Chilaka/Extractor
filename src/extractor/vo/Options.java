package extractor.vo;

import java.util.ArrayList;

public class Options {
	
	private OptionDate expiry;
	private ArrayList<Put> puts;
	private ArrayList<Call> calls;
	private String eodPrice;
	
	public OptionDate getExpiry() {
		return expiry;
	}
	public void setExpiry(OptionDate expiry) {
		this.expiry = expiry;
	}
	public ArrayList<Put> getPuts() {
		return puts;
	}
	public void setPuts(ArrayList<Put> puts) {
		this.puts = puts;
	}
	public ArrayList<Call> getCalls() {
		return calls;
	}
	public void setCalls(ArrayList<Call> calls) {
		this.calls = calls;
	}
	public String getEodPrice() {
		return eodPrice;
	}
	public void setEodPrice(String eodPrice) {
		this.eodPrice = eodPrice;
	}
	

}
