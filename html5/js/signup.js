'use strict';

var RMODULE = (function (my) {

  // isValidSignup - determine whether the signup properties passed in are valid for a new user signup
  //
  // signup - object containing the signup properties
  // signup.emailAddress
  // signup.phoneNumber
  // signup.mobileCarrierId
  my.isValidSignup = function (signup) {
    if (my.isValidEmailAddress(signup.emailAddress)) {
      return true;
    }

    if (my.isValidPhoneNumber(signup.phoneNumber) && my.isValidCarrier(signup.mobileCarrierId)) {
      return true;
    }

    return false;
  }

  return my;
}(RMODULE || {}));
