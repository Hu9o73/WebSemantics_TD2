package applications;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import tools.JenaEngine;
import tools.JenaEngineEnhancement;

public class PersonInfoApp {

    /**
     * Main method to run the person information application
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        String NS = "";
        
        // Read the ontology model
        Model model = JenaEngine.readModel("data/family.owl.xml");
        
        if (model == null) {
            System.out.println("Error: Unable to read the ontology model.");
            return;
        }
        
        // Get the namespace
        NS = model.getNsPrefixURI("");
        
        // Apply OWL and custom rules for inference
        Model owlInferencedModel = JenaEngine.readInferencedModelFromRuleFile(model, "data/owlrules.txt");
        if (owlInferencedModel == null) {
            System.out.println("Error: Unable to apply OWL rules.");
            return;
        }
        
        Model inferredModel = JenaEngine.readInferencedModelFromRuleFile(owlInferencedModel, "data/rules.txt");
        if (inferredModel == null) {
            System.out.println("Error: Unable to apply custom rules.");
            return;
        }
        
        // User input scanner
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Get person name from user
            System.out.print("Enter the name of a person: ");
            String personName = scanner.nextLine().trim();
            
            // Find the person in the ontology
            Resource person = findPersonByName(inferredModel, NS, personName);
            
            if (person == null) {
                System.out.println("Person not found: " + personName);
                return;
            }
            
            // Display person's basic information
            displayPersonInfo(inferredModel, NS, person);
            
            // Display parents
            displayParents(inferredModel, NS, person);
            
            // Display siblings
            displaySiblings(inferredModel, NS, person);
            
            // Check marriage status
            boolean isMarried = isPersonMarried(inferredModel, NS, person);
            
            if (isMarried) {
                // Display spouse information
                displaySpouseInfo(inferredModel, NS, person);
            } else {
                // Display potential matches
                displayPotentialMatches(inferredModel, NS, person);
            }
            
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Find a person resource by name
     * @param model The model
     * @param namespace The namespace
     * @param name The person's name
     * @return The person resource or null if not found
     */
    private static Resource findPersonByName(Model model, String namespace, String name) {
        Property nameProp = model.getProperty(namespace + "name");
        
        StmtIterator stmts = model.listStatements(null, nameProp, name);
        if (stmts.hasNext()) {
            return stmts.next().getSubject();
        }
        
        return null;
    }
    
    /**
     * Display basic information about a person
     * @param model The model
     * @param namespace The namespace
     * @param person The person resource
     */
    private static void displayPersonInfo(Model model, String namespace, Resource person) {
        Property nameProp = model.getProperty(namespace + "name");
        Property ageProp = model.getProperty(namespace + "age");
        Property nationalityProp = model.getProperty(namespace + "nationality");
        
        // Get name
        Statement nameStmt = person.getProperty(nameProp);
        String name = nameStmt != null ? nameStmt.getString() : "Unknown";
        
        // Get age
        Statement ageStmt = person.getProperty(ageProp);
        int age = ageStmt != null ? ageStmt.getInt() : 0;
        
        // Get nationality
        Statement nationalityStmt = person.getProperty(nationalityProp);
        String nationality = nationalityStmt != null ? nationalityStmt.getString() : "Unknown";
        
        System.out.println("\n--- Person Information ---");
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        if (!nationality.equals("Unknown")) {
            System.out.println("Nationality: " + nationality);
        }
        
        // Get gender
        Property typeProperty = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        StmtIterator typeStmts = person.listProperties(typeProperty);
        
        String gender = "Unknown";
        while (typeStmts.hasNext()) {
            Statement typeStmt = typeStmts.next();
            String type = typeStmt.getObject().toString();
            
            if (type.endsWith("Male")) {
                gender = "Male";
                break;
            } else if (type.endsWith("Female")) {
                gender = "Female";
                break;
            }
        }
        
        System.out.println("Gender: " + gender);
    }
    
