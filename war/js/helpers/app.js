var RSKYBOX = (function (r, $) {
  'use strict';


  var storage;

  storage = {
    // TODO - use 15 minute interval for production
    // interval: 15 * 60 * 1000, // Fifteen minutes.
    interval: 0.5 * 60 * 1000, // One-half minute for beta/testing.

    reset: function () {
      r.log.info('entering', 'storage.reset');
      this.clear();
      sessionStorage.setItem('expires', JSON.stringify(new Date(Date.now() + this.interval)));
    },

    clear: function () {
      sessionStorage.clear();
    },

    isStale: function () {
      var expires = Date.parse(JSON.parse(sessionStorage.getItem('expires')));

      if (Date.now() > expires) {
        r.log.info('session is stale', 'storage.isStale');
        this.reset();
        return true;
      }
      return false;
    },

    setFetching: function (item) {
      r.log.info(item, 'storage.setFetching');
      sessionStorage.setItem(item, 'fetching');
    },

    isFetching: function (item) {
      return sessionStorage.getItem(item) === 'fetching';
    },

    setItem: function (item, value) {
      r.log.info(item, 'storage.setItem.entering');
      this.isStale();
      sessionStorage.setItem(item, JSON.stringify(value));
    },

    getItem: function (item) {
      var results;

      r.log.info(item, 'storage.getItem.entering');
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
      applications: 'applications',
      currentUser: 'currentUser',
      mobileCarriers: 'mobileCarriers',
    },

    getEntity: function (key) {
      return storage.getItem(key);
    },

    getModel: function (key, model) {
      var cache;
      r.log.info(key, 'RSKYBOX.getModel.entering');

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
        r.log.info(key, 'RSKYBOX.getModel.cacheHit');
        model.set(cache);
      }

      return model;
    },

    getCollection: function (key, collection) {
      var cache;
      r.log.info(key, 'RSKYBOX.getCollection.entering');

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
        r.log.info(key, 'RSKYBOX.getCollection.cacheHit');
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

