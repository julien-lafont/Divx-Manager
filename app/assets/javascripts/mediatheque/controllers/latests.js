'use strict'

angular.module('app.mediatheque')
  .controller('LatestsController',
    ['$scope', 'lastEntries',
      function($scope, lastEntries) {

        $scope.lastEntries = lastEntries

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
