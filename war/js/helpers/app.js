var RSKYBOX = (function (r, $) {
  'use strict';


  var session;

  session = {
    interval: 60 * 1000, // Fifteen minutes.

    reset: function () {
      r.log.debug('reset', 'session');
      this.clear();
      sessionStorage.setItem('expires', JSON.stringify(new Date(Date.now() + this.interval)));
    },

    clear: function () {
      sessionStorage.clear();
    },

    isStale: function () {
      var expires = Date.parse(JSON.parse(sessionStorage.getItem('expires')));

      if (Date.now() > expires) {
        r.log.debug('session is stale', 'session.isStale');
        this.reset();
        return true;
      }
      return false;
    },

    setFetching: function (item) {
      r.log.debug(item, 'session.setFetching');
      sessionStorage.setItem(item, 'fetching');
    },

    isFetching: function (item) {
      return sessionStorage.getItem(item) === 'fetching';
    },

    setItem: function (item, value) {
      r.log.debug('entering', 'session.setItem');
      this.isStale();
      sessionStorage.setItem(item, JSON.stringify(value));
    },

    getItem: function (item) {
      var results;

      r.log.debug('entering', 'session.getItem');
      if (this.isStale()) { return false; }

      results = JSON.parse(sessionStorage.getItem(item));
      if (!results || results === '' || results === 'fetching') {
        return false;
      }
      return results;
    },
  };


  r.getApplications = function () {
    var results;

    r.applications = r.applications || new r.Applications();

    if (session.isFetching('applications')) {
      return r.applications;
    }

    results = session.getItem('applications');
    if (!results) {
      session.setFetching('applications');
      r.applications.fetch({
        success: function () {
          session.setItem('applications', r.applications);
        },
        statusCode: r.statusCodeHandlers(),
      });
    } else {
      r.log.debug('updating from cache', 'RSKYBOX.getApplications');
      r.applications.reset(results);
    }

    return r.applications;
  };


  session.reset();


  return r;
}(RSKYBOX || {}, jQuery));

