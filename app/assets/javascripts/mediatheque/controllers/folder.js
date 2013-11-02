'use strict'

angular.module('app.mediatheque')
  .controller('FolderController',
    ['$scope', '$state', '$stateParams', 'mediathequeService', function($scope, $state, $params, mediathequeService) {

      $scope.directory = $params.dir
      $scope.order = {
        column: 'name',
        sort: 'asc'
      }

      var rootEntry = $scope.$parent.getRootEntry($scope.directory)

      var refresh = function() {
        var $q = (rootEntry) ?
          mediathequeService.fetchEntries(rootEntry, $scope.order) :
          mediathequeService.listFiles($scope.directory, $scope.order)

        $q.then(function(data) {
          $scope.list = data
        })
      }

      $scope.sort = function(column) {
        if ($scope.order.column === column) {
          $scope.order.sort = ($scope.order.sort === "asc") ? "desc" : "asc"
        } else {
          $scope.order.column = column
          $scope.order.sort = "asc"
        }
        refresh()
      }

      refresh()
    }]
  )
