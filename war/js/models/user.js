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
      this.setUrl();
    },

    isConfirmCodeValid: function (code) {
      var CODE_LEN = 3;

      return code && code.length === CODE_LEN;
    },

    isPasswordValid: function (password) {
      var PASSWORD_MIN_LEN = 6;

      return password && password.length >= PASSWORD_MIN_LEN;
    },

    isPhoneValid: function (phone, carrier) {
      return r.isValidPhoneNumber(phone) && carrier;
    },

    isEmailValid: function (email) {
      return r.isValidEmailAddress(email);
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
