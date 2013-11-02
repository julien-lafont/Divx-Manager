'use strict'

angular.module('app.mediatheque')
  .directive('rootFolders', [function factory() {
    return {
      restrict: 'E',
      templateUrl: '/assets/views/mediatheque/directives/root-folders.html'
    }
  }])
