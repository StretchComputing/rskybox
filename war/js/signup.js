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

  // isValidConfirm - check properties user wants to submit for confirming their account agains business rules
  my.isValidConfirmation = function (confirmation) {
    var
      CONFIRMATION_CODE_LEN = 3,
      PASSWORD_MIN_LEN = 6;

    if (!confirmation.confirmationCode || confirmation.confirmationCode.length != CONFIRMATION_CODE_LEN) { return false; }
    if (!confirmation.password || confirmation.password.length < PASSWORD_MIN_LEN) { return false; }

    return true;
  }

  return my;
}(RMODULE || {}));
