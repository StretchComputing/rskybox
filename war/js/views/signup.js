'use strict';


var rskybox = (function(r, $) {
  r.SignupView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'render');
      this.model.bind('change', this.render);
      this.template = _.template($('#signup-template').html());
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
