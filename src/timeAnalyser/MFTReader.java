package timeAnalyser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * 
 */

/**
 * The MFTReader solves, reads and parses the MFT, with or without filters.
 * Does not support MFTs with over 2^31 entries.
 * @author Jelle Bouma
 *
 */
public class MFTReader {
	
	File mft;
	int entrySize;
	Filter filter = Filter.ALL;
	String[] fileNameFilter;
	boolean hasFileNameFilter = false;
	ArrayList<Integer> indexFilter;
	boolean hasIndexFilter = false;
	
	/**
	 * Constructs an MFT reader for the specified MFT and its entry size.
	 * @param mft the MFT to read and parse
	 * @param entrySize the size of an entry in the specified MFT file
	 */
	MFTReader(File mft, int entrySize) {
		this.mft = mft;
		this.entrySize = entrySize;
	}
	
	/**
	 * Constructs an MFT reader for the specified MFT, its entry size and a filter.
	 * @param mft the MFT to read and parse
	 * @param entrySize the size of an entry in the specified MFT file
	 * @param filter the filter to use when reading the MFT
	 */
	MFTReader(File mft, int entrySize, Filter filter) {
		this.mft = mft;
		this.entrySize = entrySize;
		this.filter = filter;
	}
	
	/**
	 * Constructs an MFT reader for the specified MFT, its entry size, a filter and an additional filter which specifies the indexes of the MFT entries that should be read and parsed.
	 * Reading entries with this MFTReader has the same complexity per entry as reading all entries.
	 * This does not read parent directories of the specified indexes which means that the file paths can not be reconstructed.
	 * @param mft the MFT to read and parse
	 * @param entrySize the size of an entry in the specified MFT file
	 * @param filter the filter to use when reading the MFT
	 * @param entriesToRead the indexes of the MFT entries that should be read
	 */
	MFTReader(File mft, int entrySize, Filter filter, String[] entriesToRead) {
		this.mft = mft;
		this.entrySize = entrySize;
		this.filter = filter;
		fileNameFilter = entriesToRead;
		hasFileNameFilter = true;
	}
	
	/**
	 * Constructs an MFT reader for the specified MFT, its entry size, a filter and an additional filter which specifies the names of the files that should be fully read and parsed.
	 * Entries with different file names will be partially read as some solving of the MFT is required to acquire file names, they will not have their $FN time-stamps parsed.
	 * @param mft the MFT to read and parse
	 * @param entrySize the size of an entry in the specified MFT file
	 * @param filter the filter to use when reading the MFT
	 * @param entriesToRead the names of the MFT entries that should be read
	 */
	MFTReader(File mft, int entrySize, Filter filter, ArrayList<Integer> entriesToRead) {
		this.mft = mft;
		this.entrySize = entrySize;
		this.filter = filter;
		Collections.sort(entriesToRead);
		indexFilter = entriesToRead;
		hasIndexFilter = true;
	}
	
	
	/**
	 * Reads the MFT sequentially and parses the entries accordingly with the filter parameters this MFTReader was constructed with.
	 * @return all entries, unless an index filter is used. In that case only the entries with those indexes are returned.
	 * @throws IOException
	 */
	Entry[] read() throws IOException {
		FileInputStream inputStream = new FileInputStream(mft);
		Entry[] entries;
		if (hasIndexFilter) {
			inputStream.skip(indexFilter.get(0) * entrySize);
			entries = new Entry[indexFilter.size()];
			for (int ii = 0; ii < entries.length; ii++) {
				if (ii != 0) {
					inputStream.skip((indexFilter.get(ii) - indexFilter.get(ii - 1)) * entrySize);
				}
				byte[] entryBytes = new byte[entrySize];
				inputStream.read(entryBytes);
				entries[ii] = parseEntry(entryBytes, indexFilter.get(ii));;
			}
		}
		else {
			entries = new Entry[(int) (mft.length() / entrySize)]; // array as big as there are entries, will give problems if the MFT has more than 2^31 entries
			for (int ii = 0; ii < entries.length; ii++) { // until the entries array is filled
				byte[] entryBytes = new byte[entrySize];
				inputStream.read(entryBytes); // read bytes of an entry from the MFT
				entries[ii] = parseEntry(entryBytes, ii);; // parse the bytes of the entry
			}
		}
		inputStream.close();
		return entries;
	}
	
