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
    },

    initialize: function () {
      try {
        _.bindAll(this, 'render', 'success', 'apiError');
        this.collection.on('change reset', this.render, this);
        this.collection.on('error', this.error, this);
        this.template = _.template($('#packetsTemplate').html());
        this.templateNoPackets = _.template($('#noPacketsTemplate').html());
      } catch (e) {
        r.log.error(e, 'StreamView.initialize');
      }
    },

    fetchPackets: function () {
      console.log('<<<<<<<< fetchPackets >>>>>>>>>>', 'entered');
    },

    render: function () {
      try {
        var list;

        this.appLink('back', 'streams');

        if (this.collection.length <= 0) {
          this.getContent().html(this.templateNoPackets());
        } else {
          list = $('<ul>');
          this.collection.each(function (packet) {
            var item = $('<li>');

            item.html(packet.get('body'));
            list.append(item);
          }, this);
          this.getContent().html(list);
          list.listview();
        }
        return this;
      } catch (e) {
        r.log.error(e, 'StreamView.render');
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
