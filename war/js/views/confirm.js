'use strict';


var rskybox = (function(r, $) {


  r.ConfirmView = Backbone.View.extend({
    initialize: function() {
      _.bindAll(this, 'success', 'validationError', 'apiError');
      this.model.bind('change', this.render, this);
      this.template = _.template($('#signupTemplate').html());
    },

    events: {
      'submit': 'submit'
    },

    submit: function(e) {
      r.log.debug('submit triggered');
      var form = new r.BaseModel();
      form.set({
        emailAddress: this.$("input[name='emailAddress']").val(),
        phoneNumber: this.$("input[name='phoneNumber']").val(),
        confirmationCode: this.$("select[name='confirmationCode']").val(),
        password: this.$("input[name='password']").val()
      }, {silent: true});
      form.unsetEmptyAttributes();

      this.model.save(form, {
        success: this.success,
        statusCode: {
          422: this.apiError
        }
      });
      e.preventDefault();
      return false;
    },

    success: function(model, response) {
      r.log.debug('success called');
      //$.mobile.changePage('#confirm' + r.buildQueryString(model.toJSON()));
    },

    validationError: function(model, response) {
      r.log.debug('validationError called');
      r.flashError(response, this.el);
    },

    apiError: function(jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.debug('An unknown API error occurred: ' + code);
      }
      this.model.clear({silent: true});

      r.flashError(this.apiCodes[code], this.el);
    },

    render: function() {
      r.log.debug('signup view render');

      var content = this.template(this.model.toJSON());
      $(this.el).empty();
      $(this.el).html(content);
      $(this.el).trigger('create');
      this.carriersView = new r.CarriersView({
        el: $('#mobileCarrierId'),
        collection: new r.Carriers()
      });
      this.carriersView.collection.fetch();
      return this;
    },

    apiCodes: {
      204: 'Your email address has already been confirmed.',
      205: 'Your phone number has already been confirmed.',
      500: 'Phone number and mobile carrier ID must be specified together.'
    }
  });


  return r;
})(rskybox || {}, jQuery);
