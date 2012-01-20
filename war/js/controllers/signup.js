'use strict';


var rskybox = (function(r, $) {

/*
  r.Carrier = Backbone.Model.extend({});
  r.Carriers = Backbone.Collection.extend({
    model: r.Carrier,
    url: r.getRestPrefix() + '/mobileCarriers',
    parse: function(response) {
      return response.mobileCarriers;
    }
  });

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
      _.bindAll(this, 'render', 'addCarrier');
      this.collection.bind('reset', this.render);
    },

    render: function() {
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


  //// isValidSignup - determine whether the signup properties passed in are valid for a new user signup
  ////
  //// signup - object containing the signup properties
  //// signup.emailAddress
  //// signup.phoneNumber
  //// signup.mobileCarrierId
  //r.isValidSignup = function (signup) {
    //if (my.isValidEmailAddress(signup.emailAddress)) { return true; }
    //if (my.isValidPhoneNumber(signup.phoneNumber) && signup.mobileCarrierId) { return true; }

    //return false;
  //}

  //// isValidPassword - check password user wants to submit against business rules
  //r.isValidPassword = function (password) {
    //var PASSWORD_MIN_LEN = 6;

    //if (!password || password.length < PASSWORD_MIN_LEN) {
      //return false;
    //}
    //return true;
  //}

  //// isValidConfirmationCode - check confirmation code user wants to submit against business rules
  //r.isValidConfirmationCode = function (code) {
    //var CONFIRMATION_CODE_LEN = 3;

    //if (!code || code.length != CONFIRMATION_CODE_LEN) {
      //return false;
    //}
    //return true;
  //}


*/
  r.controller = {
    signup: function() {
      r.log.debug('got the router for signup');
      //r.carriers = new r.Carriers();
      //new r.CarriersView({
        //el: $('#mobileCarrierId'),
        //collection: r.carriers
      //});
      //r.carriers.fetch();
    },
    confirm: function() {
      r.log.debug('got the router for confirm');
    },
    login: function() {
      r.log.debug('got the router for login');
    }
  };

  r.router = new $.mobile.Router({
    '#signup': 'signup',
    '#confirm': 'confirm',
    '#login': 'login'
  }, r.controller);

  return r;
})(rskybox || {}, jQuery);
