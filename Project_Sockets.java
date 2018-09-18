import java.io.*;
import java.net.*;
import java.util.*;

public class Project_Sockets {

	/*
	 * Initialize local variables. Each dc machine (node) will have a unique
	 * combination of host name, port, and node neighbors
	 */
	static String nodeHostName = null;
	static int nodePortNumber = 0;
	static String nodeNeighbors = null;
	static String[] nodeNeighborsArray;
	static int nodeNumber = 0;
	static int totalNodes = 0;

	static Socket server = null;
	static ArrayList<Socket> socClientsArray = new ArrayList<Socket>();
	static int[] neighborHopArray;

	static int numTimesUpdated = 0;

	// local variables for ReadInput function
	int num_nodes = 0;
	String[][] info_nodes;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MAIN
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		Project_Sockets n1 = new Project_Sockets(); // Initialize class

		File config_file = new File("SampleInput.txt");
		// Check if configuration file is available
		if (config_file.exists() == true) {
			System.out.println("Configuration file for input found.");
		} else {
			System.out.println("Configuration file for input not found.");
			System.exit(0); // Terminate code
		}

		// Run ReadInput function. Outputs cleaned configuration file contents in 2d
		// array
		String[][] info_nodes = n1.ReadInput(config_file);
		totalNodes = info_nodes.length; // Determine total number of nodes

		try {
			// Capture host name of dc machines (node)
			nodeHostName = InetAddress.getLocalHost().getCanonicalHostName();

			//nodeHostName = "dc02.utdallas.edu"; // DELETE. FOR TESTING ONLY
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		/*
		 * Identify node # running code. Configuration file information is partially
		 * extracted according to the node number.
		 */
		for (int i = 0; i < info_nodes.length; i++) {
			if (info_nodes[i][1].equals(nodeHostName)) { // Match node based on host name
				nodeNumber = Integer.parseInt(info_nodes[i][0]);
				nodePortNumber = Integer.parseInt(info_nodes[i][2]);
				nodeHostName = info_nodes[i][1];
				nodeNeighbors = info_nodes[i][3];
				break;
			}
		}

		/*
		 * Check if node information is initialized. Exit code if dc machine (node) not
		 * identified in configuration file
		 */
		if (nodePortNumber == 0) {
			System.out.println(
					"\nCould not find host name in the configuration file or port number does not match. Exiting. . .");
			System.exit(0); // Terminate code
		} else {
			System.out.println("\nHost " + nodeHostName + " on port #" + nodePortNumber
					+ " initialized.\nWelcome, Node #" + nodeNumber + "!");

			System.out.println("\n-------------------------------------\n");

			// Initialize neighborHopArray
			neighborHopArray = new int[info_nodes.length];
			Arrays.fill(neighborHopArray, 1000); // 1000 distance assigned for non-neighbor nodes
			neighborHopArray[nodeNumber] = 0; // 0 distance assigned for own node

			nodeNeighborsArray = nodeNeighbors.split(" ");
			for (int i = 0; i < nodeNeighborsArray.length; i++) {
				neighborHopArray[Integer.parseInt(nodeNeighborsArray[i])] = 1; // 1 distance assigned for neighbor nodes
			}
			System.out.println("Initial k-hop neighbors: " + Arrays.toString(neighborHopArray));

		}

		
		// TO DO: set up servers and clients
		// TO DO: Handling algorithm
		
		
		
		// Print final k-hop neighbors
		System.out.println("\n-------------------------------------\n");
		System.out.println("Final k-hop neighbors: " + Arrays.toString(neighborHopArray));
		System.out.println("Code complete. Exiting. . .");

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FUNCTIONS
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String[][] ReadInput(File config_file) {
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

					// Only lines which begin with an unsigned integer are considered to be valid.
					if (isInteger(temp_filerow.substring(0, 1))) {
						// REQ: The first valid line of the configuration file contains one token
						// denoting the number of nodes in the system.
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
								node_neighbors = temp_filerow.substring(1).trim();
								for (int i = 0; i < info_nodes.length; i++) {
									if (info_nodes[i][0].equals(node)) {
										info_nodes[i][3] = node_neighbors;

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
		return info_nodes;
	}

	/*
	 * FUNCTION. Sting input. Boolean output. Check whether inputed string is a
	 * valid integer value.
	 */
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
