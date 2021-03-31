/**
 * 
 */
package test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import timeAnalyser.*;

/**
 * @author Jelle Bouma
 *
 */
public class OperationTest {
	
	private final int AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE = 4;
	Operation creation = new Operation("Create", ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START);
	FileMetadata metadataCausedByCreation = new FileMetadata();
	Timestamp[] timestampsCausedByCreationSI = new Timestamp[AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE];
	Timestamp[] timestampsCausedByCreationFN = new Timestamp[AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE];

	
	@Before
	public void initialiseCreationOperationAndMetadata() {
		long TIME_VALUE = 132061996440000000L;
		for(int ii = 0; ii < AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE; ii++) {
			timestampsCausedByCreationSI[ii] = new Timestamp(TIME_VALUE);
			timestampsCausedByCreationFN[ii] = new Timestamp(TIME_VALUE);
		}
		metadataCausedByCreation.setSI(timestampsCausedByCreationSI);
		metadataCausedByCreation.setFN(timestampsCausedByCreationFN);
	}
	
	@After
	public void clearVariables() {
		creation = new Operation("Create", ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START);
		metadataCausedByCreation = new FileMetadata();
		timestampsCausedByCreationSI = new Timestamp[AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE];
		timestampsCausedByCreationFN = new Timestamp[AMOUNT_OF_TIMESTAMPS_PER_ATTRIBUTE];
	}
	
	/**
	 * Test method for {@link timeAnalyser.Operation#matches(timeAnalyser.FileMetadata, timeAnalyser.Marking)}.
	 * Time-stamps that are the result of the create file operation are compared to the creation file operation, they should match.
	 */
	@Test
	public void timestamps_Should_MatchOperation_ThatCausedThem() {
		Marking marking = new Marking();
		assertTrue("Time-stamps did not match the causing creation operation for empty marking. Likely there is something wrong with matches()", creation.matches(metadataCausedByCreation, marking));
		marking.mark(4);
		marking.mark(7);
		assertTrue("Time-stamps did not match the causing creation operation for some marking. Likely there is something wrong with matches()", creation.matches(metadataCausedByCreation, marking));
		marking.mark(creation.getMarking());
		assertFalse("Time-stamps matched an operation, while every time-stamp has been marked. Likely there is something wrong with matches()", creation.matches(metadataCausedByCreation, marking));
	}
	
