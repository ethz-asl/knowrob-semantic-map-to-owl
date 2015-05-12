package org.knowrob.map;

import java.util.ArrayList;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import org.knowrob.owl.JointInstance;
import org.knowrob.owl.ObjectInstance;
import org.knowrob.owl.OWLThing;

import org.knowrob.owl.utils.OWLImportExport;

/**
* Specialization of the Knowrob OWL file import/export used by the ROS 
* conversion service
* 
* @author Ralf Kaestner, ralf.kaestner@gmail.com
*
*/

public class SemanticMapToOWLExport extends OWLImportExport {
  public final static String MAP_FRAME = "map";
  
  /**
  * Map frame
  */
  protected String mapFrame;
  
  public SemanticMapToOWLExport() {
    this.mapFrame = MAP_FRAME;
  }
  
  /**
  * Get map frame.
  *
  * @return
  */
  public String getMapFrame() {
    return mapFrame;
  }

  /**
  * Set map frame.
  * 
  * @param frame
  */
  public void setMapFrame(String mapFrame) {
    this.mapFrame = mapFrame;
  }

  public OWLOntology createOWLMapWithActionDescription(
      String namespace, String map_id, ArrayList<ObjectInstance> objects,
      ArrayList<SemanticMapAction> actions) {
    return this.createOWLMapWithActionDescription(namespace, map_id,
      objects, actions, null);
  }
  
  public OWLOntology createOWLMapWithActionDescription(
      String namespace, String map_id, ArrayList<ObjectInstance> objects,
      ArrayList<SemanticMapAction> actions, ArrayList<String[]> address) {
    OWLOntology ontology = this.createOWLMapDescription(namespace, map_id,
      objects, address);

    if(ontology != null) {
      try {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        DefaultPrefixManager pm = PREFIX_MANAGER;
        
        for(SemanticMapAction map_act : actions) {
          createActionClass(map_act, ontology);
        }
      } catch (Exception e) {
        ontology = null;
        e.printStackTrace();
      }
    }
    
    return ontology;
  }
  
  public OWLClass createActionClass(SemanticMapAction mapAction,
      OWLOntology ontology) {
    OWLOntologyManager manager = ontology.getOWLOntologyManager();
    OWLDataFactory factory = manager.getOWLDataFactory();
    DefaultPrefixManager pm = PREFIX_MANAGER;
    
    OWLClass actClass = factory.getOWLClass(
      "map:"+mapAction.getShortName(), pm);
    manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(actClass));
    OWLClass actSuperClass = null;
    
    for(org.knowrob.owl.OWLClass s : mapAction.getSuperClasses()) {
      IRI classIRI = IRI.create(s.getIRI());
      if(pm.getPrefix(classIRI.getNamespace()) != null) {
        actSuperClass = factory.getOWLClass(s.getIRI(), pm);
      }
      else if(!classIRI.isAbsolute()) {
        actSuperClass = factory.getOWLClass("knowrob:"+s.getShortName(), pm);
      }
      else {
        actSuperClass = factory.getOWLClass(classIRI);
      }
    
      manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(actClass,
        actSuperClass)); 
    }

    OWLObjectProperty objectActedOnProp = factory.getOWLObjectProperty(
      "knowrob:objectActedOn", pm);
    SemanticMapObject o = mapAction.getObjectActedOn();
    
    if(o != null) {
      IRI objIRI = IRI.create(o.getIRI());
      OWLNamedIndividual objectActedOn = null;

      if(pm.getPrefix(objIRI.getNamespace()) != null) {
        objectActedOn = factory.getOWLNamedIndividual(
          o.getIRI(), pm);
      }
      else if(!objIRI.isAbsolute()) {
        objectActedOn = factory.getOWLNamedIndividual(
          "map:"+o.getShortName(), pm);
      }
      else {
        objectActedOn = factory.getOWLNamedIndividual(objIRI);
      }
    
      OWLClassExpression objectActedOnHasValue = factory.getOWLObjectHasValue(
        objectActedOnProp, objectActedOn);
      manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(actClass,
        objectActedOnHasValue));
    }
    
    return actClass;
  }
  
  @Override
  public OWLNamedIndividual createSemMapInst(String namespace, String map_id,
      OWLOntology ontology) {
    OWLOntologyManager manager = ontology.getOWLOntologyManager();
    OWLDataFactory factory = manager.getOWLDataFactory();
    DefaultPrefixManager pm = PREFIX_MANAGER;
    
    OWLNamedIndividual sem_map_inst = super.createSemMapInst(namespace,
      map_id, ontology);

    OWLDataProperty prop = factory.getOWLDataProperty("knowrob:tfFrame", pm);
    manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(
      prop, sem_map_inst, this.mapFrame));
    
    return sem_map_inst;
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
    
    // write tf frame data property
    if(mapObject instanceof SemanticMapObject) {
      SemanticMapObject smapObject = (SemanticMapObject) mapObject;
      
      if(!smapObject.getFrame().isEmpty()) {
        OWLDataProperty property = factory.getOWLDataProperty(
          "knowrob:tfFrame",  pm);
        manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(
          property, objInstance, smapObject.getFrame()));        
      }
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
  
  @Override
  public OWLNamedIndividual createSemObjectInstanceDescription(ObjectInstance
      map_obj, OWLNamedIndividual timestamp, OWLOntology ontology) {
    // create time instance
    OWLNamedIndividual time_inst = timestamp;
    if(map_obj instanceof SemanticMapObject) {
      SemanticMapObject smap_obj = (SemanticMapObject) map_obj;
      time_inst = createTimePointInst(smap_obj.getStamp(), ontology);
    }
    
    return super.createSemObjectInstanceDescription(map_obj, time_inst,
      ontology);
  }
}
