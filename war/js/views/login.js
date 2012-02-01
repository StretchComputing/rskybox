'use strict';


var rskybox = (function(r, $) {


  r.LoginView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#loginTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function(e) {
      var valid;

      valid = this.model.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        password: this.$("input[name='password']").val(),
        fullValidation: true
      }, {silent: true});

      if (valid) {
        this.model.prepareNewModel();

        $.ajax({
          url: this.model.url,
          dataType: 'json',
          data: this.model.getQueryObject(),
          success: this.success,
          statusCode: {
            422: this.apiError
          }
        });
      }
      e.preventDefault();
      return false;
    },

    success: function(model, response) {
      r.log.debug('Login.success');
      Cookie.set('token', model.token, 9000, '\/');
      r.changePage('applications');
    },

    error: function(model, response) {
      r.log.debug('LoginView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response, this.$el);
    },

    apiError: function(jqXHR) {
      r.log.debug('LoginView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.debug('LoginView: An unknown API error occurred: ' + code);
      }
      this.model.clear({silent: true});

      r.flashError(this.apiCodes[code], this.$el);
    },

    render: function() {
      var content = this.template(this.model.getMock());
      $(this.el).empty();
      $(this.el).html(content);
      $(this.el).trigger('create');
      return this;
    },

    apiCodes: {
      200: 'Your username (email or phone) and password were not recognized by the system.'
    }
  });


  return r;
})(rskybox || {}, jQuery);
