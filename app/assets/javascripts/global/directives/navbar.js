'use strict'

angular.module('app.global')
  .directive('navbar', ['$state', function factory($state) {
    return {
      restrict: 'E',
      templateUrl: '/assets/views/global/directives/navbar.html',
      link: function($scope) {
        $scope.$on('$locationChangeSuccess', function activate(event, url) {
          $scope.state = $state.current.name
        })
        $scope.state = $state.current.name
      }
    }
  }])
