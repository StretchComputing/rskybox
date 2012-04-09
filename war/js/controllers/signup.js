var RSKYBOX = (function (r, $) {
  'use strict';


  r.controller = {

    // Check if the user is logged in
    isLoggedIn: function (eventType, matchObj) {
      try {
        var current;
        r.log.info('entering', 'SignupController.isLoggedIn');

        if (matchObj[0].indexOf('#confirm') === 0) { return; }
        if (!r.isLoggedIn()) { return; }

        current = new r.User({ id: 'current' });
        current.fetch({
          success: function () {
            r.changePage('applications');
          },
          error: function (model, response) {
            r.log.info('no current user', 'SignupController.isLoggedIn');
          }
        });
      } catch (e) {
        r.log.error(e, 'SignupController.isLoggedIn');
      }
    },


    // Session Setup
    setupSession: function () {
      try {
        r.log.info('entering', 'SignupController.setupSession');

        r.session = {};
        r.session.params = r.router.getParams(location.hash);
      } catch (e) {
        r.log.error(e, 'SignupController.setupSestion');
      }
    },


    // Signup
    signupBeforeShow: function () {
      try {
        r.log.info('entering', 'SignupController.signupBeforeShow');

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
      } catch (e) {
        r.log.error(e, 'SignupController.signupBeforeShow');
      }
    },

    signupShow: function () {
      try {
        r.log.info('entering', 'SignupController.signupShow');

        r.signupView.render();
      } catch (e) {
        r.log.error(e, 'SignupController.signupShow');
      }
    },


    // Login
    loginBeforeShow: function () {
      try {
        r.log.info('entering', 'SignupController.loginBeforeShow');

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
      } catch (e) {
        r.log.error(e, 'SignupController.loginBeforeShow');
      }
    },

    loginShow: function () {
      try {
        r.log.info('entering', 'SignupController.loginShow');

        r.loginView.render();
      } catch (e) {
        r.log.error(e, 'SignupController.loginShow');
      }
    },


    // Confirm New User
    confirmNewUserBeforeShow: function () {
      try {
        r.log.info('entering', 'SignupController.confirmNewUserBeforeShow');

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
      } catch (e) {
        r.log.error(e, 'SignupController.confirmNewUserBeforeShow');
      }
    },

    confirmNewUserShow: function () {
      try {
        r.log.info('entering', 'SignupController.confirmNewUserShow');

        r.confirmNewUserView.render();
      } catch (e) {
        r.log.error(e, 'SignupController.confirmNewUserShow');
      }
    },

    // Confirm Existing User
    confirmExistingUserBeforeShow: function () {
      try {
        r.log.info('entering', 'SignupController.confirmExistingUserBeforeShow');

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
      } catch (e) {
        r.log.error(e, 'SignupController.confirmExistingUserBeforeShow');
      }
    },

    confirmExistingUserShow: function () {
      try {
        r.log.info('entering', 'SignupController.confirmExistingUserShow');

        r.confirmExistingUserView.render();
        r.confirmExistingUserView.$el.trigger('submit');
      } catch (e) {
        r.log.error(e, 'SignupController.confirmExistingUserShow');
      }
    },


    // Confirm Member
    confirmMemberBeforeShow: function () {
      try {
        r.log.info('entering', 'SingupController.confirmMemberBeforeShow');

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
      } catch (e) {
        r.log.error(e, 'SignupController.confirmMemberBeforeShow');
      }
    },

    confirmMemberShow: function () {
      try {
        r.log.info('entering', 'SingupController.confirmMemberShow');

        r.confirmMemberView.render();
        r.confirmMemberView.$el.trigger('submit');
      } catch (e) {
        r.log.error(e, 'SignupController.confirmMemberShow');
      }
    },


    flashCheck: function () {
      try {
        r.log.info('entering', 'SignupController.flashCheck');

        r.flash.check();
      } catch (e) {
        r.log.error(e, 'SignupController.flashCheck');
      }
    }
  };


  try {
    r.router = new $.mobile.Router([
      { '.*':       { handler: 'isLoggedIn',        events: 'bc'  } },
      { '.*':       { handler: 'setupSession',      events: 'bs'  } },
      { '.*':       { handler: 'flashCheck',        events: 's'   } },
      { '#signup':  { handler: 'signupBeforeShow',  events: 'bs'  } },
      { '#signup':  { handler: 'signupShow',        events: 's'   } },
      { '#login':   { handler: 'loginBeforeShow',   events: 'bs'  } },
      { '#login':   { handler: 'loginShow',         events: 's'   } },

      // these routes only match when preregistration is present
      { '#confirm(?=.*preregistration=true)':   { handler: 'confirmNewUserBeforeShow',      events: 'bs'  } },
      { '#confirm(?=.*preregistration=true)':   { handler: 'confirmNewUserShow',            events: 's'   } },
      { '#confirm(?=.*preregistration=false)':  { handler: 'confirmExistingUserBeforeShow', events: 'bs'  } },
      { '#confirm(?=.*preregistration=false)':  { handler: 'confirmExistingUserShow',       events: 's'   } },

      // these routes only matches when memberConfirmation is present
      { '#confirm(?=.*memberConfirmation)':     { handler: 'confirmMemberBeforeShow',       events: 'bs'  } },
      { '#confirm(?=.*memberConfirmation)':     { handler: 'confirmMemberShow',             events: 's'   } },
    ], r.controller);
  } catch (e) {
    r.log.error(e, 'RSKYBOX.signup.router');
  }


  return r;
}(RSKYBOX || {}, jQuery));
