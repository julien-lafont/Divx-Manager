'use strict'

angular.module('app.global')
  .directive('autoActiveLink', [function factory() {
    return {
      restrict: 'A',
      link: function link($scope, iElement, iAttrs, controller) {

        var checkActiveLinks = function(url) {
          var links = iElement.find('a'),
            rawUrl = decodeURI(url),
            i = 0,
            link,
            regex

          while (i < links.length) {
            link = angular.element(links[i])
            regex = new RegExp(decodeURI(link.attr('href')).toLowerCase(), "i")

            if (rawUrl.match(regex)) {
              link.addClass('active')
              link.parent().addClass('active')
            } else {
              link.removeClass('active')
              link.parent().removeClass('active')
            }

            i++
          }
        }

        $scope.$on('$locationChangeSuccess', function activate(event, url) {
          checkActiveLinks(url)
        })

        // Launch at first launch
        checkActiveLinks(window.location.href)
      }
    }
  }])

