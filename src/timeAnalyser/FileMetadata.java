/**
 * 
 */
package timeAnalyser;

/**
 * The meta-data of a file, either read from an MFT or calculated.
 * @author Jelle Bouma
 *
 */
public class FileMetadata {
	
	private boolean isDirectory;
	private boolean isDeleted;
	private String fullPath = "";
	private String name = "";
	private Timestamps timestamps;
	private int onOtherVolume = -1;
	private boolean isSplitting = false;
	private int parentIndex = 5;
	
	public FileMetadata () {
		timestamps = new Timestamps();
	}

	/**
	 * @param isDirectory if the meta-data describes a directory
	 */
	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	
	/**
	 * @return if the meta-data describes a directory
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/**
	 * @return if the meta-data is marked as deleted
	 */
	public boolean isDeleted() {
		return isDeleted;
	}

	/**
	 * @param isDeleted if the meta-data is marked as deleted
	 */
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	/**
	 * @return the $STANDARD_INFORMATION time-stamps
	 */
	public Timestamps getTimestamps() {
		return timestamps;
	}

	/**
	 * @param si the $STANDARD_INFORMATION time-stamps to set
	 */
	public void setSI(Timestamp[] si) {
		timestamps.setSI(si);
	}
	
	/**
	 * @param fn the $FILE_NAME time-stamps to set
	 */
	public void setFN(Timestamp[] fn) {
		timestamps.setFN(fn);
	}
	
	public boolean hasName() {
		return !name.equals("");
	}
	
	/**
	 * @return the file name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the file name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public void setParentID(int parentIndex) {
		this.parentIndex = parentIndex;
	}
	
	public int getParentID() {
		return parentIndex;
	}
	
	public boolean hasPath() {
		return !fullPath.equals("");
	}
	
	public void setPath(String path) {
		fullPath = path;
	}
	
	public String getPath() {
		return fullPath;
	}

	protected FileMetadata clone() {
		FileMetadata clone = new FileMetadata();
		clone.setDirectory(isDirectory);
		clone.setDeleted(isDeleted);
		clone.setName(name);
		clone.timestamps = timestamps.clone();
		return clone;
	}
	
	FileMetadata clone(int otherVolume) {
		FileMetadata clone = clone();
		clone.onOtherVolume = otherVolume;
		return clone;
	}
	
	FileMetadata splitFrom(int otherVolume) {
		FileMetadata splitting = clone(otherVolume);
		splitting.isSplitting = true;
		return splitting;
	}
	
	boolean hasSIAndFN() {
		return timestamps.hasSIAndFN();
	}
	
	int onOtherVolume() {
		return onOtherVolume;
	}
	
	boolean isSplitting() {
		return isSplitting;
	}
	
}
