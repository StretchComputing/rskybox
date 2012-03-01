var RSKYBOX = (function (r, $) {
  'use strict';


  r.CarrierView = Backbone.View.extend({
    tagName: 'option',

    initialize: function () {
      _.bindAll(this, 'render');
    },

    render: function () {
      this.$el.attr('value', this.model.get('id')).html(this.model.get('name'));
      return this;
    }
  });

  r.CarriersView = Backbone.View.extend({
    initialize: function () {
      _.bindAll(this, 'addCarrier');
      this.collection.bind('reset', this.render, this);
    },

    render: function () {
      this.$el.empty();
      this.addCarrier(new r.Carrier({ id: '', name: 'Select Mobile Carrier'}));
      this.collection.each(this.addCarrier);
      this.$el.selectmenu('refresh');
      return this;
    },

    addCarrier: function (carrier) {
      this.$el.append(new r.CarrierView({ model: carrier }).render().el);
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
