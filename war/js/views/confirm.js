'use strict';


var rskybox = (function(r, $) {


  r.ConfirmView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'apiError', 'error');
      this.model.bind('change', this.render, this);
      this.template = _.template($('#confirmTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function(e) {
      this.model.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        confirmationCode: this.$("input[name='confirmationCode']").val(),
        password: this.$("input[name='password']").val()
      }, {silent: true});

      this.model.prepareNewModel();
      this.model.set({id: 'id'}, {silent: true});
      console.log(this.model.toJSON());

      this.model.save(this.model.toJSON(), {
        success: this.success,
        error: this.error,
        statusCode: {
          422: this.apiError
        }
      });
      e.preventDefault();
      return false;
    },

    success: function(model, response) {
      r.log.debug('Confirm success.');
      $.mobile.changePage('/applications');
    },

    error: function(model, response) {
      console.log('Confirm error:', model, response);
      if (response.responseText) {

        r.log.debug('Signup error: skipping apiError');
        return;
      }
      // If we get here, we're processing a validation error.
      r.flashError(response, this.el);
    },

    apiError: function(jqXHR) {
      r.log.debug('Confirm apiError');
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
      if (this.model.get('emailAddress')) {
        this.$('#emailWrapper').show();
        this.$('#phoneWrapper').hide();
      }
      if (this.model.get('phoneNumber')) {
        this.$('#emailWrapper').hide();
        this.$('#phoneWrapper').show();
      }
      $(this.el).trigger('create');
      return this;
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      206: 'Your email address is not registered in the system.',
      207: 'Your phone number is not registered in the system.',
      308: 'Either an email address or a phone number is required.',
      309: 'Confirmation code is required.',
      411: 'Invalid confirmation code.',
      700: 'Email address and phone number are mutually exclusive.'
    }
  });


  return r;
})(rskybox || {}, jQuery);
