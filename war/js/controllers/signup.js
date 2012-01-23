'use strict';


var rskybox = (function(r, $) {

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
