var validUnconfirmedUsers = [{
  emailAddress: '1@test.com',
  testApp: true
}, {
  phoneNumber: '630-555-1212',
  mobileCarrierId: '1',
  testApp: true
}];

var invalidUnconfirmedUsers = [{
  testApp: true
}, {
  emailAddress: '1test.com',
  testApp: true
}, {
  emailAddress: '1@test.com',
  testApp: true
}, {
  phoneNumber: '630-555-1212',
  testApp: true
}, {
  mobileCarrierId: '1',
  testApp: true
}, {
  phoneNumber: '60-555-1212',
  mobileCarrierId: '1',
  testApp: true
}];


describe('UnconfirmedUser', function() {
  beforeEach(function() {
    this.user = new rskybox.UnconfirmedUser();
  });

  describe('valid user', function() {
    it('is created with valid email address', function() {
      this.user.set(validUnconfirmedUsers[0]);
      expect(this.user.get('emailAddress')).toEqual('1@test.com');
    });
  });

  describe('invalid user', function() {
    it('is not created with invalid email address', function() {
      this.user = new rskybox.UnconfirmedUser();
      this.user.set(invalidUnconfirmedUsers[1]);
      expect(this.user.get('emailAddress')).toBeUndefined();
    });
  });
});
