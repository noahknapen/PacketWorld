package agent.behavior.simple_behavior;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;

import java.awt.*;
import java.util.*;
import java.util.List;

enum REP_TYPES {
    TYPE_PACKET,
    TYPE_DESTINATION
}

public class SimpleBehavior extends Behavior {

    //REP_TYPES[][] knownCells = new REP_TYPES[50][50]; // Width and height should not be explicitly defined.
    HashMap<Coordinate, Color> packetCells = new HashMap<>();
    HashMap<Color, Coordinate> destinationCells = new HashMap<>();
    Color packetColor;
    Coordinate moveCoordinate;

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication
    }

    
    @Override
    public void act(AgentState agentState, AgentAction agentAction) {

        var perception = agentState.getPerception();
        handlePerception(perception);

        // ----------------- Calculate new movement coordinate -----------------------
        if (moveCoordinate == null) {
            // If not carrying a packet -> set destination to new packet.
            if (!agentState.hasCarry()) {
                moveCoordinate = findNewPacket();
            }
            // If carrying packet but no destination -> Find destination for packet.
            else if (packetColor != null){
                moveCoordinate = findDestination();
            }
        }

        // -------------------- Determine action -----------------------
        if (moveCoordinate != null && targetReached(agentState)) {
            // Agent has reached its move coordinate.
            if (!agentState.hasCarry()) {
                // Agent does not carry packet -> pick up packet.
                System.out.println("Pick up packet");
                packetColor = packetCells.get(moveCoordinate);
                agentAction.pickPacket(moveCoordinate.getX(), moveCoordinate.getY());
            } else if (agentState.hasCarry())
            {
                // Agent does carry packet -> drop packet.
                System.out.println("Put down packet at destinations");
                packetColor = null;
                agentAction.putPacket(moveCoordinate.getX(), moveCoordinate.getY());
            }
            moveCoordinate = null;
        } else
        {
            // Agent not close to a move coordinate.
            if (moveCoordinate == null) {
                // No movement coordinate -> Perform random action.
                randomMove(agentState, agentAction, perception);
            }
            else
            {
                // Agent has a move coordinate to move against -> move towards it.
                calcMovement(agentState, agentAction, perception);
            }
        }
    }

    private void randomMove(AgentState agentState, AgentAction agentAction, Perception perception) {
        List<Coordinate> relMoves = this.getPossibleRelMoves();
        for (Coordinate move : relMoves) {
            if (perception.getCellPerceptionOnRelPos(move.getX(), move.getY()) != null && Objects.requireNonNull(perception.getCellPerceptionOnRelPos(move.getX(), move.getY()))
                    .isWalkable()) {
                agentAction.step(agentState.getX() + move.getX(), agentState.getY() + move.getY());
                return;
            }
        }
        // If none of the moves worked -> Skip action.
        agentAction.skip();
    }

    private void calcMovement(AgentState agentState, AgentAction agentAction, Perception perception) {
        int dx = 0;
        int dy = 0;
        dx = moveCoordinate.getX() - agentState.getX() > 0 ? 1:dx;
        dx = moveCoordinate.getX() - agentState.getX() < 0 ? -1:dx;
        dy = moveCoordinate.getY() - agentState.getY() > 0 ? 1:dy;
        dy = moveCoordinate.getY() - agentState.getY() < 0 ? -1:dy;

        if (perception.getCellPerceptionOnRelPos(dx, dy) != null && Objects.requireNonNull(perception.getCellPerceptionOnRelPos(dx, dy)).isWalkable())
        {
            // Shortest path to move coordinate can be used -> Move one step towards it.
            agentAction.step(agentState.getX() + dx, agentState.getY() + dy);
            System.out.println("Moved");
        } else
        {
            // If shortest path to move coordinate can't be used -> Perform random action.
            randomMove(agentState, agentAction,perception);
            System.out.println("Random move");
        }
    }

    private Coordinate findNewPacket() {
        for (Coordinate coordinate : packetCells.keySet()) {
            return coordinate;
        }
        return null;
    }

    private Coordinate findDestination() {
        assert packetColor != null;
        return destinationCells.get(packetColor);
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

                if (cell != null) {
                    // If packet in perception cell and new packet
                    if (cell.containsPacket() && packetCells.get(getCoord(cell)) == null) {
                        PacketRep rep = Objects.requireNonNull(cell.getRepOfType(PacketRep.class));
                        packetCells.put(getCoord(cell), rep.getColor());
                        System.out.println("Added new packet");
                    } else if (!cell.containsPacket() && packetCells.get(getCoord(cell)) != null) {
                        // Remove positions where packets have disappeared.
                        packetCells.remove(getCoord(cell));

                        // If agent is moving towards a disappeared packet -> remove the move coordinate.
                        if (getCoord(cell).equals(moveCoordinate)) {
                            moveCoordinate = null;
                        }
                    }

                    // If destination in cell
                    if (cell.containsAnyDestination()) {
                        DestinationRep rep = Objects.requireNonNull(cell.getRepOfType(DestinationRep.class));

                        // If destination for this color does not already exist -> add it.
                        if (destinationCells.get(rep.getColor()) == null)
                            destinationCells.put(rep.getColor(), new Coordinate(cell.getX(), cell.getY()));
                        System.out.println("Added new destination");
                    }
                }
            }
        }
    }

    private Coordinate getCoord(CellPerception cell) {
        return new Coordinate(cell.getX(), cell.getY());
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
