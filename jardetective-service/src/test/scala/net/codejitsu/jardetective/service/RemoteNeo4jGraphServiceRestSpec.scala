package net.codejitsu.jardetective.service

import net.codejitsu.jardetective.graph.remote.RemoteNeo4jGraph

class RemoteNeo4jGraphServiceRestSpec extends RestSpec(_ => new JarDetectiveService with RemoteNeo4jGraph)
