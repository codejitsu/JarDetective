# JarDetective
Track all your project dependencies!

# Docker

* https://hub.docker.com/_/neo4j/

```
docker run --publish=7474:7474 --publish=7687:7687 --volume=$HOME/neo4j/data:/data --env=NEO4J_AUTH=none neo4j
```