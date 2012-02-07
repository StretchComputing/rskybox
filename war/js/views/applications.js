'use strict';


var rskybox = (function(r, $) {


  r.AppEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function() {
      _.bindAll(this, 'render');
      this.template = _.template($('#appEntryTemplate').html());
    },

    render: function() {
      this.$el.html(this.template(this.model.getMock()));
      return this;
    }
  });

  r.ApplicationsView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'addAppEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noAppsTemplate').html());
    },

    render: function() {
      var list;

      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function(app) {
          this.addAppEntry(list, app);
        }, this);
        this.$el.html(list);
        list.listview();
      }
      return this;
    },

    addAppEntry: function(list, app) {
      list.append(new r.AppEntryView({ model: app }).render().el);
    }
  });


  return r;
}(rskybox || {}, jQuery));
