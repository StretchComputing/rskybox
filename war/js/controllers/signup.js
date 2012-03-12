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
          r.log.debug('no current user', 'SignupController.isLoggedIn');
        }
      });
    },


    // Signup
    signupBeforeShow: function () {
      r.log.debug('entering', 'SignupController.signupBeforeShow');
      r.signup = new r.Signup();
      r.signupView = new r.SignupView({
        el: $('#signupForm'),
        model: r.signup
      });
    },

    signupShow: function () {
      r.log.debug('entering', 'SignupController.signupShow');
      r.signupView.render();
    },


    // Login
    loginBeforeShow: function () {
      r.log.debug('entering', 'SignupController.loginBeforeShow');
      r.login = new r.Login();
      r.loginView = new r.LoginView({
        el: $('#loginForm'),
        model: r.login
      });
      r.loginView.render();
    },


    // Confirm New User
    confirmNewUserBeforeShow: function () {
      r.log.debug('entering', 'SignupController.confirmNewUserBeforeShow');
      r.confirm = new r.Confirm({
        emailAddress: r.session.params.emailAddress,
        phoneNumber: r.session.params.phoneNumber,
        confirmationCode: r.session.params.confirmationCode,
        new: true,
      });
      r.confirmNewUserView = new r.ConfirmNewUserView({
        el: $('#confirmForm'),
        model: r.confirm,
      });
      r.confirmNewUserView.render();
    },


    // Confirm Existing User
    confirmExistingUserBeforeShow: function () {
      r.log.debug('entering', 'SignupController.confirmExistingUserBeforeShow');
      r.confirm = new r.Confirm({
        emailAddress: r.session.params.emailAddress,
        phoneNumber: r.session.params.phoneNumber,
        confirmationCode: r.session.params.confirmationCode,
        new: false,
      });
      r.confirm.apiUrl = '/users/confirm';
      r.confirm.setUrl();
      r.confirm.set('id', 'confirm');
      r.confirmExistingUserView = new r.ConfirmExistingUserView({
        el: $('#confirmForm'),
        model: r.confirm,
      });
      r.confirmExistingUserView.render();
      r.confirmExistingUserView.$el.trigger('submit');
    },


    // Confirm Member
    confirmMemberBeforeShow: function (e) {
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
      r.confirmMemberView.render();
      r.confirmMemberView.$el.trigger('submit');
    },


    // Session Setup
    setupSession: function (eventType, matchObj, ui, page, evt) {
      r.log.debug('entering', 'SignupController.setupSession');
      r.session = {};
      r.session.params = r.router.getParams(location.hash);
    },
  };


  r.router = new $.mobile.Router([
    { '.*':      { handler: 'isLoggedIn',       events: 'bc' } },
    { '.*':      { handler: 'setupSession',     events: 'bs' } },
    { '#signup': { handler: 'signupBeforeShow', events: 'bs' } },
    { '#signup': { handler: 'signupShow',       events: 's'  } },
    { '#login':  { handler: 'loginBeforeShow',  events: 'bs' } },

    // these routes only match when preregistration is present
    { '#confirm(?=.*preregistration=true)':   { handler: 'confirmNewUserBeforeShow',      events: 'bs' } },
    { '#confirm(?=.*preregistration=false)':  { handler: 'confirmExistingUserBeforeShow', events: 'bs' } },

    // this route only matches when memberConfirmation is present
    { '#confirm(?=.*memberConfirmation)':     { handler: 'confirmMemberBeforeShow',       events: 'bs' } },
  ], r.controller);


  return r;
}(RSKYBOX || {}, jQuery));
