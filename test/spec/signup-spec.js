var unconfirmedUsers = {
  validEmail: {
    emailAddress: '1@test.com',
    testApp: true
  },
  validPhoneCredentials: {
    phoneNumber: '630-555-1212',
    mobileCarrierId: '1',
    testApp: true
  },
  missingCredentials: {
    testApp: true
  },
  missingEmailAndPhone: {
    mobileCarrierId: '1',
    testApp: true
  },
  invalidEmail: {
    emailAddress: '1test.com',
    testApp: true
  },
  missingCarrier: {
    phoneNumber: '630-555-1212',
    testApp: true
  },
  invalidPhoneNumber: {
    phoneNumber: '60-555-1212',
    mobileCarrierId: '1',
    testApp: true
  }
};


describe('UnconfirmedUser', function() {
  beforeEach(function() {
    this.user = new rskybox.UnconfirmedUser();
  });

  describe('valid user', function() {
    it('is created with valid email address', function() {
      this.user.set(unconfirmedUsers.validEmail);
      expect(this.user.get('emailAddress')).toEqual('1@test.com');
    });
    it('is created with valid phone number credentials', function() {
      this.user.set(unconfirmedUsers.validPhoneCredentials);
      expect(this.user.get('phoneNumber')).toEqual('630-555-1212');
      expect(this.user.get('mobileCarrierId')).toEqual('1');
    });
  });

  describe('invalid user', function() {
    it('is not created with missing credentials', function() {
      this.user.set(unconfirmedUsers.missingEmail);
      expect(this.user.get('emailAddress')).toBeUndefined();
    });
    it('is not created with missing email and phone', function() {
      this.user.set(unconfirmedUsers.missingEmailAndPhone);
      expect(this.user.get('emailAddress')).toBeUndefined();
      expect(this.user.get('phoneNumber')).toBeUndefined();
    });
    it('is not created with invalid email', function() {
      this.user.set(unconfirmedUsers.invalidEmail);
      expect(this.user.get('emailAddress')).toBeUndefined();
    });
    it('is not created with missing carrier', function() {
      this.user.set(unconfirmedUsers.missingCarrier);
      expect(this.user.get('phoneNumber')).toBeUndefined();
    });
    it('is not created with invalid phone number', function() {
      this.user.set(unconfirmedUsers.invalidPhoneNumber);
      expect(this.user.get('phoneNumber')).toBeUndefined();
    });
  });

  describe('save valid user', function() {
    it('should be successful', function() {
      spyOn(Backbone, 'sync').andCallFake(function(method, model, options) {
        options.success({
          apiStatus: '100',
          emailAddress: '1@test.com',
          confirmationCode: '123'
        });
      });
      this.user.save(unconfirmedUsers.validEmail);
      expect(this.user.get('emailAddress')).toEqual('1@test.com');
      expect(this.user.get('apiStatus')).toBeUndefined();
    });
  });

  describe('save confirmed user', function() {
    it('fails when email already confirmed', function() {
      spyOn(Backbone, 'sync').andCallFake(function(method, model, options) {
        options.success({
          apiStatus: '204',
          emailAddress: '1@test.com',
          confirmationCode: '123'
        });
      });
      spyOn(rskybox, 'displayWarning');
      this.user.save(unconfirmedUsers.validEmail);
      expect(rskybox.displayWarning).toHaveBeenCalledWith(this.user.warnings.api204);
    });

    it('fails when phone already confirmed', function() {
      spyOn(Backbone, 'sync').andCallFake(function(method, model, options) {
        options.success({
          apiStatus: '205',
          phoneNumber: '630-555-1212',
          confirmationCode: '123'
        });
      });
      spyOn(rskybox, 'displayWarning');
      this.user.save(unconfirmedUsers.validPhoneCredentials);
      expect(rskybox.displayWarning).toHaveBeenCalledWith(this.user.warnings.api205);
    });
  });
});
