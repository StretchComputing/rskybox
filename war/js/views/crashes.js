var RSKYBOX = (function (r, $) {
  'use strict';


  r.CrashEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#crashEntryTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
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
      this.getContent().html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    error: function (model, response) {
      r.log.debug('CrashView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flash.error(response);
    },

    apiError: function (jqXHR) {
      r.log.debug('CrashView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('CrashView: An unknown API error occurred: ' + code);
      }

      r.flash.error(this.apiCodes[code]);
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
