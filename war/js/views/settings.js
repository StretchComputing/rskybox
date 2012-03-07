var RSKYBOX = (function (r, $) {
  'use strict';


  r.SettingsView = r.JqmPageBaseView.extend({
    events: {
      'click .logout': 'logout',
      'blur input[name=firstName]': 'saveFirstName',
      'blur input[name=lastName]': 'saveLastName',
      'change input[name=sendEmailNotifications]': 'sendEmailNotifications',
      'change input[name=sendSmsNotifications]': 'sendSmsNotifications',
      'click .requestSmsConfirmation': 'requestSmsConfirmation',
      'click .savePassword': 'savePassword',
    },

    initialize: function () {
      _.bindAll(this, 'partialSave', 'success', 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#settingsTemplate').html());
    },

    logout: function (e) {
      r.log.debug('entering', 'SettingsView.logout');
      r.unsetCookie();
      r.changePage('root', 'signup');

      e.preventDefault();
      return false;
    },

    saveFirstName: function (e) {
      r.log.debug('entering', 'SettingsView.saveFirstName');
      this.partialSave({
        firstName: this.$('input[name=firstName]').val()
      });
      e.preventDefault();
      return false;
    },

    saveLastName: function (e) {
      r.log.debug('entering', 'SettingsView.saveLastName');
      this.partialSave({
        lastName: this.$('input[name=lastName]').val()
      });
      e.preventDefault();
      return false;
    },

    sendEmailNotifications: function (e) {
      r.log.debug('entering', 'SettingsView.sendEmailNotifications');
      this.partialSave({
        sendEmailNotifications: this.$('input[name=sendEmailNotifications]')[0].checked,
      });
      e.preventDefault();
      return false;
    },

    sendSmsNotifications: function (e) {
      r.log.debug('entering', 'SettingsView.sendSmsNotifications');
      this.partialSave({
        sendSmsNotifications: this.$('input[name=sendSmsNotifications]')[0].checked,
      });
      e.preventDefault();
      return false;
    },

    savePassword: function (e) {
      r.log.debug('entering', 'SettingsView.savePassword');
      this.partialSave({
        password: this.$('input[name=password]').val()
      });
      e.preventDefault();
      return false;
    },

    requestSmsConfirmation: function (e) {
      this.partialSave({
        phoneNumber: this.$('input[name=phoneNumber]').val(),
        mobileCarrierId: this.$('select[name=mobileCarrierId]').val()
      });
      e.preventDefault();
      return false;
    },

    partialSave: function (attrs) {
      this.model.partial.save(this.model, attrs, {
        success: this.success,
        statusCode: r.statusCodeHandlers(this.apiError),
        wait: true,
      });
    },

    success: function (model, response) {
      r.flash.notice('Changes were saved');
    },

    error: function (model, response) {
      r.log.debug('entering', 'SettingsView.error');
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.error(response);                // This is a validation error.
    },

    apiError: function (jqXHR) {
      r.log.debug('entering', 'SettingsView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'SettingsView.apiError');
      }
      r.flash.error(this.apiCodes[code]);
    },

    render: function () {
      r.log.debug('entering', 'SettingsView.render');
      var content = this.template(this.model.getMock());

      this.getContent().html(content);
      this.$el.trigger('create');
      if (!this.carriersView) {
        this.carriersView = new r.CarriersView({
          el: $('#mobileCarrierId'),
          collection: new r.Carriers()
        });
        this.carriersView.value = this.model.get('mobileCarrierId');
        this.carriersView.collection.fetch();
      } else {
        this.carriersView.setElement($('#mobileCarrierId'));
        this.carriersView.value = this.model.get('mobileCarrierId');
        this.carriersView.render();
      }
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
