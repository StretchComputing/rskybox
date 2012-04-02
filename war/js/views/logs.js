var RSKYBOX = (function (r, $) {
  'use strict';


  r.LogEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#logEntryTemplate').html());
    },

    render: function () {
      var mock = this.model.getMock();

      mock.date = r.format.longDate(mock.date);
      this.$el.html(this.template(mock));
      return this;
    }
  });

  r.LogsView = r.JqmPageBaseView.extend({
    initialize: function () {
      _.bindAll(this, 'addLogEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noLogsTemplate').html());
    },

    render: function () {
      var list;

      this.getContent().empty();
      if (this.collection.length <= 0) {
        this.getContent().html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (log) {
          this.addLogEntry(list, log);
        }, this);
        this.getContent().html(list);
        list.listview();
      }
      return this;
    },

    addLogEntry: function (list, log) {
      list.append(new r.LogEntryView({ model: log }).render().el);
    }
  });


  r.LogView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
      'click .mode': 'changeMode',
    },

    initialize: function () {
      _.bindAll(this, 'changeStatus', 'changeMode', 'success');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#logTemplate').html());
    },

    render: function () {
      var mock = this.model.getMock();

      mock.date = r.format.longDate(mock.date);
      this.getContent().html(this.template(mock));
      this.$el.trigger('create');
      return this;
    },

    changeMode: function (e) {
      var json;

      json = JSON.stringify({
        mode : (this.model.get('logMode') === 'inactive' ? 'active' : 'inactive')
      });
      $.ajax({
        url: this.model.urlRoot + '/remoteControl/' + this.model.get('logName'),
        type: 'PUT',
        data: json,
        success: this.success,
        statusCode: r.statusCodeHandlers(this.apiError),
      });

      e.preventDefault();
      return false;
    },

    success: function () {
      this.model.fetch();
    },

    error: function (model, response) {
      r.log.debug('LogView.error');
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      r.log.debug('LogView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'LogView.apiError');
      }
      r.flash.warning(this.apiCodes[code]);
    },

    apiCodes: {
      201: 'Invalid status.',
      203: 'You are not authorized for this application.',
      302: 'Log ID required.',
      305: 'Application ID required.',
      603: 'Log was not found',
      605: 'Application was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
