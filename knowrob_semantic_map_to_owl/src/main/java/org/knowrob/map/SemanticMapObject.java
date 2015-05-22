package org.knowrob.map;

import org.knowrob.owl.ObjectInstance;
import org.knowrob.owl.OWLIndividual;

public class SemanticMapObject extends ObjectInstance {
  /**
  * Timestamp of the object
  */
  protected long stamp;
  
  /**
  * Frame of the object
  */
  protected String frame;
  
  /**
  * Constructor. Set the IRI and optionally a label. If none is given, 
  * it is initialized with the IRI's short name.
  * 
  * @param iri Identifier of this thing.
  */
  protected SemanticMapObject(String iri, String label) {
    super(iri, label);
    
    this.stamp = System.currentTimeMillis()/1000;
    this.frame = new String();
  }

  /**
  * Copy constructor: create SemanticMapObject from more generic
  * {@link OWLIndividual}
  * 
  * @param ind {@link OWLIndividual} to be copied into this
  *   {@link SemanticMapObject}
  */
  protected SemanticMapObject(OWLIndividual ind) {    
    super(ind);
    
    this.stamp = System.currentTimeMillis()/1000;
    this.frame = new String();
  }
  
  /**
  * SemanticMapObject factory. Return existing instance, if available, and
  * create new SemanticMapObject instance if necessary. Avoids duplicate
  * instances with the same IRI.
  * 
  * @param iri Identifier of this thing.
  * @param label Optional natural-language label.
  * @return Instance of a {@link SemanticMapObject} with the specified IRI
  */
  public static SemanticMapObject getSemanticMapObject(String iri,
      String label) {
    // return exact match if available
    if(identifiers.containsKey(iri) &&
        identifiers.get(iri) instanceof SemanticMapObject) {
      return (SemanticMapObject) identifiers.get(iri);
    }
    
    // create SemanticMapObject from higher-level objects if the existing
    // object for this IRI has a more abstract type
    SemanticMapObject res = new SemanticMapObject(
      OWLIndividual.getOWLIndividual(iri, label));
    identifiers.put(iri, res);
    return res;
  }
  
  /**
  * SemanticMapObject factory. Return existing instance, if available, and
  * create new SemanticMapObject instance if necessary. Avoids duplicate
  * instances with the same IRI.
  * 
  * @param iri Identifier of this thing.
  * @return Instance of a {@link SemanticMapObject} with the specified IRI
  */
  public static SemanticMapObject getSemanticMapObject(String iri) {
    return getSemanticMapObject(iri, null); 
  }

  /**
  * Get object timestamp.
  *
  * @return
  */
  public long getStamp() {
    return stamp;
  }

  /**
  * Set object timestamp.
  * 
  * @param stamp
  */
  public void setStamp(long stamp) {
    this.stamp = stamp;
  }
  
  /**
  * Get object frame.
  *
  * @return
  */
  public String getFrame() {
    return frame;
  }

  /**
  * Set object frame.
  * 
  * @param frame
  */
  public void setFrame(String frame) {
    this.frame = frame;
  }
}

