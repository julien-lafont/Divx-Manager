'use strict'

angular.module('app.mediatheque')
  .directive('breadcrumb', [function factory() {
    return {
      restrict: 'E',
      scope: {
        'directory': '=',
        'raw': '@'
      },
      templateUrl: '/assets/views/mediatheque/directives/breadcrumb.html',
      link: function($scope) {
        if (!$scope.raw) {
          var parts = $scope.directory.split("/")
          $scope.parts = []

          for (var i = 0; i < parts.length; i++) {
            $scope.parts.push({
              name: parts[i],
              dir: ($scope.parts[i-1] ? ($scope.parts[i-1].dir + "/") : "") + parts[i],
              isLast: i == (parts.length -1 )
            })
          }
        }
      }
    }
  }])
