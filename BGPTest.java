package BGP_Simulation_temp;

import java.awt.Dimension;
import java.awt.Point;

import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class BGPTest extends ViewableDigraph
{
    protected int nNodes = 5; //number of regular nodes (except commander in the network)
    protected int sendMsg = 1; //original message to send 
    private int [][] connectivity_matrix =
    {
        {0, 1},
        {1, 2},
        {2, 3},
        {3, 4},
        {4, 5},
        {5, 0}
    };
    
    //traitors in the network
    private int [] traitorVec = {2};
    private int nTraitors = traitorVec.length;
    
    public BGPTest()
    {
        super("Byzantine Generals Problem");
        
        // Creating the network
        Node[] network = new Node[nNodes]; 

        //creating the Network statistics object
        NetStat netStat = new NetStat(traitorVec, sendMsg, nNodes, nTraitors);

        //creating the commander (name, ID, sending message, number of nodes to send)
        Observer commander = new Observer("Commander", 0, sendMsg, netStat);
        
        
        //creating the nodes
        for (int i = 0; i < nNodes; i++) {
            network[i] = new Node("Node " + (i + 1), i + 1, netStat);
            add(network[i]);
        }
        
        //adding the commander
        add(commander);
        
        // Connect the nodes and the commander
        for (int i = 0; i < connectivity_matrix.length; i++) {
            if (connectivity_matrix[i][0] == 0) {
                addCoupling(commander,commander.OUT_PORT1, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_COMMANDER);
                addCoupling(commander,commander.OUT_PORT2, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_NODES);
                addCoupling(network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].OUT_COMMANDER, commander,commander.IN_PORT);
            }
            else if (connectivity_matrix[i][1] == 0) {
                addCoupling(commander,commander.OUT_PORT1, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_COMMANDER);
                addCoupling(commander,commander.OUT_PORT2, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_NODES);
                addCoupling(network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].OUT_COMMANDER, commander,commander.IN_PORT);
            }
            else {
                addCoupling(network[connectivity_matrix[i][0] - 1],network[connectivity_matrix[i][0] - 1].OUT_COMMANDER, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_COMMANDER);
                addCoupling(network[connectivity_matrix[i][1] - 1],network[connectivity_matrix[i][1] - 1].OUT_COMMANDER, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_COMMANDER);
                addCoupling(network[connectivity_matrix[i][0] - 1],network[connectivity_matrix[i][0] - 1].OUT_NODES, network[connectivity_matrix[i][1] - 1], network[connectivity_matrix[i][1] - 1].IN_NODES);
                addCoupling(network[connectivity_matrix[i][1] - 1],network[connectivity_matrix[i][1] - 1].OUT_NODES, network[connectivity_matrix[i][0] - 1], network[connectivity_matrix[i][0] - 1].IN_NODES);
            }
        }
    }
    
    public void layoutForSimView()
    {
        preferredSize = new Dimension(1000, 332);
        int x = 10, y = 30;
        ((ViewableComponent)withName("Commander")).setPreferredLocation(new Point(x, y));
        x += 200;
        for (int i = 0; i < nNodes; i++) {
            if (x > 900) {
                y += 100;
                x = 10;
            } 
            ((ViewableComponent)withName(String.format("Node %d", i + 1))).setPreferredLocation(new Point(x, y));
            x += 200;
        }
    }
}
