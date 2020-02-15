package BGP_Simulator_v06_SignedMessages;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class BGPTest extends ViewableDigraph 
{
    protected int nNodes; //number of regular nodes (except commander in the network)
    protected int sendMsg = 1; //original message to send 
    protected String fileName;
    protected int traitorsFileRow;
//    private int [][] connectivity_matrix =
//    {
//        {0, 1},
//        {0, 3},
//        {0, 4},
//        {0, 5},
//        {0, 7},
//        {1, 2},
//        {1, 4},
//        {1, 5},
//        {1, 6},
//        {2, 3},
//        {2, 5},
//        {2, 6},
//        {2, 7},
//        {3, 4},
//        {3, 6},
//        {3, 7},
//        {4, 5},
//        {4, 7},
//        {5, 6},
//        {6, 7},
//    };
  //traitors in the network
//    private int [] traitorVec = {1, 3, 4, 5, 6};
//    private int nTraitors = traitorVec.length;
    
    public BGPTest(int nNodes_, Vector<Integer> traitorVec_, String fileName_, Vector<Vector<Integer>> conMat_, int traitorsFileRow_) 
    {
        super("Byzantine Generals Problem");
        nNodes = nNodes_;
        fileName = fileName_;
        traitorsFileRow = traitorsFileRow_;
        String[] values = fileName.split("-");
        //get the number of traitors
        int nTraitors = traitorVec_.size();
        //create an array with traitors
        int [] traitorVec = new int [nTraitors];
        //fill the array with traitors
        for (int i = 0; i < nTraitors; i++) {
            traitorVec[i] = traitorVec_.elementAt(i);
        }
        
        //get the number of connections from the name of the file 
        int nConnections = Integer.parseInt(values[1]);
        //create the connectivity matrix with a size equal nNodes * nConnections
        int [][] connectivity_matrix = new int[((nNodes + 1) * nConnections + 1) / 2][2];
        //fill the connectivity matrix
        int row = 0;
        int col = 0;
        for (int i = 0; i < conMat_.size() - 1; i++) {
            for (int j = i + 1; j < conMat_.elementAt(i).size(); j++) {
                //fill the connectivity matrix
                if (conMat_.elementAt(i).elementAt(j) == 1) {
                    connectivity_matrix[row][col] = i;
                    connectivity_matrix[row][++col] = j;
                    col = 0;
                    row++;
                }
            }
        }
        
//        //print the connectivity matrix
//        for (int[] row1 : connectivity_matrix) { 
//            System.out.println(Arrays.toString(row1)); 
//        } 
        
        // Creating the network
        NodeCoupledModel[] network = new NodeCoupledModel[nNodes]; 

        //creating the Network statistics object
        NetStat netStat = new NetStat(traitorVec, sendMsg, nNodes, nTraitors);

        //creating the commander (name, ID, sending message, number of nodes to send)
        CommanderCoupledModel commander = new CommanderCoupledModel ("Commander", 0, sendMsg, netStat, network, fileName, traitorsFileRow);
        
        //creating the nodes
        for (int i = 0; i < nNodes; i++) {
            network[i] = new NodeCoupledModel("Node " + (i + 1), i + 1, netStat);
            network[i].setBlackBox(true);
            add(network[i]);
        }
        
        //adding the commander
        add(commander);
        
        // Connect the nodes and the commander
        for (int i = 0; i < connectivity_matrix.length; i++) {
            if (connectivity_matrix[i][0] == 0) {
                addCoupling(commander,commander.OUT_COMMANDER, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_COMMANDER);
                addCoupling(commander,commander.OUT_NODES, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_NODES);
                addCoupling(commander,commander.OUT_DECISION, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_DECISION);
                //couple out of the node with input of the commander
                addCoupling(network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].OUT_NODES, commander,commander.IN_NODES);
                addCoupling(network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].OUT_DECISION, commander,commander.IN_DECISION);
            }
            else if (connectivity_matrix[i][1] == 0) {
                addCoupling(commander,commander.OUT_COMMANDER, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_COMMANDER);
                addCoupling(commander,commander.OUT_NODES, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_NODES);
                addCoupling(commander,commander.OUT_DECISION, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_DECISION);
                //couple out of the node with input of the commander
                addCoupling(network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].OUT_NODES, commander,commander.IN_NODES);
                addCoupling(network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].OUT_DECISION, commander,commander.IN_DECISION);
            }
            else {
                addCoupling(network[connectivity_matrix[i][0] - 1],network[connectivity_matrix[i][0] - 1].OUT_COMMANDER, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_COMMANDER);
                addCoupling(network[connectivity_matrix[i][1] - 1],network[connectivity_matrix[i][1] - 1].OUT_COMMANDER, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_COMMANDER);
                addCoupling(network[connectivity_matrix[i][0] - 1],network[connectivity_matrix[i][0] - 1].OUT_NODES, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_NODES);
                addCoupling(network[connectivity_matrix[i][1] - 1],network[connectivity_matrix[i][1] - 1].OUT_NODES, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_NODES);
                addCoupling(network[connectivity_matrix[i][0] - 1],network[connectivity_matrix[i][0] - 1].OUT_DECISION, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_DECISION);
                addCoupling(network[connectivity_matrix[i][1] - 1],network[connectivity_matrix[i][1] - 1].OUT_DECISION, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_DECISION);
            }
        }
    }
    
    public void layoutForSimView()
    {
        
        preferredSize = new Dimension(2000, 2000);
        int x = 10, y = 30;
        ((ViewableComponent)withName("Commander")).setPreferredLocation(new Point(x, y));
        x += 150;
        for (int i = 0; i < nNodes; i++) {
            if (x > 700) {
                y += 100;
                x = 10;
            } 
            ((ViewableComponent)withName(String.format("Node %d", i + 1))).setPreferredLocation(new Point(x, y));
            x += 500;
        }
    }
}
