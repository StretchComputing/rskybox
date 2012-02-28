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


  r.FeedbackListView = r.JqmPageBaseView.extend({
    initialize: function () {
      this.collection.bind('reset', this.render, this);
      this.noFeedbackTemplate = _.template($('#noFeedbackTemplate').html());
    },

    render: function () {
      var list;

      this.getContent().empty();
      if (this.collection.length <= 0) {
        this.getContent().html(this.noFeedbackTemplate());
      } else {
        list = $('<ul>');
        this.collection.each(function (feedback) {
          this.addFeedbackEntry(list, feedback);
        }, this);
        this.getContent().html(list);
        list.listview();
      }
      return this;
    },

    addFeedbackEntry: function (list, feedback) {
      list.append(new r.FeedbackEntryView({ model: feedback }).render().el);
    }
  });



  r.FeedbackView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
    },

    initialize: function () {
      _.bindAll(this, 'changeStatus');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#feedbackTemplate').html());
    },

    render: function () {
      this.getContent().html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    error: function (model, response) {
      r.log.debug('FeedbackView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flash.error(response);
    },

    apiError: function (jqXHR) {
      r.log.debug('FeedbackView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('FeedbackView: An unknown API error occurred: ' + code);
      }

      r.flash.error(this.apiCodes[code]);
    },

    apiCodes: {
      201: 'Invalid status.',
      203: 'You are not authorized for this application.',
      300: 'Feedback ID required.',
      305: 'Application ID required.',
      601: 'Feedback was not found',
      605: 'Application was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
