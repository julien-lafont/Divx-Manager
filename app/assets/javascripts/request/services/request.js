'use strict'

angular.module('app.request')
  .service('requestService', ['$http', function factory($http ) {

    var root = jsRoutes.controllers.Request

    var post = function(data) {
      return $http.post(root.post().url, data)
    }

    return {
      post: post
    }

  }])
