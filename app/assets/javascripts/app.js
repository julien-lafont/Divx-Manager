'use strict'

angular.module('app', ['ui.router', 'app.mediatheque'])
  .config(
    ['$stateProvider', '$urlRouterProvider', '$locationProvider', function($state, $router, $locationProvider) {

      $locationProvider.html5Mode(true);

      $router.otherwise('/')

      $state
        .state('mediatheque'
        , { url: '/'
          //, templateUrl: '/assets/views/quoi.html'
          , controller: 'MediathequeController'
        }
      )

    }])

