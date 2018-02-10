package net.codejitsu.jardetective.service

import net.codejitsu.jardetective.graph.neo4j.EmbeddedNeo4jGraph
import net.codejitsu.jardetective.graph.remote.RemoteNeo4jGraph

class RemotedNeo4jGraphServiceRestSpec extends RestSpec(_ => new JarDetectiveService with RemoteNeo4jGraph)
