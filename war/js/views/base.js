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

    appLink: function (clazz, name) {
      try {
        return this.$el.find('.' + clazz).attr('href', '#' + name + '?appId=' + r.session.params.appId);
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.appLink');
      }
    },

    renderArchiveButton: function (pageLink) {
      try {
        var
          el,
          hrefTemplate = _.template('<%= pageLink %>?appId=<%= appId %>&status=<%= status %>'),
          model = {},
          params = r.session.params;

        model.appId = params.appId;
        model.pageLink = pageLink;
        if (params.status === 'archived') {
          model.status = 'new';
          model.display = 'Active';
        } else {
          model.status = 'archived';
          model.display = 'Archives';
        }

        el = this.getHeader().find('.archives');
        el.attr('href', hrefTemplate(model));
        el.find('.ui-btn-text').text(model.display);
      } catch (e) {
        r.log.error(e, 'JqmPageBaseView.renderArchiveButton');
      }
    },

    changeStatus: function () {
      try {
        switch (this.model.get('status')) {
        case 'new':
          this.model.set('status', 'archived');
          break;
        case 'archived':
          this.model.set('status', 'new');
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
  });


  return r;
}(RSKYBOX || {}, jQuery));
