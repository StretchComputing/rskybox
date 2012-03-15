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
      if (r.signup) { delete r.signup; }
      if (r.signupView) {
        r.signupView.undelegateEvents();
        delete r.signupView;
      }
      r.signup = new r.Signup();
      r.signupView = new r.SignupView({
        el: $('#signupForm'),
        model: r.signup,
      });
    },

    signupShow: function () {
      r.log.debug('entering', 'SignupController.signupShow');
      r.signupView.render();
    },


    // Login
    loginBeforeShow: function () {
      r.log.debug('entering', 'SignupController.loginBeforeShow');
      if (r.login) { delete r.login; }
      if (r.loginView) {
        r.loginView.undelegateEvents();
        delete r.loginView;
      }
      r.login = new r.Login();
      r.loginView = new r.LoginView({
        el: $('#loginForm'),
        model: r.login,
      });
    },

    loginShow: function () {
      r.log.debug('entering', 'SignupController.loginShow');
      r.loginView.render();
    },


    // Confirm New User
    confirmNewUserBeforeShow: function () {
      r.log.debug('entering', 'SignupController.confirmNewUserBeforeShow');
      if (r.confirm) { delete r.confirm; }
      if (r.confirmNewUserView) {
        r.confirmNewUserView.undelegateEvents();
        delete r.confirmNewUserView;
      }
      r.confirm = new r.Confirm({
        emailAddress: r.session.params.emailAddress,
        emailConfirmationCode: r.session.params.emailConfirmationCode,
        phoneNumber: r.session.params.phoneNumber,
        phoneConfirmationCode: r.session.params.phoneConfirmationCode,
        new: true,
      });
      r.confirmNewUserView = new r.ConfirmNewUserView({
        el: $('#confirmForm'),
        model: r.confirm,
      });
    },

    confirmNewUserShow: function () {
      r.log.debug('entering', 'SignupController.confirmNewUserShow');
      r.confirmNewUserView.render();
    },

    // Confirm Existing User
    confirmExistingUserBeforeShow: function () {
      r.log.debug('entering', 'SignupController.confirmExistingUserBeforeShow');
      if (r.confirm) { delete r.confirm; }
      if (r.confirmExistingUserView) {
        r.confirmExistingUserView.undelegateEvents();
        delete r.confirmExistingUserView;
      }
      r.confirm = new r.Confirm({
        emailAddress: r.session.params.emailAddress,
        emailConfirmationCode: r.session.params.emailConfirmationCode,
        phoneNumber: r.session.params.phoneNumber,
        phoneConfirmationCode: r.session.params.phoneConfirmationCode,
        new: false,
      });
      r.confirm.apiUrl = '/users/confirm';
      r.confirm.setUrl();
      r.confirm.set('id', 'confirm');
      r.confirmExistingUserView = new r.ConfirmExistingUserView({
        el: $('#confirmForm'),
        model: r.confirm,
      });
    },

    confirmExistingUserShow: function () {
      r.log.debug('entering', 'SignupController.confirmExistingUserShow');
      r.confirmExistingUserView.render();
      r.confirmExistingUserView.$el.trigger('submit');
    },


    // Confirm Member
    confirmMemberBeforeShow: function (e) {
      r.log.debug('entering', 'SingupController.confirmMemberBeforeShow');
      if (r.member) { delete r.member; }
      if (r.confirmMemberView) {
        r.confirmMemberView.undelegateEvents();
        delete r.confirmMemberView;
      }
      r.member = new r.Member({
        emailAddress: r.session.params.emailAddress,
        confirmationCode: r.session.params.confirmationCode,
        memberConfirmation: r.session.params.memberConfirmation,
      });
      r.member.apiUrl = '/appMembers/confirmation';
      r.member.setUrl();
      r.member.setAppUrl(r.session.params.applicationId);
      r.member.set('id', 'confirmation');
      r.confirmMemberView = new r.ConfirmMemberView({
        el: $('#confirmForm'),
        model: r.member,
      });
    },

    confirmMemberShow: function (e) {
      r.log.debug('entering', 'SingupController.confirmMemberShow');
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
    { '.*':      { handler: 'isLoggedIn',       events: 'bc'  } },
    { '.*':      { handler: 'setupSession',     events: 'bs'  } },
    { '#signup': { handler: 'signupBeforeShow', events: 'bs'  } },
    { '#signup': { handler: 'signupShow',       events: 's'   } },
    { '#login':  { handler: 'loginBeforeShow',  events: 'bs'  } },
    { '#login':  { handler: 'loginShow',        events: 's'   } },

    // these routes only match when preregistration is present
    { '#confirm(?=.*preregistration=true)':   { handler: 'confirmNewUserBeforeShow',      events: 'bs'  } },
    { '#confirm(?=.*preregistration=true)':   { handler: 'confirmNewUserShow',            events: 's'   } },
    { '#confirm(?=.*preregistration=false)':  { handler: 'confirmExistingUserBeforeShow', events: 'bs'  } },
    { '#confirm(?=.*preregistration=false)':  { handler: 'confirmExistingUserShow',       events: 's'   } },

    // these routes only matches when memberConfirmation is present
    { '#confirm(?=.*memberConfirmation)':     { handler: 'confirmMemberBeforeShow',       events: 'bs'  } },
    { '#confirm(?=.*memberConfirmation)':     { handler: 'confirmMemberShow',             events: 's'   } },
  ], r.controller);


  return r;
}(RSKYBOX || {}, jQuery));
