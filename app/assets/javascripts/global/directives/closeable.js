'use strict'

angular.module('app.global')
  .directive('closeable', [function factory() {
    return {
      restrict: 'A',
      scope: {
        'closeable': '@'
      },
      link: function link($scope, iElement, iAttrs, controller) {

        $scope.visible = !window.localStorage.getItem("closeable-" + $scope.closeable)

        $scope.close = function() {
          $scope.visible = false
          window.localStorage.setItem("closeable-" + $scope.closeable, true)
        }

      }
    }
  }])

