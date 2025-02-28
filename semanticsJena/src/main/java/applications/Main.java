package applications;

import java.io.File;
import java.util.Arrays;

import org.apache.jena.rdf.model.Model;

import tools.JenaEngine;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String NS = "";
        // Read the model from the ontology
        Model model = JenaEngine.readModel("data/family.owl.xml");
        
        if (model != null) {
            // Read the Namespace of the ontology
            NS = model.getNsPrefixURI("");
            
            // Apply OWL rules
            Model owlInferencedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/owlrules.txt");
            
            // Apply our custom rules on the owlInferencedModel
            Model inferedModel = JenaEngine.readInferencedModelFromRuleFile(owlInferencedModel, "data/rules.txt");
            
            // Execute all Part 3 queries
            File queryDir = new File("data/part3");
            if (!queryDir.exists()) {
                // Create directory if it doesn't exist
                queryDir.mkdirs();
                
                // Create sample query files (for demonstration)
                System.out.println("Part 3 query directory created. Please place query files in the data/part3 directory.");
            } else {
                // Execute all query files in the directory
                File[] queryFiles = queryDir.listFiles((dir, name) -> name.startsWith("query") && name.endsWith(".txt"));
                
                if (queryFiles != null && queryFiles.length > 0) {
                    // Sort files to execute in order
                    Arrays.sort(queryFiles);
                    
                    for (File queryFile : queryFiles) {
                        System.out.println("\n==== Executing query: " + queryFile.getName() + " ====");
                        String result = JenaEngine.executeQueryFile(inferedModel, queryFile.getAbsolutePath());
                        System.out.println(result);
                    }
                } else {
                    System.out.println("No query files found in data/part3 directory.");
                }
            }
            
            // Original functionality
            System.out.println("\n==== Original Main Execution ====\n");
            
            // Modify the model
            // Add a new female to the model: Nora, 50, isDaughterOf Peter
            JenaEngine.createInstanceOfClass(model, NS, "Female", "Nora");
            JenaEngine.updateValueOfDataTypeProperty(model, NS, "Nora", "age", 50);
            JenaEngine.updateValueOfObjectProperty(model, NS, "Nora", "isDaughterOf", "Peter");

            // Add a new male to the model: Rob, 51, isMarriedWith Nora
            JenaEngine.createInstanceOfClass(model, NS, "Male", "Rob");
            JenaEngine.updateValueOfDataTypeProperty(model, NS, "Rob", "age", 51);
            JenaEngine.updateValueOfDataTypeProperty(model, NS, "Rob", "name", "Rob Yeung");
            JenaEngine.updateValueOfObjectProperty(model, NS, "Rob", "isMarriedWith", "Nora");
            
            // Apply OWL rules on the model
            Model owlInferencedModelOriginal = JenaEngine.readInferencedModelFromRuleFile(model, "data/owlrules.txt");
            
            // Apply our rules on the owlInferencedModel
            Model inferedModelOriginal = JenaEngine.readInferencedModelFromRuleFile(owlInferencedModelOriginal, "data/rules.txt");
            
            // Query on the model after inference
            System.out.println(JenaEngine.executeQueryFile(inferedModelOriginal, "data/query.txt"));
        } else {
            System.out.println("Error when reading model from ontology");
        }
    }
}