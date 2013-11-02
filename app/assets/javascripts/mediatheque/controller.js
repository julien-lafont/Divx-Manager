'use strict'

angular.module('app.mediatheque')
  .controller('MediathequeController',
    ['$scope', 'roots', function($scope, roots) {

      $scope.roots = roots;

    }]
  )
