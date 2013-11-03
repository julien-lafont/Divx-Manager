'use strict'

angular.module('app.mediatheque')
  .controller('MediathequeController',
    ['$scope', '$state', '$stateParams', 'roots', 'lastEntries', function($scope, $state, $params, roots, lastEntries) {

      $scope.roots = roots
      $scope.lastEntries = lastEntries

      // Open first folder by default
      if (roots.length && $state.current.name === "mediatheque") {
        $state.go("mediatheque.folder", roots[0])
      }

      $scope.getRootEntry = function(dir) {
        return _.find($scope.roots, function(e) { return e.dir === dir })
      }

    }]
  )

