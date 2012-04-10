var RSKYBOX = (function (r, $) {
  'use strict';


  r.User = r.BaseModel.extend({
    apiUrl: '/users',
    fields: {
      id: null,
      firstName: null,
      lastName: null,
      emailAddress: null,
      isEmailConfirmed: null,
      phoneNumber: null,
      isSmsConfirmed: null,
      mobileCarrierId: null,
      sendEmailNotifications: null,
      sendSmsNotifications: null,
      emailConfirmationCode: null,
      phoneConfirmationCode: null,
      isSuperAdmin: null,
    },

    initialize: function () {
      try {
        this.setUrl();
      } catch (e) {
        r.log.error(e, 'User.initialize');
      }
    },

    isConfirmCodeValid: function (code) {
      try {
        var CODE_LEN = 3;

        return code && code.length === CODE_LEN;
      } catch (e) {
        r.log.error(e, 'User.isConfirmCodeValid');
      }
    },

    isPasswordValid: function (password) {
      try {
        var PASSWORD_MIN_LEN = 6;

        return password && password.length >= PASSWORD_MIN_LEN;
      } catch (e) {
        r.log.error(e, 'User.isPasswordValid');
      }
    },

    isPhoneValid: function (phone, carrier) {
      try {
        return r.isValidPhoneNumber(phone) && carrier;
      } catch (e) {
        r.log.error(e, 'User.isPhoneValid');
      }
    },

    isEmailValid: function (email) {
      try {
        return r.isValidEmailAddress(email);
      } catch (e) {
        r.log.error(e, 'User.isEmailValid');
      }
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
