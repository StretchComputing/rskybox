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
    initialize: function () {
      _.bindAll(this, 'addMemberEntry');
      this.collection.bind('reset', this.render, this);
      this.template = _.template($('#noMembersTemplate').html());
    },

    render: function () {
      var appId, list;

      appId = r.session.params.id;
      this.$el.find('.back').attr('href', '#application?id=' + appId);
      this.$el.find('.new').attr('href', '#newMember?id=' + appId);
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
    events: {
      'click .delete': 'deleteMember',
    },

    initialize: function () {
      r.log.debug('entering', 'MemberView.initialize');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.collection.on('reset', this.render, this);
      this.template = _.template($('#memberTemplate').html());
    },

    render: function () {
      var app, appId = r.session.params.appId, model;

      if (this.model.isNew() || this.collection.isEmpty()) { return this; }

      app = this.collection.find(function (app) {
        return app.id === appId;
      });

      this.$el.find('.back').attr('href', '#members?id=' + appId);
      model = _.extend(this.model.getMock(), {admin: app.get('role')});
      this.getContent().html(this.template(model));
      this.getContent().trigger('create');
      return this;
    },

    deleteMember: function (e) {
      if (!confirm('Are you sure you want to delete this member?.')) {
        return;
      }
      this.model.destroy({
        success: function () {
          history.back();
        },
        statusCode: r.statusCodeHandlers(this.apiError)
      });

      e.preventDefault();
      return false;
    },


    error: function (model, response) {
      r.log.debug('MemberView.error');
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      r.log.debug('MemberView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'MemberView.apiError');
      }
      r.flash.warning(this.apiCodes[code]);
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
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.debug('NewMemberView.apiError');

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'NewMemberView.apiError');
      }
      r.flash.warning(this.apiCodes[code], this.$el);
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    apiCodes: {
      306: 'A member name is required.'
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
