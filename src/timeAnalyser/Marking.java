/**
 * 
 */
package timeAnalyser;

/**
 * Marking indicating which time-stamps are matched.
 * 
 * @author Jelle Bouma
 *
 */
public class Marking {
	private int bitmap = 0;
	
	public Marking() {
	}
	
	private Marking(int bitmap) {
		this.bitmap = bitmap;
	}
	
	public boolean isMarked(int index) {
		return (bitmap & (int)Math.pow(2, index)) == (int)Math.pow(2, index);
	}
	
	/**
	 * If every index marked in the parameter marking is also marked in this marking.
	 * @param marking The input marking that may or may not be eclipsed.
	 * @return If every index marked in the parameter marking is also marked in this marking.
	 */
	public boolean eclipses(Marking marking) {
		return ((bitmap & marking.bitmap) ^ marking.bitmap) == 0;
	}
	
	public boolean equals(Marking marking) {
		return bitmap == marking.bitmap;
	}
	
	/**
	 * Marks the specified index.
	 * @param index the index to be marked
	 */
	public void mark(int index) {
		bitmap |= (int)Math.pow(2, index);
	}
	
	/**
	 * Marks all marked indexes that are marked in the marking parameter.
	 * @param marking marking parameter
	 */
	public void mark(Marking marking) {
		bitmap |= marking.bitmap;
	}

	/**
	 * Unmarks the specified index.
	 * @param index
	 */
	public void unmark(int index) {
		bitmap ^= bitmap & (int)Math.pow(2, index);
	}
	
	/**
	 * Returns the marking that has all indexes marked which are marked in this marking but not in the parameter marking.
	 * @param marking The parameter marking.
	 * @return The marking that has all indexes marked which are marked in this marking but not in the parameter marking.
	 */
	public Marking getChangeFor(Marking marking) {
		return new Marking(marking.bitmap ^ (marking.bitmap & bitmap));
	}
	
	protected Marking clone() {
		return new Marking(bitmap);
	}
	
	public boolean isFullyMarked() {
		return bitmap == 255;
	}
	
	public boolean isUnmarked() {
		return bitmap == 0;
	}
	
	
}
