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


  r.FeedbackSelectionView = Backbone.View.extend({
    initialize: function () {
      this.params = r.router.getParams(location.hash);
    },

    render: function () {
      var hrefTemplate = _.template('#feedbackList?id=<%= id %>&status=<%= status %>'),
          model = {};

      model.id = this.params.id;
      if (this.params.status === 'archived') {
        model.status = 'new';
        model.display = 'Active';
      } else {
        model.status = 'archived';
        model.display = 'Archives';
      }

      this.$el.attr('href', hrefTemplate(model));
      this.$el.find('.ui-btn-text').text(model.display);
      return this;
    }
  });


  r.FeedbackListView = r.JqmPageBaseView.extend({
    initialize: function () {
      _.bindAll(this, 'addFeedbackEntry');
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

        new r.FeedbackSelectionView({
          el: this.getHeader().find('.archives'),
        }).render();
      }
      return this;
    },

    addFeedbackEntry: function (list, feedback) {
      list.append(new r.FeedbackEntryView({ model: feedback }).render().el);
    }
  });



  r.FeedbackView = Backbone.View.extend({
    initialize: function () {
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#feedbackTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
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
      r.flashError(response, this.$el);
    },

    apiError: function (jqXHR) {
      r.log.debug('FeedbackView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('FeedbackView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    apiCodes: {
      203: 'You are not authorized for this application.',
      305: 'Application ID required.',
      601: 'Feedback was not found',
      605: 'Application was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
