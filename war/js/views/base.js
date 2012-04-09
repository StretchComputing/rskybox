var RSKYBOX = (function (r, $) {
  'use strict';


  r.JqmPageBaseView = Backbone.View.extend({
    constructor: function () {
      r.log.info('entering', 'JqmPageBaseView.constructor');
      Backbone.View.prototype.constructor.apply(this, arguments);
      if (this.options && this.options.applications) {
        this.options.applications.bind('reset', this.updateApplicationName, this);
      }
    },

    getHeader: function () {
      return this.$el.find(':jqmData(role=header)');
    },
    getContent: function () {
      return this.$el.find(':jqmData(role=content)');
    },
    getFooter: function () {
      return this.$el.find(':jqmData(role=footer)');
    },

    updateApplicationName: function () {
      var app;
      r.log.info('entering', 'JqmPageBaseView.updateApplicationName');

      if (this.options.applications.isEmpty()) { return; }

      app = this.options.applications.findById(r.session.params.appId);
      this.$el.find('.applicationName').html(app.get('name'));
    },

    renderArchiveButton: function (pageLink) {
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
    },

    changeStatus: function () {
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
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
