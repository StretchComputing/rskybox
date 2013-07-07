var RSKYBOX = (function (r, $) {
  'use strict';


  r.EndpointFilterEntryView = Backbone.View.extend({
    tagName: 'li',

    events: {
      'click': 'toggleActive'
    },

    initialize: function () {
      try {
        _.bindAll(this, 'render');
        this.model.bind('change', this.updateApplication, this);
        this.template = _.template($('#endpointFilterEntryTemplate').html());
      } catch (e) {
        r.log.error(e, 'EndpointFilterEntryView.initialize');
      }
    },

    render: function () {
      try {
        this.$el.html(this.template(this.model.getMock()));
        return this;
      } catch (e) {
        r.log.error(e, 'EndpointFilterEntryView.render');
      }
    },

    toggleActive: function () {
      this.model.save({ active: !this.model.get('active') });
    },

    updateApplication: function () {
      r.session.reset();
    }
  });

  r.EndpointFiltersView = r.JqmPageBaseView.extend({
    initialize: function () {
      try {
        this.collection.bind('reset change', this.render, this);
        this.template = _.template($('#noEndpointFiltersTemplate').html());
      } catch (e) {
        r.log.error(e, 'EndpointFiltersView.initialize');
      }
    },

    render: function () {
      try {
        var list;

        this.appLink('back', 'application');

        this.getContent().empty();
        if (this.collection.isEmpty()) {
          this.getContent().html(this.template());
        } else {
          list = $('<ul>');
          this.collection.each(function (endpointFilter) {
            this.addEndpointFilterEntry(list, endpointFilter);
          }, this);
          this.getContent().html(list);
          list.listview();
        }
        return this;
      } catch (e) {
        r.log.error(e, 'EndpointFiltersView.render');
      }
    },

    addEndpointFilterEntry: function (list, endpointFilter) {
      try {
        list.append(new r.EndpointFilterEntryView({ model: endpointFilter }).render().el);
      } catch (e) {
        r.log.error(e, 'EndpointFiltersView.addEndpointFilterEntry');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