    /**
     * Display a person's parents
     * @param model The model
     * @param namespace The namespace
     * @param person The person resource
     */
    private static void displayParents(Model model, String namespace, Resource person) {
        List<Resource> parents = new ArrayList<>();
        
        // Find father
        Property isSonOfProp = model.getProperty(namespace + "isSonOf");
        Property isDaughterOfProp = model.getProperty(namespace + "isDaughterOf");
        
        // Check if the person has isSonOf properties
        StmtIterator sonOfStmts = person.listProperties(isSonOfProp);
        while (sonOfStmts.hasNext()) {
            Statement stmt = sonOfStmts.next();
            if (stmt.getObject().isResource()) {
                parents.add(stmt.getObject().asResource());
            }
        }
        
        // Check if the person has isDaughterOf properties
        StmtIterator daughterOfStmts = person.listProperties(isDaughterOfProp);
        while (daughterOfStmts.hasNext()) {
            Statement stmt = daughterOfStmts.next();
            if (stmt.getObject().isResource()) {
                parents.add(stmt.getObject().asResource());
            }
        }
        
        // Display parents information
        if (!parents.isEmpty()) {
            System.out.println("\n--- Parents ---");
            
            Property nameProp = model.getProperty(namespace + "name");
            Property ageProp = model.getProperty(namespace + "age");
            
            for (Resource parent : parents) {
                Statement nameStmt = parent.getProperty(nameProp);
                Statement ageStmt = parent.getProperty(ageProp);
                
                String name = nameStmt != null ? nameStmt.getString() : "Unknown";
                int age = ageStmt != null ? ageStmt.getInt() : 0;
                
                System.out.println("  - " + name + " (Age: " + age + ")");
            }
        } else {
            System.out.println("\nNo parents found.");
        }
    }
    
    /**
     * Display a person's siblings
     * @param model The model
     * @param namespace The namespace
     * @param person The person resource
     */
    private static void displaySiblings(Model model, String namespace, Resource person) {
        List<Resource> siblings = new ArrayList<>();
        List<Resource> parents = new ArrayList<>();
        
        // Get the person's parents
        Property isSonOfProp = model.getProperty(namespace + "isSonOf");
        Property isDaughterOfProp = model.getProperty(namespace + "isDaughterOf");
        
        // Add all parents from isSonOf
        StmtIterator sonOfStmts = person.listProperties(isSonOfProp);
        while (sonOfStmts.hasNext()) {
            Statement stmt = sonOfStmts.next();
            if (stmt.getObject().isResource()) {
                parents.add(stmt.getObject().asResource());
            }
        }
        
        // Add all parents from isDaughterOf
        StmtIterator daughterOfStmts = person.listProperties(isDaughterOfProp);
        while (daughterOfStmts.hasNext()) {
            Statement stmt = daughterOfStmts.next();
            if (stmt.getObject().isResource()) {
                parents.add(stmt.getObject().asResource());
            }
        }
        
        // If no parents found, we can't find siblings
        if (parents.isEmpty()) {
            System.out.println("\nNo siblings found (no parents).");
            return;
        }
        
        // Find all siblings by finding other children of the parents
        for (Resource parent : parents) {
            // Find all resources that have isSonOf pointing to this parent
            StmtIterator sonsStmt = model.listStatements(null, isSonOfProp, parent);
            while (sonsStmt.hasNext()) {
                Resource sibling = sonsStmt.next().getSubject();
                if (!sibling.equals(person) && !siblings.contains(sibling)) {
                    siblings.add(sibling);
                }
            }
            
            // Find all resources that have isDaughterOf pointing to this parent
            StmtIterator daughtersStmt = model.listStatements(null, isDaughterOfProp, parent);
            while (daughtersStmt.hasNext()) {
                Resource sibling = daughtersStmt.next().getSubject();
                if (!sibling.equals(person) && !siblings.contains(sibling)) {
                    siblings.add(sibling);
                }
            }
        }
        
        // Display siblings information
        if (!siblings.isEmpty()) {
            System.out.println("\n--- Brothers and Sisters ---");
            
            Property nameProp = model.getProperty(namespace + "name");
            Property ageProp = model.getProperty(namespace + "age");
            Property typeProperty = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            Resource maleClass = model.getResource(namespace + "Male");
            
            for (Resource sibling : siblings) {
                Statement nameStmt = sibling.getProperty(nameProp);
                Statement ageStmt = sibling.getProperty(ageProp);
                
                String name = nameStmt != null ? nameStmt.getString() : "Unknown";
                int age = ageStmt != null ? ageStmt.getInt() : 0;
                
                // Determine if brother or sister
                String relation = "Sibling";
                StmtIterator typeStmts = sibling.listProperties(typeProperty);
                
                while (typeStmts.hasNext()) {
                    Statement typeStmt = typeStmts.next();
                    String type = typeStmt.getObject().toString();
                    
                    if (type.endsWith("Male")) {
                        relation = "Brother";
                        break;
                    } else if (type.endsWith("Female")) {
                        relation = "Sister";
                        break;
                    }
                }
                
                System.out.println("  - " + name + " (" + relation + ", Age: " + age + ")");
            }
        } else {
            System.out.println("\nNo siblings found.");
        }
    }
    
