var RSKYBOX = (function (r, $) {
  'use strict';


  r.LogsView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.collection.bind('reset', this.render, this);
      } catch (e) {
        r.log.error(e, 'LogsView.initialize');
      }
    },

    render: function () {
      try {
        this.$el.empty();
        this.collection.each(function (log) {
          this.$el.append(new r.LogView({
            model: log,
            attributes: {
              'data-role': 'collapsible',
              'data-theme': 'c',
              'data-content-theme': 'c',
            },
          }).render().el);
        }, this);
        this.$el.trigger('create');
        return this;
      } catch (e) {
        r.log.error(e, 'LogsView.render');
      }
    },
  });


  r.LogView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.template = _.template($('#logTemplate').html());
      } catch (e) {
        r.log.error(e, 'LogView.initialize');
      }
    },

    render: function () {
      try {
        r.log.info('entering', 'LogView.render');
        var mock = this.model.getMock();

        if (Array.isArray(mock.stackBackTrace)) {
          mock.stackBackTrace = mock.stackBackTrace.join('<br>');
        }
        this.$el.html(this.template(mock));
        return this;
      } catch (e) {
        r.log.error(e, 'LogView.render');
      }
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
