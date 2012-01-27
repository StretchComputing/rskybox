'use strict';


var rskybox = (function(r, $) {


  r.ConfirmView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'apiError');
      this.model.bind('error', this.validationError, this);
      this.model.bind('change', this.render, this);
      this.template = _.template($('#confirmTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function(e) {
      r.log.debug('Confirm submit');
      var form = new r.BaseModel();
      form.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        confirmationCode: this.$("input[name='confirmationCode']").val(),
        password: this.$("input[name='password']").val(),
        id: 'temp'
      }, {silent: true});

        form.prepareNewModel();
      r.dump(form);

      this.model.save(form, {
        success: this.success,
        statusCode: {
          422: this.apiError
        }
      });
      e.preventDefault();
      return false;
    },

    success: function(model, response) {
      r.log.debug('Confirm success.');
      //$.mobile.changePage('#confirm' + r.buildQueryString(model.toJSON()));
    },

    validationError: function(model, response) {
      r.log.debug('Confirm validationError.');
      r.dump(model);
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
      var content = this.template(this.model.toJSON());
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
