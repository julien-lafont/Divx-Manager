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
  ).filter('filterMovies', function() {
    return function(input, squery) {
      var textsearch = (squery || '').toUpperCase().split(' ')
      var i, s, found, out = []
      for (i in input) {
        found = 0

        for (s in textsearch) {
          var term = input[i].name
          if (input[i].details && input[i].details.quality) term += " " + input[i].details.quality
          if (input[i].details && input[i].details.year) term += " " + input[i].details.year
          if (term.toUpperCase().indexOf(textsearch[s]) !== -1) {
            found += 1
          }
        }

        if (found == textsearch.length) {
          out.push(input[i])
        }
      }
      return out;
    }
  })
