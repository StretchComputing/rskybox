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
  },
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
  },
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
  },
});
