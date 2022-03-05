package agent.behavior.simple_behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.packet.Packet;

public class SimpleBehavior extends Behavior {

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication necessary
        
    }

    /**
     * If the agent is not holding a packet, it should search for a packet to deliver.
     * If the agent is holding a packet, it should search for the destination to deliver its packet to.
     */
    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        List<Coordinate> relMoves = this.getPossibleRelMoves();

        if (!agentState.hasCarry()) {
            // The agent is not carrying a packet

            for (Coordinate move : relMoves) {
                Perception perception = agentState.getPerception();
                int x = move.getX();
                int y = move.getY();

                // If the move is possible, the agent will take it
                // TODO: NOTE: this is incredibly inefficient, the agent should look at least to all moves if one of them contains a packet
                CellPerception cellPerception = perception.getCellPerceptionOnRelPos(x, y);
                if (cellPerception != null) {
                        int absX = agentState.getX() + x;
                        int absY = agentState.getY() + y;

                    if (cellPerception.containsPacket()) {
                        int cellX = cellPerception.getX();
                        int cellY = cellPerception.getY();
                        System.out.println("cellperception on cell " + cellX + " " + cellY);
                        System.out.println("The agent is looking to move to " + absX + " " + absY);
                        agentAction.pickPacket(absX, absY);
                        break;
                    }
                    else {
                        System.out.println("The agent is looking to move to " + absX + " " + absY);
                        agentAction.step(absX, absY);
                        break;
                    }
                }
            }

        }
        else {
            Packet packet = agentState.getCarry().get();
            for (Coordinate move : relMoves) {
                Perception perception = agentState.getPerception();
                int x = move.getX();
                int y = move.getY();

                // If the move is possible, the agent will take it
                // TODO: NOTE: this is incredibly inefficient, the agent should look at least to all moves if one of them contains a packet
                CellPerception cellPerception = perception.getCellPerceptionOnRelPos(x, y);
                if (cellPerception != null) {
                    int absX = agentState.getX() + x;
                    int absY = agentState.getY() + y;

                    if (cellPerception.containsDestination(packet.getColor())) {
                        agentAction.putPacket(absX, absY);
                    }
                    else {
                        agentAction.step(absX, absY);
                    }
                }
 
            }
        }



        
    }

    private List<Coordinate> getPossibleRelMoves() {
        // Potential moves an agent can make (radius of 1 around the agent)
        List<Coordinate> moves = new ArrayList<>(List.of(
            new Coordinate(1, 1), new Coordinate(-1, -1),
            new Coordinate(1, 0), new Coordinate(-1, 0),
            new Coordinate(0, 1), new Coordinate(0, -1),
            new Coordinate(1, -1), new Coordinate(-1, 1)
        ));

        Collections.shuffle(moves);

        return moves;
    }
    
}
