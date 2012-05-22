var RSKYBOX = (function (r, $) {
  'use strict';


  r.JqmPageBaseView = Backbone.View.extend({
    constructor: function () {
      try {
        r.log.info('entering', 'JqmPageBaseView.constructor');

        Backbone.View.prototype.constructor.apply(this, arguments);
        if (this.options && this.options.applications) {
          this.options.applications.bind('reset', this.updateApplicationName, this);
        }
        if (this.collection) {
          this.on('more', this.more, this);
          _.bindAll(this, 'moreSuccess');
        }
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.constructor');
      }
    },

    getHeader: function () {
      try {
        return this.$el.find(':jqmData(role=header)');
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.getHeader');
      }
    },
    getContent: function () {
      try {
        return this.$el.find(':jqmData(role=content)');
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.getContent');
      }
    },
    getFooter: function () {
      try {
        return this.$el.find(':jqmData(role=footer)');
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView');
      }
    },

    updateApplicationName: function () {
      try {
        var app;
        r.log.info('entering', 'JqmPageBaseView.updateApplicationName');

        if (this.options.applications.isEmpty()) { return; }

        app = this.options.applications.findById(r.session.params.appId);
        this.$el.find('.applicationName').html(app.get('name'));
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.updateApplicationName');
      }
    },

    appLink: function (clazz, name, status) {
      try {
        var params = '';

        if (status) {
          params = '&status=' + status;
        }
        return this.$el.find('.' + clazz).attr('href', '#' + name + '?appId=' + r.session.params.appId + params);
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.appLink');
      }
    },

    renderStatusButton: function (pageLink) {
      try {
        var
          el,
          hrefTemplate = _.template('<%= pageLink %>?appId=<%= appId %>&status=<%= status %>'),
          model = {},
          params = r.session.params;

        model.appId = params.appId;
        model.pageLink = pageLink;
        if (params.status === 'closed') {
          model.status = 'open';
          model.display = 'Open';
        } else {
          model.status = 'closed';
          model.display = 'Closed';
        }

        el = this.getHeader().find('.status');
        el.attr('href', hrefTemplate(model));
        el.find('.ui-btn-text').text(model.display);
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.renderStatusButton');
      }
    },

    changeStatus: function () {
      try {
        switch (this.model.get('status')) {
        case 'open':
          this.model.set('status', 'closed');
          break;
        case 'closed':
          this.model.set('status', 'open');
          break;
        default:
          r.log.warn('Invalid status for: ' + this.model.get('id'), 'View.changeStatus');
          break;
        }
        this.model.save(null, {
          statusCode: r.statusCodeHandlers(this.apiError),
        });
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.changeStatus');
      }
    },

    resetList: function (listLinkId) {
      var that = this;

      this.setElement($.mobile.activePage);
      this.collection.setAppUrl(r.session.params.appId);
      this.collection.reset();
      this.options.first = true;
      this.options.more = true;
      delete this.options.cursor;
      r.session.getCollection(r.session.keys.applications, this.options.applications);
      this.trigger('more');
      $(window).scroll(function () {
        that.trigger('more');
      });
      if (listLinkId) {
        this.renderStatusButton(listLinkId);
      }
    },

    more: function () {
      if (this.options.first ||
          (this.options.more &&
          ($(window).scrollTop() === ($(document).height() - window.innerHeight)))) {
        this.collection.fetch({
          add: true,
          data: {
            //tag: this.options.tag,
            //status: r.session.params.status || 'open',
            pageSize: this.options.pageSize || 10,
            cursor: this.options.cursor || undefined,
          },
          success: this.moreSuccess,
        });
        this.options.first = false;
      }
    },

    moreSuccess: function (collection, response) {
      this.options.cursor = response.cursor;
      this.options.more = false;
      if (response.incidents) {
        this.options.more = response.incidents.length > 0;
      } else if (response.endUsers) {
        this.options.more = response.endUsers.length > 0;
      }
      this.render();
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
