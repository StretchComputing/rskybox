var RSKYBOX = (function (r, $) {
  'use strict';


  r.Application = r.BaseModel.extend({
    apiUrl: '/applications',
    fields: {
      id: null,
      name: null,
      version: null,
      date: null,
      token: null,
      role: null,
      numberOfOpenLogs: null,
      numberOfOpenCrashes: null,
      numberOfOpenFeedback: null,
    },

    initialize: function () {
      try {
        this.setUrl();
      } catch (e) {
        r.log.error(e, 'Application.initialize');
      }
    },

    validate: function (attrs) {
      try {
        if (attrs.name) {
          return;
        }
        return 'A name is required for your application.';
      } catch (e) {
        r.log.error(e, 'Application.validate');
      }
    }
  });


  r.Applications = r.BaseCollection.extend({
    model: r.Application,
    apiUrl: '/applications',

    initialize: function () {
      try {
        this.setUrl();
      } catch (e) {
        r.log.error(e, 'Applications.initialize');
      }
    },

    parse: function (response) {
      try {
        return response.applications;
      } catch (e) {
        r.log.error(e, 'Applications.parse');
      }
    },

    findById: function (id) {
      try {
        return this.find(function (app) {
          return app.id === id;
        });
      } catch (e) {
        r.log.error(e, 'Applications.findById');
      }
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
