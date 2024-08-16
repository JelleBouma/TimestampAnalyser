# Time-stamp Analyser
Each file in NTFS (the Windows filesystem) has eight time-stamps.
The Time-stamp Analyser aids forensic investigators by comparing the time-stamps of a file to known effects of file operations to determine what could have happened at those times.

# The problem solved by this tool
Traditionally analysis of time-stamps was done by hand where rules would be derived from known effects of file operations.
When operations need to be added or removed from the operation overview (for example, due to a Windows update), then every rule will need to be re-evaluated.
Furthermore, comparing these rules with time-stamps is still a complex process which can easily lead to human error, due to the large amount of possible time-stamp changes that need to be considered.

To solve both these problems, the Time-stamp Analyser automatically analyses the time-stamps in an NTFS MFT to reverse-engineer what could have happened at those times.

# Usage
The Time-stamp Analyser takes an MFT (Master File Table) as input and compares these to the known effects of file operations in `OperationList.java`

## Parameters
- input MFT file
- output file
- (optional) MFT entry size in bytes, default is 1024
- (optional) filter: `all` (default), `deleted` (only deleted files), `irregular` only files with time-stamps that don't match any normal file operation.
- (optional) priority: `regular` to consider forgery operations only when non-forgery file operations can not be matched (default), `equal` to consider forgery file operations always. 
- (optional) list of indexes or file names to be analysed seperated by `|`. By default every file is analysed.


# More information
Paper regarding this method and tool here: https://dl.acm.org/doi/fullHtml/10.1145/3600160.3605027
Even more information can be found in my thesis: https://www.open.ou.nl/hjo/supervision/2019-jelle.bouma-bsc-thesis.pdf

Furthermore, any questions can be asked in the Issues section or in an e-mail to me.
