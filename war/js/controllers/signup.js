var RSKYBOX = (function (r, $) {
  'use strict';


  r.controller = {
    isLoggedIn: function (eventType, matchObj) {
      var current;

      if (matchObj[0].indexOf('#confirm') === 0) { return; }

      current = new r.User({ id: 'current' });
      current.fetch({
        success: function () {
          r.changePage('applications');
        },
        error: function (model, response) {
          r.log.debug('Signup.controller.isLoggedIn: no current user');
        }
      });
    },

    signupBeforeShow: function () {
      r.signup = new r.Signup();
      r.signupView = new r.SignupView({
        el: $('#signupForm'),
        model: r.signup
      });
    },

    signupShow: function () {
      r.signupView.render();
    },

    confirmMember: function () {
      var member;

      r.log.debug('confirmMember');
      member = new r.Member({
        id: 'confirmation',
        emailAddress: r.session.params.emailAddress,
        confirmationCode: r.session.params.confirmationCode,
      });
      r.log.debug('confirmMember');

      member.setAppUrl(r.session.params.applicationID);
    },

    confirmBeforeShow: function () {
      if (r.session.params.memberConfirmation === 'true') {
        r.controller.confirmMember();
        return;
      }
      r.confirm = new r.Confirm({
        emailAddress: r.session.params.emailAddress,
        phoneNumber: r.session.params.phoneNumber,
        confirmationCode: r.session.params.confirmationCode,
      });
      r.confirmView = new r.ConfirmView({
        el: $('#confirmForm'),
        model: r.confirm
      });
      r.confirmView.render();
    },

    loginBeforeShow: function () {
      r.login = new r.Login();
      r.loginView = new r.LoginView({
        el: $('#loginForm'),
        model: r.login
      });
      r.loginView.render();
    },

    setupSession: function (eventType, matchObj, ui, page, evt) {
      r.log.debug('setupSession');
      r.session = {};
      r.session.params = r.router.getParams(location.hash);
    },
  };

  r.router = new $.mobile.Router([
    { '.*':        { handler: 'isLoggedIn', events: 'bc' } },
    { '.*':        { handler: 'setupSession', events: 'bs' } },
    { '#signup':   { handler: 'signupBeforeShow', events: 'bs' } },
    { '#signup':   { handler: 'signupShow', events: 's' } },
    { '#confirm':  { handler: 'confirmBeforeShow', events: 'bs' } },
    { '#login':    { handler: 'loginBeforeShow', events: 'bs' } },
  ], r.controller);


  return r;
}(RSKYBOX || {}, jQuery));
