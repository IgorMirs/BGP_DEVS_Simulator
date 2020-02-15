package BGP_Simulator_v06_SignedMessages;

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
	    int traitorsFileRow;
	    if (args.length < 2) {
	        System.out.println("Less 3");
	        return;
	    }
	    else {
	        nNodes = Integer.parseInt(args[0]);
	        traitorsFileRow = Integer.parseInt(args[1]);
	        fullFilePath = args[2];
	    }
	    
        //split the string with a full path on an array of separated strings divided by '\\'
        String[] names = fullFilePath.split("\\\\");
        //take the last member of this array as a file name
        fileName = names[names.length - 1];
	    
	    BufferedReader rdr;
	    int fileCounter = 0;
	    try {
	        //read the file with traitors
	        rdr = new BufferedReader(new FileReader(".\\BGP_Simulator_v06_SignedMessages\\traitors\\" + fileName + ".txt"));
	        String line = rdr.readLine();
	        //search for the specific line in the file
	        while (fileCounter != traitorsFileRow) {
	            line = rdr.readLine();
	            fileCounter++;
	        }
	        //split line number from traitors number
	        String [] trRow = line.split(" ");
	        //split traitors
	        String[] trIds = trRow[1].split(",");
	        for (int i = 0; i < trIds.length; i++) 
	            traitorVec.add(Integer.parseInt(trIds[i]));
//	            System.out.println("" + trIds[i]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
//        //split the string with a full path on an array of separated strings divided by '\\'
//	    String[] names = fullFilePath.split("\\\\");
//	    //take the last member of this array as a file name
//	    fileName = names[names.length - 1];
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
		mySimModel=new BGPTest(nNodes - 1, traitorVec, fileName, conMat, traitorsFileRow);
		coordinator simC=new coordinator(mySimModel);		
		simC.initialize();
		simC.simulate(1000000);
		
	}
}
