'use strict'

angular.module('app.mediatheque')
  .directive('entry', [function factory() {
    return {
      restrict: 'EA',
      templateUrl: '/assets/views/mediatheque/directives/entry.html'
    }
  }])
