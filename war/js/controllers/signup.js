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
          r.log.debug('no current user', 'signup.controller.isLoggedIn');
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


    confirmMember: function (e) {
      var confirmFailed, member, proceed;

      r.log.debug('entering', 'signup.controller.confirmMember');
      member = new r.Member({
        id: 'confirmation',
      });

      member.setAppUrl(r.session.params.applicationId);

      e.preventDefault();
      member.save({
        emailAddress: r.session.params.emailAddress,
        confirmationCode: r.session.params.confirmationCode,
        memberConfirmation: r.session.params.memberConfirmation,
      }, {
        success: function (model, response) {
          r.dump(model);
          // TODO - This initial block won't be necessary when Joe fixes issue #128.
          if (+model.get('apiStatus') !== 100) {
            confirmFailed({responseText: '{ "apiStatus": ' + model.get('apiStatus') + ' }' });
            return;
          }
          // TODO - end block to remove


          r.log.debug('membership confirmed', 'memberConfirmSuccess');
          proceed();
        },
        error: function (model, response) {
          if (response.responseText) {
            // This is an apiError.
            return;
          }
          // We shouldn't be seeing errors except for apiStatus returns.
          r.log.error(response, 'memberConfirmation');
        },
        statusCode: function () {
          r.statusCodeHandlers(confirmFailed);
        },
      });

      confirmFailed = function (jqXHR) {
        // remove memberconfirmation parameter from url and direct to the confirm
        // page again so the user can complete their user signup process.
        var
          apiCodes = {
            214: 'Member not pending confirmation.',
            215: 'Member not a registered user.',
            309: 'Confirmation code is required.',
            313: 'Email address is required.',
            606: 'App member not found.'
          },
          code = +r.getApiStatus(jqXHR.responseText),
          params;
        r.log.debug('member confirmation failed', 'memberConfirmFail');


        if (!apiCodes[code]) {
          r.log.debug('An unknown API error occurred: ' + code, 'confirmFailed');
          r.flash.error(undefined);
          return;
        }
        if (code === 215) {
          params = r.session.params;
          delete params.memberConfirmation;
          delete params.applicationId;
          r.changePage('confirm', 'signup', params);
        } else {
          r.flash.error(apiCodes[code]);
        }
      };

      proceed = function () {
        if (r.isCookieSet()) {
          r.changePage('applications');
        } else {
          r.changePage('login', 'signup');
        }
      };
    },


    confirmUser: function () {
      r.confirm = new r.Confirm({
        emailAddress: r.session.params.emailAddress,
        phoneNumber: r.session.params.phoneNumber,
        confirmationCode: r.session.params.confirmationCode,
      });
      r.confirmUserView = new r.ConfirmUserView({
        el: $('#confirmForm'),
        model: r.confirm
      });
      r.confirmUserView.render();
    },

    confirmBeforeShow: function (eventType, matchObj, ui, page, evt) {
      if (r.session.params.memberConfirmation === 'true') {
        this.confirmMember(evt);
      } else {
        this.confirmUser();
      }
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
