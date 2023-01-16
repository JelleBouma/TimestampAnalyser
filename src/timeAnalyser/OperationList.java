package timeAnalyser;

import java.util.ArrayList;

/**
 * 
 */

/**
 * Holds all file operations.
 * The current list of file operations and their effects on time-stamps is the main result of Jelle Bouma's thesis Computer Science.
 * File operations should be added if future versions of Windows introduce new (modifications to) file operations or currently (2019) unknown file operations are uncovered.
 * @author Jelle Bouma
 *
 */
public class OperationList {
	ArrayList<Operation> operations = new ArrayList<>();
	ArrayList<Operation> forgeryOperations = new ArrayList<>();
	ArrayList<Operation> allOperations = new ArrayList<>();
	
	public OperationList() {
		operations.add(new Operation("Create", ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START));
		operations.add(new Operation("Create with file tunneling", ResultType.TNL, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.TNL, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START));
		
		operations.add(new Operation("Copy", ResultType.OP_START, ResultType.SRC, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 0));
		operations.add(new Operation("Copy from FAT volume", ResultType.OP_START, ResultType.SRC_FATR_W, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 1));
		operations.add(new Operation("Copy with last access update enabled", ResultType.OP_START, ResultType.SRC, ResultType.OP_END, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 0));
		operations.add(new Operation("Copy with file tunneling", ResultType.TNL, ResultType.SRC, ResultType.OP_END, ResultType.OP_START, ResultType.TNL, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 0));
		operations.add(new Operation("Copy with quirk", ResultType.OP_START, ResultType.SRC, ResultType.SRC, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 0));
		operations.add(new Operation("Copy from FAT volume with last access update enabled", ResultType.OP_START, ResultType.SRC_FATR_W, ResultType.OP_END, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 1));
		operations.add(new Operation("Copy with last access update enabled and file tunneling", ResultType.TNL, ResultType.SRC, ResultType.OP_END, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 0));
		operations.add(new Operation("Copy from FAT volume with last access update enabled and file tunneling", ResultType.TNL, ResultType.SRC_FATR_W, ResultType.OP_END, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 1));
		
		operations.add(new Operation("Update", ResultType.U, ResultType.OP_END, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, -1, -1));
		operations.add(new Operation("Update directory", ResultType.U, ResultType.OP_END, ResultType.OP_START, ResultType.OP_END, ResultType.U, ResultType.U, ResultType.U, ResultType.U, -1, 1));
		operations.add(new Operation("Update with last access update enabled", ResultType.U, ResultType.OP_END, ResultType.OP_START, ResultType.OP_END, ResultType.U, ResultType.U, ResultType.U, ResultType.U, -1, -1));
		
		operations.add(new Operation("Move in the same volume", ResultType.U, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC));
		operations.add(new Operation("Move in the same volume with file tunneling", ResultType.TNL, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC));
		
		operations.add(new Operation("Move from another volume", ResultType.SRC, ResultType.SRC, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 1));
		operations.add(new Operation("Move from FAT volume", ResultType.SRC_FATR_C, ResultType.SRC_FATR_W, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 1));
		operations.add(new Operation("Move from another volume with last access update enabled", ResultType.SRC, ResultType.SRC, ResultType.OP_END, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 1));
		operations.add(new Operation("Move from another volume with quirk", ResultType.SRC, ResultType.SRC, ResultType.SRC, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 1));
		operations.add(new Operation("Move from FAT volume with last access update enabled", ResultType.SRC_FATR_C, ResultType.SRC_FATR_W, ResultType.OP_END, ResultType.OP_END, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 1));
		
		operations.add(new Operation("Overwriting copy", ResultType.U, ResultType.SRC, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0, -1));
		operations.add(new Operation("Overwriting copy from FAT volume", ResultType.U, ResultType.SRC_FATR_W, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 1, -1));
		operations.add(new Operation("Overwriting copy with last access update enabled", ResultType.U, ResultType.SRC, ResultType.OP_START, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0, -1));
		
		operations.add(new Operation("Overwriting move from another volume", ResultType.SRC, ResultType.SRC, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0, -1));
		operations.add(new Operation("Overwriting move from FAT volume", ResultType.SRC_FATR_C, ResultType.SRC_FATR_W, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 1, -1));
		operations.add(new Operation("Overwriting move with last access update enabled", ResultType.SRC, ResultType.SRC, ResultType.OP_START, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 0, -1));
		operations.add(new Operation("Overwriting move from FAT volume with last access update enabled", ResultType.SRC_FATR_C, ResultType.SRC_FATR_W, ResultType.OP_START, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, 1, -1));

		operations.add(new Operation("File name change", ResultType.U, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC));
		operations.add(new Operation("File name change with file tunneling", ResultType.TNL, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC, ResultType.SI_SRC));
		
		operations.add(new Operation("Attribute change", ResultType.U, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U, ResultType.U));
		
		operations.add(new Operation("Extract zip file", ResultType.R_W_P_TZD, ResultType.R_W_P_TZD, ResultType.OP_END, ResultType.R_W_P_TZD, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, ResultType.OP_START, 0, -1));

		operations.add(new Operation("Access with last access update enabled", ResultType.U, ResultType.U, ResultType.U, ResultType.OP_START, ResultType.U, ResultType.U, ResultType.U, ResultType.U));
		
		// The list of forgery operations, only forgery operations that can be matched to a time are included.
		forgeryOperations.add(new Operation("Use of a time-stamp change tool", ResultType.ANY, ResultType.ANY, ResultType.OP_START, ResultType.ANY, ResultType.U, ResultType.U, ResultType.U, ResultType.U));
		forgeryOperations.add(new Operation("Use of a time-stamp change tool which rounds on seconds", ResultType.R_ANY, ResultType.R_ANY, ResultType.OP_START, ResultType.R_ANY, ResultType.U, ResultType.U, ResultType.U, ResultType.U));

		allOperations.addAll(operations);
		allOperations.addAll(forgeryOperations);
	}
	
}
