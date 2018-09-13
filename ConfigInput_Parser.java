import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/*
 * Raman Sathiapalan, Anshika Singh, Usuma Thet
 * CS 6378.001
 * Project 1
 * Due: September 18, 2018
 */

public class ConfigInput_Parser {
	// Initialize local variables
	int num_nodes = 0;
	String[][] info_nodes;

	public static void main(String[] args) {
		ConfigInput_Parser main = new ConfigInput_Parser();

		File config_file = new File(
				"C:\\Users\\hinam\\Documents\\2018 Fall - CS 6378.001 - Advanced Operating System\\SampleInput.txt");
		main.ReadInput(config_file); // Run method.

	}

	public void ReadInput(File config_file) { 
		try {
			// Initialize function variables
			boolean firstlinepassed = false;
			int lineCount = 0;
			String[] temp_splitarr;
			String hostname = "";
			String node;
			String node_neighbors;

			BufferedReader br = new BufferedReader(new FileReader(config_file));
			String temp_filerow;
			String temp_line;

			while ((temp_filerow = br.readLine()) != null) {
				if (!temp_filerow.isEmpty()) { // Ignore empty lines
					
					// REQ: Only lines which begin with an unsigned integer are considered to be valid.
					if (isInteger(temp_filerow.substring(0, 1))) {
						// REQ: The first valid line of the configuration file contains one token denoting the number of nodes in the system.
						if (firstlinepassed == false) {
							num_nodes = Integer.parseInt(temp_filerow.substring(0, 1));
							info_nodes = new String[num_nodes][4];
							firstlinepassed = true;

						} else { // first line has passed
							if (lineCount < num_nodes) {
								// Populate array containing node information
								temp_splitarr = temp_filerow.split(" ");
								hostname = ""; // clear contents
								for (int i = 1; i < temp_splitarr.length - 1; i++) {
									hostname += temp_splitarr[i].trim();
								}

								// Assign hostname and port to node
								info_nodes[lineCount][0] = temp_splitarr[0].trim();
								info_nodes[lineCount][1] = hostname;
								info_nodes[lineCount][2] = temp_splitarr[temp_splitarr.length - 1].trim();

								lineCount++;
							} else {
								node = temp_filerow.substring(0, 1);
								node_neighbors = temp_filerow.substring(1).trim().replace(" ", ", "); 
								for (int i = 0; i < info_nodes.length; i++) {
									if (info_nodes[i][0].equals(node)) {
										info_nodes[i][3] = node_neighbors;
										System.out.println("Node: " + info_nodes[i][0] + "\nhostname: "
												+ info_nodes[i][1] + "\nport: " + info_nodes[i][2] + "\nNeighbors: "
												+ info_nodes[i][3] + "\n\n");

										break; // Exit loop
									}
								}
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// FUNCTION. Sting input. Boolean output. Check whether inputed string is a
	// valid integer value.
	public static boolean isInteger(String input) {
		boolean isValidInteger = false;
		try {
			Integer.parseInt(input);
			isValidInteger = true;

		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isValidInteger;
	}

}
