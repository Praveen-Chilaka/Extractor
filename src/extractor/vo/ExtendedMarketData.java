package extractor.vo;

public class ExtendedMarketData {
	//Symbol
	private String t;
	// Exchange
	private String e;
	// Last Market price
	private String l;
	// extended hour last price
	private String el;
	// extended hour time
	private String elt;
	// extended hour change in price
	private String ec;
	//extended hour change in %
	private String ecp;
	
	public String getT() {
		return t;
	}
	public void setT(String t) {
		this.t = t;
	}
	public String getE() {
		return e;
	}
	public void setE(String e) {
		this.e = e;
	}
	public String getL() {
		return l;
	}
	public void setL(String l) {
		this.l = l;
	}
	public String getEl() {
		return el;
	}
	public void setEl(String el) {
		this.el = el;
	}
	public String getElt() {
		return elt;
	}
	public void setElt(String elt) {
		this.elt = elt;
	}
	public String getEc() {
		return ec;
	}
	public void setEc(String ec) {
		this.ec = ec;
	}
	public String getEcp() {
		return ecp;
	}
	public void setEcp(String ecp) {
		this.ecp = ecp;
	}
	
	
}
