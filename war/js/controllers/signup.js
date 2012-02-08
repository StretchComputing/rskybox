'use strict';


var rskybox = (function(r, $) {


  r.controller = {
    isLoggedIn: function() {
      var current;

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

    signupBeforeShow: function() {
      r.signup = new r.Signup();
      r.signupView = new r.SignupView({
        el: $('#signupForm'),
        model: r.signup
      });
    },

    signupShow: function() {
      r.signupView.render();
    },

    confirmBeforeShow: function() {
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

    loginBeforeShow: function() {
      r.login = new r.Login();
      r.loginView = new r.LoginView({
        el: $('#loginForm'),
        model: r.login
      });
      r.loginView.render();
    }
  };

  r.router = new $.mobile.Router([
    { '.*':        { handler: 'isLoggedIn', events: 'bc' } },
    { '#signup':   { handler: 'signupBeforeShow', events: 'bs' } },
    { '#signup':   { handler: 'signupShow', events: 's' } },
    { '#confirm':  { handler: 'confirmBeforeShow', events: 'bs' } },
    { '#login':    { handler: 'loginBeforeShow', events: 'bs' } },
  ], r.controller);


  return r;
}(rskybox || {}, jQuery));

