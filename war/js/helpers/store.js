var RSKYBOX = (function (r, $) {
  'use strict';


  r.store = {
    clear: function () {
      try {
        r.log.info('entering', 'store.clear');

        localStorage.clear();
      } catch (e) {
        r.log.error(e, 'store.setItem');
      }
    },

    setItem: function (item, value) {
      try {
        r.log.info(item, 'store.setItem');

        localStorage.setItem(item, JSON.stringify(value));
      } catch (e) {
        r.log.error(e, 'store.setItem');
      }
    },

    getItem: function (item) {
      try {
        var results;
        r.log.info(item, 'store.getItem');

        results = JSON.parse(localStorage.getItem(item));
        if (!results || results === '') {
          return false;
        }
        return results;
      } catch (e) {
        r.log.error(e, 'store.getItem');
      }
    },

    removeItem: function (item) {
      try {
        r.log.info(item, 'store.removeItem');
        localStorage.removeItem(item);
      } catch (e) {
        r.log.error(e, 'store.removeItem');
      }
    },
  };


  return r;
}(RSKYBOX || {}, jQuery));
