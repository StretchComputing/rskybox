'use strict';

var RMODULE = (function (my) {

  // isValidSignup - determine whether the signup properties passed in are valid for a new user signup
  //
  // signup - object containing the signup properties
  // signup.emailAddress
  // signup.phoneNumber
  // signup.mobileCarrierId
  my.isValidSignup = function (signup) {
    if (my.isValidEmailAddress(signup.emailAddress)) { return true; }
    if (my.isValidPhoneNumber(signup.phoneNumber) && signup.mobileCarrierId) { return true; }

    return false;
  }

  // isValidPassword - check password user wants to submit against business rules
  my.isValidPassword = function (password) {
    var PASSWORD_MIN_LEN = 6;

    if (!password || password.length < PASSWORD_MIN_LEN) {
      return false;
    }
    return true;
  }

  // isValidConfirmationCode - check confirmation code user wants to submit against business rules
  my.isValidConfirmationCode = function (code) {
    var CONFIRMATION_CODE_LEN = 3;

    if (!code || code.length != CONFIRMATION_CODE_LEN) {
      return false;
    }
    return true;
  }

  return my;
}(RMODULE || {}));
