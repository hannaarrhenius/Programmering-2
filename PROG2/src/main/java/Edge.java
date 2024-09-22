// PROG2 VT2023, Inlämningsuppgift, del 1
// Grupp 031
// Hanna Arrhenius haar9434
// Erik Strandberg erst1916
// Robin Westling rowe7856
import java.io.Serializable;

public class Edge<T> implements Serializable {
    private final T node;
    private final String name;
    private int weight;

    public Edge(T node, String name, int weight){
        this.name = name;
        this.node = node;
        this.weight = weight;
    }

    public T getDestination() {
        return node;
    }

    public int getWeight(){
        return weight;
    }

    public void setWeight(int weight){
        if(weight < 0){
            throw new IllegalArgumentException();
        }else {
            this.weight = weight;
        }
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString(){
        return "to " + node + " by " + name + " takes " + weight;
    }
}