var RSKYBOX = (function (r, $) {
  'use strict';


  r.LogEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#logEntryTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      return this;
    }
  });

  r.LogsView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'addLogEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noLogsTemplate').html());
    },

    render: function () {
      var list;

      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (log) {
          this.addLogEntry(list, log);
        }, this);
        this.$el.html(list);
        list.listview();
      }
      return this;
    },

    addLogEntry: function (list, log) {
      list.append(new r.LogEntryView({ model: log }).render().el);
    }
  });


  r.LogView = Backbone.View.extend({
    initialize: function () {
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#logTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    error: function (model, response) {
      r.log.debug('LogView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response, this.$el);
    },

    apiError: function (jqXHR) {
      r.log.debug('LogView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('LogView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    apiCodes: {
      203: 'You are not authorized for this application.',
      305: 'Application ID required.',
      603: 'Log was not found',
      605: 'Application was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));