var RSKYBOX = (function (r, $) {
  'use strict';


  r.FeedbackEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#feedbackEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'FeedbackEntryView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        this.$el.html(this.template(mock));
        return this;
      } catch (e) {
        r.log.error(e, 'FeedbackEntryView.render');
      }
    }
  });


  r.FeedbackListView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.collection.bind('reset', this.render, this);
        this.template = _.template($('#noFeedbackTemplate').html());
      } catch (e) {
        r.log.error(e, 'FeedbackListView.initialize');
      }
    },

    render: function () {
      try {
        var list;

        this.appLink('back', 'application');

        this.getContent().empty();
        if (this.collection.length <= 0) {
          this.getContent().html(this.template());
        } else {
          list = $('<ul>');
          this.collection.each(function (feedback) {
            this.addFeedbackEntry(list, feedback);
          }, this);
          this.getContent().html(list);
          list.listview();
        }
        return this;
      } catch (e) {
        r.log.error(e, 'FeedbackListView.render');
      }
    },

    addFeedbackEntry: function (list, feedback) {
      try {
        list.append(new r.FeedbackEntryView({ model: feedback }).render().el);
      } catch (e) {
        r.log.error(e, 'FeedbackListView.addFeedbackEntry');
      }
    }
  });


  r.FeedbackView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
    },

    initialize: function () {
      try {
        _.bindAll(this, 'changeStatus', 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#feedbackTemplate').html());
      } catch (e) {
        r.log.error(e, 'FeedbackView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        if (!this.options.status) {
          this.options.status = this.model.get('status');
        }
        this.appLink('back', 'feedbackList', this.options.status);

        this.getContent().html(this.template(mock));
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'FeedbackView.render');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'FeedbackView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'FeedbackView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'FeedbackView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'FeedbackView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }

        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'FeedbackView.apiError');
      }
    },

    apiCodes: {
      203: 'You are not authorized for this application.',
      305: 'Application ID required.',
      319: 'Incident ID required.',
      400: 'Invalid status.',
      605: 'Application was not found',
      609: 'Incident was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
