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

    validate: function (attrs) {
      var password, PASSWORD_MIN_LEN = 6;

      r.log.debug('User.validate');
      r.dump(attrs);
      password = attrs.password;
      if (password && password.length < PASSWORD_MIN_LEN) {
        return 'Minimum password length is ' + PASSWORD_MIN_LEN + ' characters.';
      }
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
