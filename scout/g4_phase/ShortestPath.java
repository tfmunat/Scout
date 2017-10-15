package scout.g4_phase;

//A Java program for Dijkstra's single source shortest path algorithm.
//The program is for adjacency matrix representation of the graph
import java.util.*;
import java.lang.*;
import java.io.*;

class ShortestPath
{
 // A utility function to find the vertex with minimum distance value,
 // from the set of vertices not yet included in shortest path tree
 int V=9;
ArrayList<Integer> paths;
 int dist;
 int minDistance(int dist[], Boolean sptSet[])
 {
     // Initialize min value
     int min = Integer.MAX_VALUE, min_index=-1;

     for (int v = 0; v < V; v++)
         if (sptSet[v] == false && dist[v] <= min)
         {
             min = dist[v];
             min_index = v;
         }

     return min_index;
 }

 // A utility function to print the constructed distance array
 void printSolution(int dist[], int n)
 {
     System.out.println("Vertex   Distance from Source");
     for (int i = 0; i < V; i++)
         System.out.println(i+" tt "+dist[i]);
 }

 // Funtion that implements Dijkstra's single source shortest path
 // algorithm for a graph represented using adjacency matrix
 // representation
void dijkstra(int graph[][], int src, int target)
 {
	 V = graph.length; //Should be an n+1 by n+1 array of arrays so doesn't matter where
	 // I take measurement.
     int dist[] = new int[V]; // The output array. dist[i] will hold
                              // the shortest distance from src to i
     HashMap<String,ArrayList<Integer>> paths=new HashMap<String,ArrayList<Integer>>();
     // sptSet[i] will true if vertex i is included in shortest
     // path tree or shortest distance from src to i is finalized
     Boolean sptSet[] = new Boolean[V];

     // Initialize all distances as INFINITE and stpSet[] as false
     for (int i = 0; i < V; i++)
     {
         dist[i] = Integer.MAX_VALUE;
         sptSet[i] = false;
     }

     // Distance of source vertex from itself is always 0
     dist[src] = 0;

     // Find shortest path for all vertices
     for (int count = 0; count < V-1; count++)
     {
         // Pick the minimum distance vertex from the set of vertices
         // not yet processed. u is always equal to src in first
         // iteration.
         int u = minDistance(dist, sptSet);

         // Mark the picked vertex as processed
         sptSet[u] = true;
         if(u == target) {
        	 break;// break early because we got to where we wanted to go. Don't care about
        	 // getting anywhere else.
         }
         // Update dist value of the adjacent vertices of the
         // picked vertex.
         for (int v = 0; v < V; v++)

             // Update dist[v] only if is not in sptSet, there is an
             // edge from u to v, and total weight of path from src to
             // v through u is smaller than current value of dist[v]
             if (!sptSet[v] && graph[u][v]!=0 && dist[u] != Integer.MAX_VALUE && dist[u]+graph[u][v] < dist[v]) {
                 dist[v] = dist[u] + graph[u][v];
                 if (paths.get(Integer.toString(v)) == null) { //gets the value for an id)
                	 paths.put(Integer.toString(v), new ArrayList<Integer>()); //no ArrayList assigned, create new ArrayList

                	 paths.get(Integer.toString(v)).add(u); //adds value to list.
                 }
             }
         		// TODO might be here that you want to record indices to travel to
         
     }

     // print the constructed distance array
//     printSolution(dist, V);
     this.dist = dist[target];
     this.paths = paths.get(Integer.toString(target));
 }
 
 
 
// ShortestPath t = new ShortestPath();
// t.dijkstra(graph, 0);
 
}
