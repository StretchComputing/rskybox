var RSKYBOX = (function (r, $) {
  'use strict';


  r.controller = {

    // Check if the user is logged in
    isLoggedIn: function (eventType, matchObj) {
      var current;

      if (matchObj[0].indexOf('#confirm') === 0) { return; }

      current = new r.User({ id: 'current' });
      current.fetch({
        success: function () {
          r.changePage('applications');
        },
        error: function (model, response) {
          r.log.debug('no current user', 'signup.controller.isLoggedIn');
        }
      });
    },


    // Signup
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


    // Confirm Member
    confirmMember: function (e) {
      r.log.debug('entering', 'SingupController.confirmMember');
      r.member = new r.Member({
        id: 'confirmation',
        emailAddress: r.session.params.emailAddress,
        confirmationCode: r.session.params.confirmationCode,
        memberConfirmation: r.session.params.memberConfirmation,
      });
      r.member.setAppUrl(r.session.params.applicationId);
      r.confirmMemberView = new r.ConfirmMemberView({
        el: $('#confirmForm'),
        model: r.member,
      });
      //r.confirmMemberView.render();
      r.confirmMemberView.$el.trigger('submit');
    },


    // Confirm User
    confirmUser: function () {
      r.confirm = new r.Confirm({
        emailAddress: r.session.params.emailAddress,
        phoneNumber: r.session.params.phoneNumber,
        confirmationCode: r.session.params.confirmationCode,
      });
      r.confirmUserView = new r.ConfirmUserView({
        el: $('#confirmForm'),
        model: r.confirm,
      });
      r.confirmUserView.render();
    },


    // Confirm starting point
    // TODO - try to replace this with routes
    confirmBeforeShow: function (eventType, matchObj, ui, page, evt) {
      if (r.session.params.memberConfirmation === 'true') {
        evt.preventDefault();
        this.confirmMember(evt);
      } else {
        this.confirmUser();
      }
    },


    // Login
    loginBeforeShow: function () {
      r.login = new r.Login();
      r.loginView = new r.LoginView({
        el: $('#loginForm'),
        model: r.login
      });
      r.loginView.render();
    },


    // Session Setup
    setupSession: function (eventType, matchObj, ui, page, evt) {
      r.log.debug('entering', 'signup.controller.setupSession');
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
