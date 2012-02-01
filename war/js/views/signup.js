'use strict';


var rskybox = (function(r, $) {


  r.SignupView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#signupTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function(e) {
      var valid;

      r.log.debug('Signup submit called');
      e.preventDefault();

      valid = this.model.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        mobileCarrierId: this.$("select[name='mobileCarrierId']").val()
      }, {silent: true});

      if (valid) {
        this.model.prepareNewModel();

        this.model.save(null, {
          success: this.success,
          statusCode: {
            422: this.apiError
          }
        });
      }
      return false;
    },

    success: function(model, response) {
      $.mobile.changePage('#confirm' + r.buildQueryString(model.toJSON()));
    },

    error: function(model, response) {
      if (response.responseText) {
        r.log.debug('Signup error: skipping apiError');
        return;
      }
      // If we get here, we're processing a validation error.
      r.log.debug('Signup validation error.');
      r.flashError(response, this.$el);
    },

    apiError: function(jqXHR) {
      r.log.debug('Signup apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    render: function() {
      r.log.debug('Signup render');
      var content = this.template(this.model.getMock());

      $(this.el).empty();
      $(this.el).html(content);
      $(this.el).trigger('create');
      this.carriersView = new r.CarriersView({
        el: $('#mobileCarrierId'),
        collection: new r.Carriers()
      });
      this.carriersView.collection.fetch();
      return this;
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      500: 'Phone number and mobile carrier ID must be specified together.'
    }
  });


  return r;
})(rskybox || {}, jQuery);
