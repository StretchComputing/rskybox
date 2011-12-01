TestCase('SignupTest', {
  'test allow signup with proper email address': function () {
    assertTrue(RMODULE.isValidSignup({ email: 'email@test.com' }));
  },

  'test allow signup with proper phone number and carrierID': function () {
    assertTrue(RMODULE.isValidSignup({ phone: '1234567890', carrierId: '1' }));
  },

  'test disallow signup with no options specified': function () {
    assertFalse(RMODULE.isValidSignup({}));
  },

  'test disallow signup with no carrierId': function () {
    assertFalse(RMODULE.isValidSignup({ phone: '1234567890', carrierId: '-1' }));
  },

  'test disallow signup with no phone number': function () {
    assertFalse(RMODULE.isValidSignup({ carrierId: '1' }));
  },
});
