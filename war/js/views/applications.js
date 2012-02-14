var RSKYBOX = (function (r, $) {
  'use strict';


  r.AppEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#appEntryTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      return this;
    }
  });


  r.ApplicationsView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'addAppEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noAppsTemplate').html());
    },

    render: function () {
      var list;

      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (app) {
          this.addAppEntry(list, app);
        }, this);
        this.$el.html(list);
        list.listview();
      }
      return this;
    },

    addAppEntry: function (list, app) {
      list.append(new r.AppEntryView({ model: app }).render().el);
    }
  });


  r.ApplicationView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#applicationTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function (e) {
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

    success: function (model, response) {
      $.mobile.changePage('#application');
    },

    error: function (model, response) {
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

    apiError: function (jqXHR) {
      r.log.debug('ApplicationView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('ApplicationView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    apiCodes: {
      605: 'The application was not found.'
    }
  });


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
