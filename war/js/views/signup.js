'use strict';


var rskybox = (function(r, $) {


  r.SignupView = Backbone.View.extend({
    initialize: function() {
      this.model.bind('change', this.render, this);
      this.template = _.template($('#signupTemplate').html());
      this.carriers = new r.Carriers();
      this.carriersView = new r.CarriersView({
        collection: this.carriers
      });
      this.carriersView.setElId('#mobileCarrierId');
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
        success: function(model, resp) {
          r.log.debug('success saving model');
        },

        error: function() {
          r.log.debug('error saving model');
        }
      });
      e.preventDefault();
      return false;
    },

    render: function() {
      r.log.debug('signup view render');

      var content = this.template(this.model.toJSON());
      $(this.el).empty();
      $(this.el).html(content);
      $(this.el).trigger('create');
      this.carriers.fetch();
      return this;
    }
  });


  return r;
})(rskybox || {}, jQuery);
