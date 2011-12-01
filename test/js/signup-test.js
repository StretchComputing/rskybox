TestCase('SignupTest', {
  'test allow signup with proper email address': function () {
    assertTrue(RMODULE.isValidSignup({ email: 'email@test.com' }));
  },

  'test allow signup with proper phone number and carrierID': function () {
    assertTrue(RMODULE.isValidSignup({ phone: '1234567890', carrierId: '1' }));
  },

});
