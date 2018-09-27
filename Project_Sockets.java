import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

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

	static Socket server = null;
	static ArrayList<Socket> socClientsArray = new ArrayList<Socket>();
	static int[] neighborHopArray;

	static int numTimesUpdated = 0;

	// local variables for ReadInput function
	static int num_nodes = 0; // total number of nodes
	String[][] info_nodes;
	
	//check number of updates
	static boolean serverUpdateFlag = true;
	static int clientthread = 0;
	static int updatesCounter = 0;

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

		try {
			// Capture host name of dc machines (node)
			nodeHostName = InetAddress.getLocalHost().getCanonicalHostName();
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

			System.out.println("\n--------------------------------------\n");
			
			// Initialize neighborHopArray
			neighborHopArray = new int[num_nodes];
			Arrays.fill(neighborHopArray, 1000); // 1000 distance assigned for non-neighbor nodes
			neighborHopArray[nodeNumber] = 0; // 0 distance assigned for own node
			
			nodeNeighborsArray = nodeNeighbors.trim().split(" +");
			for (int i = 0; i < nodeNeighborsArray.length; i++) {
				neighborHopArray[Integer.parseInt(nodeNeighborsArray[i].trim())] = 1; // 1 distance assigned for neighbor nodes
			}
			System.out.println("Initial k-hop neighbors: " + Arrays.toString(neighborHopArray));

		}

		
		int nodeNumber1 = nodeNumber;
		int nodePortNumber1 = nodePortNumber; 
		
		System.out.println(nodeNeighbors);
		String[] nodeNeighborsArray = nodeNeighbors.split(" ");
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				neighborHopArray = new int[info_nodes.length];

				for(int i = 0; i < neighborHopArray.length; i++) {
					for(int j = 0; j < nodeNeighborsArray.length; j++) {
						if(Integer.parseInt(nodeNeighborsArray[j])== i) {
							neighborHopArray[i] = 1; 
						}
					}
				}
				
				neighborHopArray[nodeNumber1] = 0;
				
				for(int i = 0; i < neighborHopArray.length; i++) {
					if(i == nodeNumber1){
						neighborHopArray[i] = 0;
					}
					else if(neighborHopArray[i] != 1){
						neighborHopArray[i] = 1000;
					}
					System.out.print(neighborHopArray[i]+ "\t");
					System.out.println();
				}
				
				n1.setServer(nodePortNumber1, info_nodes.length, nodeNeighborsArray.length);
			}
		});

		Thread t2 = new Thread(new Runnable() {
			public void run() {
				while(updatesCounter > 0){
					try {
						Thread.sleep(1000);
						for(int i= 0; i<nodeNeighborsArray.length; i++) {
							for(int j = 0; j<info_nodes.length; j++) {
								if(info_nodes[j][0].equals(nodeNeighborsArray[i])) {
									n1.setClient(info_nodes[j][1], Integer.parseInt(info_nodes[j][2]));
								}
							}
						}
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		t1.start();
		t2.start();
	
		// Print final k-hop neighbors
		System.out.println("\n--------------------------------------\n");
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

					// Only lines which begin with an unsigned integer are considered to be valid
					if (isInteger(temp_filerow.trim().split(" +")[0])) {
						// Handle # which denotes comments. Characters after # in line are ignored
						if (temp_filerow.contains("#")) {
							temp_filerow = temp_filerow.substring(0, temp_filerow.indexOf("#"));
						}
						temp_filerow = temp_filerow.trim(); // Handle leading and trailing white spaces

						// First valid line of the configuration file contains one token denoting the
						// number of nodes in the system.
						if (firstlinepassed == false) {
							num_nodes = Integer.parseInt(temp_filerow.trim().split(" +")[0]);
							info_nodes = new String[num_nodes][4];
							firstlinepassed = true;

						} else { // first line has passed
							if (lineCount < num_nodes) {
								// Populate array containing node information
								temp_splitarr = temp_filerow.trim().split(" +");
								hostname = ""; // clear contents
								for (int i = 1; i < temp_splitarr.length - 1; i++) {
									hostname += temp_splitarr[i].trim();
								}

								// Assign host name and port to node
								info_nodes[lineCount][0] = temp_splitarr[0].trim();
								info_nodes[lineCount][1] = hostname;
								info_nodes[lineCount][2] = temp_splitarr[temp_splitarr.length - 1].trim();

								lineCount++;
							} else {
								node = temp_filerow.trim().split(" +")[0].trim();
								node_neighbors = temp_filerow.trim().split(" +", 2)[1].trim();
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
			updatesCounter = info_nodes.length * info_nodes.length;
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
	 * FUNCTION. String input. Boolean output. Check whether inputed string is a
	 * valid unsigned integer value.
	 */
	public static boolean isInteger(String input) {
		boolean isValidInteger = false;
		try {
			if (!input.contains("-")) { // Only unsigned integers are valid
				Integer.parseInt(input);
				isValidInteger = true;
			}
		} catch (NumberFormatException e) {
			// If caught, input is not an integer. Function will return false
		}

		return isValidInteger;
	}

	public void setServer(int nodePortNumber, int totalNodes, int nodeNeighborsNumber) {	
		ServerSocket ssoc = null;
		try {
			ssoc = new ServerSocket(nodePortNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int counter = 0;
		while(true) {
			try {				
				Socket soc = ssoc.accept();			
				socClientsArray.add(soc);
				counter++;
				String line = "Please send your initial k-hop array";
				if(counter == nodeNeighborsNumber){
					ArrayList<Socket> tempList = new ArrayList<>(socClientsArray);
					socClientsArray.clear();
					counter = 0;		
					try{
						Thread.sleep(1000);
						serverCommunicate(line, tempList);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void serverCommunicate(String line, ArrayList<Socket> tempList) {
		
		final CountDownLatch latch = new CountDownLatch(tempList.size());
		final boolean[] newUpdateFlag = new boolean[1];
		newUpdateFlag[0] = false;
		int counter3 = 0;
		for(Socket soc:tempList){
			counter3++;
			Socket currentSoc = soc;
			Thread newServerClientThread = new Thread(new Runnable(){
				public void run(){
					try {
						String line123 = "Have you updated your value";
						DataOutputStream out = new DataOutputStream(currentSoc.getOutputStream());
						out.writeUTF(line123);
						
					    	DataInputStream in = new DataInputStream (currentSoc.getInputStream());
						String clientFlag = in.readUTF();
						boolean updatedClientFlag = clientFlag.equals("true")? true : false;
						ObjectInputStream indis = new ObjectInputStream (currentSoc.getInputStream());
						int[] line1 =(int[])indis.readObject();
						newUpdateFlag[0] = newUpdateFlag[0] | updateNeighborHops(neighborHopArray, line1);
						for(int k=0; k<neighborHopArray.length; k++){
							System.out.print(neighborHopArray[k] + "\t");
						}
						System.out.println();
						
						out.writeUTF("DONE");
						out.flush();
						out.close();
						in.close();
						currentSoc.close();
					} catch (SocketException e) {
						System.out.println(e);
					} catch (IOException | ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					
					latch.countDown();
				}
			});	
			
			newServerClientThread.start();
			if(counter3 == tempList.size()){
				try{
					latch.await();
					serverUpdateFlag = newUpdateFlag[0];
					if(serverUpdateFlag == false){
						updatesCounter--;
					}
					System.out.println("updatesCounter" + updatesCounter);
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}	

	synchronized  public boolean updateNeighborHops(int me[], int myNeighbor[]){
		int n = info_nodes.length;
		int countUpdates = 0;
		for(int i=0; i<n; i++) {
			if(me[i]>1){
				if(myNeighbor[i]>0 && myNeighbor[i]<(me[i]-1)){
					me[i] = myNeighbor[i] + 1;
					countUpdates++;
				}
			}
		}
		this.neighborHopArray = me;
		if(countUpdates > 0){
			return true;
		} else {
			return false;
		}
	}
	
	public void setClient(String nodeHostName, int nodePortNumber) {
		int[] currentHopArray = neighborHopArray;
		clientthread++;
		
		final int ct = clientthread;
		final boolean serverUpdateFlag1[] = new boolean[1];
		serverUpdateFlag1[0] = serverUpdateFlag;
		Thread singleClientThread = new Thread(new Runnable(){
			public void run(){
				try {
					Socket clientSocket = new Socket(nodeHostName, nodePortNumber);
					while(true) {
						try {
							DataInputStream in = new DataInputStream(clientSocket.getInputStream());
							String line = in.readUTF(); 
							if(line.equalsIgnoreCase("Have you updated your value")){
							String clientFlag1 = serverUpdateFlag1[0]? "true" : "false";
							DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
							
							out.writeUTF(clientFlag1);	
							out.flush();
							ObjectOutputStream dostream = new ObjectOutputStream(clientSocket.getOutputStream());
							dostream.writeObject(currentHopArray);
							dostream.flush();
							line = in.readUTF();
							if(line.equalsIgnoreCase("DONE")){
								in.close();
								out.close();
								clientSocket.close();
								System.out.println("Connection closed"); 
								break;
							}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}			
				} catch (SocketException e) {
					System.out.println(e);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		singleClientThread.start();
	}
	
}
