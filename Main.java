import javafx.util.Pair;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            if(!args[0].contains(".dot") && !args[0].contains(".java")){
                System.err.println("ERROR: Invalid file type passed");
                System.err.println("Only <filename>.dot OR <filename>.java are compatible!");
            } else {
                // INITIALIZE THE READER AND WRITER ALONG WITH THE LIST OF EDGES
                Scanner in = null;
                PrintWriter writer = null;
                int methodCount = 0;
                ArrayList<Pair<String,String>> thyEdges = new ArrayList<Pair<String,String>>();
                try {
                    in = new Scanner(new File(args[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // STRIP THE FILE EXTENSION AND INITIALIZE WRITER
                String simpleFileName = args[0].split("[.]")[0];

                // BRANCH OFF TO ANALYZE FILE DEPENDING ON FORMAT
                if(args[0].contains(".dot")){
                    try {
                        writer = new PrintWriter(simpleFileName + ".csv", "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    convertDotToOxygen(in, writer, thyEdges);
                    outputEdges(writer,thyEdges);
                } else {
                    while(in.hasNext()){
                        String tempL = in.nextLine();
                        if(tempL.contains("{") && !tempL.contains("class")){
                            try {
                                writer = new PrintWriter(simpleFileName + methodCount + ".csv", "UTF-8");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("- START OF CONVERSION -");
                            convertJavaToOxygen(in, writer, thyEdges);
                            System.out.println("- END OF CONVERSION -");
                            methodCount++;
                            System.out.println(simpleFileName + (methodCount-1) + ".csv");
                            assert writer != null;
                            outputEdges(writer,thyEdges);
                            writer.close();
                            thyEdges = new ArrayList<Pair<String, String>>();
                        }
                    }
                }

                // CLOSE OFF THE OUTPUT FILE
                writer.close();
            }
        } catch(Exception e){
            System.err.println("ERROR: Invalid parameters passed!");
        }
    }

    public static void convertDotToOxygen(Scanner fileIn, PrintWriter fileOut, ArrayList<Pair<String,String>> edgeList){
        // PARSE AND CORRECTLY READ IN DATA
        while(fileIn.hasNext()){
            String temp = fileIn.nextLine();
            if(temp.contains("->")){
                String [] temp2 = temp.split(" ");
                edgeList.add(new Pair<String,String>(temp2[0],temp2[2]));
            }
        }
    }

    public static void convertJavaToOxygen(Scanner fileIn, PrintWriter fileOut, ArrayList<Pair<String,String>> edgeList){
        // PARSE AND CORRECTLY READ IN DATA
        int nodeCount = 0;
        int bCount = 1;
        if(fileIn.hasNext()){
            ListAndCount tLaC = levelAnalysis(fileIn, bCount, edgeList, nodeCount);
        } else {
            // ASSUMING THERE WAS NOTHING IN THE FILE
            System.err.println("ERROR: File was empty!");
        }
        edgeList.add(new Pair<String, String>("start", "0"));
    }

    public static ListAndCount levelAnalysis(Scanner fileIn, int bracketCount, ArrayList<Pair<String,String>> edgeList, int nodeCount){
        int pNC = nodeCount;
        boolean sameActivity = false;
        while(fileIn.hasNext()) {
            String temp = fileIn.nextLine();
            if(temp.trim().length() > 0){
                if(bracketCount <= 0){
                    break;
                }
                if (temp.contains("}") && !temp.contains("else") && !temp.contains("catch")) {
                    System.out.println("NC = " + nodeCount + " | BC = " + bracketCount + " | END RECURSION LEVEL | TEMP = " + temp);
                    sameActivity = false;
                    // ASSUMING END OF ONE LEVEL OR CHILDREN NODE
                    nodeCount--;
                    bracketCount--;
                    if(bracketCount <= 0){
//                        System.out.println("                                  NC = " + nodeCount + " | BC = " + bracketCount + " | BREAK NOW | TEMP = " + temp);
                        break;
                    }
                } else if (temp.contains("if(") && !temp.contains("else")) {
                    System.out.println("NC = " + nodeCount + " | BC = " + bracketCount + " | NEW RECURSION LEVEL | TEMP = " + temp);
                    sameActivity = false;
                    // ASSUMING BEGINNING OF LEVEL OR BRACKET
                    // ADD EDGE TO THE LIST
                    nodeCount++;
                    Pair<String, String> tPair = new Pair<String, String>(Integer.toString(pNC),Integer.toString(nodeCount));
                    edgeList.add(tPair);
                    // GO A LEVEL DEEP(ER)
                    ListAndCount tLaC = levelAnalysis(fileIn, bracketCount, edgeList, nodeCount);
                    edgeList = tLaC.getEdges();
                    nodeCount = tLaC.getNumOfNodes();
                    bracketCount = tLaC.getNumOfBrackets();
                    bracketCount++;
                } else if (temp.contains("else")) {
                    System.out.println("NC = " + nodeCount + " | BC = " + bracketCount + " | PART OF DECISION | TEMP = " + temp);
                    sameActivity = false;
                    // ASSUMING ANOTHER PART OF ORIGINAL DECISION ('ELSE IF' OR 'ELSE')
                    ListAndCount tLaC = levelAnalysis(fileIn, bracketCount, edgeList, nodeCount);
                    edgeList = tLaC.getEdges();
                    nodeCount = tLaC.getNumOfNodes();
                    bracketCount = tLaC.getNumOfBrackets();
                } else {
                    // ASSUMING ACTIVITY
                    if(temp.contains("try") || temp.contains("for(") || temp.contains("while(")){
                        bracketCount++;
                    }

                    if (!sameActivity) {
                        System.out.println("NC = " + nodeCount + " | BC = " + bracketCount + " | NEW ACTIVITY | TEMP = " + temp);
                        // ASSUMING NEW ACTIVITY
                        sameActivity = true;
                        nodeCount++;
                        // ADD EDGE TO THE LIST
                        Pair<String, String> tPair = new Pair<String, String>(Integer.toString(pNC),Integer.toString(nodeCount));
                        edgeList.add(tPair);
                    } else {
                        // ASSUMING THE SAME ACTIVITY
                        System.out.println("NC = " + nodeCount + " | BC = " + bracketCount + " | SAME ACTIVITY | TEMP = " + temp);
                    }
                }
            }
        }
        return new ListAndCount(edgeList, nodeCount, bracketCount);
    }

    public static void outputEdges(PrintWriter out, ArrayList<Pair<String,String>> thineEdges){
        // OUTPUT OF THE EDGES WITH AUTO-INCREMENTING STRING
        System.out.print("Nodes/Edges");
        assert out != null;
        out.print("Nodes/Edges");
        String prefixString = "";
        for(int i = 0; i < thineEdges.size(); i++){
            if(i % 26 == 0 && i != 0){
                prefixString += "A";
            }
            System.out.print(";" + prefixString + (char)((i % 26) + 'A'));
            out.print(";" + prefixString + (char)((i % 26) + 'A'));
        }
        System.out.println();
        out.println();

        // OUTPUT OF THE NODES AND THEIR CHILDREN
        for(int i = 0; i < thineEdges.size(); i++){
            try {
                System.out.print((Integer.valueOf(thineEdges.get(i).getKey())));
                out.print((Integer.valueOf(thineEdges.get(i).getKey())));
            } catch (Exception e){
                System.out.print(thineEdges.get(i).getKey());
                out.print(thineEdges.get(i).getKey());
            }

            for(int j = 0; j < thineEdges.size(); j++){
                if(i == j){
                    System.out.print(";" + thineEdges.get(i).getValue());
                    out.print(";" + thineEdges.get(i).getValue());
                } else {
                    System.out.print(";-");
                    out.print(";-");
                }
            }
            System.out.println();
            out.println();
        }
    }
}