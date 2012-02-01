'use strict';


var rskybox = (function(r, $) {


  r.controller = {
    isLoggedIn: function() {
      var current;

      r.log.debug('isLoggedIn');
      current = new r.User({ id: 'current' });
      current.fetch({
        success: function() {
          r.changePage('applications');
        },
        error: function(model, response) {
          r.log.debug('Signup.controller.isLoggedIn: no current user');
        }
      });
    },

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
        phoneNumber: r.getParameterByName(location.hash, 'phoneNumber'),
        confirmationCode: r.getParameterByName(location.hash, 'confirmationCode'),
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

  r.router = new $.mobile.Router([
    { '#signup':   { handler: 'isLoggedIn', events: 'bc' } },
    { '#signup':   'signup' },
    { '#confirm':  'confirm' },
    { '#login':    'login' }
  ], r.controller);


  return r;
})(rskybox || {}, jQuery);
