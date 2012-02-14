var RSKYBOX = (function (r, $) {
  'use strict';


  r.MemberEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#memberEntryTemplate').html());
    },

    render: function () {
      var display = '',
          mock = this.model.getMock();

      if (mock.emailAddress) {
        display += mock.emailAddress;
      } else if (mock.phoneNumber) {
        display += mock.phoneNumber;
      }
      switch (mock.role) {
      case 'owner':
        display += ' $$';
        break;
      case 'manager':
        display += ' *';
        break;
      default:
        break;
      }

      mock.display = display;
      this.$el.html(this.template(mock));
      return this;
    }
  });

  r.MembersView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'addMemberEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noMembersTemplate').html());
    },

    render: function () {
      var list;

      $(this.el).empty();
      if (this.collection.length <= 0) {
        this.$el.html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (member) {
          this.addMemberEntry(list, member);
        }, this);
        this.$el.html(list);
        list.listview();
      }
      return this;
    },

    addMemberEntry: function (list, member) {
      list.append(new r.MemberEntryView({ model: member }).render().el);
    }
  });


  r.MemberView = Backbone.View.extend({
    initialize: function () {
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#memberTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    error: function (model, response) {
      r.log.debug('MemberView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response, this.$el);
    },

    apiError: function (jqXHR) {
      r.log.debug('MemberView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('MemberView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    apiCodes: {
      203: 'You are not authorized for this application.',
      305: 'Application ID required.',
      307: 'Member ID required.',
      605: 'Application was not found',
      606: 'Member was not found',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
