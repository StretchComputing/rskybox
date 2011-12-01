'use strict';

var RMODULE = (function (my) {

  // validSignup - determine whether the signup properties passed in are valide for a new user signup.
  //
  // signup - object containing the signup properties
  // signup.email
  // signup.phone
  // signup.carrierId
  my.isValidSignup = function (signup) {
    if (my.isValidEmailAddress(signup.email)) {
      return true;
    }

    if (my.isValidPhoneNumber(signup.phone) && my.isValidCarrier(signup.carrierId)) {
      return true;
    }

    return false;
  }

  return my;
}(RMODULE || {}));
