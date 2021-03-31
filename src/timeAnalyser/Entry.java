package timeAnalyser;

import java.util.ArrayList;

/**
 * 
 */

/**
 * The parsed entry from the MFT, holds information regarding the entry and all calculated sequences of file operations that might have led to this entry.
 * @author Jelle Bouma
 *
 */
public class Entry {

	private boolean signature;
	private int indexNTFS;
	private int sequenceNumberNTFS;
	ArrayList<Sequence> sequences = new ArrayList<Sequence>();
	
	public Entry(int indexNTFS, boolean signature) {
		this.indexNTFS = indexNTFS;
		this.signature = signature;
	}
	
	public void setFileMetadata (FileMetadata metadata) {
		sequences.add(new Sequence(metadata));
	}
	
	/**
	 * @return whether the entry has irregular time-stamps (not matchable with any regular operation)
	 */
	public boolean hasIrregularTimeStamps() {
		boolean hasIrregular = false;
		for (Sequence sequence : sequences) {
			hasIrregular |= sequence.hasIrregularTimeStamps();
		}
		return hasIrregular;
	}

	/**
	 * @return the index number
	 */
	public int getNTFSIndex() {
		return indexNTFS;
	}
	
	/**
	 * @return the sequence number
	 */
	public int getNTFSSequenceNumber() {
		return sequenceNumberNTFS;
	}

	/**
	 * @param sequence the sequence number to set
	 */
	public void setNTFSSequenceNumber(int sequenceNumberNTFS) {
		this.sequenceNumberNTFS = sequenceNumberNTFS;
	}

	/**
	 * @return if the signature of the entry indicates it is intact ("FILE")
	 */
	public boolean signatureIntact() {
		return signature;
	}
	
	public int getAmountOfSequences() {
		return sequences.size();
	}
	
	public Sequence getSequence(int index) {
		return sequences.get(index);
	}
	
	public void add(Sequence sequence) {
		sequences.add(sequence);
	}
	
	public boolean hasSIAndFN() {
		return sequences.get(0).getMetadata(0).hasSIAndFN();
	}
	
	public String toString() {
		String entry = "";
		for (Sequence sequence : sequences) {
			if (sequence.isSplitting()) {
				entry += sequence + System.lineSeparator();
			}
			else {
				entry += indexNTFS + " " + sequence + System.lineSeparator();
			}
		}
		return entry.trim();
	}
	
}
