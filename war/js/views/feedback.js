var RSKYBOX = (function (r, $) {
  'use strict';


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
