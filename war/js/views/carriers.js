var RSKYBOX = (function (r, $) {
  'use strict';


  r.CarrierView = Backbone.View.extend({
    tagName: 'option',

    initialize: function () {
      try {
        _.bindAll(this, 'render');
      } catch (e) {
        r.log.error(e, 'CarrierView.initialize');
      }
    },

    render: function () {
      try {
        this.$el.attr('value', this.model.get('id')).html(this.model.get('name'));
        return this;
      } catch (e) {
        r.log.error(e, 'CarrierView.render');
      }
    }
  });

  r.CarriersView = Backbone.View.extend({
    initialize: function () {
      try {
        _.bindAll(this, 'addCarrier');
        this.collection.bind('reset', this.render, this);
        this.value = '';
      } catch (e) {
        r.log.error(e, 'CarriersView.initialize');
      }
    },

    render: function () {
      try {
        this.$el.empty();
        this.addCarrier(new r.Carrier({ id: '', name: 'Select Mobile Carrier'}));
        this.collection.each(this.addCarrier);
        this.$el.val(this.value);
        this.$el.selectmenu('refresh');
        return this;
      } catch (e) {
        r.log.error(e, 'CarriersView.render');
      }
    },

    addCarrier: function (carrier) {
      try {
        this.$el.append(new r.CarrierView({ model: carrier }).render().el);
      } catch (e) {
        r.log.error(e, 'CarriersView.addCarrier');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
