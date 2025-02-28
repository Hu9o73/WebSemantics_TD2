package tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * Enhanced JenaEngine class with new functionality
 * to read ObjectType and DataType property values
 */
public class JenaEngineEnhancement {
    
    /**
     * Read values of an ObjectType property for a given instance
     * @param model The Jena model
     * @param namespace The ontology namespace
     * @param instanceName The name of the instance
     * @param propertyName The name of the property
     * @return List of resource URIs that are values of the property
     */
    public static List<String> readObjectPropertyValues(Model model, String namespace, 
                                                       String instanceName, String propertyName) {
        List<String> values = new ArrayList<>();
        
        // Get the resource corresponding to the instance
        Resource instance = model.getResource(namespace + instanceName);
        
        // Get the property
        Property property = model.getProperty(namespace + propertyName);
        
        if (instance != null && property != null) {
            // Get all values of the property for the instance
            StmtIterator stmtIterator = instance.listProperties(property);
            
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                RDFNode objectNode = statement.getObject();
                
                if (objectNode.isResource()) {
                    // Add the URI of the resource (object value)
                    values.add(objectNode.asResource().getURI());
                }
            }
        }
        
        return values;
    }
    
    /**
     * Read values of a DataType property for a given instance
     * @param model The Jena model
     * @param namespace The ontology namespace
     * @param instanceName The name of the instance
     * @param propertyName The name of the property
     * @return List of literal values of the property
     */
    public static List<Object> readDataPropertyValues(Model model, String namespace, 
                                                    String instanceName, String propertyName) {
        List<Object> values = new ArrayList<>();
        
        // Get the resource corresponding to the instance
        Resource instance = model.getResource(namespace + instanceName);
        
        // Get the property
        Property property = model.getProperty(namespace + propertyName);
        
        if (instance != null && property != null) {
            // Get all values of the property for the instance
            NodeIterator nodeIterator = model.listObjectsOfProperty(instance, property);
            
            while (nodeIterator.hasNext()) {
                RDFNode node = nodeIterator.next();
                
                if (node.isLiteral()) {
                    // Add the value of the literal
                    values.add(node.asLiteral().getValue());
                }
            }
        }
        
        return values;
    }
    
    /**
     * Get name and age of a person by URI
     * @param model The Jena model
     * @param namespace The ontology namespace
     * @param personURI The URI of the person
     * @return Array containing name and age
     */
    public static Object[] getPersonInfo(Model model, String namespace, String personURI) {
        Object[] info = new Object[2]; // [name, age]
        
        Resource person = model.getResource(personURI);
        
        if (person != null) {
            Property nameProp = model.getProperty(namespace + "name");
            Property ageProp = model.getProperty(namespace + "age");
            
            Statement nameStmt = person.getProperty(nameProp);
            Statement ageStmt = person.getProperty(ageProp);
            
            if (nameStmt != null) {
                info[0] = nameStmt.getLiteral().getString();
            }
            
            if (ageStmt != null) {
                info[1] = ageStmt.getLiteral().getInt();
            }
        }
        
        return info;
    }
}