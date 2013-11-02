'use strict'

angular.module('app.mediatheque')
  .directive('lastEntries', [function factory() {
    return {
      restrict: 'E',
      templateUrl: '/assets/views/mediatheque/directives/last-entries.html'
    }
  }])
