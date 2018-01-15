package net.codejitsu.jardetective.service

import net.codejitsu.jardetective.graph.neo4j.EmbeddedNeo4jGraph

class EmbeddedNeo4jGraphServiceRestSpec extends RestSpec(_ => new JarDetectiveService with EmbeddedNeo4jGraph)
