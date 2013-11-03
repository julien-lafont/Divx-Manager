'use strict'

angular.module('app.mediatheque')
  .controller('MediathequeController',
    ['$scope', '$state', '$stateParams', 'mediathequeService', 'roots', 'lastEntries',
      function($scope, $state, $params, mediathequeService, roots, lastEntries) {

      $scope.roots = roots
      $scope.lastEntries = lastEntries

      // Open first folder by default
      if (roots.length && $state.current.name === "mediatheque") {
        $state.go("mediatheque.folder", roots[0])
      }

      $scope.getRootEntry = function(dir) {
        return _.find($scope.roots, function(e) { return e.dir === dir })
      }

      $('#movieModal').modal()

      $scope.detail = function(entry) {

        $scope.modalLoading = true
        $('#movieModal').modal('show')

        mediathequeService.movieDetail(entry.name)
          .success(function(data) {
            $scope.movie = data
            $scope.selectedEntry = entry
            $scope.modalLoading = false
          })
          .error(function(data) {
            $scope.movie = null
            $scope.modalLoading = false
          })
      }
    }]
  )

