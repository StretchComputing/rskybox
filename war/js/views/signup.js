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
      r.log.info('entering', 'SignupView.submit');
      var valid;

      valid = this.model.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        mobileCarrierId: this.$("select[name='mobileCarrierId']").val()
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
      var params = model.toJSON();

      params.preregistration = true;
      $.mobile.changePage('#confirm' + r.buildQueryString(params));
    },

    error: function (model, response) {
      r.log.info(response, 'SignupView.error');
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.warning(response, this.$el);    // This is a validation error.
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.info(code, 'SignupView.apiError');

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'SignupView.apiError');
      }

      r.flash.warning(this.apiCodes[code]);
    },

    render: function () {
      r.log.info('entering', 'SignupView.render');
      var content = this.template(this.model.getMock());

      this.$el.html(content);
      this.$el.trigger('create');
      if (!this.carriersView) {
        this.carriersView = new r.CarriersView({
          el: $('#mobileCarrierId'),
          collection: new r.Carriers()
        });
        this.carriersView.value = this.model.get('mobileCarrierId');
        this.carriersView.collection.fetch();
      } else {
        this.carriersView.setElement($('#mobileCarrierId'));
        this.carriersView.value = this.model.get('mobileCarrierId');
        this.carriersView.render();
      }
      return this;
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      308: 'Either an email address or a phone number is required.',
      403: 'Invalid email address.',
      404: 'Invalid mobile carrier.',
      410: 'Invalid phone number.',
      500: 'Phone number and mobile carrier ID must be specified together.',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
