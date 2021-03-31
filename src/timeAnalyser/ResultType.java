package timeAnalyser;

/**
 * 
 */

/**
 * A time-stamp result type, which describes a resulting time-stamp for a file operation.
 * @author Jelle Bouma
 *
 */
public enum ResultType {
	
	ANY (-1, 0, 8, false, 0, false, 1L, CopyStyle.NOT_COPIED), // any time
	R_ANY (-1, 0, 9, false, 0, false, 10000000L, CopyStyle.NOT_COPIED), // any time, rounded on seconds
	R_W_P_TZD (-1, 2, 4, true, 1, true, 20000000L, 1), // time-stamp rounded up on 2 seconds + time-zone difference, copied from SI.W
	SRC_FATR_C (-1, 2, 2, true, 1, false, 100000L, CopyStyle.NOT_COPIED), // SI.C rounded up on 10 milliseconds + time-zone difference, originating from FAT volume
	SRC_FATR_W (-1, 2, 3, true, 1, false, 20000000L, CopyStyle.NOT_COPIED), // SI.W rounded up on 2 seconds + time-zone difference, originating from FAT volume
	U (0, 0, 0, false, 0, false, 1L, CopyStyle.NOT_COPIED),
	SI_SRC (0, 0, 0, false, 0, false, 1L, CopyStyle.COPIED_FROM_SAME_TYPE),
	SRC (0, 1, 1, false, 1, false, 1L, CopyStyle.NOT_COPIED),
	TNL(0, 5, 7, true, 2, false, 1L, CopyStyle.NOT_COPIED),
	OP_START (1, 3, 5, true, 0, true, 1L, CopyStyle.NOT_COPIED),
	OP_END (2, 3, 6, true, 0, true, 1L, CopyStyle.NOT_COPIED); // time of operation + processing time (Delta)
	
	private final int lateness;
	private final int possibleEquivalence;
	private final int equivalenceWithSameType;
	private final boolean alwaysSelfEquivalent;
	private final int fromFile;
	private final boolean operationResult;
	private final long rounding;
	private final CopyStyle copyStyle;
	private final int copiedFrom;

    /**
     * @param lateness If the time associated with this time-stamp change should be later or earlier than other time-stamp changes.
     * For instance: time of operation (lateness 1) is always later than unchanged time (lateness 0).
     * Note that lateness is ignored if the time-stamps are equal.
     * If the lateness can not be compared then it should be set to -1, this is the case for FAT and extracted from zip time-stamp changes which can be larger or smaller than any other time-stamp due to the timezone difference.
     * @param possibleEquivalence Other time-stamp changes with the same value can have the same time.
     * The time-stamp changes that have rounding can all have the same times because they might be different times rounded to the same time.
     * @param equivalenceWithSameType Other time-stamp changes with the same equivalenceWithSameType value have the same time, if the related time-stamps are of the same type (creation, modification, entry modification or access).
	 * @param alwaysSelfEquivalent If other time-stamp changes must have equal times to this one.
     * @param fromFile Time-stamp changes with this parameter set to the same value, have time-stamps originating from the same file.
     * Different values mean the time-stamps stem from different files.
     * If the time-stamps stem from different files, the unmatched time-stamps need to be divided among these files.
     * @param operationResult If the associated time-stamp is related to the time of the operation.
     * These time-stamps will determine at which time the operation happened.
     * @param rounding The rounding the time-stamp should have, a value of 1L means rounded on 100 nanoseconds (no rounding).
     * @param copyStyle This can have one of two values:
     * NOT_COPIED if this time-stamp change is not a time-stamp copied from another location.
     * COPIED_FROM_SAME_TYPE if the time-stamp is copied from the other attribute to the same time-stamp type.
     */
    ResultType(int lateness, int possibleEquivalence, int equivalenceWithSameType, boolean alwaysSelfEquivalent, int fromFile, boolean operationResult, long rounding, CopyStyle copyStyle) {
        this.lateness = lateness;
        this.possibleEquivalence = possibleEquivalence;
        this.equivalenceWithSameType = equivalenceWithSameType;
        this.alwaysSelfEquivalent = alwaysSelfEquivalent;
        this.fromFile = fromFile;
        this.operationResult = operationResult;
        this.rounding = rounding;
        this.copyStyle = copyStyle;
        this.copiedFrom = -1;
    }
    
    /**
     * Constructor for a time-stamp change with copy style COPIED_FROM_TIMESTAMP.
     * This is a time-stamp change where time-stamps are copied from one specific time-stamp to the time-stamp associated with this time-stamp change.
     * @param copiedFrom the specific time-stamp that is copied from.
     * @see {@link #Change(int, int, int, boolean, int, boolean, long, CopyStyle)}
     */
    ResultType(int lateness, int possibleEquivalence, int equivalenceWithSameType, boolean alwaysSelfEquivalent, int fromFile, boolean operationResult, long rounding, int copiedFrom) {
        this.lateness = lateness;
        this.possibleEquivalence = possibleEquivalence;
        this.equivalenceWithSameType = equivalenceWithSameType;
        this.alwaysSelfEquivalent = alwaysSelfEquivalent;
        this.fromFile = fromFile;
        this.operationResult = operationResult;
        this.rounding = rounding;
        this.copyStyle = CopyStyle.COPIED_FROM_SAME_TYPE;
        this.copiedFrom = copiedFrom;
    }
    
    /**
	 * @param c change to be compared to.
	 * @return -1 if this Change is later than Change c.
	 * 1 if this Change is earlier than Change c.
	 * 0 if the order of the changes can not be determined.
	 */
    int compare(ResultType c) {
    	if(this.lateness == -1 || c.lateness == -1) {
    		return 0;
    	}
    	return Integer.compare(this.lateness, c.lateness);
    }
    
    /**
	 * @param c Change to be tested for equivalence with this Change.
	 * @return if the changes can have equal times
	 */
    boolean canBeEquivalentWith(ResultType c) {
    	return this.possibleEquivalence == c.possibleEquivalence;
    }
    
    /**
	 * @param c Change to be tested for equivalence with this Change if they have the same time-stamp type.
	 * @return if the changes should have equal times if they have the same time-stamp type (creation, modification etc.)
	 */
    boolean sameTypeEquivalenceWith(ResultType c) {
    	return this != c && equivalenceWithSameType == c.equivalenceWithSameType;
    }
    
    boolean isAlwaysSelfEquivalent() {
    	return alwaysSelfEquivalent;
    }
    
    boolean isOperationResult() {
    	return operationResult;
    }
    
    int fromFile() {
    	return fromFile;
    }
    
    boolean fromThisFile() {
    	return fromFile == 0;
    }
    
    boolean isCopied() {
    	return copyStyle != CopyStyle.NOT_COPIED;
    }
    
    boolean match(Timestamp timestamp) {
    	return timestamp.isRoundedOn(rounding);
    }
    
    int getCopySource(int changeIndex) {
    	switch(this.copyStyle) {
    		case COPIED_FROM_SAME_TYPE: return (changeIndex + 4) % 8;
    		case COPIED_FROM_TIMESTAMP: return copiedFrom;
    		default:					return changeIndex;
    	}
    }
    
}