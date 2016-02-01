package extractor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Stock {
	private String symbol;
	private String exchange;
	private String name;
	// Average volumne
	private String avvo;
	private String hi52;
	private String lo52;
	// Market Cap
	private String mc;
	private String pe;
	private String fwpe;
	private String beta;
	private String eps;
	private String dy;
	private String ldiv;
	// Available share
	private String shares;
	// Institutional ownership
	private String instown;
	// Sector name
	private String sname;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvvo() {
		return avvo;
	}

	public void setAvvo(String avvo) {
		this.avvo = avvo;
	}

	public String getHi52() {
		return hi52;
	}

	public void setHi52(String hi52) {
		this.hi52 = hi52;
	}

	public String getLo52() {
		return lo52;
	}

	public void setLo52(String lo52) {
		this.lo52 = lo52;
	}

	public String getMc() {
		return mc;
	}

	public void setMc(String mc) {
		this.mc = mc;
	}

	public String getPe() {
		return pe;
	}

	public void setPe(String pe) {
		this.pe = pe;
	}

	public String getFwpe() {
		return fwpe;
	}

	public void setFwpe(String fwpe) {
		this.fwpe = fwpe;
	}

	public String getBeta() {
		return beta;
	}

	public void setBeta(String beta) {
		this.beta = beta;
	}

	public String getEps() {
		return eps;
	}

	public void setEps(String eps) {
		this.eps = eps;
	}

	public String getDy() {
		return dy;
	}

	public void setDy(String dy) {
		this.dy = dy;
	}

	public String getLdiv() {
		return ldiv;
	}

	public void setLdiv(String ldiv) {
		this.ldiv = ldiv;
	}

	public String getShares() {
		return shares;
	}

	public void setShares(String shares) {
		this.shares = shares;
	}

	public String getInstown() {
		return instown;
	}

	public void setInstown(String instown) {
		this.instown = instown;
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

}
