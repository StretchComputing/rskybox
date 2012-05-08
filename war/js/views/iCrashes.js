var RSKYBOX = (function (r, $) {
  'use strict';


  r.iCrashEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#iCrashEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'iCrashEntryView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        this.$el.html(this.template(mock));
        return this;
      } catch (e) {
        r.log.error(e, 'iCrashEntryView.render');
      }
    }
  });


  r.iCrashesView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.collection.bind('reset', this.render, this);
        this.template = _.template($('#iNoCrashesTemplate').html());
      } catch (e) {
        r.log.error(e, 'iCrashesView.initialize');
      }
    },

    render: function () {
      try {
        var list;
        r.log.info('entering', 'iCrashesView.render');

        this.appLink('back', 'application');

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
      } catch (e) {
        r.log.error(e, 'iCrashesView.render');
      }
    },

    addCrashEntry: function (list, crash) {
      try {
        list.append(new r.iCrashEntryView({ model: crash }).render().el);
      } catch (e) {
        r.log.error(e, 'iCrashesView.addCrashEntry');
      }
    }
  });


  r.iCrashView = r.JqmPageBaseView.extend({
    events: {
      'click .changeStatus': 'changeStatus',
      'click .getCrashes': 'getCrashes',
    },

    initialize: function () {
      try {
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#iCrashTemplate').html());
      } catch (e) {
        r.log.error(e, 'iCrashView.initialize');
      }
    },

    render: function () {
      try {
        var mock = this.model.getMock();

        this.appLink('back', 'iCrashes');

        this.getContent().html(this.template(mock));
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'iCrashView.render');
      }
    },

    getCrashes: function (evt) {
      try {
        this.crashesView = new r.CrashesView({
          el: this.$el.find('.crashesView'),
          collection: new r.Crashes()
        });
        r.dump(this.$el.html());
        r.dump(this.crashesView.$el.html());
        this.crashesView.collection.setAppUrl(r.session.params.appId);

        this.crashesView.collection.fetch({data: { incidentId : this.model.get('id') }});
        this.crashesView.$el.show();
        this.$el.find('.getCrashes').parent('.ui-btn').hide();

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'iCrashView.getCrashes');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'iCrashView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'iCrashView.');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'iCrashView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'iCrashView.apiError');
          this.apiCodes[code] = 'An unknown error occurred. Please try again.';
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'iCrashView.');
      }
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
