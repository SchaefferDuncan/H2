import javafx.util.Pair;
import java.util.ArrayList;

public class ListAndCount {
    public ArrayList<Pair<String,String>> edges;
    public int numOfNodes;
    public int numOfBrackets;

    public ListAndCount(ArrayList<Pair<String,String>> e, int n, int b){
        this.edges = e;
        this.numOfNodes = n;
        this.numOfBrackets = b;
    }

    public ArrayList<Pair<String, String>> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Pair<String, String>> edges) {
        this.edges = edges;
    }

    public int getNumOfNodes() {
        return numOfNodes;
    }

    public void setNumOfNodes(int numOfNodes) {
        this.numOfNodes = numOfNodes;
    }

    public int getNumOfBrackets() {
        return numOfBrackets;
    }

    public void setNumOfBrackets(int numOfBrackets) {
        this.numOfBrackets = numOfBrackets;
    }
}
