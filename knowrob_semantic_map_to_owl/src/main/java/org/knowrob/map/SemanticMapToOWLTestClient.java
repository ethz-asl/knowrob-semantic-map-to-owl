package org.knowrob.map;

import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

import knowrob_semantic_map_msgs.SemMapObject;

/**
* ROS test client to convert a mod_semantic_map/SemMap message into an OWL
* description
* 
* @author Moritz Tenorth, tenorth@cs.tum.edu
* @author Lars Kunze, kunzel@cs.tum.edu
* @author Ralf Kaestner, ralf.kaestner@gmail.com
*
*/

public class SemanticMapToOWLTestClient extends AbstractNodeMain {
  ConnectedNode node;
  
  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("semantic_map_to_owl_test_client");
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    this.node = connectedNode;
    
    ServiceClient<knowrob_semantic_map_msgs.GenerateSemanticMapOWLRequest,
      knowrob_semantic_map_msgs.GenerateSemanticMapOWLResponse> serviceClient;
    
    try {
      serviceClient = connectedNode.newServiceClient(
        "knowrob_semantic_map_to_owl/generate_owl_map",
        knowrob_semantic_map_msgs.GenerateSemanticMapOWL._TYPE);
    }
    catch (ServiceNotFoundException e) {
      throw new RosRuntimeException(e);
    }

    // create semantic map message
    final knowrob_semantic_map_msgs.GenerateSemanticMapOWLRequest req = 
      serviceClient.newMessage();
    
    // Set the IRI for the map that will be created
    req.getMap().getHeader().setFrameId("http://www.example.com/foo.owl#");
    req.getMap().getHeader().setStamp(node.getCurrentTime());

    // create cupboard
    SemMapObject cupboard = node.getTopicMessageFactory().newFromType(
      SemMapObject._TYPE);
    
    cupboard.setId("Cupboard1");
    cupboard.setType("Cupboard");

    cupboard.getSize().setX(55.6f);
    cupboard.getSize().setY(60.7f);
    cupboard.getSize().setZ(70.6f);

    cupboard.getPose().getPosition().setX(2.3f);
    cupboard.getPose().getPosition().setY(1.2f);
    cupboard.getPose().getPosition().setZ(0.4f);
    cupboard.getPose().getOrientation().setX(0.0f);
    cupboard.getPose().getOrientation().setY(0.0f);
    cupboard.getPose().getOrientation().setZ(0.0f);
    cupboard.getPose().getOrientation().setW(1.0f);
    
    req.getMap().getObjects().add(cupboard);
    
    // create door
    SemMapObject door = node.getTopicMessageFactory().newFromType(
      SemMapObject._TYPE);
    door.setId("Door1");
    door.setPartOf("Cupboard1");
    door.setType("Door");

    door.getSize().setX(0.6f);
    door.getSize().setY(60.7f);
    door.getSize().setZ(70.6f);

    door.getPose().getPosition().setX(2.4f);
    door.getPose().getPosition().setY(1.3f);
    door.getPose().getPosition().setZ(0.4f);
    door.getPose().getOrientation().setX(0.0f);
    door.getPose().getOrientation().setY(0.0f);
    door.getPose().getOrientation().setZ(0.0f);
    door.getPose().getOrientation().setW(1.0f);

    req.getMap().getObjects().add(door);
    
    // create hinge
    SemMapObject hinge = node.getTopicMessageFactory().newFromType(
      SemMapObject._TYPE);
    hinge.setId("HingedJoint1");
    hinge.setPartOf("Door1");
    hinge.setType("HingedJoint");

    hinge.getSize().setX(5.6f);
    hinge.getSize().setY(0.7f);
    hinge.getSize().setZ(0.6f);

    hinge.getPose().getPosition().setX(2.5f);
    hinge.getPose().getPosition().setY(1.4f);
    hinge.getPose().getPosition().setZ(0.4f);
    hinge.getPose().getOrientation().setX(0.0f);
    hinge.getPose().getOrientation().setY(0.0f);
    hinge.getPose().getOrientation().setZ(0.0f);
    hinge.getPose().getOrientation().setW(1.0f);

    req.getMap().getObjects().add(hinge);

    // create handle
    SemMapObject handle = node.getTopicMessageFactory().newFromType(
      SemMapObject._TYPE);
    handle.setId("Handle1");
    handle.setPartOf("Door1");
    handle.setType("Handle");

    handle.getSize().setX(5.6f);
    handle.getSize().setY(6.7f);
    handle.getSize().setZ(7.6f);

    handle.getPose().getPosition().setX(2.6f);
    handle.getPose().getPosition().setY(1.5f);
    handle.getPose().getPosition().setZ(0.4f);
    handle.getPose().getOrientation().setX(0.0f);
    handle.getPose().getOrientation().setY(0.0f);
    handle.getPose().getOrientation().setZ(0.0f);
    handle.getPose().getOrientation().setW(1.0f);

    req.getMap().getObjects().add(handle);
    
    serviceClient.call(req, new ServiceResponseListener<
        knowrob_semantic_map_msgs.GenerateSemanticMapOWLResponse>() {
      @Override
      public void onSuccess(
          knowrob_semantic_map_msgs.GenerateSemanticMapOWLResponse response) {        
        connectedNode.getLog().info(
          String.format("%s", response.getOwlmap()));
      }

      @Override
      public void onFailure(RemoteException e) {
        throw new RosRuntimeException(e);
      }
    });
  }
}

