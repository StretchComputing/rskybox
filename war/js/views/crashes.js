var RSKYBOX = (function (r, $) {
  'use strict';


  r.CrashesView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.collection.bind('reset', this.render, this);
      } catch (e) {
        r.log.error(e, 'CrashesView.initialize');
      }
    },

    render: function () {
      try {
        this.$el.empty();
        this.collection.each(function (crash) {
          console.log('****');
          this.$el.append(new r.CrashView({
            model: crash,
            attributes: {
              'data-role': 'collapsible',
              'data-theme': 'c',
              'data-content-theme': 'c',
              'data-mini': 'true',
            },
          }).render().el);
        }, this);
        console.log(this.$el.html());
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'CrashesView.render');
      }
    },
  });


  r.CrashView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.template = _.template($('#crashTemplate').html());
      } catch (e) {
        r.log.error(e, 'CrashView.');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        this.$el.html(this.template(mock));
        return this;
      } catch (e) {
        r.log.error(e, 'CrashView.render');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'CrashView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'CrashView.');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'CrashView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'CrashView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'CrashView.');
      }
    },

    apiCodes: {
      203: 'You are not authorized for this application.',
      305: 'Application ID required.',
      602: 'Crash was not found',
      605: 'Application was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
