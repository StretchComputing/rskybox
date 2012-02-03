'use strict';


var rskybox = (function(r, $) {


  r.ApplicationView = Backbone.View.extend({
    tagName: 'li',

    initialize: function() {
      _.bindAll(this, 'render');
      template: _.template('#appListTemplate');
    },

    render: function() {
      $(this.el).attr('value', this.model.get('id')).html(this.model.get('name'));
      return this;
    }
  });

  r.ApplicationsView = Backbone.View.extend({
    tagName: 'ul',

    initialize: function() {
      _.bindAll(this, 'addApplication');
      this.collection.bind('reset', this.render, this);
    },

    render: function() {
      $(this.el).empty();
      this.collection.each(this.addApplication);
      //$(this.el).selectmenu('refresh');
      return this;
    },

    addApplication: function(app) {
      $(this.el).append(new r.ApplicationView({ model: app }).render().el);
    }
  });


  return r;
})(rskybox || {}, jQuery);
