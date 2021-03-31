/**
 * 
 */
package timeAnalyser;

import java.util.ArrayList;

/**
 * A Sequence is a sequence of operations that may have happened, the times that those operations happened and the before and after meta-data.
 * The duties of a Sequence are: Adding of matched operations and the meta-data that was formed by the operations, getting and storing the time and meta-data for which the matched operations happened.
 * A sequence has a marking which keeps track of which time-stamps have been matched to file operations and which have not.
 * @author Jelle Bouma
 * @see FileMetadata TimeMatch
 */
public class Sequence {
	
	// A sequence holds a list of TimeMatch objects.
	// It also contains a list of FileMetadata objects (which might be references to the same object) which hold the before and after state of the operations described in the TimeMatch objects.
	// This is because the meta-data can change as a result of some file operations.
	// A FileMetadata is located at the same index as the TimeMatch that formed it.
	// There is always one more FileMetadata object than there are TimeMatch objects, because the highest index FileMetadata is the FileMetadata before any of the matched file operations took place.
	ArrayList<TimeMatch> matches = new ArrayList<TimeMatch>();
	ArrayList<FileMetadata> metadataList = new ArrayList<FileMetadata>();
	private Marking matchedTimestamps = new Marking();
	private boolean hasForgery = false;
	
	
	/**
	 * Constructs a Sequence object from the initial file meta-data read from the MFT.
	 * This initial file meta-data is the state after a sequence of file operations.
	 * @param metadata the initial file meta-data read from the MFT
	 */
	public Sequence(FileMetadata metadata) {
		metadataList.add(metadata);
	}
	
	/**
	 * Private constructor for cloning.
	 */
	private Sequence(ArrayList<TimeMatch> matches, ArrayList<FileMetadata> metadataList, Marking matchedTimestamps) {
		this.matches = matches;
		this.metadataList = metadataList;
		this.matchedTimestamps = matchedTimestamps;
	}
	
	/**
	 * Adds some matched operations with the same time and effect on meta-data to the sequence.
	 * Also gets and adds the matched meta-data from before any of those operations to the sequence.
	 * If the operations split time-stamps across files use addSplit instead.
	 * If the operations copy time-stamps within the meta-data use addWithCopying instead.
	 * @param metadata the file meta-data as it is after operation
	 * @param matchedOperations the operations matched to the meta-data that they caused
	 * @see TimeMatch
	 */
	public void add(FileMetadata metadata, ArrayList<Operation> matchedOperations) {
		Operation firstMatch = matchedOperations.get(0);
		matches.add(findTimeMatch(metadata, matchedOperations));
		int fromOtherVolume = firstMatch.fromOtherVolume();
		if (fromOtherVolume != -1) { // If the operation (0 = possibly, 1 = definitely) happened on another volume.
			metadata = metadata.clone(fromOtherVolume); // Create a new copy of the FileMetaData that is (possibly or definitely) on another volume.
		}
		metadataList.add(metadata);
		matchedTimestamps.mark(firstMatch.getMarking());
	}
	
	/**
	 * Adds some matched forgery operations to the sequence.
	 * @param metadata the file meta-data as it is after operation
	 * @param matchedOperations the operations matched to the meta-data that they caused
	 * @see TimeMatch
	 */
	public void addForgery(FileMetadata metadata, ArrayList<Operation> matchedOperations) {
		matches.add(findTimeMatch(metadata, matchedOperations));
		metadataList.add(metadata);
		hasForgery = true;
	}
	
	/**
	 * Adds some matched operations with the same time and effect on meta-data to the sequence.
	 * Also adds the matched meta-data from after any of those operations to the sequence.
	 * Copies time-stamps back to their locations before operation.
	 * Only use for operations that copy time-stamps within the file meta-data.
	 * @param metadata the file meta-data as it is after operation
	 * @param matchedOperations the operations matched to the meta-data that they caused
	 * @see TimeMatch
	 */
	public void addWithCopying(FileMetadata metadata, ArrayList<Operation> matchedOperations) {
		Operation firstMatch = matchedOperations.get(0);
		matches.add(findTimeMatch(metadata, matchedOperations));
		matchedTimestamps.mark(firstMatch.getMarking());
		FileMetadata newMetadata = metadata.clone(firstMatch.fromOtherVolume());
		Timestamps timestamps = newMetadata.getTimestamps();
		for (int ii = 0; ii < 8; ii++) { // for each time-stamp/result type
			ResultType change = firstMatch.effect[ii];
			if (change.isCopied() && !matchedTimestamps.isMarked(ii)) {
				timestamps.copyTimestamp(ii, change.getCopySource(ii)); // copy time-stamp back
				matchedTimestamps.unmark(change.getCopySource(ii)); // unmark the copy source time-stamp
			}
		}
		matchedTimestamps.mark(firstMatch.getCopyMarking());
		metadataList.add(newMetadata);
	}
	
