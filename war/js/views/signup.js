'use strict';


var rskybox = (function(r, $) {


  r.SignupView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'handleError', 'handleSuccess', 'handleApiError');
      this.model.bind('change', this.render, this);
      this.model.bind('apiError', this.apiError);
      this.template = _.template($('#signupTemplate').html());
    },

    events: {
      'submit': 'onSubmit'
    },

    onSubmit: function(e) {
      r.log.debug('onSubmit triggered');
      var form = {};

      r.addProperty(form, 'emailAddress', $("input[name='emailAddress']").val());
      r.addProperty(form, 'phoneNumber', $("input[name='phoneNumber']").val());
      r.addProperty(form, 'mobileCarrierId', $("select[name='mobileCarrierId']").val());

      this.model.save(form, {
        success: this.handleSuccess,
        statusCode: {
          422: this.handleApiError
        }
      });
      e.preventDefault();
      return false;
    },

    handleSuccess: function(model, response) {
      r.log.debug('handleSuccess called');
      $.mobile.changePage('#confirm');
    },

    handleError: function(model, response) {
      r.log.debug('handleError called');
      r.flashError(response, this.el);
    },

    handleApiError: function(jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiErrors[code]) {
        r.log.debug('An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiErrors[code], this.el);
    },

    render: function() {
      r.log.debug('signup view render');

      var content = this.template(this.model.toJSON());
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

    apiErrors: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      500: 'Phone number and mobile carrier ID must be specified together.'
    }
  });


  return r;
})(rskybox || {}, jQuery);
