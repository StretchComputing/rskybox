'use strict';


var rskybox = (function(r, $) {


  r.LoginView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'apiError', 'error');
      this.model.bind('change', this.render, this);
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
        password: this.$("input[name='password']").val()
      }, {
        error: this.error
      });

      if (valid) {
        this.model.prepareNewModel();
        console.log(this.model.url);
        console.log(JSON.stringify(this.model.getQueryObject()));

        $.ajax({
          url: this.model.url,
          dataType: 'json',
          data: this.model.getQueryObject(),
          success: this.success,
          error: this.error,
          statusCode: {
            422: this.apiError
          }
        });
      }
      e.preventDefault();
      return false;
    },

    success: function(model, response) {
      r.log.debug('Login success.');
      $.mobile.changePage('/applications');
    },

    error: function(model, response) {
      console.log('Login error:', model, response);
      if (response.responseText) {

        r.log.debug('Login error: skipping apiError');
        return;
      }
      // If we get here, we're processing a validation error.
      r.flashError(response, this.el);
    },

    apiError: function(jqXHR) {
      r.log.debug('Login apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.debug('An unknown API error occurred: ' + code);
      }
      this.model.clear({silent: true});

      r.flashError(this.apiCodes[code], this.el);
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
