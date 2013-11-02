'use strict'

angular.module('app.mediatheque')
  .service('mediathequeService', ['$http', 'q', function factory($http, q) {

    var root = jsRoutes.controllers.Api

    var roots = function() {
      return $http(root.roots()).then(q.data)
    }

    var lastEntries = function() {
     return $http(root.newFiles()).then(q.data)
    }

    return {
      roots: roots,
      lastEntries: lastEntries
    }

  }])
