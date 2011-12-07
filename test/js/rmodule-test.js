'use strict';


TestCase('PhoneNumberTest', {
  'test properly formatted phone number is valid': function () {
    assertTrue(RMODULE.isValidPhoneNumber('123-456-7890'));
  },

  'test 9 digit only number is valid': function () {
    assertTrue(RMODULE.isValidPhoneNumber('1234567890'));
  },

  'test null phone number is invalid': function () {
    assertFalse(RMODULE.isValidPhoneNumber());
  },

  'test blank phone number is invalid': function () {
    assertFalse(RMODULE.isValidPhoneNumber(''));
  },

  'test improperly formatted phone number is invalid': function () {
    assertFalse(RMODULE.isValidPhoneNumber('123--456--7890'));
  }
});


TestCase('EmailAddressTest', {
  'test properly formatted email address is valid': function () {
    assertTrue(RMODULE.isValidEmailAddress('test@test.com'));
  },

  'test null email address is invalid': function () {
    assertFalse(RMODULE.isValidEmailAddress());
  },

  'test blank email address is invalid': function () {
    assertFalse(RMODULE.isValidEmailAddress(''));
  },

  'test improperly formatted email address is invalid': function () {
    assertFalse(RMODULE.isValidEmailAddress('test@test'));
  }
});


TestCase('PasswordTest', {
  'test password with minimum length is valid': function () {
    assertTrue(RMODULE.isValidPassword('password'));
  },

  'test password less than minimum length is invalid': function () {
    assertFalse(RMODULE.isValidPassword('passw'));
  },

  'test null password is invalid': function () {
    assertFalse(RMODULE.isValidPassword(null));
  },

  'test undefined password is invalid': function () {
    assertFalse(RMODULE.isValidPassword());
  }
});


TestCase('LoginTest', {
  'test login is valid with valid email and password': function () {
    assertTrue(RMODULE.isValidLogin({ emailAddress: 'test@test.com', password: 'password'} ));
  },

  'test login is valid with valid phone and password': function () {
    assertTrue(RMODULE.isValidLogin({ phoneNumber: '1234567890', password: 'password' }));
  },

  'test login is valid with valid email, phone and password': function () {
    assertTrue(RMODULE.isValidLogin({ emailAddress: 'test@test.com', phoneNumber: '1234567890', password: 'password' }));
  },

  'test login is invalid with no email and phone, and valid password': function () {
    assertFalse(RMODULE.isValidLogin({ password: 'password' }));
  },

  'test login is invalid with email and phone, and no password': function () {
    assertFalse(RMODULE.isValidLogin({ emailAddress: 'test@test.com', phoneNumber: '1234567890' }));
  }
});


TestCase('QueryStringTest', {
  setUp: function () {
    this.props = {
      firstParam: 'firstValue',
      secondParam: 'secondValue',
      thirdParam: 'thirdValue'
    }
  },

  'test valid properties produce valid query string': function () {
    assertEquals(RMODULE.getQueryString(this.props), '?firstParam=firstValue&secondParam=secondValue&thirdParam=thirdValue');
  }
});
