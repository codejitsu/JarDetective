package net.codejitsu.jardetective.service

import net.codejitsu.jardetective.graph.mock.MockDependencyGraph

class MockGraphServiceRestSpec extends RestSpec(_ => new JarDetectiveService with MockDependencyGraph)