	/**
	 * Parses an entry from an array of bytes accordingly with the filter parameters this MFTReader was constructed with.
	 * @param bytes the bytes of the MFT entry, bytes.length should be equal to entrySize
	 * @param index the index of the entry
	 * @return an Entry object parsed from the bytes
	 * @throws UnsupportedEncodingException
	 */
	Entry parseEntry(byte[] bytes, int index) throws UnsupportedEncodingException {
		boolean signatureIntact = ByteBuffer.wrap(bytes, 0, 4).getInt() == 0x46494C45; // checks if the signature of the entry indicates it is intact ("FILE")
		Entry entry = new Entry(index, signatureIntact);
		if (signatureIntact) {
			entry.setNTFSSequenceNumber(wrapAndOrder(bytes, 0x10, 2).getShort());
			int attributeOffset = wrapAndOrder(bytes, 0x14, 2).getShort();
			FileMetadata metadata = new FileMetadata();
			
			// the bytes at offset 0x16 hold the flags which determine if a file is deleted and/or a directory.
			metadata.setDirectory(wrapAndOrder(bytes, 0x16, 2).getShort() > 0x01);
			boolean isDeleted = wrapAndOrder(bytes, 0x16, 2).getShort() % 0x02 == 0x00;
			
			metadata.setDeleted(isDeleted);
			if (isDeleted || filter != Filter.DELETED) {
				while (attributeOffset < bytes.length) {
					int attributeType = wrapAndOrder(bytes, attributeOffset, 4).getInt();
					if (attributeType == 0xFFFFFFFF) { // end of attribute list marker
						break;
					}
					int attributeSize = wrapAndOrder(bytes, attributeOffset + 0x4, 4).getInt();
					int contentOffset = attributeOffset + wrapAndOrder(bytes, attributeOffset + 0x14, 2).getShort();
					attributeOffset += attributeSize; // find next attribute
					if(attributeOffset > 1024 || attributeOffset < 0) {
						break;
					}
					if (attributeType == 0x00000010) { // $STANDARD_INFORMATION
						parseStandardInformation(bytes, contentOffset, metadata);
					}
					if (attributeType == 0x00000030) { // $FILE_NAME
						parseFileName(bytes, contentOffset, attributeOffset, metadata);
					}
				}
			}
			entry.setFileMetadata(metadata);
		}
		return entry;
	}
	
	/**
	 * Parses the $STANDARD_INFORMATION attribute from an array of bytes containing a file entry.
	 * @param bytes the bytes of the MFT entry, bytes.length should be equal to entrySize
	 * @param contentOffset the offset to the contents of the $STANDARD_INFORMATION attribute
	 * @param metadata the meta-data object to write to.
	 */
	private void parseStandardInformation(byte[] bytes, int contentOffset, FileMetadata metadata) {
		Timestamp[] si = new Timestamp[4];
		for (int ii = 0; ii < si.length; ii++) {
			si[ii] = new Timestamp(wrapAndOrder(bytes, contentOffset + ii * 8, 8).getLong());
		}
		metadata.setSI(si);
	}
	
	/**
	 * Parses the $FILE_NAME attribute from an array of bytes containing a file entry.
	 * @param bytes the bytes of the MFT entry, bytes.length should be equal to entrySize
	 * @param contentOffset the offset to the contents of the $FILE_NAME attribute
	 * @param endOffset the offset to the end of the $FILE_NAME attribute
	 * @param metadata the meta-data object to write to.
	 */
	private void parseFileName(byte[] bytes, int contentOffset, int endOffset, FileMetadata metadata) throws UnsupportedEncodingException {
		metadata.setParentID(wrapAndOrder(bytes, contentOffset, 4).getInt()); // parent directory 
		Timestamp[] fn = new Timestamp[4];
		for (int ii = 0; ii < fn.length; ii++) {
			fn[ii] = new Timestamp(wrapAndOrder(bytes, contentOffset + ii * 8 + 8, 8).getLong());
		}
		int fileNameOffset = contentOffset + 0x42;
		String name = new String(Arrays.copyOfRange(bytes, fileNameOffset, endOffset), "UTF-16LE").trim();
		if (hasFileNameFilter) {
			for (String fileName : fileNameFilter) {
				if (fileName.equalsIgnoreCase(name)) {
					metadata.setFN(fn);
				}
			}
		}
		else {
			metadata.setFN(fn);
		}
		metadata.setName(name);
	}
	
	/**
	 * Help method to read little endian byte order numbers from the MFT.
	 * Wraps a byte array to a buffer, using little endian byte order.
	 * @param bs the byte array that will back the new buffer
	 * @param off offset to the bytes that will be wrapped
	 * @param len length, amount of bytes to be wrapped
	 * @see ByteBuffer
	 */
	private ByteBuffer wrapAndOrder(byte[] bs, int off, int len) {
		return ByteBuffer.wrap(bs, off, len).order(ByteOrder.LITTLE_ENDIAN);
	}
	
}
