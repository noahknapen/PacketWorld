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
import environment.world.packet.PacketRep;

public class SimpleBehavior extends Behavior {

    private List<Coordinate> relMoves = new ArrayList<>(List.of(
        new Coordinate(1, 1), new Coordinate(-1, -1),
        new Coordinate(1, 0), new Coordinate(-1, 0),
        new Coordinate(0, 1), new Coordinate(0, -1),
        new Coordinate(1, -1), new Coordinate(-1, 1)
        ));
    


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
        List<Coordinate> possibleRelMoves = this.getPossibleWalkableRelMoves(agentState);

        if (!agentState.hasCarry()) {
            // The agent is not carrying a packet
            
            if (agentState.seesPacket()) {
                // The agent sees a packet.
                // Scan the tiles in the agent's vision to see where the packet is and then use manhattan distance to see if the move is a move towards the packet.
                Perception perception = agentState.getPerception();
                int widthOfPerception = perception.getWidth();
                int heightOfPerception = perception.getHeight();

                for (int w=0; w < widthOfPerception; w++) {
                    for (int h=0; h < heightOfPerception; h++) {
                        CellPerception cellPerception= perception.getCellAt(w, h);

                        if (cellPerception.containsPacket()) {
                            int packetXCoordinate = perception.getOffsetX() + w;
                            int packetYCoordinate = perception.getOffsetY() + h;

                            // measure distance from current cell to packet cell, then go over all possible moves and see if the distance decreases. If it does, take that move
                            int currentDistance = Perception.distance(agentState.getX(), agentState.getY(), packetXCoordinate, packetYCoordinate);
                            List<Coordinate> equalValueSteps = new ArrayList<>();

                            for (Coordinate move : possibleRelMoves) {
                                int nextX = agentState.getX() + move.getX();
                                int nextY = agentState.getY() + move.getY();
                                int nextDistance = Perception.distance(nextX, nextY, packetXCoordinate, packetYCoordinate);
                                System.out.println(String.format("The move to be considered is to coordinate (%d,%d) which has a distance %d from the packet", nextX, nextY, nextDistance));
                                
                                if (currentDistance == 1) {
                                    for (Coordinate possibleRelMoveToPacket : this.relMoves) {
                                        // The agent is next to the packet and should pick it up
                                        System.out.println("the agent is next to the packet");
                                        if (perception.getCellPerceptionOnRelPos(possibleRelMoveToPacket.getX(), possibleRelMoveToPacket.getY()).containsPacket()) {
                                            System.out.println("the agent should now pick up the packet");
                                            agentAction.pickPacket(agentState.getX() + possibleRelMoveToPacket.getX(), agentState.getY() + possibleRelMoveToPacket.getY());
                                            return;
                                        }
                                    }
                                    
                                }
                                else if (nextDistance < currentDistance) {
                                    // look for steps that decrease the distance
                                    agentAction.step(nextX, nextY);
                                    return;
                                } else if (nextDistance == currentDistance) {
                                    // look for steps that equal the current distance as current distance
                                    equalValueSteps.add(new Coordinate(nextX, nextY));
                                }
                            }

                            if (equalValueSteps.size() > 0) {
                                Coordinate equalCoordinate = equalValueSteps.get(0);
                                agentAction.step(agentState.getX() + equalCoordinate.getX(), agentState.getY() + equalCoordinate.getY());
                                return;
                            } else {
                                // if there is no other option, just take a step back
                                Coordinate previousCoordinate = possibleRelMoves.get(possibleRelMoves.size()-1);
                                agentAction.step(agentState.getX() + previousCoordinate.getX(), agentState.getY() + previousCoordinate.getY());
                                return;
                            }
                        }
                    }
                }

            } else {
                Perception perception = agentState.getPerception();
                Coordinate nextMoveCoordinate = possibleRelMoves.get(0);

                 for (Coordinate move : possibleRelMoves) {
                    int x = move.getX();
                    int y = move.getY();
                    

                    // If the area is null, it is outside the bounds of the environment
                    //  (when the agent is at any edge for example some moves are not possible)
                    if (perception.getCellPerceptionOnRelPos(x, y) != null && perception.getCellPerceptionOnRelPos(x, y).isWalkable()) {
                        agentAction.step(agentState.getX() + nextMoveCoordinate.getX(), agentState.getY() + nextMoveCoordinate.getY());
                        return;
                    }
                }   
            }
        }

