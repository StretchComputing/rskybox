var RSKYBOX = (function (r, $) {
  'use strict';


  r.EnduserEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#enduserEntryTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      return this;
    }
  });

  r.EndusersView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'addEnduserEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noEndusersTemplate').html());
    },

    render: function () {
      var list;

      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (enduser) {
          this.addEnduserEntry(list, enduser);
        }, this);
        this.$el.html(list);
        list.listview();
      }
      return this;
    },

    addEnduserEntry: function (list, enduser) {
      list.append(new r.EnduserEntryView({ model: enduser }).render().el);
    }
  });


  r.EnduserView = Backbone.View.extend({
    initialize: function () {
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#enduserTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    error: function (model, response) {
      r.log.debug('EnduserView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response, this.$el);
    },

    apiError: function (jqXHR) {
      r.log.debug('EnduserView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('EnduserView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    apiCodes: {
      203: 'You are not authorized for this application.',
      304: 'End user ID required.',
      305: 'Application ID required.',
      604: 'End user was not found',
      605: 'Application was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
