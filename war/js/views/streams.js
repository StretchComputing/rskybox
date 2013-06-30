var RSKYBOX = (function (r, $) {
  'use strict';


  r.StreamEntryView = Backbone.View.extend({
    tagName: 'li',

    events: {
      'click .join': 'join',
      'click .close': 'close'
    },

    initialize: function () {
      try {
        _.bindAll(this, 'render', 'join', 'close');
        this.template = _.template($('#streamEntryTemplate').html());
				this.model.setAppUrl(r.session.params.appId);
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
    },

    close: function(evt) {
      try {
        r.log.info('entering', 'StreamEntryView.close');
				r.closeStream(this.model);
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'StreamEntryView.close');
      }
    },

    join: function(evt) {
      try {
        r.log.info('entering', 'StreamEntryView.join');
				r.joinStream(this.model);
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'StreamEntryView.join');
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
        // By adding the "id" to the view constructor, the list item with contain that ID as an attribute -->  <li id='<xyz>' >
        // With this unique ID, list items can be modified later (e.g. updating the stream status after joining)
        list.append(new r.StreamEntryView({ model: stream, id: stream.get('id') }).render().el);
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
        _.bindAll(this, 'render', 'success', 'apiError', 'fetchPackets');
        this.packetTimer = null;
        this.collection.on('error', this.error, this);
        this.template = _.template($('#packetsTemplate').html());
        this.templateNoPackets = _.template($('#noPacketsTemplate').html());
      } catch (e) {
        r.log.error(e, 'StreamView.initialize');
      }
    },

    fetchPackets: function () {
      var that = this;

      this.collection.fetch({
        success: this.render,
        statusCode: r.statusCodeHandlers(this.apiError)
      });
      this.packetTimer = window.setTimeout(function () { that.fetchPackets(); }, 4000);
    },

    render: function () {
      try {
        var list = this.getContent().find('ul');

        this.appLink('back', 'streams');

        if (!list[0] && this.collection.length <= 0) {
          this.getContent().html(this.templateNoPackets());
        } else {
          list = list[0] ? $(list[0]) : $('<ul>');
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
      225: 'The stream is closed.',
      305: 'Application ID required.',
      605: 'Application was not found'
    }
  });

  // may be called by multiple stream views
  r.closeStream = function(streamModel) {
    try {
			var closeUrl = streamModel.urlRoot + "/" + streamModel.get('id');
      var jsonobj = {"status" : "closed"};

      $.ajax({
        type: 'put',
        data: JSON.stringify(jsonobj),
        datatype: 'json',
        contenttype: 'application/json',
        url: closeUrl,
        statuscode: r.statusCodeHandlers(),
        success: function() {
                    try {
                      // in the stream entry list, update the status of this stream
                      $('#' + streamModel.get('id')).find('.streamStatus').html("Closed");
                      $('#' + streamModel.get('id')).find('.close').hide();
                      r.flash.info("stream successfully closed", 3);
                    } catch (e) {
                      r.log.error(e, 'closeStream.success');
                    }
                  }
      });
    } catch (e) {
      r.log.error(e, 'closeStream');
    }
  };

  // may be called by multiple stream views
  r.joinStream = function(streamModel) {
    try {
      var currentUser = r.store.getItem(r.session.keys.currentUser);
      var jsonobj = {"name" : streamModel.get('name'), "memberId" : currentUser.emailAddress};

      $.ajax({
        type: 'post',
        data: JSON.stringify(jsonobj),
        datatype: 'json',
        contenttype: 'application/json',
        url: streamModel.urlRoot,
        statuscode: r.statusCodeHandlers(),
        success: function() {
                    try {
                      // in the stream entry list, update the status of this stream
                      $('#' + streamModel.get('id')).find('.streamStatus').html("Open");
                      $('#' + streamModel.get('id')).find('.join').hide();
                      $('#' + streamModel.get('id')).find('.close').show();
                      r.flash.info("stream successfully joined", 3);
                    } catch (e) {
                      r.log.error(e, 'joinStream.success');
                    }
                  }
      });
    } catch (e) {
      r.log.error(e, 'joinStream');
    }
  };

  return r;
}(RSKYBOX || {}, jQuery));
