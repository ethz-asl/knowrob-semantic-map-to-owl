package org.knowrob.map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import org.knowrob.owl.JointInstance;
import org.knowrob.owl.ObjectInstance;

import org.knowrob.owl.utils.OWLImportExport;

/**
* Specialization of the Knowrob OWL file import/export used by the ROS 
* conversion service
* 
* @author Ralf Kaestner, ralf.kaestner@gmail.com
*
*/

public class SemanticMapToOWLExport extends OWLImportExport {
  public SemanticMapToOWLExport() {
  }
  
  @Override
  public OWLNamedIndividual createObjectInst(ObjectInstance mapObject,
      OWLOntology ontology) {
    OWLOntologyManager manager = ontology.getOWLOntologyManager();
    OWLDataFactory factory = manager.getOWLDataFactory();
    DefaultPrefixManager pm = PREFIX_MANAGER;

    OWLNamedIndividual objInstance = factory.getOWLNamedIndividual(
      "map:"+mapObject.getShortName(), pm);
    OWLClass objClass = null;
    
    for(org.knowrob.owl.OWLClass t : mapObject.getTypes()) {
      IRI classIRI = IRI.create(t.getIRI());
      if(pm.getPrefix(classIRI.getNamespace()) != null) {
        objClass = factory.getOWLClass(t.getIRI(), pm);
      }
      else if(!classIRI.isAbsolute()) {
        objClass = factory.getOWLClass("knowrob:"+t.getShortName(), pm);
      }
      else {
        objClass = factory.getOWLClass(classIRI);
      }
    
      manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(objClass,
        objInstance)); 
    }
    
    // write all normal data properties contained in the properties hashmap 
    for(String prop : mapObject.getDataProperties().keySet()) {
      for(String val : mapObject.getDataPropValues(prop)) {
        if( prop.endsWith("depthOfObject") || 
            prop.endsWith("widthOfObject") || 
            prop.endsWith("heightOfObject") )
          continue;
        
        OWLDataProperty property = factory.getOWLDataProperty(
          "knowrob:" + prop.split("#")[1],  pm);
        if(property!=null)
          manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(
          property, objInstance, val));        
      }
    }
      
    // create hinge-specific properties
    if(mapObject instanceof JointInstance) {
      // set direction for prismatic joints
      if(mapObject.hasType("PrismaticJoint")) {
        OWLNamedIndividual dirVec = createDirVector(
          ((JointInstance) mapObject).direction, manager, factory, pm,
          ontology);
        
        OWLObjectProperty direction = factory.getOWLObjectProperty(
          "knowrob:direction", pm);
        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(
          direction, objInstance, dirVec));
      }
    }
    
    return objInstance;
  }
}
