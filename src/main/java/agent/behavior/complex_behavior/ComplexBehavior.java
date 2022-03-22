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
    private HashMap<Coordinate, Color> packetCells = new HashMap<>();
    private HashMap<Color, Coordinate> destinationCells = new HashMap<>();
    private Color packetColor;
    private Coordinate moveCoordinate;
    private int counter = 0;

    private String behavior = "explore";

    private Coordinate edgeStartPos;
    private Coordinate prePos;


    private Coordinate unexploredNode = new Coordinate(4, 1);
    private List<Coordinate> unexploredPositions;

    List<Coordinate> possibleMoves = new ArrayList<>(List.of(
            new Coordinate(1, 1), new Coordinate(-1, -1),
            new Coordinate(1, 0), new Coordinate(-1, 0),
            new Coordinate(0, 1), new Coordinate(0, -1),
            new Coordinate(1, -1), new Coordinate(-1, 1)
    ));

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication
    }

    
    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        if (graph == null) {
            graph = new Graph(agentState.getX(), agentState.getY());
            Coordinate currPos = new Coordinate(agentState.getX(), agentState.getY());
            edgeStartPos = currPos;
            prePos = currPos;
            // path = perceptionSearch(agentState, unexploredNode);
        }
        // Path start node
        // Pre pos node
        // maxX
        // maxY

        // maxX = max(maxX, currX)
        // maxY = max(maxY, currY)


        // ------------------ Perception Step ----------------
        // Handle graph
        // If current pose  not exists in graph
        //    Add new node
        // else
        //    bahavior = explore
        // Draw edge to current pos
        //    If not pre node on the line (path start node) -> current node
        //         Add edge (path start node -> pre node)
        //         path start node = pre node
        //    pre node = current node


        // ---------------- Action Step ----------------
        // If follow wall mode
        //     if behavior == explore
        //          Check going straight cell first (if it is close to wall)
        //          Then check the other free cells (that are close to wall)
        //      else
        //         behaviorchange(explore)
        //

        // If explore mode
        //     neigbours = lookForWalls()
        //     if non visited neigbour next to wall exists && neigbour not in graph paths
        //         path start = curr pos
        //         prepos = curr pos
        //         behaviorchange(followwall)

        //     else
        //         performRandomAction()

        handleGraph(agentState);

        int dx = 1;
        int dy = 0;

        if (counter >= 2) {
            dx = 1;
            dy = 1;
        }

        if (counter >= 3) {
            dx = 0;
            dy = 1;
        }

        if (counter >= 4) {
            dx = -1;
            dy = 0;
        }

        if (counter >= 7) {
            dx = 0;
            dy = -1;
        }

        agentAction.step(agentState.getX() + dx, agentState.getY() + dy);
        counter++;

    }

    private void handleGraph(AgentState agentState) {
        int currX = agentState.getX();
        int cuurY = agentState.getY();

        Coordinate currPos = new Coordinate(currX, cuurY);
        /*if (!graph.nodeExists(currPos))
        {
            graph.addNode(currPos);
        } else
        {
            behavior = "explore";
        }
        */

        if (!edgeStartPos.equals(prePos) && !prePos.equals(currPos)) {
            if (!graph.onTheLine(edgeStartPos, currPos, prePos))
            {
                Node n = graph.addNode(prePos);
                graph.addEdge(edgeStartPos, prePos);
                edgeStartPos = prePos;
                System.out.println("Added new edge");
            }
        }
        prePos = currPos;
    }

    private void doExplore(AgentState agentState, AgentAction agentAction) {
        if (moveCoordinate == null) {
            HashMap<String, ArrayList<Coordinate>> neighbours = lookForWalls(agentState.getPerception());
            Collections.shuffle(neighbours.get("free"));

            for (Coordinate freeCell : neighbours.get("free")) {
                if (closeTo(freeCell, neighbours.get("walls")) && !graph.nodeExists(freeCell)) {
                    // step freecell
                }
            }
            System.out.println("Hej");
        }
    }

    private boolean closeTo(Coordinate freeCell, ArrayList<Coordinate> walls) {
        for (Coordinate wall : walls) {
            int distX = Math.abs(wall.getX() - freeCell.getX());
            int distY = Math.abs(wall.getY() - freeCell.getY());

            // If at least one wall is
            if (distX == 1 || distY == 1) {
                return true;
            }
        }
        return false;
    }

    private HashMap<String, ArrayList<Coordinate>> lookForWalls(Perception perception) {
        HashMap<String, ArrayList<Coordinate>> moves = new HashMap<>();
        moves.put("walls", new ArrayList<>());
        moves.put("free", new ArrayList<>());
        for (Coordinate move : possibleMoves) {
            if (perception.getCellPerceptionOnRelPos(move.getX(), move.getY()) == null) {
                moves.get("walls").add(move);
            }
            else if (Objects.requireNonNull(perception.getCellPerceptionOnRelPos(move.getX(), move.getY())).isWalkable()){
                moves.get("free").add(move);
            }
        }
        return moves;
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

    private void handlePerception(AgentState agentState) {
        var perception = agentState.getPerception();
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);

                if (cell != null) {
                    // If packet in perception cell and new packet
                    if (cell.containsPacket() && packetCells.get(getCoord(cell)) == null) {
                        PacketRep rep = Objects.requireNonNull(cell.getRepOfType(PacketRep.class));
                        packetCells.put(getCoord(cell), rep.getColor());
                        System.out.println("Added new packet");
                    }
                    else if (!cell.containsPacket() && packetCells.get(getCoord(cell)) != null) {
                        // Remove positions where packets have disappeared.
                        packetCells.remove(getCoord(cell));

                        // If agent is moving towards a disappeared packet -> remove the move coordinate.
                        if (getCoord(cell).equals(moveCoordinate)) {
                            moveCoordinate = null;
                        }
                    }

                    // If destination in cell
                    if (cell.containsAnyDestination() && !graph.nodeExists(cell.getX(), cell.getY())) {
                        DestinationRep rep = Objects.requireNonNull(cell.getRepOfType(DestinationRep.class));

                        // If this destination is not already in the graph -> add it.
                        addDestinationToGraph(agentState, cell, rep.getColor());

                    }
                }
            }
        }
    }

    private void addDestinationToGraph(AgentState agentState, CellPerception destCell, Color destinationColor) {
        List<Coordinate> possibleCoords = getPossibleCoordsAround(destCell, agentState);
        List<Node> newlyAdded = new ArrayList<>();
        for (Coordinate c : possibleCoords) {
            Node n = graph.addNode(c);
            newlyAdded.add(n);
        }
        addPossibleEdges(newlyAdded); // TODO: What if list is empty?

        // Create node for current agent position and add edge to one of the closest one.
        Node n = graph.addNode(new Coordinate(agentState.getX(), agentState.getY()));
        graph.addEdge(n.getPosition(), graph.closestNode(newlyAdded, n).getPosition());
        // System.out.println("hej");
    }

    private void addPossibleEdges(List<Node> newlyAdded) {
        for (int i = 0; i < newlyAdded.size(); i++) {
            for (int j = i; j < newlyAdded.size(); j++) {
                Node n1 = newlyAdded.get(i);
                Node n2 = newlyAdded.get(j);
                if (i != j && graph.distance(n1, n2) <= 1) {
                    graph.addEdge(n1.getPosition(), n2.getPosition());
                }
            }
        }
    }

    private Coordinate getCoord(CellPerception cell) {
        return new Coordinate(cell.getX(), cell.getY());
    }

    private List<Coordinate> getPossibleCoordsAround(CellPerception destCell, AgentState agentState) {
        List<Coordinate> moves = new ArrayList<>(List.of(
                new Coordinate(1, 0), new Coordinate(-1, 0),
                new Coordinate(0, 1), new Coordinate(0, -1)
        ));
        List<Coordinate> possibleCoords = new ArrayList<>();
        for (Coordinate move : moves) {
            int x = destCell.getX() + move.getX();
            int y = destCell.getY() + move.getY();
            if (agentState.getPerception().getCellPerceptionOnAbsPos(x, y) != null &&
                    Objects.requireNonNull(agentState.getPerception().getCellPerceptionOnAbsPos(x, y)).isWalkable())
                possibleCoords.add(new Coordinate(x, y));
        }
        return possibleCoords;
    }


}

