var RSKYBOX = (function (r, $) {
  'use strict';


  r.StreamEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#streamEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'StreamEntryView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        this.$el.html(this.template(mock));
        return this;
      } catch (e) {
        r.log.error(e, 'StreamEntryView.render');
      }
    }
  });


  r.StreamsView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'render');
        // this.collection.bind('reset', this.render, this);
        this.template = _.template($('#noStreamsTemplate').html());
      } catch (e) {
        r.log.error(e, 'StreamsView.initialize');
      }
    },

    render: function () {
      try {
        var list;

        this.appLink('back', 'application');

        this.getContent().empty();
        if (this.collection.isEmpty()) {
          this.getContent().html(this.template());
        } else {
          list = $('<ul>');
          this.collection.each(function (stream) {
            this.addStreamEntry(list, stream);
          }, this);
          this.getContent().html(list);
          list.listview();
        }
        return this;
      } catch (e) {
        r.log.error(e, 'StreamsView.render');
      }
    },

    addStreamEntry: function (list, stream) {
      try {
        list.append(new r.StreamEntryView({ model: stream }).render().el);
      } catch (e) {
        r.log.error(e, 'StreamsView.addStreamEntry');
      }
    }
  });


  r.StreamView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
      'click .mode': 'changeMode',
      'click .issueTracking': 'issueTracking'
    },

    initialize: function () {
      try {
        _.bindAll(this, 'changeStatus', 'changeMode', 'issueTracking', 'success', 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#streamTemplate').html());
      } catch (e) {
        r.stream.error(e, 'StreamView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        if (!this.options.status) {
          this.options.status = this.model.get('status');
        }
        this.appLink('back', 'streams', this.options.status);

        this.getContent().html(this.template(mock));
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'StreamView.render');
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
          statusCode: r.statusCodeHandlers(this.apiError)
        });

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'StreamView.changeMode');
      }
    },

    issueTracking: function (evt) {
      try {
        var json;
				var githubUrl = this.model.get('githubUrl');

				if(githubUrl && githubUrl.length > 0) {
					// the issue already exists - link to the issue using the URL. This link leaves the rskybox application
					window.location.href = githubUrl;
				} else {
					// create the github issue
					json = JSON.stringify({
						issueTracking : 'create'
					});
					$.ajax({
						url: this.model.urlRoot + "/" + this.model.get('id'),
						type: 'PUT',
						data: json,
						success: this.success,
						statusCode: r.statusCodeHandlers(this.apiError)
					});
				}

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'StreamView.changeMode');
      }
    },

    success: function () {
      try {
        this.model.fetch();
      } catch (e) {
        r.log.error(e, 'StreamView.success');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'StreamView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'StreamView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'StreamView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'StreamView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'StreamView.apiError');
      }
    },

    apiCodes: {
      203: 'You are not authorized for this application.',
      305: 'Application ID required.',
      316: 'Mode is required.',
      319: 'Incident ID required.',
      400: 'Invalid status.',
      416: 'Invalid mode.',
      605: 'Application was not found',
      609: 'Incident was not found'
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
