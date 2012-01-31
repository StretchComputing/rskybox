'use strict';


var rskybox = (function(r, $) {


  r.controller = {
    signup: function() {
      r.signup = new r.Signup();
      r.signupView = new r.SignupView({
        el: $('#signupForm'),
        model: r.signup
      });
      r.signupView.render();
    },

    confirm: function() {
      r.confirm = new r.Confirm({
        emailAddress: r.getParameterByName(location.hash, 'emailAddress'),
        phoneNumber: r.getParameterByName(location.hash, 'phoneNumber')
      });
      r.confirmView = new r.ConfirmView({
        el: $('#confirmForm'),
        model: r.confirm
      });
      r.confirmView.render();
    },

    login: function() {
      r.login = new r.Login();
      r.loginView = new r.LoginView({
        el: $('#loginForm'),
        model: r.login
      });
      r.loginView.render();
    }
  };

  r.router = new $.mobile.Router({
    '#signup': 'signup',
    '#confirm': 'confirm',
    '#login': 'login'
  }, r.controller);


  return r;
})(rskybox || {}, jQuery);
