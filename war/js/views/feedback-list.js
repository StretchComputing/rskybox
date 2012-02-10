var RSKYBOX = (function (r, $) {
  'use strict';


  r.FeedbackEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#feedbackEntryTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      return this;
    }
  });

  r.FeedbackListView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'addFeedbackEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noFeedbackTemplate').html());
    },

    render: function () {
      var list;

      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (app) {
          this.addFeedbackEntry(list, app);
        }, this);
        this.$el.html(list);
        list.listview();
      }
      return this;
    },

    addFeedbackEntry: function (list, feedback) {
      list.append(new r.FeedbackEntryView({ model: feedback }).render().el);
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
