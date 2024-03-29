var RSKYBOX = (function (r, $) {
  'use strict';


  r.SettingsView = r.JqmPageBaseView.extend({
    events: {
      'click .logout': 'logout',
      'blur input[name=firstName]': 'saveFirstName',
      'blur input[name=lastName]': 'saveLastName',
      'change input[name=sendEmailNotifications]': 'sendEmailNotifications',
      'change input[name=sendSmsNotifications]': 'sendSmsNotifications',
      'click .confirmEmail': 'confirmEmail',
      'click .confirmPhone': 'confirmPhone',
      'click .requestEmailConfirmation': 'requestEmailConfirmation',
      'click .requestSmsConfirmation': 'requestSmsConfirmation',
      'click .savePassword': 'savePassword',
    },

    initialize: function () {
      try {
        _.bindAll(this, 'success', 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#settingsTemplate').html());
      } catch (e) {
        r.log.error(e, 'SettingsView.initialize');
      }
    },

    logout: function (evt) {
      try {
        r.log.info('entering', 'SettingsView.logout');
        r.logOut();

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.logout');
      }
    },

    saveFirstName: function (evt) {
      try {
        var name = this.$('input[name=firstName]').val();
        r.log.info('entering', 'SettingsView.saveFirstName');

        if (name || this.model.get('firstName')) {
          this.partialSave({
            firstName: name,
          }, true);
        }
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.saveFirstName');
      }
    },

    saveLastName: function (evt) {
      try {
        var name = this.$('input[name=lastName]').val();
        r.log.info('entering', 'SettingsView.saveLastName');

        if (name || this.model.get('lastName')) {
          this.partialSave({
            lastName: name,
          }, true);
        }
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.saveLastName');
      }
    },

    sendEmailNotifications: function (evt) {
      try {
        r.log.info('entering', 'SettingsView.sendEmailNotifications');
        this.partialSave({
          sendEmailNotifications: this.$('input[name=sendEmailNotifications]')[0].checked,
        }, true);
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.sendEmailNotifications');
      }
    },

    sendSmsNotifications: function (evt) {
      try {
        r.log.info('entering', 'SettingsView.sendSmsNotifications');
        this.partialSave({
          sendSmsNotifications: this.$('input[name=sendSmsNotifications]')[0].checked,
        }, true);
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.sendSmsNotifications');
      }
    },

    savePassword: function (evt) {
      try {
        var password = this.$('input[name=password]').val();

        if (this.model.isPasswordValid(password)) {
          this.partialSave({
            password: password,
          }, true);
        } else {
          r.flash.warning('Minimum password length is 6 characters.');
        }
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.savePassword');
      }
    },

    requestEmailConfirmation: function (evt) {
      try {
        var email = this.$('input[name=emailAddress]').val();

        if (this.model.isEmailValid(email)) {
          this.partialSave({
            emailAddress: email,
          }, false, true);
        } else {
          r.flash.warning('Valid email address required.');
        }
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.requestEmailConfirmation');
      }
    },

    requestSmsConfirmation: function (evt) {
      try {
        var
          phone = this.$('input[name=phoneNumber]').val(),
          carrier = this.$('select[name=mobileCarrierId]').val();

        if (this.model.isPhoneValid(phone, carrier)) {
          this.partialSave({
            phoneNumber: phone,
            mobileCarrierId: carrier,
          }, false, true);
        } else {
          r.flash.warning('Valid phone number and mobile carrier selection required.');
        }
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.requestSmsConfirmation');
      }
    },

    confirmEmail: function (evt) {
      try {
        var
          code = this.$('input[name=emailConfirmationCode]').val(),
          params;

        if (this.model.isConfirmCodeValid(code)) {
          params = {
            emailAddress: this.$('input[name=confirmEmailAddress]').val(),
            emailConfirmationCode: code,
            preregistration: false,
          };
          r.changePage('confirm', 'signup', params);
        } else {
          r.flash.warning('Confirmation code must be 3 characters.');
        }
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.confirmEmail');
      }
    },

    confirmPhone: function (evt) {
      try {
        var
          code = this.$('input[name=phoneNumberConfirmationCode]').val(),
          params;

        if (this.model.isConfirmCodeValid(code)) {
          params = {
            phoneNumber: this.$('input[name=confirmPhoneNumber]').val(),
            phoneConfirmationCode: code,
            preregistration: false,
          };
          r.changePage('confirm', 'signup', params);
        } else {
          r.flash.warning('Confirmation code must be 3 characters.');
        }
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'SettingsView.confirmPhone');
      }
    },

    partialSave: function (attrs, silent, force) {
      try {
        this.model.partial.save(this.model, attrs, {
          success: this.success,
          statusCode: r.statusCodeHandlers(this.apiError),
          wait: true,
          silent: !!silent,
        }, force);
      } catch (e) {
        r.log.error(e, 'SettingsView.partialSave');
      }
    },

    success: function (model, response) {
      try {
        r.flash.success('Changes were saved');
        model.set({ password: undefined }, { silent: true });
        this.$('input[name=password]').val('');
        r.session.reset();
      } catch (e) {
        r.log.error(e, 'SettingsView.success');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'SettingsView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'SettingsView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'SettingsView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'SettingsView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'SettingsView.apiError');
      }
    },

    render: function () {
      try {
        r.log.info('entering', 'SettingsView.render');
        var content = this.template(this.model.getMock());

        this.getContent().html(content);
        this.$el.trigger('create');
        if (!this.carriersView) {
          this.carriersView = new r.CarriersView({
            el: $('#mobileCarrierId'),
            collection: new r.Carriers()
          });
          this.carriersView.value = this.model.get('mobileCarrierId');
          r.session.getCollection(r.session.keys.mobileCarriers, this.carriersView.collection);
        } else {
          this.carriersView.setElement($('#mobileCarrierId'));
          this.carriersView.value = this.model.get('mobileCarrierId');
          this.carriersView.render();
        }
        return this;
      } catch (e) {
        r.log.error(e, 'SettingsView.render');
      }
    },

    apiCodes: {
      208: 'Email address can no longer be modified.',
      209: 'Phone number can no longer be modified.',
      218: 'User not authorized.',
      303: 'User ID requried.',
      405: 'Email address is already in use.',
      412: 'Password is too short.',
      413: 'Phone number is already in use.',
      501: 'Phone number and mobile carrier ID must be specified together.',
      502: 'Mobile carrier selection is missing.',
      600: 'User not found.',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
