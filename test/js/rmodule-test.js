TestCase('PhoneNumberTest', {
  'test properly formatted phone number is valid': function () {
    assertEquals(RMODULE.isValidPhoneNumber('123-456-7890'), true);
  },

  'test 9 digit only number is valid': function () {
    assertEquals(RMODULE.isValidPhoneNumber('1234567890'), true);
  },

  'test null phone number is invalid': function () {
    assertEquals(RMODULE.isValidPhoneNumber(), false);
  },

  'test blank phone number is invalid': function () {
    assertEquals(RMODULE.isValidPhoneNumber(''), false);
  },

  'test improperly formatted phone number is invalid': function () {
    assertEquals(RMODULE.isValidPhoneNumber('123--456--7890'), false);
  },
});


TestCase('EmailAddressTest', {
  'test properly formatted email address is valid': function () {
    assertEquals(RMODULE.isValidEmailAddress('test@test.com'), true);
  },

  'test null email address is invalid': function () {
    assertEquals(RMODULE.isValidEmailAddress(), false);
  },

  'test blank email address is invalid': function () {
    assertEquals(RMODULE.isValidEmailAddress(''), false);
  },

  'test improperly formatted email address is invalid': function () {
    assertEquals(RMODULE.isValidEmailAddress('test@test'), false);
  },
});
