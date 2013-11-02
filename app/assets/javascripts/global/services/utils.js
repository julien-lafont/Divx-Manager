'use strict'

angular.module('app.global')

  /**
   * `$q.then`-able utility functions.
   */
  .service('q', ['$q', function($q) {

    var success = function() {
      var defer = $q.defer()
      defer.resolve.apply(defer, arguments)
      return defer.promise
    }

    var failure = function() {
      var defer = $q.defer()
      defer.reject.apply(defer, arguments)
      return defer.promise
    }

    var data = _.compose(success, function(o) { return o.data })

    return _.extend(
      $q
      , { data: data
        , success: success
        , failure: failure
      }
    )
  }])
