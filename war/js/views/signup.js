'use strict';


var rskybox = (function(r, $) {


  r.SignupView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'apiError', 'error', 'getMockModel');
      this.model.bind('change', this.render, this);
      this.template = _.template($('#signupTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function(e) {
      r.log.debug('Signup submit called');
      var form = new r.BaseModel();

      form.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        mobileCarrierId: this.$("select[name='mobileCarrierId']").val()
      }, {silent: true});

      if (this.model.isNew()) {
        form.prepareNewModel();
      }

      this.model.save(form, {
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
      model.prepareNewModel();
      r.dump(model);
      $.mobile.changePage('#confirm' + r.buildQueryString(model.toJSON()));
    },

    error: function(model, response) {
      if (response.responseText) {
        r.log.debug('Signup error: skipping apiError');
        r.dump(model);
        r.dump(this.model);
        this.model.set(model.toJSON);
        return;
      }
      // If we get here, we're processing a validation error.
      r.flashError(response, this.el);
    },

    apiError: function(jqXHR) {
      r.log.debug('Signup apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.el);
    },

    render: function() {
      r.log.debug('Signup render');
      var content = this.template(this.getMockModel());

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

    getMockModel: function() {
      var mock = {};

      mock.emailAddress = this.model.get('emailAddress') || '';
      mock.phoneNumber = this.model.get('phoneNumber') || '';
      mock.mobileCarrierId = this.model.get('mobileCarrierId') || '';
      return mock;
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      500: 'Phone number and mobile carrier ID must be specified together.'
    }
  });


  return r;
})(rskybox || {}, jQuery);
