package timeAnalyser;

/**
 * 
 */

/**
 * A file operation, more specifically: the way the file operation changes time-stamps.
 * 
 * @author Jelle Bouma
 *
 */
public class Operation {
	
	private String name;
	ResultType[] effect = new ResultType[8];
	private Marking operationResult = new Marking();
	private Marking copied = new Marking();
	private int fromOtherVolume = -1;
	private int appliesToDirectories = 0; // 0 means this operation applies to files and directories, -1 means it does not apply to directories, 0 means it applies only to directories
	boolean isCombiningOperation = false;
	Marking[] splitMarkings;
	boolean isCreatingOperation = true;
	
	/**
	 * Constructs a file operation from its name and array of time-stamp changes, the operation can not transfer time-stamps between volumes and applies to files and directories.
	 *
	 * @param String name the operation name
	 * @param siC time-stamp change for the $SI.C time-stamp
	 */
	public Operation(String name, ResultType siC, ResultType siW, ResultType siE, ResultType siA, ResultType fnC, ResultType fnW, ResultType fnE, ResultType fnA) {
		this.name = name;
		ResultType[] changes = {siC, siW, siE, siA, fnC, fnW, fnE, fnA};
		this.effect = changes;
		calculateMarkings();
		calculateIsCreatingOperation();
		calculateIsCombiningOperation();
		if (isCombiningOperation) {
			calculateSplitMarkings();
		}
	}
	
	/**
	 * Constructs a file operation from its name, array of time-stamp changes and an integer indicating if this file operation can transfer time-stamps between volumes.
	 * The file operation applies to files and directories.
	 *
	 * @param String name the operation name
	 * @param siC time-stamp change for the $SI.C time-stamp
	 * @param fromOtherVolume integer which is -1 if the operation can not transfer time-stamps between volumes, 0 if it might and 1 if it will.
	 */
	public Operation(String name, ResultType siC, ResultType siW, ResultType siE, ResultType siA, ResultType fnC, ResultType fnW, ResultType fnE, ResultType fnA, int fromOtherVolume) {
		this(name, siC, siW, siE, siA, fnC, fnW, fnE, fnA);
		this.fromOtherVolume = fromOtherVolume;
	}
	
	/**
	 * Constructs a file operation from its name, array of time-stamp changes, an integer indicating if this file operation can transfer time-stamps between volumes and an integer indicating if this file operation applies to files, directories or both.
	 *
	 * @param String name the operation name
	 * @param siC time-stamp change for the $SI.C time-stamp
	 * @param fromOtherVolume integer which is -1 if the operation can not transfer time-stamps between volumes, 0 if it might and 1 if it will.
	 */
	public Operation(String name, ResultType siC, ResultType siW, ResultType siE, ResultType siA, ResultType fnC, ResultType fnW, ResultType fnE, ResultType fnA, int fromOtherVolume, int appliesToDirectories) {
		this(name, siC, siW, siE, siA, fnC, fnW, fnE, fnA);
		this.fromOtherVolume = fromOtherVolume;
		this.appliesToDirectories = appliesToDirectories;
	}
	
