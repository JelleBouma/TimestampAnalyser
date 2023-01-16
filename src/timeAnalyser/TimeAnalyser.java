package timeAnalyser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 
 */

/**
 * Main class of the time-stamp analyser.
 * Parses the input arguments and executes the main line of operation.
 * @author Jelle Bouma
 *
 */
public class TimeAnalyser {

	static final OperationList OPERATION_LIST = new OperationList();
	static MFTReader reader;
	static Entry[] entries;
	static final int DEFAULT_ENTRY_SIZE = 1024;
	static final Operation DELETION_OPERATION = new Operation("Delete", ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U);
	static PrintWriter outputWriter;
	private static Priority priority = Priority.REGULAR;
	
	/**
	 * Main method, validates and parses the arguments and then delegates the work.
	 * @param args
	 * args[0] input file
	 * args[1] output file
	 * args[2] (optional) MFT entry size in bytes: default is 1024.
	 * args[3] (optional) filter: either ``deleted'' to analyse only time-stamps of deleted files, ``irregular'' to find files with irregular time-stamps or ``all'' for everything (default).
	 * args[4] (optional) priority: either ``equal'' to consider forgery operations always or ``regular'' to consider forgery operations only when regular operations can not be matched (default).
	 * args[5] (optional) list of indexes or file names to be analysed separated by ``|''. By default every file in the MFT is analysed.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			throw new IllegalArgumentException("Not enough parameters, specify at least an input and output file.");
		}
		File input = new File(args[0]);
		if (!input.exists()) {
			throw new IllegalArgumentException("The input file " + args[0] + " does not exist.");
		}
		int entrySize = DEFAULT_ENTRY_SIZE;
		if (args.length > 2) {
			try {
				entrySize = Integer.parseInt(args[2]);
				if(entrySize < 1024 || entrySize % 1024 != 0) {
					throw new IllegalArgumentException("Entry size needs to be a positive integer divisible by 1024 " + args[2] + " is not.");
				}
			}
			catch(NumberFormatException numberEx) {
				throw new IllegalArgumentException("Entry size needs to be a positive integer divisible by 1024 " + args[2] + " is not.");
			}
		}
		Filter filter = null;
		if (args.length > 3) {
			filter = Filter.getFilter(args[3]);
			if (filter == null) {
				throw new IllegalArgumentException(args[3] + " is not a valid filter, use 'deleted', 'irregular' or 'all'.");
			}
		}
		if (args.length > 4 && args[4].equals("equal")) {
			priority = Priority.EQUAL;
		}
		boolean hasIndexFilter = false;
		if (args.length > 5) {
			hasIndexFilter = args[5].matches("[\\d]([\\d]*|(\\|[\\d]))+");
		}
		if (args.length > 6) {
			throw new IllegalArgumentException("Too many parameters, use the following: input, output, filter, list of indexes or list of file names separated by |");
		}
		switch (args.length) {
			case 4:		reader = new MFTReader(new File(args[0]), entrySize, filter);
						break;
			case 6:		if(hasIndexFilter) {
							String[] stringIndexes = args[5].split("\\|");
							ArrayList<Integer> indexes = new ArrayList<>();
							for(String index : stringIndexes) {
								indexes.add(Integer.parseInt(index));
							}
							reader = new MFTReader(new File(args[0]), entrySize, filter, indexes);
						}
						else {
							reader = new MFTReader(new File(args[0]), entrySize, filter, args[5].split("\\|"));
						}
						break;
			default: 	reader = new MFTReader(new File(args[0]), entrySize);
		}
		entries = reader.read();
		if(!hasIndexFilter) {
			findFullPaths();
		}
		for (Entry entry : entries) {
			if (entry.signatureIntact() && entry.hasSIAndFN()) {
				analyseEntry(entry);
			}
		}
		outputWriter = new PrintWriter(args[1]);
		for (Entry entry : entries) {
			if (entry.signatureIntact() && entry.hasSIAndFN() && (filter != Filter.IRREGULAR || entry.hasIrregularTimeStamps())) {
				outputWriter.println(entry);
			}
		}
		outputWriter.close();
	}
	
	/**
	 * Find the full file paths of all entries.
	 */
	private static void findFullPaths() {
		for(int ii = 0; ii < entries.length; ii++) {
			if (entries[ii].signatureIntact()) {
				FileMetadata metadata = entries[ii].getSequence(0).getMetadata(0);
				if (!metadata.isDeleted() && metadata.hasSIAndFN()) {
					metadata.setPath(findFullPath(ii));
				}
			}
		}
	}
	
