package BGP_Simulation_v05_NetworkTopology_Worst_sim2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import model.modeling.digraph;
import model.simulation.coordinator;;

public class sim {

	protected static digraph mySimModel;
	
	public static void main(String[] args) {
	    int nNodes;
	    Vector<Integer> traitorVec = new Vector<Integer>();
	    String fullFilePath;
	    String fileName;
	    
	    if (args.length < 3) {
	        System.out.println("Less 3");
	        return;
	    }
	    else {
	        nNodes = Integer.parseInt(args[0]);
	        traitorVec.add(Integer.parseInt(args[1])); 
	        fullFilePath = args[2];
	    }
	    
        
        //split the string with a full path on an array of separated strings divided by '\\'
	    String[] names = fullFilePath.split("\\\\");
	    //take the last member of this array as a file name
	    fileName = names[names.length - 1];
	    BufferedReader reader;
	    //connectivity matrix
	    Vector<Vector<Integer>> conMat = new Vector<Vector<Integer>>();
        try {
            reader = new BufferedReader(new FileReader(fullFilePath + ".csv"));
            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(",");
                Vector<Integer> tempV = new Vector<Integer>();
                //add the values of the splited values into the vector
                for (int i = 0; i < values.length; i++) {
                    tempV.add(Integer.parseInt(values[i]));
                }
                //add the vector to connectivity matrix
                conMat.add(tempV);
                //read next line
                line = reader.readLine();
            }
            reader.close();
          } 
        
        catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
		mySimModel=new BGPTest(nNodes - 1, traitorVec, fileName, conMat);
		coordinator simC=new coordinator(mySimModel);		
		simC.initialize();
		simC.simulate(1000000);
		
	}
}
