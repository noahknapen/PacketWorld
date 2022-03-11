package agent.behavior.complex_behavior;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;
import util.mapping.Graph;
import util.mapping.Node;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ComplexBehavior extends Behavior {

    private Graph graph;
    HashMap<Coordinate, Color> packetCells = new HashMap<>();
    HashMap<Color, Coordinate> destinationCells = new HashMap<>();
    Color packetColor;

    private Coordinate unexploredNode = new Coordinate(4, 1);
    private List<Coordinate> unexploredPositions;



    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication
    }

    
    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        if (graph == null) {
            graph = new Graph(agentState.getX(), agentState.getY());
            // path = perceptionSearch(agentState, unexploredNode);
        }

        // Handle perception
        //     Here you will add new unexplored points.
        //     Here you will also add nodes to the graph.
        //     Save positions of packets.

        // If not holding and packet in perception (seesPacket == true)
        //     Go and pick up packet.

        // Else go to unexplored node

        // Walk to destination
        if (!path.isEmpty()) {
            Coordinate pos = path.remove(0);
            agentAction.step(agentState.getX() + pos.getX(), agentState.getY() + pos.getY());
            return;
        }


        agentAction.skip();


    }

    private List<Coordinate> perceptionSearch(AgentState agentState, Coordinate p) {
        int distX = p.getX() - agentState.getX();
        int distY = p.getY() - agentState.getY();
        int minDist = Math.min(distX, distY);
        int dx = Integer.signum(distX);
        int dy = Integer.signum(distY);

        List<Coordinate> path = new ArrayList<>();
        for (int i = 0; i < minDist; i++) {
            path.add(new Coordinate(dx, dy));
        }

        if (distX > distY) {
            for (int i = 0; i < (distX - minDist); i++) {
                path.add(new Coordinate(dx, 0));
            }
        } else if (distY > distX) {
            for (int i = 0; i < (distY - minDist); i++) {
                path.add(new Coordinate(0, dy));
            }
        }

        return path;
    }

}
