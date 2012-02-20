var RSKYBOX = (function (r, $) {
  'use strict';


  r.JqmPageBaseView = Backbone.View.extend({
    getHeader: function () {
      return this.$el.find(':jqmData(role=header)');
    },
    getContent: function () {
      return this.$el.find(':jqmData(role=content)');
    },
    getFooter: function () {
      return this.$el.find(':jqmData(role=footer)');
    },

    renderArchiveButton: function (pageLink) {
      var
        el,
        hrefTemplate = _.template('<%= pageLink %>?id=<%= id %>&status=<%= status %>'),
        model = {},
        params = r.session.params;

      model.id = params.id;
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
        r.log.error('Invalid status for feedback: ' + this.model.get('id'));
        break;
      }
      this.model.save(null, {
        statusCode: {
          422: this.apiError
        }
      });
    },

    renderStatusButton: function () {
      var el, text;

      text = this.model.get('status') === 'new' ? 'Archive' : 'Un-archive';
      el = this.getHeader().find('.changeStatus');
      el.find('.ui-btn-text').text(text);
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
