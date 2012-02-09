var RSKYBOX = (function (r, $) {
  'use strict';


  r.NewApplicationView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#newAppTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function (e) {
      var valid;
      r.log.debug('NewApplicationView.submit');

      valid = this.model.set({
        name: this.$("input[name='name']").val(),
        version: this.$("input[name='version']").val(),
      });

      if (valid) {
        this.model.prepareNewModel();

        this.model.save(null, {
          success: this.success,
          statusCode: r.statusCodeHandlers(this.apiError)
        });
      }

      e.preventDefault();
      return false;
    },

    success: function (model, response) {
      $.mobile.changePage('#application?id=' + model.get('applicationId'));
    },

    error: function (model, response) {
      r.log.debug('NewApplicationView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response, this.$el);
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.debug('NewApplicationView.apiError');

      if (!this.apiCodes[code]) {
        r.log.error('NewApplicationView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    apiCodes: {
      306: 'An application name is required.'
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
