package org.knowrob.map;

import java.util.Map;
import java.util.Vector;

import org.knowrob.owl.OWLClass;

import org.knowrob.map.SemanticMapObject;

public class SemanticMapAction extends OWLClass {
  /**
   * Constructor. Set the IRI and optionally a label. If none is given, 
   * it is initialized with the IRI's short name.
   * 
   * @param iri Identifier of this thing.
   */
  protected SemanticMapAction(String iri, String label) {
    super(iri, label);    
  }

  /**
   * Copy constructor: create SemanticMapAction from more generic
   * {@link OWLClass}
   * 
   * @param ind {@link OWLClass} to be copied into this
   *   {@link SemanticMapAction}
   */
  protected SemanticMapAction(OWLClass cls) {
    super(cls);
  }
  
  /**
   * SemanticMapAction factory. Return existing instance, if available, and
   * create new SemanticMapAction instance if necessary. Avoids duplicate
   * instances with the same IRI.
   * 
   * @param iri Identifier of this thing.
   * @param label Optional natural-language label.
   * @return Instance of a {@link SemanticMapAction} with the specified IRI
   */
  public static SemanticMapAction getSemanticMapAction(String iri, String
      label) {
    // return exact match if available
    if(identifiers.containsKey(iri) &&
        identifiers.get(iri) instanceof SemanticMapAction) {
      return (SemanticMapAction) identifiers.get(iri);     
    }
    
    // create SemanticMapAction from higher-level objects if the existing
    // object for this IRI has a more abstract type
    SemanticMapAction res = new SemanticMapAction(
      OWLClass.getOWLClass(iri, label));
    identifiers.put(iri, res);
    return res;
  }
  
  /**
   * SemanticMapAction factory. Return existing instance, if available, and
   * create new SemanticMapAction instance if necessary. Avoids duplicate
   * instances with the same IRI.
   * 
   * @param iri Identifier of this thing.
   * @return Instance of an {@link SemanticMapAction} with the specified IRI
   */
  public static SemanticMapAction getSemanticMapAction(String iri) {
    return getSemanticMapAction(iri, null); 
  }

  /**
  * Get object acted on.
  *
  * @return
  */
  public SemanticMapObject getObjectActedOn() {
    Map<String, Vector<String>> hasValue = this.getHasValue();
    
    if(hasValue.containsKey("knowrob:objectActedOn")) {
      return SemanticMapObject.getSemanticMapObject(
        hasValue.get("knowrob:objectActedOn").get(0));
    }
    else {
      return null;
    }
  }

  /**
  * Set object acted on.
  * 
  * @param stamp
  */
  public void setObjectActedOn(SemanticMapObject objectActedOn) {
    Map<String, Vector<String>> hasValue = this.getHasValue();

    if(!has_value.containsKey("knowrob:objectActedOn")) {
      has_value.put("knowrob:objectActedOn", new Vector<String>());
    }
    has_value.get("knowrob:objectActedOn").clear();
    has_value.get("knowrob:objectActedOn").add(objectActedOn.getIRI());
  }
  
  public void setObjectActedOn(String iri) {
    this.setObjectActedOn(SemanticMapObject.getSemanticMapObject(iri));
  }
}
