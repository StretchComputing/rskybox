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
      this.options.applications.bind('reset', this.render, this);
      this.template = _.template($('#noMembersTemplate').html());
    },

    render: function () {
      var app, list;

      if (this.collection.isEmpty() || this.options.applications.isEmpty()) { return this; }

      app = this.options.applications.findById(r.session.params.appId);

      this.$el.find('.back').attr('href', '#application?appId=' + app.id);
      if (app.role === 'member') {
        this.$el.find('.new').attr('href', '#').hide();
      } else {
        this.$el.find('.new').attr('href', '#newMember?appId=' + app.id).show();
      }
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
      'change .role': 'updateRole',
    },

    initialize: function () {
      _.bindAll(this, 'partialSave', 'success', 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.options.applications.on('reset', this.render, this);
      this.template = _.template($('#memberTemplate').html());
    },

    render: function () {
      var app, model;

      if (!this.model.get('apiStatus') || this.options.applications.isEmpty()) { return this; }

      app = this.options.applications.findById(r.session.params.appId);

      this.$el.find('.back').attr('href', '#members?appId=' + app.id);
      model = _.extend(this.model.getMock(), {admin: app.get('role')});
      this.getContent().html(this.template(model));
      this.$el.find('.role').val(this.model.get('role'));
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

    updateRole: function (e) {
      r.log.info('entering', 'MemberView.updateRole');
      this.partialSave({
        role: this.$('select[name=role]').val(),
      });
      e.preventDefault();
      return false;
    },

    partialSave: function (attrs, force) {
      this.model.partial.save(this.model, attrs, {
        success: this.success,
        statusCode: r.statusCodeHandlers(this.apiError),
        wait: true,
      }, force);
    },

    success: function (model, response) {
      r.flash.success('Changes were saved');
    },

    error: function (model, response) {
      if (response.responseText) { return; }  // This is an apiError.
      r.log.info(response, 'MemberView.error');
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.info(code, 'MemberView.apiError');

      r.dump(this.apiCodes);
      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'MemberView.apiError');
      }
      r.flash.warning(this.apiCodes[code]);
    },

    apiCodes: {
      201: 'Invalid status.',
      212: 'You are not authorized for this action.',
      213: 'You are not authorized for this action.',
      307: 'Member ID required.',
      408: 'Invalid role.',
      606: 'Member was not found',
    }
  });


  r.NewMemberView = r.JqmPageBaseView.extend({
    initialize: function () {
      _.bindAll(this, 'apiError');
      this.model.on('change', this.render, this);
      this.model.on('error', this.error, this);
      this.template = _.template($('#newMemberTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    render: function () {
      this.getContent().html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },

    submit: function (e) {
      var valid;
      r.log.info('entering', 'NewMemberView.submit');

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
      if (response.responseText) { return; }  // This is an apiError.
      r.log.info(response, 'NewMemberView.error');
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.info(code, 'NewMemberView.apiError');

      if (!this.apiCodes[code]) {
        r.log.error('Undefined apiStatus: ' + code, 'NewMemberView.apiError');
      }
      r.flash.warning(this.apiCodes[code]);
    },

    apiCodes: {
      203: 'You are not authorized for this action.',
      210: 'You are not authorized for this action.',
      211: 'You are not authorized for this action.',
      305: 'Application ID required.',
      313: 'Email address is required.',
      314: 'Role is required.',
      407: 'User is already a member.',
      605: 'Application was not found.',
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