    /**
     * Check if a person is married
     * @param model The model
     * @param namespace The namespace
     * @param person The person resource
     * @return True if the person is married, false otherwise
     */
    private static boolean isPersonMarried(Model model, String namespace, Resource person) {
        Property isMarriedWithProp = model.getProperty(namespace + "isMarriedWith");
        return person.hasProperty(isMarriedWithProp);
    }
    
    /**
     * Display information about a person's spouse
     * @param model The model
     * @param namespace The namespace
     * @param person The person resource
     */
    private static void displaySpouseInfo(Model model, String namespace, Resource person) {
        Property isMarriedWithProp = model.getProperty(namespace + "isMarriedWith");
        
        Statement marriedStmt = person.getProperty(isMarriedWithProp);
        if (marriedStmt != null && marriedStmt.getObject().isResource()) {
            Resource spouse = marriedStmt.getObject().asResource();
            
            Property nameProp = model.getProperty(namespace + "name");
            Property ageProp = model.getProperty(namespace + "age");
            
            Statement nameStmt = spouse.getProperty(nameProp);
            Statement ageStmt = spouse.getProperty(ageProp);
            
            String name = nameStmt != null ? nameStmt.getString() : "Unknown";
            int age = ageStmt != null ? ageStmt.getInt() : 0;
            
            System.out.println("\n--- Spouse ---");
            System.out.println("  - " + name + " (Age: " + age + ")");
        }
    }
    
    /**
     * Display potential matches for a single person
     * @param model The model
     * @param namespace The namespace
     * @param person The person resource
     */
    private static void displayPotentialMatches(Model model, String namespace, Resource person) {
        // Determine gender of the person
        Property typeProperty = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Resource maleClass = model.getResource(namespace + "Male");
        Resource femaleClass = model.getResource(namespace + "Female");
        
        boolean isMale = person.hasProperty(typeProperty, maleClass);
        boolean isFemale = person.hasProperty(typeProperty, femaleClass);
        
        if (!isMale && !isFemale) {
            System.out.println("\nUnable to determine gender for matching.");
            return;
        }
        
        // Get the age of the person
        Property ageProp = model.getProperty(namespace + "age");
        Statement ageStmt = person.getProperty(ageProp);
        
        if (ageStmt == null) {
            System.out.println("\nUnable to determine age for matching.");
            return;
        }
        
        int personAge = ageStmt.getInt();
        
        // Find potential matches - opposite gender, not married, age within +/- 5 years
        List<Resource> matches = new ArrayList<>();
        
        // Get opposite gender class
        Resource oppositeGenderClass = isMale ? femaleClass : maleClass;
        
        // Find all persons of opposite gender
        StmtIterator potentialMatches = model.listStatements(null, typeProperty, oppositeGenderClass);
        Property isMarriedWithProp = model.getProperty(namespace + "isMarriedWith");
        
        while (potentialMatches.hasNext()) {
            Resource potentialMatch = potentialMatches.next().getSubject();
            
            // Skip if married
            if (potentialMatch.hasProperty(isMarriedWithProp)) {
                continue;
            }
            
            // Check age
            Statement matchAgeStmt = potentialMatch.getProperty(ageProp);
            if (matchAgeStmt != null) {
                int matchAge = matchAgeStmt.getInt();
                
                // Check if age is within +/- 5 years
                if (Math.abs(personAge - matchAge) <= 5) {
                    matches.add(potentialMatch);
                }
            }
        }
        
        // Display potential matches
        if (!matches.isEmpty()) {
            System.out.println("\n--- Potential Matches ---");
            System.out.println("Single persons of opposite gender with age +/- 5 years:");
            
            Property nameProp = model.getProperty(namespace + "name");
            
            for (Resource match : matches) {
                Statement nameStmt = match.getProperty(nameProp);
                Statement matchAgeStmt = match.getProperty(ageProp);
                
                String name = nameStmt != null ? nameStmt.getString() : "Unknown";
                int age = matchAgeStmt != null ? matchAgeStmt.getInt() : 0;
                
                System.out.println("  - " + name + " (Age: " + age + ")");
            }
        } else {
            System.out.println("\nNo potential matches found.");
        }
    }
}