var RSKYBOX = (function (r, $) {
  'use strict';


  r.CrashEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#crashEntryTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      return this;
    }
  });

  r.CrashesView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'addCrashEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noCrashesTemplate').html());
    },

    render: function () {
      var list;

      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (crash) {
          this.addCrashEntry(list, crash);
        }, this);
        this.$el.html(list);
        list.listview();
      }
      return this;
    },

    addCrashEntry: function (list, crash) {
      list.append(new r.CrashEntryView({ model: crash }).render().el);
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
