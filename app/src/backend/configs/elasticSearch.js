const elasticsearch = require('elasticsearch')

const esClient = new elasticsearch.Client( {
  hosts: [
    'https://localhost:9200/'
  ]
});

module.exports = esClient;
