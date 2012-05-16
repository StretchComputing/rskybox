var RSKYBOX = (function (r, $) {
  'use strict';


  r.CrashEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#crashEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'CrashEntryView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        this.$el.html(this.template(mock));
        return this;
      } catch (e) {
        r.log.error(e, 'CrashEntryView.render');
      }
    }
  });


  r.CrashesView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.collection.bind('reset', this.render, this);
        this.template = _.template($('#noCrashesTemplate').html());
      } catch (e) {
        r.log.error(e, 'CrashesView.initialize');
      }
    },

    render: function () {
      try {
        var list;
        r.log.info('entering', 'CrashesView.render');

        this.appLink('back', 'application');

        this.getContent().empty();
        if (this.collection.length <= 0) {
          this.getContent().html(this.template());
        } else {
          list = $('<ul>');
          this.collection.each(function (crash) {
            this.addCrashEntry(list, crash);
          }, this);
          this.getContent().html(list);
          list.listview();
        }
        return this;
      } catch (e) {
        r.log.error(e, 'CrashesView.render');
      }
    },

    addCrashEntry: function (list, crash) {
      try {
        list.append(new r.CrashEntryView({ model: crash }).render().el);
      } catch (e) {
        r.log.error(e, 'CrashesView.addCrashEntry');
      }
    }
  });


  r.CrashView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
    },

    initialize: function () {
      try {
        _.bindAll(this, 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#crashTemplate').html());
      } catch (e) {
        r.log.error(e, 'CrashView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        if (!this.options.status) {
          this.options.status = this.model.get('status');
        }
        this.appLink('back', 'crashes', this.options.status);

        this.getContent().html(this.template(mock));
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'CrashView.render');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'CrashView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'CrashView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'CrashView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'CrashView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'CrashView.apiError');
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
