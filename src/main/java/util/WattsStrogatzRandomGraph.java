package util;/*
 * util.WattsStrogatzRandomGraph.java
 * Created Aug 6, 2010
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Provides methods for generating a Watts-Strogatz Random Graph
 * @author Elisha Peterson
 */
public class WattsStrogatzRandomGraph {

    /**
     * @param n number of nodes in resulting graph
     * @param deg average degree of resulting graph
     * @param rewiring probability of edge-rewiring in resulting graph
     * @return 
     * @return new instance of a Watts-Strogatz random graph
     */
    public static ArrayList<Integer[]> connectSmallWorld(int n, int deg, float rewiring) {
        if (n < 1)
            throw new IllegalArgumentException("Invalid order = " + n);
        if (deg % 2 != 0) {
            //System.out.println("Degree must be an even integer: changing from " + deg + " to " + (deg-1));
            deg = deg-1;
        }
        if (deg < 0 || deg > n-1)
            throw new IllegalArgumentException("Degree outside of range [0, " + (n-1) + "]");
        if (rewiring < 0 || rewiring > 1)
            throw new IllegalArgumentException("Invalid rewiring parameter = " + rewiring + " (should be between 0 and 1)");

        ArrayList<Integer[]> edges = new ArrayList<Integer[]>();
        for (int i = 0; i < n; i++)
            for (int off = 1; off <= (deg/2); off++)
                edges.add(new Integer[]{i, (i+off)%n});
        // could stop here for a regular ring lattice graph

        // generate list of edges to rewire
        for (Integer[] e : edges)
            if (Math.random() < rewiring)
                randomlyRewire(edges, e, n);

        
        return edges;
        //return GraphFactory.getGraph(false, GraphFactory.intList(n), edges);
    }

    /** 
     * Randomly rewires the specified edge, by randomly moving
     * one of the edge's endpoints, provided the resulting edge does not already exist in the set.
     * @param edges current list of edges
     * @param e the edge to rewire
     * @param n total # of vertices
     * @return new edge.
     */
    private static void randomlyRewire(ArrayList<Integer[]> edges, Integer[] e, int n) {
        Integer[] potential = new Integer[] {e[0], e[1]};
        TreeSet<Integer[]> edgeTree = new TreeSet<Integer[]>(PAIR_COMPARE_UNDIRECTED);
        edgeTree.addAll(edges);
        while (edgeTree.contains(potential)) {
            if (Math.random() < .5)
                potential = new Integer[] {e[0], randomNot(e[0], n)};
            else
                potential = new Integer[] {randomNot(e[1], n), e[1]};
        }
        e[0] = potential[0];
        e[1] = potential[1];
    }

    /** @returns a random value between 0 and n-1, not including exclude */
    private static int randomNot(int exclude, int n) {
        int result = exclude;
        while (result == exclude || result == n)
            result = (int) Math.floor(n*Math.random());
        return result;
    }
    
    static final Comparator<Integer[]> PAIR_COMPARE_UNDIRECTED = new Comparator<Integer[]>() {
        public int compare(Integer[] o1, Integer[] o2) {
            if (o1.length != 2 || o2.length != 2)
                throw new IllegalStateException("This object only compares integer pairs.");
            int min1 = Math.min(o1[0], o1[1]);
            int min2 = Math.min(o2[0], o2[1]);
            return min1==min2 ? Math.max(o1[0],o1[1])-Math.max(o2[0],o2[1]) : min1-min2;
        }
    };


}
