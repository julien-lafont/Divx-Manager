'use strict'

angular.module('app.request')
  .controller('RequestController',
    ['$scope', 'requestService', function($scope, requestService) {

      $scope.req = {
        type: 'Film',
        qualite: 'Pas de préférence',
        langue: 'Pas de préférence'
      }

      // KISS
      $scope.send = function() {
        $scope.error = null

        requestService.post($scope.req)
          .success(function() {
            $scope.done = true
          })
          .error(function(data, status) {
            console.error(status, data)
            $scope.error = true
          })
      }

      $scope.retry = function() {
        $scope.req.titre = ""
        $scope.req.com = ""
        $scope.done = false
      }

    }]
  )
