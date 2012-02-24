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

    updating: {},

    setUpdating: function (field) {
      this.updating[field] = true;
    },

    clearUpdating: function (field) {
      this.updating = {};
    },

    toJSON: function () {
      var json = {};

      if (Object.keys(this.updating).length > 0) {
        Object.keys(this.updating).forEach(function (field) {
          json[field] = this.get(field);
        }, this);
        return json;
      }

      // This is the exact line from backbone's toJSON method.
      return _.clone(this.attributes);
    },

    validate: function (attrs) {
      var password, PASSWORD_MIN_LEN = 6;

      r.log.debug('User.validate');
      password = attrs.password;
      if (this.updating.password && password.length < PASSWORD_MIN_LEN) {
        return 'Minimum password length is ' + PASSWORD_MIN_LEN + ' characters.';
      }
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
