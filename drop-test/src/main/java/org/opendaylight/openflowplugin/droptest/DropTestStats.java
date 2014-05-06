package org.opendaylight.openflowplugin.droptest;

public class DropTestStats {
	private final int _rcvd;
	private final int _sent;
	private final int _excs;
	private final String _message;

	public DropTestStats(int sent, int rcvd) {
		this._sent = sent;
		this._rcvd = rcvd;
		this._excs = 0;
		this._message = null;
	}
	
	public DropTestStats(int sent, int rcvd, int excs) {
		this._sent = sent;
		this._rcvd = rcvd;
		this._excs = excs;
		this._message = null;
	}

		public DropTestStats(String message) {
		this._sent = -1;
		this._rcvd = -1;
		this._excs = -1;
		this._message = message;
	}
	
	public int getSent() { return this._sent; }
	public int getRcvd() { return this._rcvd; }
	public String getMessage() { return this._message; };

	@Override 
	public String toString() {
	    StringBuilder result = new StringBuilder();
	    if (this._message == null) {
	    	result.append("Rcvd: " + this._rcvd);
	    	result.append(", Sent: " + this._sent);
	    	result.append("; Exceptions: " + this._excs);
	    } else {
	    	result.append(this._message);
	    }
	    	
		return result.toString();
	}
	
}
