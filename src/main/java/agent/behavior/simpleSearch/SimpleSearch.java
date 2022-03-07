package agent.behavior.simpleSearch;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.Representation;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;

import java.util.*;
import java.util.List;

public class SimpleSearch extends Behavior {

    Representation[][] knownCells = new Representation[12][12];
    Queue<PacketRep> packetQueue = new LinkedList<>();
    List<DestinationRep> destinations = new ArrayList<>();

    PacketRep currentPacket;
    Coordinate moveCoordinate;
    boolean holdingPacket = false;

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication
    }

    
    @Override
    public void act(AgentState agentState, AgentAction agentAction) {

        var perception = agentState.getPerception();
        handlePerception(perception);

        // Calculate new destination
        if (moveCoordinate == null) {
            // If not carrying a packet -> set destination to new packet.
            if (!agentState.hasCarry() && !packetQueue.isEmpty()) {
                PacketRep packet = packetQueue.poll();
                currentPacket = packet;
                moveCoordinate = new Coordinate(packet.getX(), packet.getY());
            }
            // If carrying packet but no destination -> Find destination for packet.
            else if (agentState.hasCarry()){
                DestinationRep destination = findDestination();
                if (destination != null){
                    moveCoordinate = new Coordinate(destination.getX(), destination.getY());
                }
            }

        }

        // -------------------- DO ACTION -----------------------
        if (moveCoordinate != null && targetReached(agentState)) {
            // Pickup / drop packet to destination
            if (!agentState.hasCarry()) {
                // At packet
                agentAction.pickPacket(moveCoordinate.getX(), moveCoordinate.getY());
            } else
            {
                // At destination
                agentAction.putPacket(moveCoordinate.getX(), moveCoordinate.getY());
            }
            moveCoordinate = null;
        } else
        {

            if (moveCoordinate == null){
                // Perform random move action
                randomMove(agentState, agentAction, perception);
                System.out.println("Random null Move");
            }
            else
            {
                // Calc calculate movement with dijkstra
                // Calc movement and move (Temporary)
                calcMovement(agentState, agentAction, perception);
            }
        }
    }

    private void randomMove(AgentState agentState, AgentAction agentAction, Perception perception) {
        List<Coordinate> relMoves = this.getPossibleRelMoves();
        for (Coordinate move : relMoves) {
            if (perception.getCellPerceptionOnRelPos(move.getX(), move.getY()) != null
                    && perception.getCellPerceptionOnRelPos(move.getX(), move.getY()).isWalkable()) {
                agentAction.step(agentState.getX() + move.getX(), agentState.getY() + move.getY());
            }
        }
    }

    private void calcMovement(AgentState agentState, AgentAction agentAction, Perception perception) {
        int dx = 0;
        int dy = 0;
        dx = moveCoordinate.getX() - agentState.getX() > 0 ? 1:dx;
        dx = moveCoordinate.getX() - agentState.getX() < 0 ? -1:dx;
        dy = moveCoordinate.getY() - agentState.getY() > 0 ? 1:dy;
        dy = moveCoordinate.getY() - agentState.getY() < 0 ? -1:dy;

        if (perception.getCellPerceptionOnRelPos(dx, dy) != null && perception.getCellPerceptionOnRelPos(dx, dy).isWalkable())
        {
            agentAction.step(agentState.getX() + dx, agentState.getY() + dy);
            System.out.println("Moved");
        } else
        {
            randomMove(agentState, agentAction,perception);
            System.out.println("Random move");
        }
    }

    private DestinationRep findDestination() {
        for (DestinationRep dest : destinations) {
            if (currentPacket.getColor() == dest.getColor()){
                return dest;
            }
        }
        return null;

    }

    private boolean targetReached(AgentState agentState) {
        int dx = Math.abs(moveCoordinate.getX() - agentState.getX());
        int dy = Math.abs(moveCoordinate.getY() - agentState.getY());
        return dx <= 1 && dy <= 1;
    }

    private void handlePerception(Perception perception) {
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);
                assert cell != null;

                // If packet in perception cell and new packet
                if (cell.containsPacket() && knownCells[y][x] == null){
                    PacketRep rep = Objects.requireNonNull(cell.getRepOfType(PacketRep.class)); // TODO: Should only save (x,y)
                    packetQueue.add(rep);
                    knownCells[cell.getY()][cell.getX()] = rep;
                    System.out.println("Added new packet");
                }

                // If destination in cell
                if (cell.containsAnyDestination() && knownCells[y][x] == null) {
                    DestinationRep rep = Objects.requireNonNull(cell.getRepOfType(DestinationRep.class)); // TODO: Should only save (x,y)
                    destinations.add(rep);
                    knownCells[cell.getY()][cell.getX()] = rep;
                    System.out.println("Added new destination");
                }
            }
        }
        System.out.println("Handled perception");
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
