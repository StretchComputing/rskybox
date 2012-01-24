'use strict';


var rskybox = (function(r, $) {


  r.SignupView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'handleError');
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
        error: this.handleError,
      });
      e.preventDefault();
      return false;
    },

    handleSuccess: function(model, response) {
      r.log.debug('handleSuccess called');
      if (response.apiStatus) {
        // The apiError event handler will take care of this for us.
        return;
      }
      $.mobile.changePage('#confirm');
    },

    handleError: function(model, response) {
      r.log.debug('handleError called');
      r.flashError(this.el, response);
    },

    apiError: function(error) {
      r.log.debug(error);
      r.flashError(error);
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
    }
  });


  return r;
})(rskybox || {}, jQuery);