	/**
	 * Checks if the time-stamps from the file meta-data match this operation for the provided marking.
	 * If the operation matches then it might have happened for this file at this stage/marking.
	 * If it does not match then it can not have happened for this file at this stage/marking.
	 * The operation will match if any and all unmarked time-stamps match the time-stamp changes, at least on of which holds a time-stamp that can be used to determine when the operation happened.
	 *
	 * @param metadata the file meta-data to match against the operation
	 * @param progressMarking marking which indicates which time-stamps have been matched already
	 * @return if the time-stamps from the file meta-data match this operation for the provided marking.
	 */
	public boolean matches(FileMetadata metadata, Marking progressMarking) {
		if ((metadata.isDirectory() && appliesToDirectories == -1) || (!metadata.isDirectory() && appliesToDirectories == 1)) { // check if the file is a directory and if this operation is available to it.
			return false;
		}
		if ((!hasCopying() && progressMarking.eclipses(operationResult)) || (hasCopying() && progressMarking.eclipses(copied))) { // checks if this operation can be matched to a time for the current marking
			return false;
		}
		Timestamp[] timestamps = metadata.getTimestamps().getAll();
		for (int ii = 0; ii < effect.length; ii++) {
			if (!effect[ii].match(timestamps[ii]) && !progressMarking.isMarked(ii)) { // checks if the time-stamp can match the time-stamp change (has proper rounding)
				return false;
			}
		}
		for (int ii = 0; ii < effect.length - 1; ii++) {
			for (int jj = ii + 1; jj < effect.length; jj++) { // compare every pair of time-stamps and time-stamp changes
				int changeComparison = effect[ii].compare(effect[jj]);
				int timeComparison = timestamps[ii].compare(timestamps[jj]);
				if (!progressMarking.isMarked(ii) && !progressMarking.isMarked(jj)) {
					if ((changeComparison > 0 && timeComparison < 0) || (changeComparison < 0 && timeComparison > 0)) { // check if the time-stamps have to be bigger/smaller
						return false;
					}
					if (effect[ii] == effect[jj] && effect[ii].isAlwaysSelfEquivalent() && timeComparison != 0) { // check if the time-stamps should be equal
						return false;
					}
					if (!effect[ii].canBeEquivalentWith(effect[jj]) && timeComparison == 0) { // check if the time-stamps are allowed to be equal
						return false;
					}
					if(jj - ii == 4 && effect[ii].sameTypeEquivalenceWith(effect[jj]) && timeComparison != 0) { // check if the time-stamps should be equal if they have the same type
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public Marking getMarking() {
		return operationResult;
	}
	
	public Marking getCopyMarking() {
		return copied;
	}
	
	/**
	 * @return if the operation copies time-stamps within the file meta-data
	 */
	public boolean hasCopying() {
		return !copied.isUnmarked();
	}
	
	public int fromOtherVolume() {
		return fromOtherVolume;
	}
	
	/**
	 * Infers which time-stamps this operation should mark.
	 * This method should only be called once per file operation.
	 */
	private void calculateMarkings() {
		for(int ii = 0; ii < effect.length; ii++) {
			if (effect[ii].isOperationResult()) {
				operationResult.mark(ii);
			}
			if(effect[ii].getCopySource(ii) != ii) {
				copied.mark(ii);
			}
		}
	}
	
	/**
	 * Given a marking, finds out if this operation will match a duration of time.
	 * @param marking
	 * @return if this file operation will match a duration of time for the marking 
	 */
	public boolean hasDurationResultFor(Marking marking) {
		for(int ii = 0; ii < effect.length - 1; ii++) {
			for(int jj = ii + 1; jj < effect.length; jj++) { 
				if(!marking.isMarked(ii) && !marking.isMarked(jj) && effect[ii].compare(effect[jj]) != 0 && effect[ii].isOperationResult() && effect[jj].isOperationResult()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Given a marking, finds out if this operation will match a range of time.
	 * @param marking
	 * @return if this file operation will match a range of time for the marking 
	 */
	public boolean hasRangeResultFor(Marking marking) {
		if(!hasCopying()) {
			return false;
		}
		for(int ii = 0; ii < effect.length; ii++) {
			if(!marking.isMarked(ii) && effect[ii].isOperationResult()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Given a marking and file meta-data, finds the duration of time at which this file operation might have happened.
	 * Before calling this method make sure `hasDurationResultFor(Marking)' returns true for this file operation and marking.
	 * @param marking
	 * @return an array of two time-stamps which contain the start and end time-stamp for this file operation.
	 */
	public Timestamp[] getDuration(FileMetadata metadata, Marking marking) {
		Timestamp[] timestamps = metadata.getTimestamps().getAll();
		Timestamp[] duration = new Timestamp[2];
		for (int ii = 0; ii < effect.length; ii++) {
			for (int jj = ii + 1; jj < effect.length; jj++) { 
				int latenessDifference = effect[ii].compare(effect[jj]);
				if (latenessDifference != 0 && !marking.isMarked(ii) && !marking.isMarked(jj) && effect[ii].isOperationResult() && effect[jj].isOperationResult()) {
					duration[(latenessDifference + 1) / 2] = timestamps[ii];
					duration[((latenessDifference + 1) / 2) ^ 1] = timestamps[jj];
					return duration;
				}
			}
		}
		System.out.println("Attempt to get duration of an event without duration. THIS SHOULD NEVER HAPPEN");
		return null;
	}
	
	/**
	 * Given a marking and file meta-data, finds the range of time in which this file operation may have happened.
	 * Before calling this method make sure `hasRangeResultFor(Marking)' returns true for this file operation and marking.
	 * @param marking
	 * @return an array of two time-stamps which contain the start and end time-stamp for the range during which this file operation may have happened.
	 */
	public Timestamp[] getRange(FileMetadata metadata) {
		Timestamp[] timestamps = metadata.getTimestamps().getAll();
		Timestamp[] range = {new Timestamp(0L), new Timestamp(-1L)};
		for (int ii = 0; ii < effect.length; ii++) {
			if (effect[ii].isCopied()) {
				if(timestamps[ii].compare(range[0]) > 0) {
					range[0] = timestamps[ii];
				}
			}
		}
		for (int ii = 0; ii < effect.length; ii++) {
			if (!effect[ii].isCopied()) {
				if(timestamps[ii].compare(range[0]) > 0 && timestamps[ii].compare(range[1]) < 0) {
					range[1] = timestamps[ii];
				}
			}
		}
		return range;
	}
	
	/**
	 * Given a marking and file meta-data, finds time at which this file operation may have happened.
	 * Do not use this method if the file operation matches a range or duration.
	 * @param marking
	 * @return an array of two time-stamps which contain the start and end time-stamp for the range during which this file operation may have happened.
	 */
	public Timestamp getTime(FileMetadata metadata, Marking marking) {
		Timestamp[] timestamps = metadata.getTimestamps().getAll();
		for (int ii = 0; ii < effect.length; ii++) {
			if (effect[ii].isOperationResult() && !marking.isMarked(ii)) {
				return timestamps[ii];
			}
		}
		return null;
	}
	
	public Marking[] getSplitMarkings() {
		return splitMarkings;
	}
	
	/**
	 * Finds out if this file operation combines time-stamps (has time-stamps from two different files).
	 * This method should only be called once per file operation.
	 */
	private void calculateIsCombiningOperation() {
		for (int ii = 0; ii < effect.length; ii++) {
			for (int jj = ii + 1; jj < effect.length; jj++) {
				if (!effect[ii].isOperationResult() && !effect[jj].isOperationResult() && effect[ii].fromFile() != effect[jj].fromFile()) {
					isCombiningOperation = true;
					break;
				}
			}
		}
	}
	
	/**
	 * Calculates the markings for the two source files for this combining operation.
	 * This method should only be called once per file operation, and only if it is a combining operation.
	 */
	private void calculateSplitMarkings() {
		Marking[] markings = {operationResult.clone(), operationResult.clone()};
		int file1;
		if (!isCreatingOperation) {
			file1 = 0;
		}
		else {
			file1 = effect[0].fromFile();
		}
		for (int ii = 0; ii < effect.length; ii++) {
			if (!operationResult.isMarked(ii)) {
				if (effect[ii].fromFile() == file1) {
					markings[1].mark(ii);
				}
				else {
					markings[0].mark(ii);
				}
			}
		}
		splitMarkings = markings;
	}
	
	/**
	 * Finds out if this file operation is a creating operation (has only time-stamps from time of operation or other files).
	 * This method should only be called once per file operation.
	 */
	private void calculateIsCreatingOperation() {
		for (int ii = 0; ii < effect.length; ii++) {
			if (effect[ii].fromThisFile() && !effect[ii].isOperationResult()) {
				isCreatingOperation = false;
				break;
			}
		}
	}
	
	public boolean isCreatingOperation() {
		return isCreatingOperation;
	}
	
	public boolean isCombiningOperation() {
		return isCombiningOperation;
	}
	
	/**
	 * Checks if this is a combining operation (has time-stamps from two different files) for the given marking.
	 * @param marking
	 * @returns if this is a combining operation (has time-stamps from two different files) for the given marking
	 */
	public boolean isCombiningOperationFor(Marking marking) {
		for (int ii = 0; ii < effect.length; ii++) {
			for (int jj = ii + 1; jj < effect.length; jj++) {
				if (!marking.isMarked(ii) && !marking.isMarked(jj) && !effect[ii].isOperationResult() && !effect[jj].isOperationResult() && effect[ii].fromFile() != effect[jj].fromFile()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if the file operations have the same effects on meta-data for a marking.
	 * @param operation operation to compare against
	 * @param currentSequenceMarking The marking for which to compare 
	 * @returns if these file operations have the same effects on meta-data for the given marking
	 */
	public boolean equalsForMarking(Operation operation, Marking currentSequenceMarking) {
		Marking markingChange1 = currentSequenceMarking.getChangeFor(getMarking());
		Marking markingChange2 = currentSequenceMarking.getChangeFor(operation.getMarking());
		if (!markingChange1.equals(markingChange2)) {
			return false;
		}
		if (fromOtherVolume != operation.fromOtherVolume) {
			return false;
		}
		if (hasCopying() || operation.hasCopying()) {
			for (int ii = 0; ii < effect.length; ii++) {
				if ((effect[ii].isCopied() || operation.effect[ii].isCopied()) && effect[ii] != operation.effect[ii]) {
					return false;
				}
			}
		}
		if (isCombiningOperationFor(currentSequenceMarking) != operation.isCombiningOperationFor(currentSequenceMarking)) {
			return false;
		}
		if (isCombiningOperationFor(currentSequenceMarking) && operation.isCombiningOperationFor(currentSequenceMarking)) {
			for (int ii = 0; ii < effect.length; ii++) {
				if (effect[ii].fromFile() != operation.effect[ii].fromFile()) {
					return false;
				}
			}
		}
		return true;
	}
	
	public String toString() {
		return name;
	}
	
}