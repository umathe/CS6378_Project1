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

		
		int nodeNumber1 = n1.nodeNumber;
		int nodePortNumber1 = nodePortNumber; 
		
		System.out.println(nodeNeighbors);
		String[] nodeNeighborsArray = nodeNeighbors.split(" ");
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				n1.neighborHopArray = new int[info_nodes.length];

				for(int i = 0; i < n1.neighborHopArray.length; i++) {
					for(int j = 0; j < nodeNeighborsArray.length; j++) {
						if(Integer.parseInt(nodeNeighborsArray[j])== i) {
							n1.neighborHopArray[i] = 1; 
						}
					}
				}
				
				n1.neighborHopArray[nodeNumber1] = 0;
				
				for(int i = 0; i < n1.neighborHopArray.length; i++) {
					if(i == nodeNumber1){
						n1.neighborHopArray[i] = 0;
					}
					else if(n1.neighborHopArray[i] != 1){
						n1.neighborHopArray[i] = 1000;
					}
					System.out.print(n1.neighborHopArray[i]+ "\t");
					System.out.println();
				}
				
				n1.setServer(nodePortNumber1, info_nodes.length, nodeNeighborsArray.length);
			}
		});

		Timer delayInterval = new Timer();
		TimerTask taskClientDelay = new TimerTask() {
			public void run() {
				for(int i= 0; i<nodeNeighborsArray.length; i++) {
					for(int j = 0; j<info_nodes.length; j++) {
						if(info_nodes[j][0].equals(nodeNeighborsArray[i])) {
							n1.setClient(info_nodes[j][1], Integer.parseInt(info_nodes[j][2]));
						}
					}
				}
			}
		};
		
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				delayInterval.schedule(taskClientDelay, 5000);
			}
		});
		t1.start();
		t2.start();
		
		
		
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
	
	public void setServer(int nodePortNumber, int totalNodes, int nodeNeighborsNumber) {
			
	ServerSocket ssoc = null;
	//Socket soc;
		try {
			ssoc = new ServerSocket(nodePortNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int counter = 0;
		while(true) {
			try {
				Socket soc = ssoc.accept(); 
				this.socClientsArray.add(soc);
				System.out.println("Client is accepted. Address: " + soc.getInetAddress() + " Port: "+soc.getPort() );
				counter++; 
				if(counter == nodeNeighborsNumber){
					String line = "Please send your initial k-hop array";
					int i = 0;
					
					int interval = nodeNumber*totalNodes*10+50;
					for(i = 0; i < (totalNodes*totalNodes); i++){
						//serverCommunicate();
						try {
							Thread.sleep(interval);
							serverCommunicate(line);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					
					if(i == (totalNodes*totalNodes)){
						try {
							Thread.sleep(interval);
							line = "Done";
							serverCommunicate(line);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void serverCommunicate(String line) {
		for(int i = 0; i< this.socClientsArray.size(); i++){
			Socket currentSoc = this.socClientsArray.get(i);
			try {
				DataOutputStream out = new DataOutputStream(currentSoc.getOutputStream());
				out.writeUTF(line);
				ObjectInputStream in = new ObjectInputStream (currentSoc.getInputStream());
				int[] line1 =(int[])in.readObject();
				updateNeighborHops(neighborHopArray, line1);
				for(int k=0; k<neighborHopArray.length; k++){
					System.out.print(neighborHopArray[k] + "\t");
				}
				System.out.println();

			} catch (EOFException exception) {
				try {
					currentSoc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}  catch (IOException | ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}
	public void updateNeighborHops(int me[], int myNeighbor[])
	{
		int n = info_nodes.length;
		for(int i=0; i<n; i++)
		{
			if(me[i]>1)
			{
				if(myNeighbor[i]>0 && myNeighbor[i]<me[i])
				{
					me[i] = myNeighbor[i] + 1;
				}
			}
		}
		this.neighborHopArray = me;
	}
	public void setClient(String nodeHostName, int nodePortNumber) {
		int[] currentHopArray = this.neighborHopArray;
		try {
			final Socket clientSocket = new Socket(nodeHostName, nodePortNumber);
			DataInputStream in = null;
			ObjectOutputStream out = null;
			while(true) {
				try {
					in = new DataInputStream(clientSocket.getInputStream());
					String line = in.readUTF();
					if(line.equalsIgnoreCase("Done")){
						clientSocket.close();
						System.out.println("Connection closed"); 
						break;
					}
					out = new ObjectOutputStream(clientSocket.getOutputStream());
					out.writeObject(currentHopArray);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
