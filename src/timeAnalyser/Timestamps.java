/**
 * 
 */
package timeAnalyser;

/**
 * @author Jelle Bouma
 *
 */
public class Timestamps {
	Timestamp[] si;
	Timestamp[] fn;

	/**
	 * @return the $SI time-stamps
	 */
	public Timestamp[] getSI() {
		return si;
	}

	/**
	 * @param si the $SI time-stamps to set
	 */
	protected void setSI(Timestamp[] si) {
		this.si = si;
	}

	/**
	 * @return the $FN time-stamps
	 */
	public Timestamp[] getFN() {
		return fn;
	}

	/**
	 * @param fn the $FN time-stamps to set
	 */
	protected void setFN(Timestamp[] fn) {
		this.fn = fn;
	}
	
	public Timestamp[] getAll() {
		Timestamp[] all = new Timestamp[8];
		System.arraycopy(si, 0, all, 0, 4);
		System.arraycopy(fn, 0, all, 4, 4);
		return all;
	}
	
	public String toString() {
		String string = "\n$SI = ";
		for(Timestamp timestamp : si) {
			string += timestamp + " ";
		}
		string += "\n$FN = ";
		for(Timestamp timestamp : fn) {
			string += timestamp + " ";
		}
		return string;
	}
	
	public boolean hasSIAndFN() {
		return si != null && fn != null;
	}
	
	public void copyTimestamp(int from, int to) {
		Timestamp[] all = getAll();
		if (to < 4) {
			si[to] = all[from];
		}
		else {
			fn[to % 4] = all[from];
		}
	}
	
	public Timestamps clone() {
		Timestamps clone = new Timestamps();
		clone.setSI(si.clone());
		clone.setFN(fn.clone());
		return clone;
	}
	
}