	/**
	 * Adds some matched operations with the same time and effect on meta-data to the sequence.
	 * Also adds the matched meta-data from after any of those operations to the sequence.
	 * Splits time-stamps to their origin meta-data, use only for splitting operations 
	 * @param metadata the file meta-data as it is after operation
	 * @param matchedOperations operations with equal effect on meta-data, matched to the meta-data that any of them caused
	 * @return a sequence for the split off file
	 * @see TimeMatch
	 */
	public Sequence addSplit(FileMetadata metadata, ArrayList<Operation> matchedOperations) {
		Operation firstMatch = matchedOperations.get(0);
		Sequence split = split();
		matches.add(findTimeMatch(metadata, matchedOperations));
		split.matches.add(findTimeMatch(metadata, matchedOperations));
		metadataList.add(metadata);
		split.metadataList.add(metadata.splitFrom(firstMatch.fromOtherVolume()));
		matchedTimestamps.mark(firstMatch.getSplitMarkings()[0]);
		split.matchedTimestamps.mark(firstMatch.getSplitMarkings()[1]);
		return split;
	}
	
	/**
	 * Adds the deletion file operation, which does not have an effect on time-stamps but will have happened after the last time-stamp.
	 * @param metadata the file meta-data as it is after operation
	 */
	public void addDeletionOperation(FileMetadata metadata) {
		ArrayList<Operation> matchedDeletion = new ArrayList<Operation>();
		matchedDeletion.add(TimeAnalyser.DELETION_OPERATION);
		Timestamp[] timestamps = metadata.getTimestamps().getAll();
		Timestamp latestTimestamp = timestamps[0];
		for (Timestamp timestamp : timestamps) {
			if (latestTimestamp.compare(timestamp) < 0) {
				latestTimestamp = timestamp;
			}
		}
		matches.add(new TimeMatch(latestTimestamp, null, matchedDeletion, true));
		FileMetadata undeleted = metadata.clone();
		undeleted.setDeleted(false);
		metadataList.add(undeleted);
	}
	
	
	/**
	 * Finds the time that the file operations match.
	 * @param metadata the meta-data after any of the file operations
	 * @param matchedOperations operations with equal effect on meta-data, matched to the meta-data that any of them caused
	 * @return the time match
	 */
	private TimeMatch findTimeMatch (FileMetadata metadata, ArrayList<Operation> matchedOperations) {
		Operation firstMatch = matchedOperations.get(0);
		if (firstMatch.hasDurationResultFor(matchedTimestamps)) { // If the operation matches a duration (operation was happening from duration[0] to duration[1]).
			Timestamp[] duration = firstMatch.getDuration(metadata, matchedTimestamps);
			return new TimeMatch(duration[0], duration[1], matchedOperations, false);
		}
		if (firstMatch.hasRangeResultFor(matchedTimestamps)) { // If the operation matches a range (operation happened at some point between range[0] and range[1]).
			Timestamp[] range = firstMatch.getRange(metadata);
			return new TimeMatch(range[0], range[1], matchedOperations, true);
		}
		// Operation does not match a duration or a range, instead it matches a single time-stamp.
		Timestamp matchedTime = firstMatch.getTime(metadata, matchedTimestamps);
		return new TimeMatch(matchedTime, matchedOperations);
	}
	
	public Marking getMarking() {
		return matchedTimestamps;
	}
	
	public boolean isFullyMatched() {
		return matchedTimestamps.isFullyMarked();
	}
	
	protected Sequence clone() {
		return new Sequence((ArrayList<TimeMatch>)matches.clone(), (ArrayList<FileMetadata>)metadataList.clone(), matchedTimestamps.clone());
	}
	
	/**
	 * Returns split off sequence for combined operations.
	 * @return split off sequence
	 */
	private Sequence split() {
		Sequence splitting = new Sequence(getEarliestMetadata());
		splitting.matchedTimestamps = matchedTimestamps.clone();
		return splitting;
	}
	
	public int getAmountOfMetadata() {
		return metadataList.size();
	}
	
	public FileMetadata getMetadata(int index) {
		return metadataList.get(index);
	}
	
	public TimeMatch getTimeMatch(int index) {
		return matches.get(index);
	}
	
	public FileMetadata getEarliestMetadata() {
		return metadataList.get(metadataList.size() - 1);
	}
	
	public boolean hasIrregularTimeStamps() {
		return hasForgery || matches.size() == 0;
	}
	
	public String toString() {
		String sequence = "";
		FileMetadata metadata = metadataList.get(0);
		if(metadata.hasPath()) {
			sequence += metadata.getPath() + " ";
		}
		else {
			if (metadata.hasName()) {
				 sequence += metadata.getName() + " ";
			}
			else {
				sequence += "no file name ";
			}
		}
		if (matches.size() == 0) {
			if(metadataList.get(0).hasSIAndFN()) {
				return sequence + "irregular time-stamps";
			}
			else {
				return sequence + "no time-stamps";
			}
		}
		if (hasForgery) {
			sequence += "irregular time-stamps: ";
		}
		for(int ii = 0; ii < matches.size() - 1; ii++) {
			sequence += matchToString(ii);
			sequence += " <- ";
		}
		return sequence + matchToString(matches.size() - 1);
	}
	
	private String matchToString(int index) {
		String match = "";
		match += "(" + matches.get(index) + ")";
		switch (metadataList.get(index).onOtherVolume()) {
			case 0:		return match + " possibly on other volume";
			case 1:		return match + " on other volume";
			default: 	return match;
		}
	}
	
	/**
	 * @return if this sequence is split from another sequence, because of a combining operation.
	 */
	boolean isSplitting() {
		for (FileMetadata metadata : metadataList) {
			if (metadata.isSplitting()) {
				return true;
			}
		}
		return false;
	}
	
	
}
