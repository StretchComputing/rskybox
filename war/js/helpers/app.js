var RSKYBOX = (function (r, $) {
  'use strict';


  var storage;

  storage = {
    // TODO - use 15 minute interval for production
    // interval: 15 * 60 * 1000, // Fifteen minutes.
    interval: 0.5 * 60 * 1000, // One-half minute for beta/testing.

    reset: function () {
      r.log.debug('reset', 'storage');
      this.clear();
      sessionStorage.setItem('expires', JSON.stringify(new Date(Date.now() + this.interval)));
    },

    clear: function () {
      sessionStorage.clear();
    },

    isStale: function () {
      var expires = Date.parse(JSON.parse(sessionStorage.getItem('expires')));

      if (Date.now() > expires) {
        r.log.debug('session is stale', 'storage.isStale');
        this.reset();
        return true;
      }
      return false;
    },

    setFetching: function (item) {
      r.log.debug(item, 'storage.setFetching');
      sessionStorage.setItem(item, 'fetching');
    },

    isFetching: function (item) {
      return sessionStorage.getItem(item) === 'fetching';
    },

    setItem: function (item, value) {
      r.log.debug('entering', 'storage.setItem');
      this.isStale();
      sessionStorage.setItem(item, JSON.stringify(value));
    },

    getItem: function (item) {
      var results;

      r.log.debug('entering', 'storage.getItem');
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

    getEntity: function (key) {
      return storage.getItem(key);
    },

    getModel: function (key, model) {
      var cache;
      r.log.debug('entering', 'RSKYBOX.getModel');

      if (storage.isFetching(key)) {
        return model;
      }

      cache = storage.getItem(key);
      if (!cache) {
        storage.setFetching(key);
        model.fetch({
          success: function (fetched) {
            storage.setItem(key, fetched);
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

      if (storage.isFetching(key)) {
        return collection;
      }

      cache = storage.getItem(key);
      if (!cache) {
        storage.setFetching(key);
        collection.reset();
        collection.fetch({
          success: function (fetched) {
            storage.setItem(key, fetched);
          },
          statusCode: r.statusCodeHandlers(),
        });
      } else {
        r.log.debug('cache hit', 'RSKYBOX.getCollection');
        collection.reset(cache);
      }

      return collection;
    },

    reset: function () {
      storage.reset();
    },
  };


  r.session.reset();


  return r;
}(RSKYBOX || {}, jQuery));

