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
  });


  return r;
}(RSKYBOX || {}, jQuery));
