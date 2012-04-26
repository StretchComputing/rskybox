var RSKYBOX = (function (r, $) {
  'use strict';


  r.AppEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#appEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'AppEntryView.initialize');
      }
    },

    render: function () {
      try {
        this.$el.html(this.template(this.model.getMock()));
        return this;
      } catch (e) {
        r.log.error(e, 'AppEntryView.render');
      }
    }
  });


  r.ApplicationsView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'addAppEntry');
        this.collection.bind('reset', this.render, this);
        this.template = _.template($('#noAppsTemplate').html());
        r.log.error(new Error('testing appActions'), 'ApplicationsView');
      } catch (e) {
        r.log.error(e, 'ApplicationsView.initialize');
      }
    },

    render: function () {
      try {
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
      } catch (e) {
        r.log.error(e, 'ApplicationsView.render');
      }
    },

    addAppEntry: function (list, app) {
      try {
        list.append(new r.AppEntryView({ model: app }).render().el);
      } catch (e) {
        r.log.error(e, 'ApplicationsView.addAppEntry');
      }
    }
  });


  r.ApplicationView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.options.applications.on('reset', this.setModel, this);
        this.template = _.template($('#applicationTemplate').html());
      } catch (e) {
        r.log.error(e, 'ApplicationView.initialize');
      }
    },

    events: {
      'submit': 'submit'
    },

    setModel: function () {
      try {
        this.model.set(this.options.applications.findById(r.session.params.appId));
      } catch (e) {
        r.log.error(e, 'ApplicationView.setModel');
      }
    },

    // TODO - implement update application attributes form
    // This submit function isn't used, yet.
    submit: function (evt) {
      try {
        var valid;
        r.log.info('entering', 'ApplicationView.submit');

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

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'ApplicationView.submit');
      }
    },

    success: function (model, response) {
      try {
        $.mobile.changePage('#application');
      } catch (e) {
        r.log.error(e, 'ApplicationView.success');
      }
    },

    error: function (model, response) {
      try {
        r.log.info(response, 'ApplicationView.error');

        if (response.responseText) { return; }  // This is an apiError.
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'ApplicationView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'ApplicationView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'ApplicationView.apiError');
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'ApplicationView.apiError');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        mock.date = r.format.longDate(mock.date);
        this.getContent().html(this.template(mock));
        this.getContent().trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'ApplicationView.render');
      }
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
      r.log.info('entering', 'NewApplicationView.submit');

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
      if (response.responseText) { return; }  // This is an apiError.
      r.log.info(response, 'NewApplicationView.error');
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.info(code, 'NewApplicationView.apiError');

      if (!this.apiCodes[code]) {
        r.log.warn('Undefined apiStatus: ' + code, 'NewApplicationView.apiError');
      }
      r.flash.warning(this.apiCodes[code]);
    },

    render: function () {
      r.log.info('entering', 'NewApplicationView.render');
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
