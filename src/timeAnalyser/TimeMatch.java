/**
 * 
 */
package timeAnalyser;

import java.util.ArrayList;

/**
 * A time (either range, duration or precise time) and the operations that might have happened at that time.
 * @author Jelle Bouma
 *
 */
public class TimeMatch {
	Timestamp lower;
	Timestamp upper;
	ArrayList<Operation> matchedOperations = new ArrayList<Operation>();
	boolean isRange = false;
	
	/**
	 * Constructs a time match from a time-stamp and the operations matched to this time-stamp.
	 * @param time The precise time at which the operations might have happened.
	 * @param matchedOperations The operations matched to this time-stamp.
	 */
	public TimeMatch(Timestamp time, ArrayList<Operation> matchedOperations) {
		this.lower = time;
		this.matchedOperations = matchedOperations;
	}
	
	/**
	 * Constructs a time match from a range or duration of time and the operations matched to this range or duration.
	 * @param lower The lower time-stamp, this is the lower end of a duration or range.
	 * @param upper The upper time-stamp, this is the upper end of a duration or range.
	 * @param matchedOperations The operations matched to this range or duration.
	 * @param isRange whether the time-stamps are part of a range, or a duration.
	 */
	public TimeMatch(Timestamp lower, Timestamp upper, ArrayList<Operation> matchedOperations, boolean isRange) {
		this.lower = lower;
		if(!(upper==null) && !(lower.compare(upper) == 0)) {
			this.upper = upper;
		}
		this.isRange = isRange;
		this.matchedOperations = matchedOperations;
	}
	
	/**
	 * Returns true if the matched time has an upper bound, false otherwise.
	 * If the matched time has an upper bound, it is not a precise time, but a range or duration.
	 * @return if the matched time has an upper bound
	 */
	public boolean hasUpperBound() {
		return upper != null;
	}
	
	public Timestamp[] getTimestamps() {
		Timestamp[] timestamps = {lower, upper};
		return timestamps;
	}
	
	public boolean isRange() {
		return isRange;
	}
	
	public String toString() {
		String timeMatch = "";
		if (!hasUpperBound()) {
			if (isRange) {
				timeMatch += "After " + lower;
			}
			else {
				timeMatch += "At " + lower;
			}
		}
		else {
			if (isRange) {
				timeMatch += "Between " + lower + " and " + upper;
			}
			else {
				timeMatch += "From " + lower + " to " + upper;
			}
		}
		timeMatch += ": " + matchedOperations.get(0);
		for (int ii = 1; ii < matchedOperations.size(); ii++) {
			timeMatch += " | " + matchedOperations.get(ii);
		}
		return timeMatch;
	}
	
}
