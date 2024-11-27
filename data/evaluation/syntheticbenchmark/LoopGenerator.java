package syntheticbenchmark;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LoopGenerator {

  public static void generateLoopClass(int nestedLoops, int sequentialLoops,
                                       String assignment) {
    StringBuilder classContent = new StringBuilder();
    classContent.append("public class Loop {\n");
    classContent.append("    public void executeLoops() {\n");

    // Generate nested loops
    for (int i = 0; i < nestedLoops; i++) {
      if(i==0){
        classContent.append(generateIndentation(i))
            .append("for (Integer i")
            .append(i)
            .append(" = null; i")
            .append(i)
            .append(" < 10; i")
            .append(i)
            .append("++) {\n");
      }
      else{
        classContent.append(generateIndentation(i))
            .append("for (Integer i")
            .append(i)
            .append(" = i")
            .append(i-1)
            .append("; i")
            .append(i)
            .append(" < 10; i")
            .append(i)
            .append("++) {\n");
      }

    }

    // Assignment
    classContent.append(generateIndentation(nestedLoops))
        .append("String $tmp = i")
        .append(nestedLoops - 1)
        .append(".toString();\n");
    classContent.append(generateIndentation(nestedLoops))
    .append("$tmp.toString();\n");
      
        

    // Close nested loops
    for (int i = nestedLoops - 1; i >= 0; i--) {
      classContent.append(generateIndentation(i)).append("}\n");
    }

    // Generate sequential loops
    for (int i = 0; i < sequentialLoops; i++) {
      classContent.append("        for (int j")
          .append(i)
          .append(" = 0; j")
          .append(i)
          .append(" < 10; j")
          .append(i)
          .append("++) {\n");
      classContent.append("            String $tmp")
          .append(i)
          .append(" = i")
          .append(i)
          .append(".toString();\n");
      classContent.append("        }\n");
    }

    classContent.append("    }\n");
    classContent.append("}\n");

    // Write to file
    try (BufferedWriter writer =
             new BufferedWriter(new FileWriter("Loop.java"))) {
      writer.write(classContent.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String generateIndentation(int level) {
    StringBuilder indentation = new StringBuilder();
    for (int i = 0; i < level + 2; i++) {
      indentation.append("    ");
    }
    return indentation.toString();
  }

  public static void main(String[] args) {
    // Example usage
    int nestedLoops =
        Integer.parseInt(args[0]); // Number of nested loops (e.g. 2
    int sequentialLoops =
        Integer.parseInt(args[1]); // Number of sequential loops (e.g. 3)
    String assignment =
        "\"test\""; // Assignment value, use "null" for a null assignment

    generateLoopClass(nestedLoops, sequentialLoops, assignment);
  }
}
