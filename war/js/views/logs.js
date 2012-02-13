var RSKYBOX = (function (r, $) {
  'use strict';


  r.LogEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#logEntryTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      return this;
    }
  });

  r.LogsView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'addLogEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noLogsTemplate').html());
    },

    render: function () {
      var list;

      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (log) {
          this.addLogEntry(list, log);
        }, this);
        this.$el.html(list);
        list.listview();
      }
      return this;
    },

    addLogEntry: function (list, log) {
      list.append(new r.LogEntryView({ model: log }).render().el);
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
