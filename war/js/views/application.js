'use strict';


var rskybox = (function(r, $) {


  r.ApplicationView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'render');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#applicationTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function(e) {
      var valid;

      r.log.debug('ApplicationView.submit');

      valid = this.model.set({
        name: this.$("input[name='name']").val(),
        version: this.$("input[name='version']").val(),
      });

      if (valid) {
        this.model.prepareNewModel();

        this.model.save(null, {
          success: this.success,
          statusCode: {
            422: this.apiError
          }
        });
      }

      e.preventDefault();
      return false;
    },

    success: function(model, response) {
      $.mobile.changePage('#application');
    },

    error: function(model, response) {
      r.log.debug('ApplicationView.error');
      r.dump(model);
      r.dump(response);
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response, this.$el);
    },

    apiError: function(jqXHR) {
      r.log.debug('ApplicationView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('ApplicationView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    render: function() {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    apiCodes: {
      605: 'The application was not found.'
    }
  });


  return r;
}(rskybox || {}, jQuery));
