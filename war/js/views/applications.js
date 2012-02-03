'use strict';


var rskybox = (function(r, $) {


  r.ApplicationView = Backbone.View.extend({
    tagName: 'li',

    initialize: function() {
      _.bindAll(this, 'render');
      this.template = _.template($('#appListTemplate').html());
    },

    render: function() {
      this.$el.html(this.template(this.model));
      return this;
    }
  });

  r.ApplicationsView = Backbone.View.extend({
    tagName: 'ul',

    initialize: function() {
      _.bindAll(this, 'addApplication');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#appEmptyTemplate').html());
    },

    render: function() {
      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        this.collection.each(this.addApplication);
      }
      //this.$el.selectmenu('refresh');
      return this;
    },

    addApplication: function(app) {
      this.$el.append(new r.ApplicationView({ model: app }).render().el);
    }
  });


  return r;
})(rskybox || {}, jQuery);
