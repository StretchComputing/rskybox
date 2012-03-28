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


  r.ApplicationsView = r.JqmPageBaseView.extend({
    initialize: function () {
      _.bindAll(this, 'addAppEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noAppsTemplate').html());
    },

    render: function () {
      var list;

      if (this.collection.length <= 0) {
        this.getContent().html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (app) {
          this.addAppEntry(list, app);
        }, this);
        this.getContent().html(list);
        list.listview();
      }
      return this;
    },

    addAppEntry: function (list, app) {
      list.append(new r.AppEntryView({ model: app }).render().el);
    }
  });


  r.ApplicationView = r.JqmPageBaseView.extend({
    initialize: function () {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#applicationTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    // TODO - implement update application attributes form
    // This submit function isn't used, yet.
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
          statusCode: r.statusCodeHandlers(this.apiError)
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
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      r.log.debug('ApplicationView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'ApplicationView.apiError');
      }
      r.flash.warning(this.apiCodes[code]);
    },

    render: function () {
      this.getHeader().find('h1').html(this.model.get('name'));
      this.getContent().html(this.template(this.model.getMock()));
      this.getContent().trigger('create');
      return this;
    },

    apiCodes: {
      605: 'The application was not found.',
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
      r.session.reset();
      $.mobile.changePage('#application?appId=' + model.get('applicationId'));
    },

    error: function (model, response) {
      r.log.debug('NewApplicationView.error');
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.debug('NewApplicationView.apiError');

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'NewApplicationView.apiError');
      }
      r.flash.warning(this.apiCodes[code], this.$el);
    },

    render: function () {
      r.log.debug('entering', 'NewApplicationView.render');
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
