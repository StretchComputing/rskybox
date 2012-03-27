var RSKYBOX = (function (r, $) {
  'use strict';


  var session;

  session = {
    // TODO - use 15 minute interval for production
    // interval: 15 * 60 * 1000, // Fifteen minutes.
    interval: 0.5 * 60 * 1000, // One-half minute for beta/testing.

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


  r.session = r.session || {
    keys: {
      currentUser: 'currentUser',
      applications: 'applications',
    },

    getModel: function (key, model) {
      var cache;
      r.log.debug('entering', 'RSKYBOX.getModel');

      if (session.isFetching(key)) {
        return model;
      }

      cache = session.getItem(key);
      if (!cache) {
        session.setFetching(key);
        model.fetch({
          success: function (fetched) {
            session.setItem(key, fetched);
          },
          statusCode: r.statusCodeHandlers(),
        });
      } else {
        r.log.debug('cache hit', 'RSKYBOX.getModel');
        model.set(cache);
      }

      return model;
    },

    getCollection: function (key, collection) {
      var cache;
      r.log.debug('entering', 'RSKYBOX.getCollection');

      if (session.isFetching(key)) {
        return collection;
      }

      cache = session.getItem(key);
      if (!cache) {
        session.setFetching(key);
        collection.reset();
        collection.fetch({
          success: function (fetched) {
            session.setItem(key, fetched);
          },
          statusCode: r.statusCodeHandlers(),
        });
      } else {
        r.log.debug('cache hit', 'RSKYBOX.getCollection');
        collection.reset(cache);
      }

      return collection;
    },
  };


  session.reset();


  return r;
}(RSKYBOX || {}, jQuery));

