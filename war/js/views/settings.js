var RSKYBOX = (function (r, $) {
  'use strict';


  r.SettingsView = r.JqmPageBaseView.extend({
    events: {
      'click .logout': 'logout',
      'blur input[name=firstName]': 'saveFirstName',
      'blur input[name=lastName]': 'saveLastName',
      'click .savePassword': 'savePassword'
    },

    initialize: function () {
      _.bindAll(this, 'partialSave', 'success', 'apiError');
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

    saveFirstName: function (e) {
      r.log.debug('Settings.saveFirstName');
      this.partialSave({
        firstName: this.$('input[name=firstName]').val()
      });
      e.preventDefault();
      return false;
    },

    saveLastName: function (e) {
      r.log.debug('Settings.saveLastName');
      this.partialSave({
        lastName: this.$('input[name=lastName]').val()
      });
      e.preventDefault();
      return false;
    },

    savePassword: function (e) {
      this.partialSave({
        password: this.$('input[name=password]').val()
      });
      e.preventDefault();
      return false;
    },

    partialSave: function (attrs) {
      this.model.partial.save(this.model, attrs, {
        success: this.success,
        statusCode: r.statusCodeHandlers(this.apiError)
      });
    },

    success: function (model, response) {
      r.flash.notice('Changes were saved');
    },

    error: function (model, response) {
      r.log.debug('SettingsView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flash.error(response);
    },

    apiError: function (jqXHR) {
      r.log.debug('SettingsView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('SettingsView: An unknown API error occurred: ' + code);
      }

      r.flash.error(this.apiCodes[code]);
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

      //this.carriersView.collection.fetch();
      return this;
    },

    apiCodes: {
      208: 'Email address can no longer be modified.',
      209: 'Phone number can no longer be modified.',
      218: 'User not authorized.',
      303: 'User ID requried.',
      405: 'Email address is already in use.',
      413: 'Phone number is already in use.',
      501: 'Phone number and mobile carrier ID must be specified together.',
      600: 'User not found.'
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
