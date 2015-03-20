cmake_minimum_required(VERSION 2.8.3)
project(knowrob_semantic_map_to_owl)

find_package(catkin REQUIRED rosjava_build_tools knowrob_semantic_map_msgs knowrob_common)


catkin_rosjava_setup(installMavenJavaPublicationToRosMavenRepository installApp)

catkin_package(
    DEPENDS knowrob_semantic_map_msgs knowrob_common
)

##############################################################################
# Installation
##############################################################################

# Change this to match the maven group name you have specified in the
# allprojects closure the root build.gradle
install(DIRECTORY ${CATKIN_DEVEL_PREFIX}/${CATKIN_GLOBAL_JAR_DESTINATION}/
        DESTINATION ${CATKIN_GLOBAL_JAR_DESTINATION}/)
install(DIRECTORY ${CATKIN_DEVEL_PREFIX}/${CATKIN_GLOBAL_MAVEN_DESTINATION}/
        DESTINATION ${CATKIN_GLOBAL_MAVEN_DESTINATION}/)

install(PROGRAMS knowrob_semantic_map_to_owl/build/scripts/knowrob_semantic_map_to_owl
        DESTINATION ${CATKIN_PACKAGE_SHARE_DESTINATION}/scripts)
install(PROGRAMS scripts/semantic_map_to_owl_node
                 scripts/semantic_map_to_owl_test_client
        DESTINATION ${CATKIN_PACKAGE_SHARE_DESTINATION}/bin)
