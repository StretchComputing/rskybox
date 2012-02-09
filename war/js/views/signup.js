var RSKYBOX = (function (r, $) {
  'use strict';


  r.SignupView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#signupTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function (e) {
      var valid;

      r.log.debug('SignupView.submit');

      valid = this.model.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        mobileCarrierId: this.$("select[name='mobileCarrierId']").val()
      });

      if (valid) {
        this.model.prepareNewModel();

        this.model.save(null, {
          success: this.success,
          statusCode: {
            422: this.apiError
          }
        });
      }

      e.preventDefault();
      return false;
    },

    success: function (model, response) {
      $.mobile.changePage('#confirm' + r.buildQueryString(model.toJSON()));
    },

    error: function (model, response) {
      r.log.debug('SignupView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response, this.$el);
    },

    apiError: function (jqXHR) {
      r.log.debug('SignupView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('SignupView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    render: function () {
      var content = this.template(this.model.getMock());

      r.log.debug('SignupView.render');

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
}(RSKYBOX || {}, jQuery));
