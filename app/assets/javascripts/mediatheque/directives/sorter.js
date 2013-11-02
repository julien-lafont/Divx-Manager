'use strict'

angular.module('app.mediatheque')
  .directive('sorter', [function factory() {
    return {
      restrict: 'EA',
      scope: {
        'column': '@',
        'order': '='
      },
      transclude: true,
      templateUrl: '/assets/views/mediatheque/directives/sorter.html',
      link: function($scope) {

        $scope.up = function() {
          return $scope.order.column == $scope.column && $scope.order.sort == 'asc'
        }

        $scope.down = function() {
          return $scope.order.column == $scope.column && $scope.order.sort == 'desc'
        }
      }
    }
  }])
