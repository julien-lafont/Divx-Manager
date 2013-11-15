'use strict'

angular.module('app', ['ui.router', 'app.global', 'app.mediatheque', 'app.request'])
  .config(
    ['$stateProvider', '$urlRouterProvider', '$locationProvider', function($state, $router, $locationProvider) {

      $locationProvider.html5Mode(true);

      $router.otherwise('/mediatheque')

      $state
        .state('mediatheque', {
          url: '/mediatheque',
          templateUrl: '/assets/views/mediatheque/main.html',
          controller: 'MediathequeController',
          resolve: {
            roots: ['mediathequeService', function(svc) { return svc.roots() }],
            lastEntries: ['mediathequeService', function(svc) { return svc.lastEntries() }]
          }
        })
          .state('mediatheque.news', {
            url: '/nouveautés',
            templateUrl: '/assets/views/mediatheque/latests.html',
            controller: 'LatestsController',
            resolve: {
              lastEntries: ['mediathequeService', function(svc) { return svc.lastEntries() }]
            }
          })
          .state('mediatheque.folder', {
            url: '/*dir',
            templateUrl: '/assets/views/mediatheque/list.html',
            controller: 'FolderController'
          })
          

        .state('new-request', {
          url: '/requête',
          templateUrl: '/assets/views/request/new.html',
          controller: 'RequestController'
        })

    }]
  )
