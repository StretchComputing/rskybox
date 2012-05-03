var RSKYBOX = (function (r, $) {
  'use strict';


  r.iLogEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#iLogEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'iLogEntryView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        mock.lastUpdatedDate = r.format.longDate(mock.lastUpdatedDate);
        // TODO - remove this line once appId is set on the server.
        mock.appId = mock.appId || r.session.params.appId;
        this.$el.html(this.template(mock));
        return this;
      } catch (e) {
        r.log.error(e, 'iLogEntryView.render');
      }
    }
  });


  r.iLogsView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        r.log.info('entering', 'iLogsView.initialize');
        _.bindAll(this, 'addLogEntry');
        this.collection.bind('reset', this.render, this);
        this.template = _.template($('#iNoLogsTemplate').html());
      } catch (e) {
        r.log.error(e, 'iLogsView.initialize');
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
        r.log.error(e, 'iLogsView.render');
      }
    },

    addLogEntry: function (list, log) {
      try {
        list.append(new r.iLogEntryView({ model: log }).render().el);
      } catch (e) {
        r.log.error(e, 'iLogsView.addLogEntry');
      }
    }
  });


  r.iLogView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
      'click .mode': 'changeMode',
      'click .more': 'moreLogs',
    },

    initialize: function () {
      try {
        _.bindAll(this, 'changeStatus', 'changeMode', 'success');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#iLogTemplate').html());
      } catch (e) {
        r.log.error(e, 'iLogView.');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        this.appLink('back', 'ilogs');

        mock.lastUpdatedDate = r.format.longDate(mock.lastUpdatedDate);
        mock.createdDate = r.format.longDate(mock.createdDate);
        if (Array.isArray(mock.stackBackTrace)) {
          mock.stackBackTrace = mock.stackBackTrace.join('<br>');
        }
        this.getContent().html(this.template(mock));
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'iLogView.render');
      }
    },

    changeMode: function (evt) {
      try {
        var json;

        json = JSON.stringify({
          mode : (this.model.get('mode') === 'inactive' ? 'active' : 'inactive')
        });
        $.ajax({
          url: this.model.urlRoot + '/remoteControl/' + this.model.get('id'),
          type: 'PUT',
          data: json,
          success: this.success,
          statusCode: r.statusCodeHandlers(this.apiError),
        });

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'iLogView.changeMode');
      }
    },

    moreLogs: function (evt) {
      try {
        this.logsView = new r.LogsView({
          el: this.$el.find('.logsView'),
          collection: new r.Logs()
        });
        this.logsView.collection.setAppUrl(r.session.params.appId);

        this.logsView.collection.fetch({data: { incidentId : this.model.get('id') }});

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'iLogView.moreLogs');
      }
    },

    success: function () {
      try {
        this.model.fetch();
      } catch (e) {
        r.log.error(e, 'iLogView.succes');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'iLogView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'iLogView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'iLogView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'iLogView.apiError');
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'iLogView.apiError');
      }
    },

    apiCodes: {
      201: 'Invalid status.',
      203: 'You are not authorized for this application.',
      305: 'Application ID required.',
      316: 'Mode is required.',
      319: 'Incident ID required.',
      416: 'Invalid mode.',
      605: 'Application was not found',
      609: 'Incident was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