        // List<Coordinate> relMoves = this.getPossibleRelMoves();

        // if (!agentState.hasCarry()) {
        //     // The agent is not carrying a packet
        //     boolean agentSeesPacket = agentState.seesPacket();

        //     for (Coordinate move : relMoves) {
        //         Perception perception = agentState.getPerception();
        //         int x = move.getX();
        //         int y = move.getY();

        //         // If the move is possible, the agent will take it
        //         // TODO: NOTE: this is incredibly inefficient, the agent should look at least to all moves if one of them contains a packet
        //         CellPerception cellPerception = perception.getCellPerceptionOnRelPos(x, y);
        //         if (cellPerception != null) {
        //                 int absX = agentState.getX() + x;
        //                 int absY = agentState.getY() + y;

        //             if (cellPerception.containsPacket()) {
        //                 agentAction.pickPacket(absX, absY);
        //                 return;
        //             }
        //             else {
        //                 System.out.println("The agent is looking to move to " + absX + " " + absY);
        //                 agentAction.step(absX, absY);
        //                 return;
        //             }
        //         }
        //     }

        // }
        // else {
        //     Packet packet = agentState.getCarry().get();
        //     for (Coordinate move : relMoves) {
        //         Perception perception = agentState.getPerception();
        //         int x = move.getX();
        //         int y = move.getY();

        //         // If the move is possible, the agent will take it
        //         // TODO: NOTE: this is incredibly inefficient, the agent should look at least to all moves if one of them contains a packet
        //         CellPerception cellPerception = perception.getCellPerceptionOnRelPos(x, y);
        //         if (cellPerception != null) {
        //             int absX = agentState.getX() + x;
        //             int absY = agentState.getY() + y;

        //             if (cellPerception.containsDestination(packet.getColor())) {
        //                 agentAction.putPacket(absX, absY);
        //                 return;
        //             }
        //             else {
        //                 agentAction.step(absX, absY);
        //                 return;
        //             }
        //         }
 
        //     }
        // }
    }

    /**
     * Get the possible moves from a location. A move is possible when it does not go outside the environment. This method favors moves that do not go to the previous coordinate the agent was on.
     * @param agentState
     * @return Return a list of type {@code Coordinate} where the last element of this list is always the move to go to the previous location of the agent.
     */
    private List<Coordinate> getPossibleWalkableRelMoves(AgentState agentState) {

        List<Coordinate> possibleRelMoves = new ArrayList<>();
        Coordinate relMoveToPreviousCoordinate = null;

        for (Coordinate relMove : this.relMoves) {

            Perception perception = agentState.getPerception();
            int x = relMove.getX();
            int y = relMove.getY();

            CellPerception cellPerception = perception.getCellPerceptionOnRelPos(x, y);
            
            if (cellPerception != null) {
                Coordinate cellPerceptionCoordinate = new Coordinate(cellPerception.getX(), cellPerception.getY());

                CellPerception previousCellPerception = agentState.getPerceptionLastCell();
                Coordinate previousCellPerceptionCoordinate = previousCellPerception == null ? null : new Coordinate(previousCellPerception.getX(), previousCellPerception.getY());
                if (previousCellPerceptionCoordinate != null && cellPerceptionCoordinate.equals(previousCellPerceptionCoordinate)) {
                    relMoveToPreviousCoordinate = relMove;
                }
                if (cellPerception != null && cellPerception.isWalkable() && (previousCellPerceptionCoordinate == null || !cellPerceptionCoordinate.equals(previousCellPerceptionCoordinate))) {
                    possibleRelMoves.add(relMove);
                }
            }
            

            Collections.shuffle(possibleRelMoves);
        }
        // add the move to the previous candidate as last move so it will only be used as a desperate solution.
            if (relMoveToPreviousCoordinate != null) {
                possibleRelMoves.add(relMoveToPreviousCoordinate);
            }
        return possibleRelMoves;
    }
}
