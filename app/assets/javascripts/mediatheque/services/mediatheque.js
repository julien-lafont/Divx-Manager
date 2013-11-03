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

    var listFiles = function(dir, order) {
      return $http(root.list(dir, order.column, order.sort)).then(q.data)
    }

    var listDirs = function(dir, order) {
      return $http(root.listDirs(dir, order.column, order.sort)).then(q.data)
    }

    var fetchEntries = function(root, order) {
      if (root.type == "TvShow" || root.type == "Other")
        return listDirs(root.dir, order)
      else
        return listFiles(root.dir, order)
    }

    var movieDetail = function(name) {
      return $http(jsRoutes.controllers.Movie.detail(name)).then(q.data)
    }

    return {
      roots: roots,
      lastEntries: lastEntries,
      listFiles: listFiles,
      listDirs: listDirs,
      fetchEntries: fetchEntries,
      movieDetail: movieDetail
    }

  }])
