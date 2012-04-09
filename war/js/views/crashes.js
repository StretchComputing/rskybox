var RSKYBOX = (function (r, $) {
  'use strict';


  r.CrashEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#crashEntryTemplate').html());
    },

    render: function () {
      var mock = this.model.getMock();

      mock.date = r.format.longDate(mock.date);
      this.$el.html(this.template(mock));
      return this;
    }
  });


  r.CrashesView = r.JqmPageBaseView.extend({
    initialize: function () {
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noCrashesTemplate').html());
    },

    render: function () {
      var list;

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
    },

    addCrashEntry: function (list, crash) {
      list.append(new r.CrashEntryView({ model: crash }).render().el);
    }
  });


  r.CrashView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
    },

    initialize: function () {
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#crashTemplate').html());
    },

    render: function () {
      var mock = this.model.getMock();

      mock.date = r.format.longDate(mock.date);
      this.getContent().html(this.template(mock));
      this.$el.trigger('create');
      return this;
    },

    error: function (model, response) {
      if (response.responseText) { return; }  // This is an apiError.
      r.log.info(response, 'CrashView.error');
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.info(code, 'CrashView.apiError');

      if (!this.apiCodes[code]) {
        r.log.warn('Undefined apiStatus: ' + code, 'CrashView.apiError');
      }
      r.flash.warning(this.apiCodes[code]);
    },

    apiCodes: {
      203: 'You are not authorized for this application.',
      305: 'Application ID required.',
      602: 'Crash was not found',
      605: 'Application was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
