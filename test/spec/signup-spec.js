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
    beforeEach(function() {
      this.eventSpy = sinon.spy();
      this.user.bind('error', this.eventSpy);
    });

    afterEach(function() {
      expect(this.eventSpy).toHaveBeenCalled();
      expect(this.user.get('emailAddress')).toBeUndefined();
      expect(this.user.get('phoneNumber')).toBeUndefined();
    });

    it('is not created with missing credentials', function() {
      this.user.set(unconfirmedUsers.missingCredentials);
    });
    it('is not created with missing email and phone', function() {
      this.user.set(unconfirmedUsers.missingEmailAndPhone);
    });
    it('is not created with invalid email', function() {
      this.user.set(unconfirmedUsers.invalidEmail);
    });
    it('is not created with missing carrier', function() {
      this.user.set(unconfirmedUsers.missingCarrier);
    });
    it('is not created with invalid phone number', function() {
      this.user.set(unconfirmedUsers.invalidPhoneNumber);
    });
  });

  describe('save user', function() {
    beforeEach(function() {
      this.spy = sinon.spy(this.user, 'handleSuccess');
      this.server = sinon.fakeServer.create();
    });
    afterEach(function() {
      expect(this.server.requests.length).toEqual(1);
      expect(this.spy).toHaveBeenCalled();
      this.server.restore();
    });

    describe('with valid credentials', function() {
      it('should be successful', function() {
        this.server.respondWith(
          'POST',
          rskybox.getRestPrefix() + '/users/requestConfirmation',
          this.validResponse({
            apiStatus: '100',
            emailAddress: '1@test.com',
            confirmationCode: '123',
          }, 201)
        );
        this.user.save(unconfirmedUsers.validEmail, { success: this.user.handleSuccess });
        this.server.respond();
      });
    });

    describe('already confirmed', function() {
      beforeEach(function() {
        this.spyWarning = sinon.spy(rskybox, 'displayWarning');
      });
      afterEach(function() {
        rskybox.displayWarning.restore();
      });

      it('fails when email already confirmed', function() {
        this.server.respondWith(
          'POST',
          rskybox.getRestPrefix() + '/users/requestConfirmation',
          this.validResponse({
            apiStatus: '204',
            emailAddress: '1@test.com',
            confirmationCode: '123'
          }, 201)
        );
        this.user.save(unconfirmedUsers.validEmail, { success: this.user.handleSuccess });
        this.server.respond();
        expect(this.spyWarning).toHaveBeenCalledWith(this.user.warnings.api204);
      });

      it('fails when phone already confirmed', function() {
        this.server.respondWith(
          'POST',
          rskybox.getRestPrefix() + '/users/requestConfirmation',
          this.validResponse({
            apiStatus: '205',
            phoneNumber: '630-555-1212',
            confirmationCode: '123'
          }, 201)
        );
        this.user.save(unconfirmedUsers.validPhoneCredentials, { success: this.user.handleSuccess });
        this.server.respond();
        expect(this.spyWarning).toHaveBeenCalledWith(this.user.warnings.api205);
      });
    });
  });
});
