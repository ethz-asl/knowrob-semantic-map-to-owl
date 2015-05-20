package org.knowrob.map;

import java.util.Map;
import java.util.Vector;

import org.knowrob.owl.OWLClass;

import org.knowrob.map.SemanticMapAction;

public class SemanticMapTask extends OWLClass {
  /**
  * Quantification of the task
  */
  public enum Quantification {
    INTERSECTION_OF,
    UNION_OF
  };
  
  protected Quantification quantification = Quantification.INTERSECTION_OF;
  
  /**
  * Ordering of actions
  */
  protected Boolean ordered = true;  
  
  /**
  * Actions of the task
  */
  protected Vector<SemanticMapAction> actions;
  
  /**
   * Constructor. Set the IRI and optionally a label. If none is given, 
   * it is initialized with the IRI's short name.
   * 
   * @param iri Identifier of this thing.
   */
  protected SemanticMapTask(String iri, String label) {
    super(iri, label);
    this.actions = new Vector<SemanticMapAction>();
  }

  /**
   * Copy constructor: create SemanticMapTask from more generic
   * {@link OWLClass}
   * 
   * @param ind {@link OWLClass} to be copied into this
   *   {@link SemanticMapTask}
   */
  protected SemanticMapTask(OWLClass cls) {
    super(cls);
    this.actions = new Vector<SemanticMapAction>();
  }
  
  /**
   * SemanticMapTask factory. Return existing instance, if available, and
   * create new SemanticMapTask instance if necessary. Avoids duplicate
   * instances with the same IRI.
   * 
   * @param iri Identifier of this thing.
   * @param label Optional natural-language label.
   * @return Instance of a {@link SemanticMapTask} with the specified IRI
   */
  public static SemanticMapTask getSemanticMapTask(String iri, String
      label) {
    // return exact match if available
    if(identifiers.containsKey(iri) &&
        identifiers.get(iri) instanceof SemanticMapTask) {
      return (SemanticMapTask) identifiers.get(iri);     
    }
    
    // create SemanticMapTask from higher-level objects if the existing
    // object for this IRI has a more abstract type
    SemanticMapTask res = new SemanticMapTask(
      OWLClass.getOWLClass(iri, label));
    identifiers.put(iri, res);
    return res;
  }
  
  /**
   * SemanticMapTask factory. Return existing instance, if available, and
   * create new SemanticMapTask instance if necessary. Avoids duplicate
   * instances with the same IRI.
   * 
   * @param iri Identifier of this thing.
   * @return Instance of an {@link SemanticMapTask} with the specified IRI
   */
  public static SemanticMapTask getSemanticMapTask(String iri) {
    return getSemanticMapTask(iri, null); 
  }
  
  /**
  * Get quantification.
  *
  * @return
  */
  public Quantification getQuantification() {
    return this.quantification;
  }

  /**
  * Set quantification.
  *
  * @return
  */
  public void setQuantification(Quantification quantification) {
    this.quantification = quantification;
  }
  
  /**
  * Get ordered.
  *
  * @return
  */
  public Boolean getOrdered() {
    return this.ordered;
  }

  /**
  * Set ordered.
  *
  * @return
  */
  public void setOrdered(Boolean ordered) {
    this.ordered = ordered;
  }
  
  /**
  * Get actions.
  *
  * @return
  */
  public Vector<SemanticMapAction> getActions() {
    return this.actions;
  }

  /**
  * Add action.
  * 
  * @param stamp
  */
  public void addAction(SemanticMapAction action) {
    if(!this.actions.contains(action)) {
      this.actions.add(action);
    }
  }
  
  public void addAction(String iri) {
    this.addAction(SemanticMapAction.getSemanticMapAction(iri));
  }
}
