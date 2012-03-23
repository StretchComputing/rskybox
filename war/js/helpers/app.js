var RSKYBOX = (function (r, $) {
  'use strict';


  var applications, session;

  session = {
    interval: 15 * 60 * 1000, // Fifteen minutes.

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


  r.getApplicationsRef = function () {
    applications = applications || new r.Applications();
    return applications;
  };

  r.getApplications = function () {
    var apps, results;
    r.log.debug('entering', 'RSKYBOX.getApplications');

    apps = r.getApplicationsRef();

    if (session.isFetching('applications')) {
      return apps;
    }

    results = session.getItem('applications');
    if (!results) {
      session.setFetching('applications');
      apps.reset();
      apps.fetch({
        success: function (collection) {
          session.setItem('applications', collection);
        },
        statusCode: r.statusCodeHandlers(),
      });
    } else {
      r.log.debug('updating from cache', 'RSKYBOX.getApplications');
      apps.reset(results);
    }

    return apps;
  };


  session.reset();


  return r;
}(RSKYBOX || {}, jQuery));

