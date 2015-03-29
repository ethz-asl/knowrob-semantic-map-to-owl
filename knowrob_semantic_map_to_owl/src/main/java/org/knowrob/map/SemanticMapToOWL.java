package org.knowrob.map;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.service.ServiceResponseBuilder;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import org.knowrob.owl.OWLClass;
import org.knowrob.owl.ObjectInstance;
import org.knowrob.owl.utils.OWLFileUtils;
import org.knowrob.owl.utils.PackageIRIMapper;

import knowrob_semantic_map_msgs.*;

import org.knowrob.map.SemanticMapToOWLExport;

/**
* ROS service to convert a mod_semantic_map/SemMap message into an OWL
* description
* 
* @author Moritz Tenorth, tenorth@cs.tum.edu
* @author Lars Kunze, kunzel@cs.tum.edu
* @author Ralf Kaestner, ralf.kaestner@gmail.com
*
*/

public class SemanticMapToOWL extends AbstractNodeMain {
  ConnectedNode node;

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("semantic_map_to_owl");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
    this.node = connectedNode;
    connectedNode.newServiceServer(
      "knowrob_semantic_map_to_owl/generate_owl_map", 
      knowrob_semantic_map_msgs.GenerateSemanticMapOWL._TYPE,
      new ConvertToOwlCallback());
  }

  class ConvertToOwlCallback implements ServiceResponseBuilder<
      knowrob_semantic_map_msgs.GenerateSemanticMapOWLRequest,
      knowrob_semantic_map_msgs.GenerateSemanticMapOWLResponse> {
    @Override
    public void build(knowrob_semantic_map_msgs.GenerateSemanticMapOWLRequest
        req, knowrob_semantic_map_msgs.GenerateSemanticMapOWLResponse res) {
      res.setOwlmap("");

      if (req.getMap() != null && req.getMap().getObjects().size()>0) {
        SemanticMapToOWLExport export = new SemanticMapToOWLExport();

        // get address from message
        ArrayList<String[]> address = new ArrayList<String[]>();
        if(!req.getMap().getAddress().getRoomNr().isEmpty())
          address.add(new String[]{"knowrob:RoomInAConstruction",
            "knowrob:roomNumber", req.getMap().getAddress().getRoomNr()});
        if(!req.getMap().getAddress().getFloorNr().isEmpty())
          address.add(new String[]{"knowrob:LevelOfAConstruction",
            "knowrob:floorNumber", req.getMap().getAddress().getFloorNr()});
        if(!req.getMap().getAddress().getStreetNr().isEmpty())
          address.add(new String[]{"knowrob:Building",
            "knowrob:streetNumber", req.getMap().getAddress().getStreetNr()});
        if(!req.getMap().getAddress().getStreetName().isEmpty())
          address.add(new String[]{"knowrob:Street", "rdfs:label",
            req.getMap().getAddress().getStreetName()});
        if(!req.getMap().getAddress().getCityName().isEmpty())
          address.add(new String[]{"knowrob:City", "rdfs:label",
            req.getMap().getAddress().getCityName()});
            
        // use IAS_MAP as default, PREFIX_MANAGER is set by default
        String namespace = SemanticMapToOWLExport.IAS_MAP;
        if(!req.getMap().getNamespace().isEmpty()) {
          namespace = req.getMap().getNamespace();

          if(!namespace.endsWith("#"))
            namespace += "#";
        }
        System.err.println("Using map namespace: " + namespace);        
        
        DefaultPrefixManager pm = SemanticMapToOWLExport.PREFIX_MANAGER;
        for(SemMapPrefix pref : req.getMap().getPrefixes()) {
          if(pref.getName().endsWith(":")) {
            pm.setPrefix(pref.getName(), pref.getPrefix());
          }
          else {
            pm.setPrefix(pref.getName()+":", pref.getPrefix());
          }
        }
        
        HashMap<String, ObjectInstance> mos = semMapObj2MapObj(namespace,
          req.getMap().getObjects());
        OWLOntology owlmap = export.createOWLMapDescription(namespace, 
          "SemanticEnvironmentMap" + new SimpleDateFormat(
          "yyyyMMddHHmmss").format(Calendar.getInstance().getTime()), 
          new ArrayList<ObjectInstance>(mos.values()), address);
          
        OWLOntologyManager manager = owlmap.getOWLOntologyManager();
        PackageIRIMapper im = new PackageIRIMapper();
        manager.addIRIMapper(im);
        OWLDataFactory factory = manager.getOWLDataFactory();
        PrefixOWLOntologyFormat pf = (PrefixOWLOntologyFormat)
          manager.getOntologyFormat(owlmap);

        for(SemMapPrefix pref : req.getMap().getPrefixes()) {
          if(pref.getName().endsWith(":")) {
            pf.setPrefix(pref.getName(), pref.getPrefix());
          }
          else {
            pf.setPrefix(pref.getName()+":", pref.getPrefix());
          }
        }
          
        for(String imp : req.getMap().getImports()) {
          OWLImportsDeclaration oid = factory.getOWLImportsDeclaration(
            IRI.create(imp));
          AddImport addImp = new AddImport(owlmap,oid);
          manager.applyChange(addImp);
        }
        
        Set<OWLImportsDeclaration> imps = owlmap.getImportsDeclarations();
        for(OWLImportsDeclaration imp: imps) {
          try {
            manager.makeLoadImportRequest(imp);
          }
          catch(UnloadableImportException e) {
            System.out.println(e.getMessage());
          }
        }
        
        for(SemMapObjectProperty smop : req.getMap().getObjectProperties()) {
          ObjectInstance subj = mos.get(smop.getSubject());
          ObjectInstance obj = mos.get(smop.getObject());
            
          if((subj != null) && (obj != null)) {
            OWLObjectProperty op = null;
            OWLNamedIndividual subjInd = null;
            OWLNamedIndividual objInd = null;
            
            IRI opIRI = IRI.create(smop.getId());
            if(pm.getPrefix(opIRI.getNamespace()) != null) {
              op = factory.getOWLObjectProperty(smop.getId(), pm);
            }
            else {
              op = factory.getOWLObjectProperty(opIRI);
            }
            
            IRI subjIRI = IRI.create(namespace + smop.getSubject());
            if(pm.getPrefix(subjIRI.getNamespace()) != null) {
              subjInd = factory.getOWLNamedIndividual(smop.getSubject(), pm);
            }
            else {
              subjInd = factory.getOWLNamedIndividual(subjIRI);
            }
            
            IRI objIRI = IRI.create(namespace + smop.getObject());
            if(pm.getPrefix(objIRI.getNamespace()) != null) {
              objInd = factory.getOWLNamedIndividual(smop.getObject(), pm);
            }
            else {
              objInd = factory.getOWLNamedIndividual(objIRI);
            }
            
            OWLObjectPropertyAssertionAxiom opAxiom =
              factory.getOWLObjectPropertyAssertionAxiom(op, subjInd, objInd);
            manager.addAxiom(owlmap, opAxiom);
          }
        }
                    
        for(SemMapDataProperty smdp : req.getMap().getDataProperties()) {
          ObjectInstance subj = mos.get(smdp.getSubject());
            
          if(subj != null) {
            OWLDataProperty dp = null;
            OWLNamedIndividual subjInd = null;
            
            IRI dpIRI = IRI.create(smdp.getId());
            if(pm.getPrefix(dpIRI.getNamespace()) != null) {
              dp = factory.getOWLDataProperty(smdp.getId(), pm);
            }
            else {
              dp = factory.getOWLDataProperty(dpIRI);
            }
            
            IRI subjIRI = IRI.create(namespace + smdp.getSubject());
            if(pm.getPrefix(subjIRI.getNamespace()) != null) {
              subjInd = factory.getOWLNamedIndividual(smdp.getSubject(), pm);
            }
            else {
              subjInd = factory.getOWLNamedIndividual(subjIRI);
            }
            
            OWLDataPropertyAssertionAxiom dpAxiom = null;
            if(smdp.getValueType() == smdp.VALUE_TYPE_BOOL) {
              dpAxiom = factory.getOWLDataPropertyAssertionAxiom(dp,
                subjInd, Boolean.parseBoolean(smdp.getValue()));
            }
            else if(smdp.getValueType() == smdp.VALUE_TYPE_FLOAT) {
              dpAxiom = factory.getOWLDataPropertyAssertionAxiom(dp,
                subjInd, Double.parseDouble(smdp.getValue()));
            }
            else if(smdp.getValueType() == smdp.VALUE_TYPE_INT) {
              dpAxiom = factory.getOWLDataPropertyAssertionAxiom(dp,
                subjInd, Integer.parseInt(smdp.getValue()));
            }
            else {
              dpAxiom = factory.getOWLDataPropertyAssertionAxiom(dp,
                subjInd, smdp.getValue());
            }
            
            manager.addAxiom(owlmap, dpAxiom);
          }
        }
                            
        res.setOwlmap(OWLFileUtils.saveOntologytoString(owlmap,
          owlmap.getOWLOntologyManager().getOntologyFormat(owlmap)));
      }
    }
  }

  private HashMap<String, ObjectInstance> semMapObj2MapObj(String map_id,
      List<SemMapObject> smos) {
    HashMap<String, ObjectInstance> mos = new
      HashMap<String, ObjectInstance>();

    for(SemMapObject smo : smos) {
      ObjectInstance mo = ObjectInstance.getObjectInstance(smo.getId());
      mos.put(smo.getId(), mo);

      mo.addType(OWLClass.getOWLClass(smo.getType()));

      mo.getDimensions().x=smo.getSize().getX();
      mo.getDimensions().y=smo.getSize().getY();
      mo.getDimensions().z=smo.getSize().getZ();

      Vector3d position = new Vector3d();
      position.x = smo.getPose().getPosition().getX();
      position.y = smo.getPose().getPosition().getY();
      position.z = smo.getPose().getPosition().getZ();

      Quat4d orientation = new Quat4d();
      orientation.x = smo.getPose().getOrientation().getX();
      orientation.y = smo.getPose().getOrientation().getY();
      orientation.z = smo.getPose().getOrientation().getZ();
      orientation.w = smo.getPose().getOrientation().getW();
      
      mo.setPoseQuaternion(position, orientation, 1.0);
      
      if(mos.get(smo.getPartOf()) != null)
        mos.get(smo.getPartOf()).addPhysicalPart(mo);
    }

    return mos;
  }
}
