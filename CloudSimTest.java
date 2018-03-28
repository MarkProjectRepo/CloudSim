package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A simple example showing how to create a data center with one host and run one cloudlet on it.
 */
public class CloudSimTest {
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample1...");

		try {
			// First step: Initialize the CloudSim package. It should be called before creating any entities.
			int num_user = 10; // number of cloud users
			Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date and time.
 			boolean trace_flag = false; // trace events

			/* Comment Start - Dinesh Bhagwat 
			 * Initialize the CloudSim library. 
			 * init() invokes initCommonVariable() which in turn calls initialize() (all these 3 methods are defined in CloudSim.java).
			 * initialize() creates two collections - an ArrayList of SimEntity Objects (named entities which denote the simulation entities) and 
			 * a LinkedHashMap (named entitiesByName which denote the LinkedHashMap of the same simulation entities), with name of every SimEntity as the key.
			 * initialize() creates two queues - a Queue of SimEvents (future) and another Queue of SimEvents (deferred). 
			 * initialize() creates a HashMap of of Predicates (with integers as keys) - these predicates are used to select a particular event from the deferred queue. 
			 * initialize() sets the simulation clock to 0 and running (a boolean flag) to false.
			 * Once initialize() returns (note that we are in method initCommonVariable() now), a CloudSimShutDown (which is derived from SimEntity) instance is created 
			 * (with numuser as 1, its name as CloudSimShutDown, id as -1, and state as RUNNABLE). Then this new entity is added to the simulation 
			 * While being added to the simulation, its id changes to 0 (from the earlier -1). The two collections - entities and entitiesByName are updated with this SimEntity.
			 * the shutdownId (whose default value was -1) is 0    
			 * Once initCommonVariable() returns (note that we are in method init() now), a CloudInformationService (which is also derived from SimEntity) instance is created 
			 * (with its name as CloudInformatinService, id as -1, and state as RUNNABLE). Then this new entity is also added to the simulation. 
			 * While being added to the simulation, the id of the SimEntitiy is changed to 1 (which is the next id) from its earlier value of -1. 
			 * The two collections - entities and entitiesByName are updated with this SimEntity.
			 * the cisId(whose default value is -1) is 1
			 * Comment End - Dinesh Bhagwat 
			 */
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm>();

			// VM description
			int vmid = 0;
			int mips = 1000;
			long size = 10000; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name

			// create VM
			for(int i = 0; i < 25; i++){
				Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
				vmlist.add(vm);
			}
			// add the VM to the vmList

			// submit vm list to the broker
			broker.submitVmList(vmlist);

			// Fifth step: Create one Cloudlet
			cloudletList = new ArrayList<Cloudlet>();

			//Random Cloudlet properties
			/*long length = 400;
			long fileSize = 300;
			long outputSize = 300;
			//Random within the range of the given values
			cloudletList = generateCloudletList(10000,brokerId, length, fileSize, outputSize);
			*/

			//Range based cloudlet properties
			int[] range = {30000, 50000, 20000}; // Range represents the {Low , Medium , High} range cloudlets
			long[] rangelength = {1000, 25000, 40000}; // The following properties are the same as for cloudlet but for their respective activity levels
			long[] rangefileSize = {750, 15000, 30000};
			long[] rangeoutputSize = {750, 15000, 30000};
			cloudletList = generateCloudletList(range, brokerId, rangelength, rangefileSize, rangeoutputSize);
			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();
			try {
				Log.setOutput(new FileOutputStream("D:\\Documents\\cloudsim-4.0\\cloudsim-cloudsim-4.0\\modules\\cloudsim-examples\\src\\main\\java\\org\\cloudbus\\cloudsim\\examples\\Output.csv"));
			}catch(FileNotFoundException e){}

			//Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}
	private static ArrayList<Cloudlet> generateCloudletList(int number, int brokerId, long length, long fileSize, long outputSize){
		ArrayList<Cloudlet> cloudletList = new ArrayList<Cloudlet>();

		UtilizationModel utilizationModel = new UtilizationModelStochastic();
		Cloudlet cloudlet;
		for (int i = 0; i < number; i++) {
			cloudlet =
					new Cloudlet(i, (long) (Math.random() * length), 1, (long) (Math.random() * fileSize),
							(long) (Math.random() * outputSize), utilizationModel, utilizationModel,
							utilizationModel);

			cloudlet.setUserId(brokerId);
			cloudletList.add(cloudlet);
		}

		return cloudletList;
	}

	private static long RandomRangeLong(long min, long max){
		return (long) (Math.random()*(max - min + 1) + min);
	}

	private static ArrayList<Cloudlet> generateCloudletList(int[] range, int brokerId, long[] length, long[] fileSize, long[] outputSize){
		ArrayList<Cloudlet> cloudletList = new ArrayList<Cloudlet>();

		UtilizationModel utilizationModel = new UtilizationModelStochastic();
		Cloudlet cloudlet;

		int loc = 0;
		for (int n : range){
			if(loc == 0){
				for (int i = 0; i < n; i++) {
					cloudlet =
							new Cloudlet(i, (long) (Math.random() * length[loc]), 1, (long) (Math.random() * fileSize[loc]),
									(long) (Math.random() * outputSize[loc]), utilizationModel, utilizationModel,
									utilizationModel);
					cloudlet.setUserId(brokerId);
					cloudletList.add(cloudlet);
				}
			}else {
				for (int i = 0; i < n; i++) {
					cloudlet =
							new Cloudlet(i, RandomRangeLong(length[loc-1], length[loc]), 1, RandomRangeLong(fileSize[loc], fileSize[loc-1]),
									RandomRangeLong(outputSize[loc], outputSize[loc-1]), utilizationModel, utilizationModel,
									utilizationModel);
					cloudlet.setUserId(brokerId);
					cloudletList.add(cloudlet);
				}
			}
			loc++;
		}

		return cloudletList;
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 100000;

		// 3. Create PEs and add these into a list.
		//peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		for(int i = 0; i < 20; i++){
			peList.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		}

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 24000; // host memory (MB)
		long storage = 100000000; // host storage
		int bw = 1000000;

		hostList.add(
			new Host(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerTimeShared(peList)
			)
		); // This is our machine

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 *
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = ",";
		Log.printLine("VM ID" + indent + "Actual CPU" + indent
				+ "Length" + indent + "Input Size" + indent + "Output Size" + indent + "End Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);

			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.printLine(cloudlet.getVmId()
						+ indent
						+ dft.format(cloudlet.getActualCPUTime())
						+ indent + dft.format(cloudlet.getCloudletLength())
						+ indent
						+ dft.format(cloudlet.getCloudletFileSize())
                        + indent + dft.format(cloudlet.getCloudletOutputSize())
						+ indent + dft.format(cloudlet.getFinishTime()));
			}
		}
	}
}
