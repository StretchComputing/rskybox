var RSKYBOX = (function (r, $) {
  'use strict';


  var storage = {
    interval: 15 * 60 * 1000, // Fifteen minutes.

    reset: function () {
      try {
        r.log.info('entering', 'storage.reset');

        this.clear();
        sessionStorage.setItem('expires', JSON.stringify(new Date(Date.now() + this.interval)));
      } catch (e) {
        r.log.error(e, 'storage.reset');
      }
    },

    clear: function () {
      sessionStorage.clear();
    },

    isStale: function () {
      try {
        var expires = Date.parse(JSON.parse(sessionStorage.getItem('expires')));

        if (Date.now() > expires) {
          r.log.info('session is stale', 'storage.isStale');
          this.reset();
          return true;
        }
        return false;
      } catch (e) {
        r.log.error(e, 'storage.isStale');
      }
    },

    setFetching: function (item) {
      r.log.info(item, 'storage.setFetching');

      sessionStorage.setItem(item, 'fetching');
    },

    isFetching: function (item) {
      r.log.info(item, 'storage.isFetching');

      return sessionStorage.getItem(item) === 'fetching';
    },

    setItem: function (item, value) {
      r.log.info(item, 'storage.setItem');

      this.isStale();
      sessionStorage.setItem(item, JSON.stringify(value));
    },

    getItem: function (item) {
      try {
        var results;
        r.log.info(item, 'storage.getItem');

        if (this.isStale()) { return false; }

        results = JSON.parse(sessionStorage.getItem(item));
        if (!results || results === '' || results === 'fetching') {
          return false;
        }
        return results;
      } catch (e) {
        r.log.error(e, 'storage.getItem');
      }
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
      try {
        var cache;
        r.log.info(key, 'session.getModel');

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
          r.log.info('cacheHit', 'session.getModel');
          model.set(cache);
        }

        return model;
      } catch (e) {
        r.log.error(e, 'session.getModel');
      }
    },

    getCollection: function (key, collection) {
      try {
        var cache;
        r.log.info(key, 'session.getCollection');

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
          r.log.info('cacheHit', 'session.getCollection');
          collection.reset(cache);
        }

        return collection;
      } catch (e) {
        r.log.error(e, 'session.getCollection');
      }
    },

    reset: function () {
      r.log.info('entering', 'session.reset');

      storage.reset();
    },
  };


  try {
    r.session.reset();

    return r;
  } catch (e) {
    r.log.error(e, 'RSKYBOX.session.reset');
  }
}(RSKYBOX || {}, jQuery));

