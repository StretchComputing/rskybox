var RSKYBOX = (function (r, $) {
  'use strict';


  r.MemberEntryView = Backbone.View.extend({
    tagName: 'li',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.template = _.template($('#memberEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'MemberEntryView.initialize');
      }
    },

    render: function () {
      try {
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
      } catch (e) {
        r.log.error(e, 'MemberEntryView.render');
      }
    }
  });

  r.MembersView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'addMemberEntry');
        this.collection.bind('reset', this.render, this);
        this.options.applications.bind('reset', this.render, this);
        this.template = _.template($('#noMembersTemplate').html());
      } catch (e) {
        r.log.error(e, 'MembersView.initialize');
      }
    },

    render: function () {
      try {
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
      } catch (e) {
        r.log.error(e, 'MembersView.render');
      }
    },

    addMemberEntry: function (list, member) {
      try {
        list.append(new r.MemberEntryView({ model: member }).render().el);
      } catch (e) {
        r.log.error(e, 'MembersView.addMemberEntry');
      }
    }
  });


  r.MemberView = r.JqmPageBaseView.extend({
    events: {
      'click .delete': 'deleteMember',
      'change .role': 'updateRole',
    },

    initialize: function () {
      try {
        _.bindAll(this, 'partialSave', 'success', 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.options.applications.on('reset', this.render, this);
        this.template = _.template($('#memberTemplate').html());
      } catch (e) {
        r.log.error(e, 'MemberView.initialize');
      }
    },

    render: function () {
      try {
        var app, mock = this.model.getMock();


        if (!this.model.get('apiStatus') || this.options.applications.isEmpty()) { return this; }

        app = this.options.applications.findById(r.session.params.appId);

        this.$el.find('.back').attr('href', '#members?appId=' + app.id);
        mock.date = r.format.longDate(mock.date);
        mock = _.extend(mock, {admin: app.get('role')});
        this.getContent().html(this.template(mock));
        this.$el.find('.role').val(this.model.get('role'));
        this.getContent().trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'MemberView.render');
      }
    },

    deleteMember: function (evt) {
      try {
        if (!confirm('Are you sure you want to delete this member?.')) {
          return;
        }
        this.model.destroy({
          success: function () {
            history.back();
          },
          statusCode: r.statusCodeHandlers(this.apiError)
        });

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'MemberView.deleteMember');
      }
    },

    updateRole: function (evt) {
      try {
        r.log.info('entering', 'MemberView.updateRole');
        this.partialSave({
          role: this.$('select[name=role]').val(),
        });
        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'MemberView.updateRole');
      }
    },

    partialSave: function (attrs, force) {
      try {
        this.model.partial.save(this.model, attrs, {
          success: this.success,
          statusCode: r.statusCodeHandlers(this.apiError),
          wait: true,
        }, force);
      } catch (e) {
        r.log.error(e, 'MemberView.partialSave');
      }
    },

    success: function (model, response) {
      try {
        r.flash.success('Changes were saved');
      } catch (e) {
        r.log.error(e, 'MemberView.success');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'MemberView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'MemberView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'MemberView.apiError');

        r.dump(this.apiCodes);
        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'MemberView.apiError');
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'MemberView.apiError');
      }
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
      try {
        _.bindAll(this, 'apiError');
        this.model.on('change', this.render, this);
        this.model.on('error', this.error, this);
        this.template = _.template($('#newMemberTemplate').html());
      } catch (e) {
        r.log.error(e, 'NewMemberView.initialize');
      }
    },

    events: {
      'submit': 'submit'
    },

    render: function () {
      try {
        this.getContent().html(this.template(this.model.getMock()));
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'NewMemberView.render');
      }
    },

    submit: function (evt) {
      try {
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

        evt.preventDefault();
        return false;
      } catch (e) {
        r.log.error(e, 'NewMemberView.submit');
      }
    },

    success: function (model, response) {
      try {
        var url = '#member?id=' + model.get('id') + '&appId=' + model.get('appId');
        $.mobile.changePage(url);
      } catch (e) {
        r.log.error(e, 'NewMemberView.success');
      }
    },

    error: function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.
        r.log.info(response, 'NewMemberView.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        r.log.error(e, 'NewMemberView.error');
      }
    },

    apiError: function (jqXHR) {
      try {
        var code = r.getApiStatus(jqXHR.responseText);
        r.log.info(code, 'NewMemberView.apiError');

        if (!this.apiCodes[code]) {
          r.log.warn('Undefined apiStatus: ' + code, 'NewMemberView.apiError');
        }
        r.flash.warning(this.apiCodes[code]);
      } catch (e) {
        r.log.error(e, 'NewMemberView.apiError');
      }
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
