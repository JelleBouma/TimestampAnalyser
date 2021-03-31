package timeAnalyser;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 
 */

/**
 * Immutable time-stamp read from MFT.
 * @author Jelle Bouma
 *
 */
public class Timestamp {
	
	ZonedDateTime date;
	long timeValue;
	
	public Timestamp(long timeValue) { // timeValue holds the unsigned amount of tenth microseconds from 1601-01-01 0:0:0
		this.timeValue = timeValue;
		date = ZonedDateTime.of(1601, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")); // set date to NTFS epoch
		long seconds = Long.divideUnsigned(timeValue, 10000000L); // get signed seconds from the unsigned timeValue by dividing it by 10,000,000
		long nanos = Long.remainderUnsigned(timeValue, 10000000L) * 100L; // get signed tenth micros from the unsigned timeValue and multiply by 100 to get the amount of nanos
		date = date.plusSeconds(seconds).plusNanos(nanos); // add seconds and nanos to NTFS epoch to get the correct time-stamp
	}
	
	public boolean isRoundedOn(long tenthMicros) {
		return Long.remainderUnsigned(timeValue, tenthMicros) == 0L;
	}
	
	public int compare(Timestamp t) {
		return Long.compareUnsigned(timeValue, t.timeValue);
	}
	
	public String toString() {
		return date.getYear() + "-" + date.getMonth() + "-" + date.getDayOfMonth() + " "
				+ date.getHour() + ":" + date.getMinute() + ":" + date.getSecond() + "." + String.format("%07d", date.getNano() / 100) + " " + date.getZone();
	}
	
}