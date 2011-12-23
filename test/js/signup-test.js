'use strict';


TestCase('SignupTest', {
  'test allow signup with proper email address': function () {
    assertTrue(RMODULE.isValidSignup({ emailAddress: 'email@test.com' }));
  },

  'test allow signup with proper phone number and carrierID': function () {
    assertTrue(RMODULE.isValidSignup({ phoneNumber: '1234567890', mobileCarrierId: '1' }));
  },

  'test disallow signup with no options specified': function () {
    assertFalse(RMODULE.isValidSignup({}));
  },

  'test disallow signup with no carrierId': function () {
    assertFalse(RMODULE.isValidSignup({ phoneNumber: '1234567890', mobileCarrierId: '' }));
  },

  'test disallow signup with no phone number': function () {
    assertFalse(RMODULE.isValidSignup({ mobileCarrierId: '1' }));
  }
});


TestCase('CredentialsTest', {
  'test valid confirmationCode': function () {
    assertTrue(RMODULE.isValidConfirmationCode('abc'));
  },

  'test is invalid with missing confirmationCode': function () {
    assertFalse(RMODULE.isValidConfirmationCode());
  },

  'test is invalid when confirmationCode is too short': function () {
    assertFalse(RMODULE.isValidConfirmationCode('12'));
  },

  'test valid password': function () {
    assertTrue(RMODULE.isValidPassword('password'));
  },

  'test is invalid when password is missing': function () {
    assertFalse(RMODULE.isValidPassword());
  },

  'test is invalid when password is too short': function () {
    assertFalse(RMODULE.isValidPassword('12345'));
  }
});
