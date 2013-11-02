'use strict'

angular.module('app.global')
  .directive('navbar', [function factory() {
    return {
      restrict: 'E',
      templateUrl: '/assets/views/global/directives/navbar.html'
    }
  }])
