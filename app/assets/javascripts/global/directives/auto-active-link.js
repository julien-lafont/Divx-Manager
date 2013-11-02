'use strict'

angular.module('app.global')
  .directive('autoActiveLink', [function factory() {
    return {
      restrict: 'A',
      link: function link($scope, iElement, iAttrs, controller) {

        // Set or unset the `active` class on links.
        $scope.$on('$locationChangeSuccess', function activate(event, url) {

          var links = iElement.find('a'),
            rawUrl = decodeURI(url),
            i = 0,
            link,
            regex

          while (i < links.length) {
            link = angular.element(links[i])
            regex = new RegExp(link.attr('href') + "$")

            if (rawUrl.match(regex)) {
              link.addClass('active')
              link.parent().addClass('active')
            } else {
              link.removeClass('active')
              link.parent().removeClass('active')
            }

            i++
          }
        })
      }
    }
  }])

