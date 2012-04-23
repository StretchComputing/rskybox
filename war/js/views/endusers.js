var RSKYBOX = (function (r, $) {
  'use strict';


  r.EnduserEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#enduserEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'EnduserEntryView.initialize');
      }
    },

    render: function () {
      try {
        this.$el.html(this.template(this.model.getMock()));
        return this;
      } catch (e) {
        r.log.error(e, 'EnduserEntryView.render');
      }
    }
  });

  r.EndusersView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'addEnduserEntry');
        this.collection.bind('reset', this.render, this);
        this.template = _.template($('#noEndusersTemplate').html());
      } catch (e) {
        r.log.error(e, 'EndusersView.initialize');
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
          this.collection.each(function (enduser) {
            this.addEnduserEntry(list, enduser);
          }, this);
          this.getContent().html(list);
          list.listview();
        }
        return this;
      } catch (e) {
        r.log.error(e, 'EndusersView.render');
      }
    },

    addEnduserEntry: function (list, enduser) {
      try {
        list.append(new r.EnduserEntryView({ model: enduser }).render().el);
      } catch (e) {
        r.log.error(e, 'EndusersView.addEnduserEntry');
      }
    }
  });


  r.EnduserView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#enduserTemplate').html());
      } catch (e) {
        r.log.error(e, 'EnduserView.initialize');
      }
    },

    render: function () {
      try {
        this.appLink('back', 'endusers');

        this.getContent().html(this.template(this.model.getMock()));
        this.getContent().trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'EnduserView.render');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'EnduserView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'EnduserView.error');
      }
    },

    //  This may not be in use.  Thanks Joe for pointing out my mistakes.
    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'EnduserView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'EnduserView.apiError');
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'EnduserView.apiError');
      }
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