	/**
	 * Finds a full path of a file by recursively finding the file paths of parent directory entries.
	 * @param entryID the MFT entry index of the file for which the full file path should be found.
	 * @return full file path of the file at the specified MFT entry
	 */
	public static String findFullPath(int entryID) {
		FileMetadata metadata = entries[entryID].getSequence(0).getMetadata(0);
		if (metadata.hasPath()) {
			return metadata.getPath();
		}
		int parentID = metadata.getParentID();
		if (parentID == -1) {
			return "";
		}
		if (parentID == entryID) {
			return metadata.getName();
		}
		return findFullPath(parentID) + "\\" + metadata.getName();
	}
	
	
	/**
	 * Analyses the entry finding all possible sequences of operations that might have led to the meta-data of the entry.
	 * @param entry 
	 */
	private static void analyseEntry(Entry entry) {
		Sequence firstSequence = entry.getSequence(0);
		FileMetadata latestMetadata = firstSequence.getMetadata(0);
		if (latestMetadata.isDeleted()) {
			firstSequence.addDeletionOperation(latestMetadata);
		}
		int ii = 0;
		while (ii < entry.getAmountOfSequences()) {
			fillSequence(entry, entry.getSequence(ii));
			ii++;
		}
	}
	
	
	/**
	 * Fills a sequence by matching all time-stamps with file operations.
	 * This might create additional sequences as file operations with different effects on meta-data can be matched to the same time-stamps.
	 * @param entry The entry that the sequence belongs to, additional sequences can be added to the entry by this method.
	 * @param sequence The sequence to be filled.
	 */
	private static void fillSequence(Entry entry, Sequence sequence) {
		while (!sequence.isFullyMatched()) { // While there are unmarked time-stamps left (there is a return within this loop in case nothing can be matched anymore).
			FileMetadata metadata = sequence.getEarliestMetadata();
			Marking prevMarking = sequence.getMarking();
			ArrayList<ArrayList<Operation>> matchedOperations = fillList(priority == Priority.REGULAR ? OPERATION_LIST.operations : OPERATION_LIST.allOperations, metadata, prevMarking);
			if (matchedOperations.get(0).size() == 0) { // If no regular file operations can be matched, try to match forgery operations.
				for (Operation operation : OPERATION_LIST.forgeryOperations) {
					if (operation.matches(metadata, prevMarking)) {
						matchedOperations.get(0).add(operation);
					}
				}
				if(matchedOperations.get(0).size() > 0) { // If a forgery operation has been matched.
					sequence.addForgery(metadata, matchedOperations.get(0));
				}
				return; // If forgery has been committed or the time-stamps can't be matched with anything else, stop trying to match more time-stamps.
			}
			for (int ii = matchedOperations.size() - 2; ii >= 0; ii--) { // For every list of matched operations, starting at the tail (tail list is ignored because it is empty).
				Operation firstOperation = matchedOperations.get(ii).get(0);
				Sequence newSequence;
				if (ii == 0) { // The first list of matched operations continues the initial sequence.
					newSequence = sequence;
				}
				else { // Other lists of matched operations create a new sequence that continue where the initial sequence left off.
					newSequence = sequence.clone();
				}
				if (firstOperation.hasCopying()) { // If an operation copies time-stamps then they need to be copied back, to reverse engineer the operations that happened before.
					newSequence.addWithCopying(metadata, matchedOperations.get(ii));
				}
				else {
					if (firstOperation.isCombiningOperation() && firstOperation.isCombiningOperationFor(prevMarking)) { // If an operation combines time-stamps from two files, then the files need to be split again, to reverse engineer the operations that happened before.
						Sequence otherFileSequence = newSequence.addSplit(metadata, matchedOperations.get(ii));
						entry.add(otherFileSequence);
					}
					else { // Operation does not copy time-stamps or combine time-stamps from different files.
						newSequence.add(metadata, matchedOperations.get(ii));
					}
				}
				if (ii != 0) {
					entry.add(newSequence);
				}
			}
		}
	}

	public static ArrayList<ArrayList<Operation>> fillList(ArrayList<Operation> list, FileMetadata metadata, Marking prevMarking) {
		ArrayList<ArrayList<Operation>> matchedOperations = new ArrayList<>(); // 2 dimensional arraylist, the operations are put in lists of operations that have the same effect on meta-data. The last list is kept empty.
		matchedOperations.add(new ArrayList<>());
		for (Operation operation : list) { // For every operation.
			if (operation.matches(metadata, prevMarking)) { // If operation matches the time-stamps and associated marking.
				for (ArrayList<Operation> timeMatchOperations : matchedOperations) { // For every list of matched operations.
					if (timeMatchOperations.isEmpty()) { // If this list is empty (is the last list).
						timeMatchOperations.add(operation);
						matchedOperations.add(new ArrayList<>());
						break;
					}
					if (timeMatchOperations.get(0).equalsForMarking(operation, prevMarking)) { // If the operation has the same effect on meta-data as another one for this marking, put them in the same list
						timeMatchOperations.add(operation);
						break;
					}
				}
			}
		}
		return matchedOperations;
	}
}
