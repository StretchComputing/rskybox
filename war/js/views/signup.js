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

    render: function() {
      var content = this.template(this.model.toJSON());
      $(this.el).html(content);
      $(this.el).trigger('create');
      this.carriers.fetch();
      return this;
    }
  });


  return r;
})(rskybox || {}, jQuery);
