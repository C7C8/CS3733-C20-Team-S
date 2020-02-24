package edu.wpi.cs3733.c20.teamS.pathfinding;

import com.google.common.graph.MutableGraph;
import edu.wpi.cs3733.c20.teamS.database.NodeData;

public class PathingContext {

    IPathfinder algorithm;
    public PathingContext(IPathfinder alg){
        this.algorithm = alg;
    }

    public Path executePathfind(MutableGraph<NodeData> graph, NodeData start, NodeData goal){
        return algorithm.findPath(graph, start, goal);
    }
}
