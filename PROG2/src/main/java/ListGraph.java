// PROG2 VT2023, Inlämningsuppgift, del 1
// Grupp 031
// Hanna Arrhenius haar9434
// Erik Strandberg erst1916
// Robin Westling rowe7856

import java.util.*;
import java.io.Serializable;

public class ListGraph<T> implements Graph<T>, Serializable {

    private final Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    public void add(T node) {
        nodes.putIfAbsent(node, new HashSet<>());
    }

    public void remove(T node) {
        if(!nodes.containsKey(node)){
            throw new NoSuchElementException();
        }
        for(T t : nodes.keySet()){
            if(getEdgeBetween(node, t) != null){
                disconnect(t, node);
            }
        }
        nodes.remove(node);
    }

    public void connect(T node1, T node2, String name, int weight) {
        if(!nodes.containsKey(node1) || !nodes.containsKey(node2))
            throw new NoSuchElementException();
        else if(weight<0)
            throw new IllegalArgumentException();
        else if(getEdgeBetween(node1, node2) != null)
            throw new IllegalStateException();
        else {
            nodes.get(node1).add(new Edge<>(node2, name, weight));
            nodes.get(node2).add(new Edge<>(node1, name, weight));
        }
    }

    public void disconnect(T node1, T node2) {
        if(!nodes.containsKey(node1) || !nodes.containsKey(node2))
            throw new NoSuchElementException();
        else if(getEdgeBetween(node1, node2) == null)
            throw new IllegalStateException();
        else{
            nodes.get(node1).remove(getEdgeBetween(node1, node2));
            nodes.get(node2).remove(getEdgeBetween(node2, node1));
        }
    }

    public void setConnectionWeight(T node1, T node2, int weight) {
        if(!nodes.containsKey(node1) || !nodes.containsKey(node2))
            throw new NoSuchElementException();
        else if(getEdgeBetween(node1, node2) == null){
            throw new NoSuchElementException();
        }
        else if(weight<0)
            throw new IllegalArgumentException();
        else{
            getEdgeBetween(node1, node2).setWeight(weight);
            getEdgeBetween(node2, node1).setWeight(weight);
        }
    }

    public Set<T> getNodes() {
        return nodes.keySet();
    }

    public boolean pathExists(T from, T to) {
        if(!nodes.containsKey(from) || !nodes.containsKey(to))
            return false;
        Set<T> visited = new HashSet<>();
        search(from, to, visited);
        return visited.contains(to);
    }

    private void search(T current, T searchedFor, Set<T> visited) {
        visited.add(current);
        if(nodes.get(current).equals(nodes.get(searchedFor))) {
            visited.add(current);
        }
        for (Edge<T> edge : nodes.get(current)) {
            if (!visited.contains(edge.getDestination())) {
                search(edge.getDestination(), searchedFor, visited);
            }
        }
    }

    public List<Edge<T>> getPath(T from, T to) {
        Map<T, T> connections = new HashMap<>();
        Map<T, Integer> weights = new HashMap<>();
        LinkedList<T> queue = new LinkedList<>();

        connections.put(from, null);
        weights.put(from, 0);
        queue.add(from);

        while (!queue.isEmpty()) {
            T t = queue.pollFirst();
            for (Edge<T> edge : nodes.get(t)) {
                T destination = edge.getDestination();
                int weightToDestination = weights.get(t) + edge.getWeight();
                if (!weights.containsKey(destination) || weightToDestination < weights.get(destination)) {
                    connections.put(destination, t);
                    weights.put(destination, weightToDestination);
                    queue.add(destination);
                }
            }
        }
        if (!connections.containsKey(to)) {
            return null;
        }
        return gatherPath(from, to, connections);
    }

    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> connections) {
        LinkedList<Edge<T>> path = new LinkedList<>();
        T current = to;

        while (!current.equals(from)) {
            T next = connections.get(current);
            Edge<T> edge = getEdgeBetween(next, current);
            path.addFirst(edge);
            current = next;
        }
        return path;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T t : nodes.keySet()) {
            sb.append(t).append(":").append(nodes.get(t)).append("\n");
        }
        return sb.toString();
    }

    public Collection<Edge<T>> getEdgesFrom(T node) {
        if(!nodes.containsKey(node)) {
            throw new NoSuchElementException();
        }else {
            Collection<Edge<T>> edges = new HashSet<>();
            for(Edge edge : nodes.get(node)){
                edges.add(edge);
            }
            return edges;
        }
    }

    public Edge<T> getEdgeBetween(T node1, T node2) {
        if(!nodes.containsKey(node1) || !nodes.containsKey(node2)){
            throw new NoSuchElementException();
        }
        for (Edge<T> edge : nodes.get(node1)){
            if (edge.getDestination().equals(node2)){
                return edge;
            }
        }
        return null;
    }
}
