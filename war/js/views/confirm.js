var RSKYBOX = (function (r, $) {
  'use strict';


  r.ConfirmUserView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#confirmTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function (e) {
      r.log.debug('entering', 'ConfirmUserView.submit');
      var valid;

      valid = this.model.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        confirmationCode: this.$("input[name='confirmationCode']").val(),
        password: this.$("input[name='password']").val()
      });

      if (valid) {
        this.model.prepareNewModel();
        this.model.save(null, {
          success: this.success,
          statusCode: r.statusCodeHandlers(this.apiError)
        });
      }

      e.preventDefault();
      return false;
    },

    success: function (model, response) {
      r.log.debug('entering', 'ConfirmUserView.success');
      r.setCookie(model.get('token'));
      r.changePage('settings');
    },

    error: function (model, response) {
      r.log.debug('entering', 'ConfirmUserView.error');
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.error(response, this.$el);      // This is a validation error.
    },

    apiError: function (jqXHR) {
      r.log.debug('entering', 'ConfirmUserView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'ConfirmUserView.apiError');
      }
      this.model.clear({silent: true});
      r.flash.error(this.apiCodes[code], this.$el);
    },

    render: function () {
      var content = this.template(this.model.getMock());
      this.$el.html(content);
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
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      206: 'Your email address is not registered in the system.',
      207: 'Your phone number is not registered in the system.',
      308: 'Either an email address or a phone number is required.',
      309: 'Confirmation code is required.',
      311: 'Password is required.',
      403: 'Invalid email address.',
      404: 'Invalid mobile carrier.',
      411: 'Invalid confirmation code.',
      412: 'Password too short.',
      607: 'Email address not found.',
      608: 'Phone number not found.',
      700: 'Email address and phone number are mutually exclusive.',
    }
  });


  r.ConfirmMemberView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'apiError', 'success');
      //this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#confirmTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function (e) {
      r.log.debug('entering', 'ConfirmMemberView.submit');
      this.model.save(null, {
        success: this.success,
        statusCode: r.statusCodeHandlers(this.apiError),
        wait: true,
      });
      e.preventDefault();
      return false;
    },

    success: function (model, response) {
      r.log.debug('entering', 'ConfirmMemberView.success');
      var params;

      if (+model.get('apiStatus') === 215) {
        r.log.debug('Member is not a registered user.', 'ConfirmMemberView.success');
        params = r.session.params;
        delete params.memberConfirmation;
        delete params.applicationId;
        r.changePage('confirm', 'signup', params);
        return;
      }

      r.log.debug('membership confirmed', 'ConfirmMemberView.success');
      this.proceed();
    },

    error: function (model, response) {
      r.log.debug('entering', 'ConfirmMemberView.error');
      if (response.responseText) { return; }  // This is an apiError.

      // Shouldn't see errors except for apiStatus returns handled above.
      r.log.error('Unexpected execution: ' + response, 'ConfirmMemberView.error');
    },

    apiError: function (jqXHR) {
      var code = +r.getApiStatus(jqXHR.responseText);

      switch (code) {
      case 214:
        this.proceed();
        return;
      case 606:
        r.log.error('App Member not found.', 'ConfirmMemberView.apiError');
        this.proceed(true);
        return;
      case undefined:
        r.log.error('Undefined apiStatus: ' + code, 'ConfirmMemberView.apiError');
        break;
      }
      this.model.clear({silent: true});
      r.flash.error(this.apiCodes[code], this.$el);
    },

    render: function () {
      r.log.debug('entering', 'ConfirmMemberView.render');
      var content = this.template(this.model.getMock());

      this.$el.html(content);
      this.$('#emailWrapper').show();
      this.$('#phoneWrapper').hide();
      this.$el.trigger('create');
      return this;
    },

    proceed: function (signup) {
      if (signup) {
        r.changePage('root', 'signup');
      } else if (r.isCookieSet()) {
        r.changePage('applications');
      } else {
        r.changePage('login', 'signup');
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
