var RSKYBOX = (function (r, $) {
  'use strict';


  r.LoginView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#loginTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function (e) {
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
      e.preventDefault();
      return false;
    },

    success: function (model, response) {
      r.log.debug('entering', 'Login.success');
      r.logIn(model.token);
    },

    error: function (model, response) {
      r.log.debug('entering', 'LoginView.error');
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.warning(response, this.$el);    // This is a validation error.
    },

    apiError: function (jqXHR) {
      r.log.debug('entering', 'LoginView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'LoginView.apiError');
      }
      this.model.clear({silent: true});
      r.flash.warning(this.apiCodes[code], this.$el);
    },

    render: function () {
      var content = this.template(this.model.getMock());
      $(this.el).empty();
      $(this.el).html(content);
      $(this.el).trigger('create');
      return this;
    },

    apiCodes: {
      200: 'Your username (email or phone) and password were not recognized by the system.',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
