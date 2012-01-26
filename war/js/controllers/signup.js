'use strict';


var rskybox = (function(r, $) {


  r.controller = {
    signup: function() {
      r.log.debug('got the router for signup');
      r.signup = new r.Signup();
      r.signupView = new r.SignupView({
        el: $('#signupForm'),
        model: r.signup
      });
      r.signupView.render();
    },

    confirm: function() {
      r.log.debug('got the router for confirm');
      r.confirm = new r.Confirm({
        emailAddress: r.getParameterByName(location.hash, 'emailAddress'),
        phoneNumber: r.getParameterByName(location.hash, 'phoneNumber')
      });
      r.confirm.prepareNewModel();
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
