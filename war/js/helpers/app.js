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

  r.getApplications = function (callback) {
    var results;
    r.log.debug('entering', 'RSKYBOX.getApplications');

    r.getApplicationsRef();

    if (session.isFetching('applications')) {
      r.log.debug('isFetching', 'RSKYBOX.getApplications');
      return applications;
    }

    results = session.getItem('applications');
    if (!results) {
      session.setFetching('applications');
      applications.fetch({
        success: function (collection) {
          r.log.debug('fetch success', 'RSKYBOX.getApplications');
          session.setItem('applications', collection);
          if (callback) { callback(applications); }
        },
        error: function () {
          r.log.error('fetch error', 'RSKYBOX.getApplications');
          if (callback) { callback(); }
        },
        statusCode: r.statusCodeHandlers(),
      });
    } else {
      r.log.debug('updating from cache', 'RSKYBOX.getApplications');
      applications.reset(results);
      if (callback) { callback(applications); }
    }

    r.log.debug('leaving', 'RSKYBOX.getApplications');
    return applications;
  };

  r.getApplication = function (appId, callback) {
    var app = new r.Application({});

    r.getApplications(function (apps) {
      app = _.find(apps, function (app) {
        return app.id === appId;
      });
    });
    return app;
  };


  session.reset();


  return r;
}(RSKYBOX || {}, jQuery));

