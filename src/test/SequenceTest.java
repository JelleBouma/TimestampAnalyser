/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import timeAnalyser.ResultType;
import timeAnalyser.FileMetadata;
import timeAnalyser.Operation;
import timeAnalyser.Sequence;
import timeAnalyser.Timestamp;

/**
 * 
 * @author Jelle Bouma
 *
 */
public class SequenceTest {

	private final int AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE = 4;
	FileMetadata metadataCausedByCreate = new FileMetadata();
	final long TIME_VALUE = 132061996440000000L; // some time-stamp
	Timestamp[] timestampsSI = new Timestamp[AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE];
	Timestamp[] timestampsFN = new Timestamp[AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE];
	
	@Before
	public void initialiseMetadataAndItsTimestamps() {
		for(int ii = 0; ii < AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE; ii++) {
			timestampsSI[ii] = new Timestamp(TIME_VALUE);
			timestampsFN[ii] = new Timestamp(TIME_VALUE);
		}
		metadataCausedByCreate.setSI(timestampsSI);
		metadataCausedByCreate.setFN(timestampsFN);
	}
	
	@After
	public void clearMetadataAndItsTimestamps() {
		metadataCausedByCreate = new FileMetadata();
		timestampsSI = new Timestamp[AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE];
		timestampsFN = new Timestamp[AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE];
	}
	
	/**
	 * Test method for {@link timeAnalyser.Sequence#add(timeAnalyser.FileMetadata, java.util.ArrayList)}.
	 */
	@Test
	public void operations_Should_BeMatched_WithATimestamp_WhenTheyAreAdded() {
		Operation create = new Operation("Create", ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START);
		ArrayList<Operation> matchedCreate = new ArrayList<Operation>();
		matchedCreate.add(create);
		Sequence sequence = new Sequence(metadataCausedByCreate);
		sequence.add(metadataCausedByCreate, matchedCreate);
		assertTrue("Creation operation did not get matched to the correct time when added, something likely went wrong with adding the operation to sequence.", sequence.getTimeMatch(0).getTimestamps()[0].compare(timestampsSI[0]) == 0);
	}

	/**
	 * Test method for {@link timeAnalyser.Sequence#addWithCopying(timeAnalyser.FileMetadata, java.util.ArrayList)}.
	 * Tests correct copying back of time-stamps from $FN to $SI for the "File name change" file operation.
	 */
	@Test
	public void onlyUnmarkedCopiedTimestamps_Should_BeCopiedBack_ForACopyingOperation() {
		// initialise file name change operation
		Operation fileNameChange = new Operation("File name change", ResultType.U, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC);
		ArrayList<Operation> matchedFileNameChange = new ArrayList<Operation>();
		matchedFileNameChange.add(fileNameChange);
		
		// empty marking
		Sequence sequence = new Sequence(metadataCausedByCreate);
		sequence.addWithCopying(metadataCausedByCreate, matchedFileNameChange);
		FileMetadata metadataBeforeCopyingOperation = sequence.getEarliestMetadata();
		Timestamp[] timestampsInSIBeforeCopyingOperation = metadataBeforeCopyingOperation.getTimestamps().getSI();
		boolean copiedBackCorrectly = true;
		for(int ii = 0; ii < timestampsInSIBeforeCopyingOperation.length; ii++) { // all unmarked $FN time-stamps after file name change should be the same as $SI time-stamps before file name change
			copiedBackCorrectly = copiedBackCorrectly && timestampsFN[ii] == timestampsInSIBeforeCopyingOperation[ii];
		}
		assertTrue("Unmarked copied time-stamps were not copied back. Something likely went wrong with copying the timestamps back.", copiedBackCorrectly);
		
		sequence = new Sequence(metadataCausedByCreate);
		
		// adding test operation that marks $FN.W and $FN.E
		Operation operationThatMarksInFN = new Operation("Test operation", ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.OP_START, ResultType.OP_START, ResultType.U);
		ArrayList<Operation> matchedTestOperation = new ArrayList<Operation>();
		matchedTestOperation.add(operationThatMarksInFN);
		sequence.add(metadataCausedByCreate, matchedTestOperation);
		
		// $FN.W and $FN.E are marked so they should not be copied back.
		sequence.addWithCopying(sequence.getEarliestMetadata(), matchedFileNameChange);
		metadataBeforeCopyingOperation = sequence.getEarliestMetadata();
		timestampsInSIBeforeCopyingOperation = metadataBeforeCopyingOperation.getTimestamps().getSI();
		copiedBackCorrectly = true;
		copiedBackCorrectly = copiedBackCorrectly && timestampsSI[0] == timestampsInSIBeforeCopyingOperation[0];
		copiedBackCorrectly = copiedBackCorrectly && timestampsFN[1] == timestampsInSIBeforeCopyingOperation[1];
		copiedBackCorrectly = copiedBackCorrectly && timestampsFN[2] == timestampsInSIBeforeCopyingOperation[2];
		copiedBackCorrectly = copiedBackCorrectly && timestampsSI[3] == timestampsInSIBeforeCopyingOperation[3];
		assertTrue("Marked time-stamps were copied back for a copying operation. Something likely went wrong with copying the timestamps back.", copiedBackCorrectly);
	}

	/**
	 * Test method for {@link timeAnalyser.Sequence#addSplit(timeAnalyser.FileMetadata, java.util.ArrayList)}.
	 */
	@Test
	public void testAddSplit() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Sequence#isSplitting()}.
	 */
	@Test
	public void aSplitSequence_Should_BeSeenAsSuch() {
		
	}

}
