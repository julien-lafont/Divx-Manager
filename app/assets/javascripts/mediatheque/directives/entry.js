'use strict'

angular.module('app.mediatheque')
  .directive('entry', ['mediathequeService', function factory(mediathequeService) {
    return {
      restrict: 'EA',
      templateUrl: '/assets/views/mediatheque/directives/entry.html',
      scope: {
        mixte: '=',
        item: '=',
        detail: '&'
      }
    }
  }])
