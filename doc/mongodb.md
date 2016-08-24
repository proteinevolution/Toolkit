Toolkit MongoDB and ElasticSearch config
=============================================

Here we describe how to set up the data storage with MongoDB (3.2.9) and ElasticSearch (2.2.2).

The big picture
---------------

We use ElasticSearch for filtering jobs. One main feature is job hashing to prevent that already processed job
get unnecessarily on our cluster again.
Therefore we use ElasticSearch to scan our database for jobs which have the same signature in terms of: `hash value`,
`database type` and `database version`.


Setup
-----
ElasticSearch indexes MongoDB via [Mongo-Connector](https://github.com/mongodb-labs/mongo-connector) and [Elastic2-Doc-Manager](https://github.com/mongodb-labs/elastic2-doc-manager)
ElasticSearch binds to port 9200 and 9300.


Sharding and Replication
------------------------
It is required to run MongoDB as a replSet with an odd number of nodes. It is recommended to have at least 3 nodes
for production systems which are not located on the same physical machine:

    In a production, deploy each member of the replica set to its own machine and if possible bind to the standard MongoDB port of 27017


Compression
-----------


Validation
----------