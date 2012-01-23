'use strict';


var rskybox = (function(r, $) {


  r.CarrierView = Backbone.View.extend({
    tagName: 'option',

    initialize: function() {
      _.bindAll(this, 'render');
    },

    render: function() {
      $(this.el).attr('value', this.model.get('id')).html(this.model.get('name'));
      return this;
    }
  });

  r.CarriersView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'addCarrier', 'setElId');
      this.collection.bind('reset', this.render, this);
    },

    // This workaround is required because the element is part of another view and not
    // available when this view is created. So we store the ID and set this.el later in
    // the render function.
    setElId: function(id) {
      this.elId = id;
    },

    render: function() {
      this.el = $(this.elId);
      $(this.el).empty();
      this.addCarrier(new r.Carrier({ id: '', name: 'Select Mobile Carrier'}));
      this.collection.each(this.addCarrier);
      $(this.el).selectmenu('refresh');
      return this;
    },

    addCarrier: function(carrier) {
      $(this.el).append(new r.CarrierView({ model: carrier }).render().el);
    }
  });


  return r;
})(rskybox || {}, jQuery);
