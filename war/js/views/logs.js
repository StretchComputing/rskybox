var RSKYBOX = (function (r, $) {
  'use strict';


  r.LogEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#logEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'LogEntryView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        mock.date = r.format.longDate(mock.date, true);
        this.$el.html(this.template(mock));
        return this;
      } catch (e) {
        r.log.error(e, 'LogEntryView.render');
      }
    }
  });

  r.LogsView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'addLogEntry');
        this.collection.bind('reset', this.render, this);
        this.template = _.template($('#noLogsTemplate').html());
      } catch (e) {
        r.log.error(e, 'LogsView.initialize');
      }
    },

    render: function () {
      try {
        var list;

        this.appLink('back', 'application');

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
      } catch (e) {
        r.log.error(e, 'LogsView.render');
      }
    },

    addLogEntry: function (list, log) {
      try {
        list.append(new r.LogEntryView({ model: log }).render().el);
      } catch (e) {
        r.log.error(e, 'LogsView.addLogEntry');
      }
    }
  });


  r.LogView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
      'click .mode': 'changeMode',
    },

    initialize: function () {
      try {
        _.bindAll(this, 'changeStatus', 'changeMode', 'success');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#logTemplate').html());
      } catch (e) {
        r.log.error(e, 'LogView.');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        this.appLink('back', 'logs');

        mock.date = r.format.longDate(mock.date, true);
        if (Array.isArray(mock.stackBackTrace)) {
          mock.stackBackTrace = mock.stackBackTrace.join('<br>');
        }
        this.getContent().html(this.template(mock));
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'LogView.render');
      }
    },

    changeMode: function (evt) {
      try {
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

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'LogView.changeMode');
      }
    },

    success: function () {
      try {
        this.model.fetch();
      } catch (e) {
        r.log.error(e, 'LogView.succes');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'LogView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'LogView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'LogView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'LogView.apiError');
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'LogView.apiError');
      }
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
