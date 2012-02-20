var RSKYBOX = (function (r, $) {
  'use strict';


  r.MemberEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      _.bindAll(this, 'render');
      this.template = _.template($('#memberEntryTemplate').html());
    },

    render: function () {
      var
        display = '',
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

  r.MembersView = r.JqmPageBaseView.extend({
    events: {
      'click .newMember': 'newMember',
    },

    initialize: function () {
      _.bindAll(this, 'addMemberEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noMembersTemplate').html());
    },

    newMember: function () {
      $.mobile.changePage('#newMember?id=' + r.session.params.id);
    },

    render: function () {
      var list;

      this.getContent().empty();
      if (this.collection.length <= 0) {
        this.getContent().html(this.template());
      } else {
        list = $('<ul>');
        this.collection.each(function (member) {
          this.addMemberEntry(list, member);
        }, this);
        this.getContent().html(list);
        list.listview();
      }
      return this;
    },

    addMemberEntry: function (list, member) {
      list.append(new r.MemberEntryView({ model: member }).render().el);
    }
  });


  r.MemberView = r.JqmPageBaseView.extend({
    initialize: function () {
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#memberTemplate').html());
    },

    render: function () {
      this.getContent().html(this.template(this.model.getMock()));
      this.getContent().trigger('create');
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


  r.NewMemberView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#newMemberTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function (e) {
      var valid;
      r.log.debug('NewMemberView.submit');

      valid = this.model.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        role: this.$("select[name='role']").val(),
      });

      if (valid) {
        this.model.prepareNewModel();

        this.model.save(null, {
          success: this.success,
          statusCode: r.statusCodeHandlers(this.apiError)
        });
      }

      e.preventDefault();
      return false;
    },

    success: function (model, response) {
      var url = '#member?id=' + model.get('id') + '&appId=' + model.get('appId');
      $.mobile.changePage(url);
    },

    error: function (model, response) {
      r.log.debug('NewMemberView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flashError(response, this.$el);
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.debug('NewMemberView.apiError');

      if (!this.apiCodes[code]) {
        r.log.error('NewMemberView: An unknown API error occurred: ' + code);
      }

      r.flashError(this.apiCodes[code], this.$el);
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    apiCodes: {
      306: 'An member name is required.'
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
