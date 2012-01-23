'use strict';


var rskybox = (function(r, $) {


  r.SignupView = Backbone.View.extend({
    initialize: function() {
      this.model.bind('change', this.render, this);
      this.template = _.template($('#signupTemplate').html());
    },

    render: function() {
      var content = this.template(this.model.toJSON());
      $(this.el).html(content);
      $(this.el).trigger('create');
      return this;
    }
  });


  return r;
})(rskybox || {}, jQuery);
