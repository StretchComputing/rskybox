var RSKYBOX = (function (r, $) {
  'use strict';


  r.SettingsView = r.JqmPageBaseView.extend({
    events: {
      'click .logout': 'logout',
      'click .savePassword': 'savePassword'
    },

    initialize: function () {
      _.bindAll(this, 'save', 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#settingsTemplate').html());
    },

    logout: function (e) {
      r.log.debug('logout');
      r.unsetCookie();
      r.changePage('root', 'signup');

      e.preventDefault();
      return false;
    },

    savePassword: function (e) {
      this.save({
        password: this.$('input[name=password]').val()
      });
      e.preventDefault();
      return false;
    },

    save: function (attrs) {
      this.model.save(attrs, {
        success: this.success,
        statusCode: r.statusCodeHandlers(this.apiError)
      });
    },

    success: function (model, response) {
      //$.mobile.changePage('#confirm' + r.buildQueryString(model.toJSON()));
    },

    error: function (model, response) {
      r.log.debug('SettingsView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response);
    },

    apiError: function (jqXHR) {
      r.log.debug('SettingsView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('SettingsView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code]);
    },

    render: function () {
      var content = this.template(this.model.getMock());

      r.log.debug('SettingsView.render');

      this.getContent().html(content);
      this.$el.trigger('create');
      this.carriersView = new r.CarriersView({
        el: $('#mobileCarrierId'),
        collection: new r.Carriers()
      });

      this.carriersView.collection.fetch();
      return this;
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      500: 'Phone number and mobile carrier ID must be specified together.'
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
