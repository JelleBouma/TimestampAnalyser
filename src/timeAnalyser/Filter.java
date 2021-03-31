/**
 * 
 */
package timeAnalyser;

/**
 * @author Jelle Bouma
 *
 */
public enum Filter {
	DELETED,
	IRREGULAR,
	ALL;
	
	static Filter getFilter(String readFilterString) {
		if(readFilterString.equalsIgnoreCase("deleted")) {
			return DELETED;
		}
		if(readFilterString.equalsIgnoreCase("irregular")) {
			return IRREGULAR;
		}
		if(readFilterString.equalsIgnoreCase("all")) {
			return ALL;
		}
		return null;
	}
}
