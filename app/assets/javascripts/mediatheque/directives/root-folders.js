'use strict'

angular.module('app.global')
  .directive('rootFolders', [function factory() {
    return {
      restrict: 'E',
      templateUrl: '/assets/views/mediatheque/directives/root-folders.html'
    }
  }])
