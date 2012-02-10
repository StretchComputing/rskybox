var RSKYBOX = (function (r, $) {
  'use strict';


  r.FeedbackView = Backbone.View.extend({
    initialize: function () {
      //_.bindAll(this, 'render');
      this.template = _.template($('#feedbackTemplate').html());
    },

    render: function () {
      this.$el.html(this.template(this.model.getMock()));
      this.$el.trigger('create');
      return this;
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
