var RSKYBOX = (function (r, $) {
  'use strict';


  r.LoginView = Backbone.View.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#loginTemplate').html());
      } catch (e) {
        r.log.error(e, 'LoginView.initialize');
      }
    },

    events: {
      'submit': 'submit'
    },

    submit: function (evt) {
      try {
        var valid;

        valid = this.model.set({
          emailAddress: this.$("input[name='emailAddress']").val(),
          phoneNumber: this.$("input[name='phoneNumber']").val(),
          password: this.$("input[name='password']").val()
        });

        if (valid) {
          this.model.prepareNewModel();

          $.ajax({
            url: this.model.url,
            dataType: 'json',
            data: this.model.getQueryObject(),
            success: this.success,
            statusCode: r.statusCodeHandlers(this.apiError)
          });
        }
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'LoginView.submit');
      }
    },

    success: function (model, response) {
      try {
        r.log.info('entering', 'Login.success');
        r.logIn(model.token);
      } catch (e) {
        r.log.error(e, 'LoginView.success');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'LoginView.error');
        r.flash.warning(response);    // This is a validation error.
      } catch (e) {
        r.log.error(e, 'LoginView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'LoginView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'LoginView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }
        this.model.clear({silent: true});
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'LoginView.apiError');
      }
    },

    render: function () {
      try {
        var content = this.template(this.model.getMock());

        $(this.el).empty();
        $(this.el).html(content);
        $(this.el).trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'LoginView.render');
      }
    },

    apiCodes: {
      200: 'Your username (email or phone) and password were not recognized by the system.',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
