'use strict'

angular.module('app.mediatheque')
  .controller('MediathequeController',
    ['$scope', 'roots', 'lastEntries', function($scope, roots, lastEntries) {

      $scope.roots = roots
      $scope.lastEntries = lastEntries

    }]
  )
