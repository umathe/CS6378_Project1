import java.io.*;
import java.net.*;
import java.util.*;

public class NodeSetup3 {
	int num_nodes = 0;
	String[][] info_nodes;
	int nodeNumber = 0;

	
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
								node_neighbors = temp_filerow.substring(1).trim(); 
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
		return info_nodes;
	}

	// FUNCTION. Sting input. Boolean output. Check whether inputed string is a valid integer value.
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
	int[] neighborHopArray;
	ArrayList<Socket> socArray  = new ArrayList<Socket>();

	
	public void setServer(int nodePortNumber, int totalNodes) {
			
		ServerSocket ssoc = null;
		//Socket soc;
		try {
			ssoc = new ServerSocket(nodePortNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true) {
			try {
				Socket soc = ssoc.accept(); 
				this.socArray.add(soc);
				System.out.println("Client is accepted ");
				for(int i = 0; i < totalNodes; i++){
					try {
						Thread.sleep(this.nodeNumber*500);
						serverCommunicate();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void serverCommunicate() {
		for(int i = 0; i< this.socArray.size(); i++){
			Socket currentSoc = this.socArray.get(i);
			String line = "Please send your initial k-hop array";
			
			try {
				DataOutputStream out = new DataOutputStream(currentSoc.getOutputStream());
				out.writeUTF(line);
				ObjectInputStream in = new ObjectInputStream (currentSoc.getInputStream());
				int[] line1 =(int[])in.readObject();
				System.out.println(line1); 
			} catch (IOException | ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void setClient(String nodeHostName, int nodePortNumber) {
		//Socket clientSocket = null;
		int[] currentHopArray = this.neighborHopArray;
		try {
			final Socket clientSocket = new Socket(nodeHostName, nodePortNumber);
			System.out.println(clientSocket.getPort());
			
			
			new Timer().schedule(
				new TimerTask(){
					public void run() {
						DataInputStream in;
						while(true) {
							try {
								in = new DataInputStream(clientSocket.getInputStream());
								String line = in.readUTF(); 
								System.out.println(line);
								ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
								if(line.equalsIgnoreCase("Please send your initial k-hop array")) {
									out.writeObject(currentHopArray);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}, 20000
			);		
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		
		//Run Parser over Config file
		//ConfigInput_Parser main = new ConfigInput_Parser();

		
		NodeSetup3 n1 = new NodeSetup3();
		File config_file = new File("C:\\Personal Stuff\\ProjectWorkspace\\ProjectAssignment1\\src\\FindNodes\\SampleInput.txt");
		System.out.println(config_file.exists());
		String[][] info_nodes = n1.ReadInput(config_file);
		String nodeHostName = null;
		int nodePortNumber = 0; 
		String nodeNeighbors = null;
		try {
			nodeHostName = InetAddress.getLocalHost().getCanonicalHostName();
			System.out.println(nodeHostName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		/*for(int i = 0; i<info_nodes.length; i++) {
			System.out.println("info_nodes[i][1] == nodeHostName "+(info_nodes[i][1].equals(nodeHostName))+ " " +info_nodes[i][1]+ " " +nodeHostName);
			if(info_nodes[i][1].equals(nodeHostName)) { 
				nodeNumber = Integer.parseInt(info_nodes[i][0]);
				nodePortNumber = Integer.parseInt(info_nodes[i][2]);
				nodeHostName = info_nodes[i][1];
				nodeNeighbors = info_nodes[i][3];
				break;
			}
		}*/
		n1.nodeNumber = Integer.parseInt(info_nodes[3][0]);
		nodePortNumber = Integer.parseInt(info_nodes[3][2]);
		nodeHostName = info_nodes[3][1];
		nodeNeighbors = info_nodes[3][3];
		//String nodeHostName1 = nodeHostName;
		int nodeNumber1 = n1.nodeNumber;
		int nodePortNumber1 = nodePortNumber; 
		
		System.out.println(nodeNeighbors);
		String[] nodeNeighborsArray = nodeNeighbors.split(" ");
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				n1.neighborHopArray = new int[info_nodes.length];

				for(int i = 0; i < n1.neighborHopArray.length; i++) {
					//System.out.println(i);
					for(int j = 0; j < nodeNeighborsArray.length; j++) {
						//System.out.println(nodeNeighborsArray[j] + " " +(Integer.parseInt(nodeNeighborsArray[j])== i));
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
					System.out.println(n1.neighborHopArray[i]);
				}
				
				n1.setServer(nodePortNumber1, nodeNumber1);
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
		TimerTask taskServerCommDelay = new TimerTask() {
			public void run() {
				n1.serverCommunicate();
			}
		};
		
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				delayInterval.schedule(taskClientDelay, 5000);
				
				//delayInterval.schedule(taskServerCommDelay, 10000);
			}
		});
		
		t1.start();
		t2.start();
	}
}
