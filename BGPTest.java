package BGP_Simulation_v05_NetworkTopology;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class BGPTest extends ViewableDigraph 
{
    protected int nNodes = 5; //number of regular nodes (except commander in the network)
    protected int sendMsg = 1; //original message to send 
    private int [][] connectivity_matrix =
    {
        {0, 1},
        {0, 2},
        {0, 3},
        {0, 4},
        {0, 5},
        {1, 2},
        {1, 3},
        {1, 4},
        {1, 5},
        {2, 3},
        {2, 4},
        {2, 5},
        {3, 4},
        {3, 5},
        {4, 5},
    };
    
    //traitors in the network
    private int [] traitorVec = {4};
    private int nTraitors = traitorVec.length;
    
    public BGPTest() 
    {
        super("Byzantine Generals Problem");
        System.out.println("sdfsdfsdf");
        /* Read from file       
        try {
            File data2 = new File("C:/Users/garik/eclipse-workspace/DEVS/ Enviroment/Models/BGP_Simulation_v04_Internal_decisions/Settings.txt");
            Scanner myReader = new Scanner(data2);
            while (myReader.hasNextLine()) {
              String data = myReader.nextLine();
              System.out.println(data);
            }
            myReader.close();
          } 
        catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
        */
        
        // Creating the network
        NodeCoupledModel[] network = new NodeCoupledModel[nNodes]; 

        //creating the Network statistics object
        NetStat netStat = new NetStat(traitorVec, sendMsg, nNodes, nTraitors);

        //creating the commander (name, ID, sending message, number of nodes to send)
//        Observer commander = new Observer("Commander", 0, sendMsg, netStat, network);
        CommanderCoupledModel commander = new CommanderCoupledModel ("Commander", 0, sendMsg, netStat, network);
        
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
