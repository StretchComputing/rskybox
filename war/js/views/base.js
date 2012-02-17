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
      var el,
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

  });


  return r;
}(RSKYBOX || {}, jQuery));
