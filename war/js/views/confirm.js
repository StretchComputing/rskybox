var RSKYBOX = (function (r, $) {
  'use strict';


  r.ConfirmNewUserView = Backbone.View.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#confirmTemplate').html());
      } catch (e) {
        r.log.error(e, 'ConfirmNewUserView.initialize');
      }
    },

    events: {
      'submit': 'submit'
    },

    submit: function (evt) {
      try {
        r.log.info('entering', 'ConfirmNewUserView.submit');
        var valid;

        valid = this.model.set({
          emailAddress: this.$("input[name='emailAddress']").val(),
          emailConfirmationCode: this.$("input[name='emailConfirmationCode']").val(),
          phoneNumber: this.$("input[name='phoneNumber']").val(),
          phoneConfirmationCode: this.$("input[name='phoneConfirmationCode']").val(),
          password: this.$("input[name='password']").val(),
        });

        if (valid) {
          this.model.prepareNewModel();
          this.model.save(null, {
            success: this.success,
            statusCode: r.statusCodeHandlers(this.apiError)
          });
        }

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'ConfirmNewUserView.submit');
      }
    },

    success: function (model, response) {
      try {
        r.log.info('entering', 'ConfirmNewUserView.success');
        r.destination.set('/html5#settings');
        r.logIn(model.get('token'));
      } catch (e) {
        r.log.error(e, 'ConfirmNewUserView.success');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'ConfirmNewUserView.error');
        r.flash.warning(response);    // This is a validation error.
      } catch (e) {
        r.log.error(e, 'ConfirmNewUserView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'ConfirmNewUserView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'ConfirmNewUserView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }
        this.model.clear({silent: true});
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'ConfirmNewUserView.apiError');
      }
    },

    render: function () {
      try {
        var content = this.template(this.model.getMock());
        r.getHeaderDiv().find('h1').text('Complete Signup');
        this.$el.html(content);
        this.$el.find('input[type=submit]').text('Complete Signup');
        if (this.model.get('emailAddress')) {
          this.$('#emailWrapper').show();
        }
        if (this.model.get('phoneNumber')) {
          this.$('#phoneWrapper').show();
        }
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'ConfirmNewUserView.render');
      }
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      206: 'Your email address is not registered in the system.',
      207: 'Your phone number is not registered in the system.',
      220: 'Phone number or email address is already in use by another user.',
      308: 'Either an email address or a phone number is required.',
      311: 'Password is required.',
      317: 'Email confirmation code is required.',
      318: 'Phone confirmation code is required.',
      403: 'Invalid email address.',
      404: 'Invalid mobile carrier.',
      412: 'Password too short.',
      417: 'Invalid email confirmation code.',
      418: 'Invalid phone confirmation code.',
      501: 'Phone number is missing.',
      502: 'Mobile carrier selection is missing.',
      607: 'Email address not found.',
      608: 'Phone number not found.',
    }
  });


  r.ConfirmExistingUserView = Backbone.View.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#confirmTemplate').html());
      } catch (e) {
        r.log.error(e, 'ConfirmExistingUserView.initialize');
      }
    },

    events: {
      'submit': 'submit'
    },

    submit: function (evt) {
      try {
        var valid;
        r.log.info('entering', 'ConfirmExistingUserView.submit');

        valid = this.model.set({
          emailAddress: this.$("input[name='emailAddress']").val(),
          emailConfirmationCode: this.$("input[name='emailConfirmationCode']").val(),
          phoneNumber: this.$("input[name='phoneNumber']").val(),
          phoneConfirmationCode: this.$("input[name='phoneConfirmationCode']").val(),
        });

        if (valid) {
          this.model.prepareNewModel();
          this.model.save(null, {
            success: this.success,
            statusCode: r.statusCodeHandlers(this.apiError)
          });
        }

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'ConfirmExistingUserView.submit');
      }
    },

    success: function (model, response) {
      try {
        r.log.info('entering', 'ConfirmExistingUserView.success');
        r.changePage('settings');
      } catch (e) {
        r.log.error(e, 'ConfirmExistingUserView.success');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'ConfirmExistingUserView.error');
        r.flash.warning(response);    // This is a validation error.
      } catch (e) {
        r.log.error(e, 'ConfirmExistingUserView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'ConfirmExistingUserView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'ConfirmExistingUserView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }
        this.model.clear({silent: true});
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'ConfirmExistingUserView.apiError');
      }
    },

    render: function () {
      try {
        var content = this.template(this.model.getMock());
        r.getHeaderDiv().find('h1').text('Confirmation');
        this.$el.html(content);
        this.$el.find('input[type=submit]').text('Confirm');
        this.$('#passwordWrapper').hide();
        if (this.model.get('emailAddress')) {
          this.$('#emailWrapper').show();
          this.$('#phoneWrapper').hide();
        }
        if (this.model.get('phoneNumber')) {
          this.$('#emailWrapper').hide();
          this.$('#phoneWrapper').show();
        }
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'ConfirmExistingUserView.render');
      }
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      206: 'Your email address is not registered in the system.',
      207: 'Your phone number is not registered in the system.',
      308: 'Either an email address or a phone number is required.',
      309: 'Confirmation code is required.',
      411: 'Invalid confirmation code.',
      607: 'Email address not found.',
      608: 'Phone number not found.',
      700: 'Email address and phone number are mutually exclusive.',
    }
  });


  r.ConfirmMemberView = Backbone.View.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'apiError', 'success');
        this.model.on('error', this.error, this);
        this.template = _.template($('#confirmMemberTemplate').html());
      } catch (e) {
        r.log.error(e, 'ConfirmMemberView.initialize');
      }
    },

    events: {
      'submit': 'submit'
    },

    submit: function (evt) {
      try {
        r.log.info('entering', 'ConfirmMemberView.submit');
        this.model.save(null, {
          success: this.success,
          statusCode: r.statusCodeHandlers(this.apiError),
          wait: true,
        });
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'ConfirmMemberView.submit');
      }
    },

    success: function (model, response) {
      try {
        var params;
        r.log.info('entering', 'ConfirmMemberView.success');

        if (+model.get('apiStatus') === 215) {
          r.log.info('Member is not a registered user.', 'ConfirmMemberView.success');
          params = r.session.params;
          delete params.memberConfirmation;
          delete params.applicationId;
          params.preregistration = true;
          params.emailConfirmationCode = params.confirmationCode;
          delete params.confirmationCode;
          r.changePage('confirm', 'signup', params);
          return;
        }

        r.log.info('membership confirmed', 'ConfirmMemberView.success');
        this.proceed();
      } catch (e) {
        r.log.error(e, 'ConfirmMemberView.success');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) {  // This is an apiError.
          r.log.info(response.responseText, 'ConfirmMemberView.error');
          return;
        }

        // Shouldn't see errors except for apiStatus returns handled above.
        r.log.error('Unexpected execution: ' + response, 'ConfirmMemberView.error');
      } catch (e) {
        r.log.error(e, 'ConfirmMemberView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = +r.getApiStatus(jqXHR.responseText);

        switch (code) {
        case 214:
          this.proceed();
          return;
        case 606:
          r.log.warn('App Member not found.', 'ConfirmMemberView.apiError');
          this.proceed(true);
          return;
        case undefined:
          r.log.warn('Undefined apiStatus: ' + code, 'ConfirmMemberView.apiError');
          break;
        }
        this.model.clear({silent: true});
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'ConfirmMemberView.apiError');
      }
    },

    render: function () {
      try {
        r.log.info('entering', 'ConfirmMemberView.render');
        var content = this.template(this.model.getMock());

        r.getHeaderDiv().find('h1').text('Confirmation');
        this.$el.html(content);
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'ConfirmMemberView.render');
      }
    },

    proceed: function (signup) {
      try {
        if (signup) {
          r.changePage('root', 'signup');
          return;
        }
        r.changePage('application', 'app', { appId: r.session.params.applicationId });
      } catch (e) {
        r.log.error(e, 'ConfirmMemberView.proceed');
      }
    },

    apiCodes: {
      214: 'Member not pending confirmation.',
      215: 'Member not a registered user.',
      309: 'Confirmation code is required.',
      313: 'Email address is required.',
      606: 'App member not found.'
    },
  });

  return r;
}(RSKYBOX || {}, jQuery));
