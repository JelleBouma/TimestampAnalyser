/**
 * 
 */
package test;

import static org.junit.Assert.*;

import org.junit.Test;

import timeAnalyser.Marking;

/**
 * @author Jelle Bouma
 *
 */
public class MarkingTest {
	
	private final int TOTAL_AMOUNT_OF_TIMESTAMPS = 8;
	private final int MARKING_SIZE = TOTAL_AMOUNT_OF_TIMESTAMPS;

	/**
	 * Test method for {@link timeAnalyser.Marking#isMarked(int)}.
	 */
	@Test
	public void emptyMarking_ShouldNot_AppearMarked() {
		Marking marking = new Marking();
		boolean isMarked = false;
		for(int ii = 0; ii < MARKING_SIZE; ii++) {
			isMarked = isMarked && marking.isMarked(ii);
		}
		assertTrue("An empty marking returned true for isMarked(), there is likely a problem with isMarked().", isMarked);
	}

	/**
	 * Test method for {@link timeAnalyser.Marking#eclipses(timeAnalyser.Marking)}.
	 */
	@Test
	public void anyMarking_Should_EclipseEmptyMarking() {
		Marking eclipsingMarking = new Marking();
		assertTrue("An empty marking did not eclipse another empty marking, there is likely a problem with eclipses().", eclipsingMarking.eclipses(new Marking()));
		eclipsingMarking.mark(0);
		eclipsingMarking.mark(1);
		assertTrue("A marking did not eclipse an empty marking, there is likely a problem with eclipses().", eclipsingMarking.eclipses(new Marking()));
	}
	
	/**
	 * Test method for {@link timeAnalyser.Marking#eclipses(timeAnalyser.Marking)}.
	 */
	@Test
	public void MarkingWithoutFirstIndexMarked_ShouldNot_EclipseAnyMarking_WithFirstIndexMarked() {
		Marking eclipsingMarking = new Marking();
		Marking markingWithFirstIndexMarked = new Marking();
		markingWithFirstIndexMarked.mark(0);
		assertFalse("An empty marking eclipsed a marking with index 0 marked, there is likely a problem with eclipses().", eclipsingMarking.eclipses(markingWithFirstIndexMarked));
		eclipsingMarking.mark(1);
		eclipsingMarking.mark(7);
		eclipsingMarking.mark(5);
		assertFalse("A marking without index 0 marked eclipsed a marking with index 0 marked, there is likely a problem with eclipses().", eclipsingMarking.eclipses(markingWithFirstIndexMarked));
	}

	/**
	 * Test method for {@link timeAnalyser.Marking#mark(int)}.
	 */
	@Test
	public void testMarkInt() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Marking#mark(timeAnalyser.Marking)}.
	 */
	@Test
	public void testMarkMarking() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Marking#unmark(int)}.
	 */
	@Test
	public void testUnmark() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Marking#getChangeFor(timeAnalyser.Marking)}.
	 */
	@Test
	public void testGetChangeFor() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Marking#isFullyMarked()}.
	 */
	@Test
	public void testIsFullyMarked() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link timeAnalyser.Marking#isUnmarked()}.
	 */
	@Test
	public void testIsUnmarked() {
		fail("Not yet implemented");
	}

}