	/**
	 * Test method for {@link timeAnalyser.Operation#matches(timeAnalyser.FileMetadata, timeAnalyser.Marking)}.
	 * Time-stamps that are the result of the create file operation are compared to the file name change operation, they should not match.
	 */
	@Test
	public void timestamps_ShouldNot_MatchAnOperation_ThatCouldNotHaveCausedThem() {
		Operation fileNameChange = new Operation("File name change", ResultType.U, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC);
		Marking marking = new Marking();
		assertFalse("Time-stamps matched file name change operation while the file has only been created. Likely there is something wrong with matches()", fileNameChange.matches(metadataCausedByCreation, marking));
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#hasCopiedTimestamps()}.
	 */
	@Test
	public void fileNameChange_Should_HaveTimestampCopying() {
		Operation fileNameChange = new Operation("File name change", ResultType.U, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC);
		assertTrue("The file name change operation copies time-stamps from $SI to $FN, hasCopying() should be true.", fileNameChange.hasCopying());
	}
	
	/**
	 * Test method for {@link timeAnalyser.Operation#hasCopiedTimestamps()}.
	 */
	@Test
	public void createOperation_ShouldNot_HaveTimestampCopying() {
		Operation creation = new Operation("Create", ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START);
		assertFalse("the create operation does not copy time-stamps, hasCopying() should be false", creation.hasCopying());
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#hasRangeResultFor(timeAnalyser.Marking)}.
	 */
	@Test
	public void createOperation_ShouldNever_HaveDurationResult() {
		Marking marking = new Marking();
		assertFalse("The creation operation only changes time-stamps to time of operation, this should not result in a duration of time-stamps for an empty marking.", creation.hasDurationResultFor(marking));
		marking.mark(2);
		marking.mark(4);
		assertFalse("The creation operation only changes time-stamps to time of operation, this should not result in a duration of time-stamps for a marking.", creation.hasDurationResultFor(marking));
		marking.mark(creation.getMarking());
		assertFalse("The creation operation only changes time-stamps to time of operation, this should not result in a duration of time-stamps for a full marking.", creation.hasDurationResultFor(marking));
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#hasRangeResultFor(timeAnalyser.Marking)}.
	 */
	@Test
	public void createOperation_ShouldNever_HaveRangeResult() {
		Marking marking = new Marking();
		assertFalse("The creation operation matched a range of time-stamps for an empty marking. Likely there is something wrong with hasRangeResultFor()", creation.hasRangeResultFor(marking));
		marking.mark(4);
		marking.mark(7);
		assertFalse("The creation operation matched a range of time-stamps for a marking. Likely there is something wrong with hasRangeResultFor()", creation.hasRangeResultFor(marking));
		marking.mark(creation.getMarking());
		assertFalse("The creation operation matched a range of time-stamps for a full marking. Likely there is something wrong with hasRangeResultFor()", creation.hasRangeResultFor(marking));
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#getDuration(timeAnalyser.FileMetadata, timeAnalyser.Marking)}.
	 */
	@Test
	public void testGetDuration() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#getRange(timeAnalyser.FileMetadata)}.
	 */
	@Test
	public void testGetRange() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#getTime(timeAnalyser.FileMetadata, timeAnalyser.Marking)}.
	 */
	@Test
	public void testGetTime() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#getSplitMarkings()}.
	 */
	@Test
	public void testGetSplitMarkings() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#isCreatingOperation()}.
	 */
	@Test
	public void create_Should_BeCreatingOperation() {
		Operation creation = new Operation("Create", ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START);
		assertTrue("the create operation is not seen as a creating operation. Likely there is something wrong with checking if the operation is creating.", creation.isCreatingOperation());
	}
	
	/**
	 * Test method for {@link timeAnalyser.Operation#isCreatingOperation()}.
	 */
	@Test
	public void fileNameChange_ShouldNot_BeCreatingOperation() {
		Operation fileNameChange = new Operation("File name change", ResultType.U, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC);
		assertFalse("the file name change operation is seen as a creating operation. Likely there is something wrong with checking if the operation is creating.", fileNameChange.isCreatingOperation());
	}

	/**
	 * Operations that overwrite files have time-stamps from the file being overwritten and the overwriting file, they should be combining operations as they combine time-stamps from two files.
	 * Test method for {@link timeAnalyser.Operation#isCombiningOperation()}.
	 */
	@Test
	public void overwritingOperations_Should_BeCombining() {
		Operation overwritingCopy = new Operation("Overwriting copy", ResultType.U, ResultType.SRC, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0);
		assertTrue("Overwriting copy has time-stamps from the file being overwritten and the overwriting file, yet is not seen as a combining operation. Likely there is something wrong with checking if the operation is combining.", overwritingCopy.isCombiningOperation());
		Operation overwritingMoveFromAnotherVolume = new Operation("Overwriting move from another volume", ResultType.SRC, ResultType.SRC, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0);
		assertTrue("Overwriting move from another volume has time-stamps from the file being overwritten and the overwriting file, yet is not seen as a combining operation. Likely there is something wrong with checking if the operation is combining.", overwritingMoveFromAnotherVolume.isCombiningOperation());
	}
	
	/**
	 * Test method for {@link timeAnalyser.Operation#isCombiningOperationFor(timeAnalyser.Marking)}.
	 */
	@Test
	public void overwritingOperations_Should_BeCombining_ForAnEmptyMarking() {
		Marking marking = new Marking();
		Operation overwritingCopy = new Operation("Overwriting copy", ResultType.U, ResultType.SRC, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0);
		assertTrue("Overwriting copy has time-stamps from the file being overwritten and the overwriting file, yet is not seen as a combining operation. Likely there is something wrong with checking if the operation is combining.", overwritingCopy.isCombiningOperationFor(marking));
		Operation overwritingMoveFromAnotherVolume = new Operation("Overwriting move from another volume", ResultType.SRC, ResultType.SRC, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0);
		assertTrue("Overwriting move from another volume has time-stamps from the file being overwritten and the overwriting file, yet is not seen as a combining operation. Likely there is something wrong with checking if the operation is combining.", overwritingMoveFromAnotherVolume.isCombiningOperationFor(marking));
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#isCombiningOperationFor(timeAnalyser.Marking)}.
	 */
	@Test
	public void overwritingOperations_ShouldNot_BeCombining_WhenTheTimestampsFromOneFileHaveAllBeenMarked() {
		Marking marking = new Marking();
		
		// by marking $SI.C and $SI.W neither "Overwriting copy" nor "Overwriting move from another volume" will have unmarked time-stamps from the overwriting file, making them not combining operations for this marking.
		marking.mark(0);
		marking.mark(1);
		
		Operation overwritingCopy = new Operation("Overwriting copy", ResultType.U, ResultType.SRC, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0);
		assertFalse("Overwriting copy has no time-stamps from the overwriting file for a marking, yet is seen as a combining operation for this marking. Likely there is something wrong with checking if the operation is combining.", overwritingCopy.isCombiningOperationFor(marking));
		Operation overwritingMoveFromAnotherVolume = new Operation("Overwriting move from another volume", ResultType.SRC, ResultType.SRC, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0);
		assertFalse("Overwriting move from another volume has no time-stamps from the overwriting file for this marking, yet is seen as a combining operation for this marking. Likely there is something wrong with checking if the operation is combining.", overwritingMoveFromAnotherVolume.isCombiningOperationFor(marking));
	}

	/**
	 * Test method for {@link timeAnalyser.Operation#equalsForMarking(timeAnalyser.Operation, timeAnalyser.Marking)}.
	 */
	@Test
	public void testEqualsForMarking() {
		fail("Not yet implemented");
	}

}
