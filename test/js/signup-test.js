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
  },
});

TestCase('ConfirmTest', {
  'test confirm is valid with valid properties': function () {
    assertTrue(RMODULE.isValidConfirmation({ confirmationCode: 'abc', password: 'password' }));
  },

  'test confirm is invalid with missing password': function () {
    assertFalse(RMODULE.isValidConfirmation({ confirmationCode: 'abc' }));
  },

  'test confirm is invalid with missing confirmationCode': function () {
    assertFalse(RMODULE.isValidConfirmation({ password: 'password' }));
  },
});
