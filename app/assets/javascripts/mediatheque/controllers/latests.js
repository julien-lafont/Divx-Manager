'use strict'

angular.module('app.mediatheque')
  .controller('LatestsController',
    ['$scope', 'lastEntries',
      function($scope, lastEntries) {

        $scope.lastEntries = lastEntries
        $scope.mixte = true

      }]
  )
